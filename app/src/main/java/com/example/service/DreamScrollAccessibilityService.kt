package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DreamScrollAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var autoScrollLoopJob: Job? = null
    private var lastLiveSkipTimestamp: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
        setupNotificationChannel()
        startAutoScrollLoop()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        if (pkgName.isNotEmpty()) {
            _currentPackage.value = pkgName
            val isTikTok = isTikTokPackage(pkgName)
            val isShortsOrReels = isShortsOrReelsPackage(pkgName)
            val isTarget = isTikTok || (_includeShortsAndReels.value && isShortsOrReels)
            _isTargetAppInForeground.value = isTarget
            _foregroundAppName.value = when {
                isTikTok -> "TikTok"
                isShortsOrReels -> if (pkgName.contains("youtube")) "YouTube Shorts" else "Instagram Reels"
                pkgName == packageName -> "DreamScroll"
                else -> "Other App"
            }
        }

        if (!_isAutoScrollEnabled.value) return

        // Live stream detection & instant auto-skip
        if (_skipLivesEnabled.value && (_isTargetAppInForeground.value || _universalMode.value)) {
            val eventType = event.eventType
            if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            ) {
                checkAndSkipLiveBroadcast()
            }
        }
    }

    private fun isTikTokPackage(pkg: String): Boolean {
        return pkg.contains("musically") || pkg.contains("trill") || pkg.contains("aweme")
    }

    private fun isShortsOrReelsPackage(pkg: String): Boolean {
        return pkg.contains("youtube") || pkg.contains("instagram")
    }

    private fun checkAndSkipLiveBroadcast() {
        val now = System.currentTimeMillis()
        if (now - lastLiveSkipTimestamp < 2200) return // Cooldown to avoid multiple swipe spam

        val root = rootInActiveWindow ?: return
        try {
            if (isLiveStreamNode(root)) {
                lastLiveSkipTimestamp = now
                performSwipeUpGesture()
                _totalLivesSkipped.value += 1
                _lastSkippedNotice.value = "⚡ Live stream detected & instantly skipped!"
                vibrateBriefly()
            }
        } finally {
            try {
                root.recycle()
            } catch (_: Exception) {}
        }
    }

    private fun isLiveStreamNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val text = node.text?.toString()?.uppercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.uppercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        if (text == "LIVE" || text.contains("TAP TO WATCH LIVE") || text.contains("JOIN LIVE") ||
            text.contains("SWIPE FOR MORE LIVES") || contentDesc.contains("LIVE STREAM") ||
            contentDesc.contains("HOST IS LIVE") || viewId.contains("live_tag") || viewId.contains("live_badge")
        ) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (isLiveStreamNode(child)) {
                return true
            }
        }
        return false
    }

    fun performSwipeUpGesture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels.toFloat()
            val screenHeight = displayMetrics.heightPixels.toFloat()

            // Natural upward swipe gesture
            val startX = screenWidth / 2f
            val startY = screenHeight * 0.78f
            val endX = screenWidth / 2f
            val endY = screenHeight * 0.22f

            val swipePath = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }

            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(swipePath, 0, 260))
                .build()

            dispatchGesture(gesture, object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    _totalVideosScrolled.value += 1
                    _testSwipeNotice.value = "✓ Gesture executed"
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    _testSwipeNotice.value = "Gesture interrupted"
                }
            }, null)
        }
    }

    /**
     * Navigates back to the home launcher screen when the sleep timer expires.
     * Conserves battery and turns screen off gracefully according to system display timeout.
     */
    fun performGoHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun startAutoScrollLoop() {
        autoScrollLoopJob?.cancel()
        autoScrollLoopJob = serviceScope.launch {
            _countdownSeconds.value = _scrollDelaySeconds.value
            while (isActive) {
                delay(1000)
                if (!_isAutoScrollEnabled.value) {
                    continue
                }

                // Check if target app is in foreground or universal mode is active
                val shouldScroll = _universalMode.value || _isTargetAppInForeground.value
                if (!shouldScroll) {
                    continue
                }

                val remaining = _countdownSeconds.value - 1
                if (remaining <= 0) {
                    _countdownSeconds.value = _scrollDelaySeconds.value
                    performSwipeUpGesture()
                } else {
                    _countdownSeconds.value = remaining
                }
            }
        }
    }

    private fun vibrateBriefly() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {}
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DreamScroll Background Controller",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active TikTok auto-scrolling status in background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onInterrupt() {
        _isServiceActive.value = false
        autoScrollLoopJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceActive.value = false
        autoScrollLoopJob?.cancel()
    }

    companion object {
        const val CHANNEL_ID = "dreamscroll_service_channel"

        var instance: DreamScrollAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive = _isServiceActive.asStateFlow()

        private val _isAutoScrollEnabled = MutableStateFlow(true)
        val isAutoScrollEnabled = _isAutoScrollEnabled.asStateFlow()

        private val _skipLivesEnabled = MutableStateFlow(true)
        val skipLivesEnabled = _skipLivesEnabled.asStateFlow()

        private val _universalMode = MutableStateFlow(false)
        val universalMode = _universalMode.asStateFlow()

        private val _includeShortsAndReels = MutableStateFlow(true)
        val includeShortsAndReels = _includeShortsAndReels.asStateFlow()

        private val _scrollDelaySeconds = MutableStateFlow(10)
        val scrollDelaySeconds = _scrollDelaySeconds.asStateFlow()

        private val _countdownSeconds = MutableStateFlow(10)
        val countdownSeconds = _countdownSeconds.asStateFlow()

        private val _totalVideosScrolled = MutableStateFlow(0)
        val totalVideosScrolled = _totalVideosScrolled.asStateFlow()

        private val _totalLivesSkipped = MutableStateFlow(0)
        val totalLivesSkipped = _totalLivesSkipped.asStateFlow()

        private val _currentPackage = MutableStateFlow("")
        val currentPackage = _currentPackage.asStateFlow()

        private val _isTargetAppInForeground = MutableStateFlow(false)
        val isTargetAppInForeground = _isTargetAppInForeground.asStateFlow()

        private val _foregroundAppName = MutableStateFlow("None")
        val foregroundAppName = _foregroundAppName.asStateFlow()

        private val _lastSkippedNotice = MutableStateFlow<String?>(null)
        val lastSkippedNotice = _lastSkippedNotice.asStateFlow()

        private val _testSwipeNotice = MutableStateFlow<String?>(null)
        val testSwipeNotice = _testSwipeNotice.asStateFlow()

        fun setAutoScrollEnabled(enabled: Boolean) {
            _isAutoScrollEnabled.value = enabled
        }

        fun setSkipLivesEnabled(enabled: Boolean) {
            _skipLivesEnabled.value = enabled
        }

        fun setUniversalMode(enabled: Boolean) {
            _universalMode.value = enabled
        }

        fun setIncludeShortsAndReels(enabled: Boolean) {
            _includeShortsAndReels.value = enabled
        }

        fun setScrollDelaySeconds(seconds: Int) {
            _scrollDelaySeconds.value = seconds
            _countdownSeconds.value = seconds
        }

        fun performManualSwipe(): Boolean {
            val inst = instance
            return if (inst != null) {
                inst.performSwipeUpGesture()
                true
            } else {
                _testSwipeNotice.value = "Accessibility Service is not enabled"
                false
            }
        }

        fun dismissSkippedNotice() {
            _lastSkippedNotice.value = null
        }

        fun dismissTestSwipeNotice() {
            _testSwipeNotice.value = null
        }

        fun triggerGoHome() {
            instance?.performGoHome()
        }

        /**
         * Checks if DreamScroll accessibility service is enabled by the user in Android Settings.
         */
        fun isAccessibilityServiceEnabled(context: Context): Boolean {
            if (instance != null) return true
            val expected = ComponentName(context, DreamScrollAccessibilityService::class.java).flattenToString()
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expected, ignoreCase = true) ||
                    componentName.equals("${context.packageName}/${DreamScrollAccessibilityService::class.java.name}", ignoreCase = true)
                ) {
                    return true
                }
            }
            return false
        }

        /**
         * Direct shortcut to open Android System Accessibility Settings.
         */
        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        /**
         * Directly launches TikTok application on the user's device.
         */
        fun launchTikTok(context: Context): Boolean {
            val tiktokPackages = listOf(
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill",
                "com.ss.android.ugc.aweme",
                "com.zhiliao.musically.go"
            )
            for (pkg in tiktokPackages) {
                val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return true
                }
            }

            // Fallback: Open Google Play Store page or web TikTok
            return try {
                val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.zhiliaoapp.musically")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(playStoreIntent)
                true
            } catch (_: Exception) {
                try {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
}
