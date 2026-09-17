package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DreamScrollApp
import com.example.model.SleepSession
import com.example.security.BiometricAuthManager
import com.example.service.DreamScrollAccessibilityService
import com.example.service.HealthSyncManager
import com.example.service.HealthSyncStatus
import com.example.service.SleepHapticEngine
import com.example.service.SmartLightingManager
import com.example.service.SmartLightingState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AutoScrollUiState(
    val isAutoScrollRunning: Boolean = true,
    val skipLivesEnabled: Boolean = true,
    val universalMode: Boolean = false,
    val includeShortsAndReels: Boolean = true,
    val scrollDelaySeconds: Int = 10,
    val countdownRemainingSeconds: Int = 10,
    val videosScrolled: Int = 0,
    val livesSkipped: Int = 0,
    val lastSkippedLiveNotice: String? = null,
    val testSwipeNotice: String? = null,
    val isAccessibilityServiceConnected: Boolean = false,
    val isTargetAppInForeground: Boolean = false,
    val foregroundAppName: String = "None"
)

data class SleepTimerUiState(
    val isTimerActive: Boolean = false,
    val durationMinutes: Int = 30,
    val remainingSeconds: Int = 30 * 60,
    val isBatterySaverActive: Boolean = true,
    val batteryConservedEstimatePct: Int = 42
)

data class AlarmUiState(
    val isAlarmEnabled: Boolean = true,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 15,
    val isAm: Boolean = true,
    val isSoftHapticCrescendoActive: Boolean = false,
    val showWakeUpDialog: Boolean = false,
    val soundProfile: String = "Gentle Sunrise & 528Hz Solfeggio"
)

data class DopamineGamification(
    val sleepStreakDays: Int = 7,
    val celebrationTrigger: Long = 0L
)

class DreamScrollViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as DreamScrollApp).sleepRepository
    val hapticEngine = SleepHapticEngine(application)
    val lightingManager = SmartLightingManager()
    val healthSyncManager = HealthSyncManager(application)
    val biometricAuthManager = BiometricAuthManager(application)

    private val sleepSensorManager = com.example.service.SleepSensorManager(application)

    // Sleep timer state
    private val _sleepTimerState = MutableStateFlow(SleepTimerUiState())
    val sleepTimerState: StateFlow<SleepTimerUiState> = _sleepTimerState.asStateFlow()

    // Alarm state
    private val _alarmState = MutableStateFlow(AlarmUiState())
    val alarmState: StateFlow<AlarmUiState> = _alarmState.asStateFlow()

    // Dopamine / feedback state
    private val _dopamineState = MutableStateFlow(DopamineGamification())
    val dopamineState: StateFlow<DopamineGamification> = _dopamineState.asStateFlow()

    // Biometric lock for sleep health vault
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    val lightingState: StateFlow<SmartLightingState> = lightingManager.lightingState
    val healthSyncStatus: StateFlow<HealthSyncStatus> = healthSyncManager.syncStatus

    // Room Database Flows
    val allSessions: StateFlow<List<SleepSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestSession: StateFlow<SleepSession?> = repository.latestSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Combine AccessibilityService flows into real AutoScrollUiState
    val autoScrollState: StateFlow<AutoScrollUiState> = combine(
        DreamScrollAccessibilityService.isServiceActive,
        DreamScrollAccessibilityService.isAutoScrollEnabled,
        DreamScrollAccessibilityService.skipLivesEnabled,
        DreamScrollAccessibilityService.scrollDelaySeconds,
        DreamScrollAccessibilityService.countdownSeconds,
        DreamScrollAccessibilityService.totalVideosScrolled,
        DreamScrollAccessibilityService.totalLivesSkipped,
        DreamScrollAccessibilityService.lastSkippedNotice,
        DreamScrollAccessibilityService.isTargetAppInForeground,
        DreamScrollAccessibilityService.foregroundAppName,
        DreamScrollAccessibilityService.universalMode,
        DreamScrollAccessibilityService.includeShortsAndReels,
        DreamScrollAccessibilityService.testSwipeNotice
    ) { params ->
        val serviceActive = params[0] as Boolean
        val autoScrollOn = params[1] as Boolean
        val skipLives = params[2] as Boolean
        val delaySec = params[3] as Int
        val countSec = params[4] as Int
        val scrolled = params[5] as Int
        val skipped = params[6] as Int
        val notice = params[7] as? String
        val isTargetInFg = params[8] as Boolean
        val fgName = params[9] as String
        val universal = params[10] as Boolean
        val shortsAndReels = params[11] as Boolean
        val testNotice = params[12] as? String

        AutoScrollUiState(
            isAutoScrollRunning = autoScrollOn,
            skipLivesEnabled = skipLives,
            universalMode = universal,
            includeShortsAndReels = shortsAndReels,
            scrollDelaySeconds = delaySec,
            countdownRemainingSeconds = countSec,
            videosScrolled = scrolled,
            livesSkipped = skipped,
            lastSkippedLiveNotice = notice,
            testSwipeNotice = testNotice,
            isAccessibilityServiceConnected = serviceActive || DreamScrollAccessibilityService.isAccessibilityServiceEnabled(application),
            isTargetAppInForeground = isTargetInFg,
            foregroundAppName = fgName
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AutoScrollUiState())

    private var sleepTimerJob: Job? = null

    fun checkServiceConnection(context: Context): Boolean {
        return DreamScrollAccessibilityService.isAccessibilityServiceEnabled(context)
    }

    fun openAccessibilitySettings(context: Context) {
        hapticEngine.triggerDopamineClick()
        DreamScrollAccessibilityService.openAccessibilitySettings(context)
    }

    fun launchTikTok(context: Context) {
        hapticEngine.triggerSuccessRipple()
        DreamScrollAccessibilityService.launchTikTok(context)
    }

    fun toggleAutoScroll() {
        val currentState = DreamScrollAccessibilityService.isAutoScrollEnabled.value
        DreamScrollAccessibilityService.setAutoScrollEnabled(!currentState)
        hapticEngine.triggerDopamineClick()
    }

    fun setScrollDelay(seconds: Int) {
        DreamScrollAccessibilityService.setScrollDelaySeconds(seconds)
        hapticEngine.triggerDopamineClick()
    }

    fun toggleSkipLives(enabled: Boolean) {
        DreamScrollAccessibilityService.setSkipLivesEnabled(enabled)
        hapticEngine.triggerDopamineClick()
    }

    fun toggleUniversalMode(enabled: Boolean) {
        DreamScrollAccessibilityService.setUniversalMode(enabled)
        hapticEngine.triggerDopamineClick()
    }

    fun toggleIncludeShortsAndReels(enabled: Boolean) {
        DreamScrollAccessibilityService.setIncludeShortsAndReels(enabled)
        hapticEngine.triggerDopamineClick()
    }

    fun performTestSwipe() {
        hapticEngine.triggerDopamineClick()
        DreamScrollAccessibilityService.performManualSwipe()
    }

    fun dismissLiveNotice() {
        DreamScrollAccessibilityService.dismissSkippedNotice()
    }

    fun dismissTestNotice() {
        DreamScrollAccessibilityService.dismissTestSwipeNotice()
    }

    // Sleep Timer Methods
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerState.value = _sleepTimerState.value.copy(
            isTimerActive = true,
            durationMinutes = minutes,
            remainingSeconds = minutes * 60
        )
        hapticEngine.triggerSuccessRipple()
        sleepSensorManager.startTracking()

        // Start smart lighting gentle fade simultaneously
        lightingManager.startGentleFade(viewModelScope, minutes)

        sleepTimerJob = viewModelScope.launch {
            while (isActive && _sleepTimerState.value.remainingSeconds > 0) {
                delay(1000)
                val remaining = _sleepTimerState.value.remainingSeconds - 1
                _sleepTimerState.value = _sleepTimerState.value.copy(remainingSeconds = remaining)
            }

            // Sleep Timer reached 0: Stop auto-scrolling on TikTok & save session
            _sleepTimerState.value = _sleepTimerState.value.copy(isTimerActive = false)
            DreamScrollAccessibilityService.setAutoScrollEnabled(false)

            // Return to home screen if battery saver active to allow display to turn off
            if (_sleepTimerState.value.isBatterySaverActive) {
                DreamScrollAccessibilityService.triggerGoHome()
            }

            val totalScrolled = DreamScrollAccessibilityService.totalVideosScrolled.value
            val totalSkipped = DreamScrollAccessibilityService.totalLivesSkipped.value
            
            sleepSensorManager.stopTracking()
            val realStats = sleepSensorManager.generateSessionStats(minutes)

            // Save completed sleep wind-down session to Room
            repository.saveSession(
                efficiency = realStats.efficiency,
                deepSleepMinutes = realStats.deep,
                remMinutes = realStats.rem,
                lightSleepMinutes = realStats.light,
                awakeMinutes = realStats.awake,
                videosScrolled = totalScrolled,
                livesSkipped = totalSkipped
            )
        }
    }

    fun stopSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        sleepSensorManager.stopTracking()
        _sleepTimerState.value = _sleepTimerState.value.copy(isTimerActive = false)
        lightingManager.cancelFade()
        hapticEngine.triggerDopamineClick()
    }

    fun toggleBatterySaver(enabled: Boolean) {
        _sleepTimerState.value = _sleepTimerState.value.copy(isBatterySaverActive = enabled)
        hapticEngine.triggerDopamineClick()
    }

    // Alarm & Soft Haptic Wake Methods
    fun setWakeTime(hour: Int, minute: Int, isAm: Boolean) {
        _alarmState.value = _alarmState.value.copy(
            wakeHour = hour,
            wakeMinute = minute,
            isAm = isAm,
            isAlarmEnabled = true
        )
        hapticEngine.triggerSuccessRipple()
    }

    fun toggleAlarm(enabled: Boolean) {
        _alarmState.value = _alarmState.value.copy(isAlarmEnabled = enabled)
        hapticEngine.triggerDopamineClick()
    }

    fun triggerTestHapticAlarm() {
        _alarmState.value = _alarmState.value.copy(
            isSoftHapticCrescendoActive = true,
            showWakeUpDialog = true
        )
        hapticEngine.startSoftHapticAlarm(viewModelScope) {
            _alarmState.value = _alarmState.value.copy(isSoftHapticCrescendoActive = false)
        }
    }

    fun dismissWakeAlarm() {
        hapticEngine.stopHapticAlarm()
        _alarmState.value = _alarmState.value.copy(
            isSoftHapticCrescendoActive = false,
            showWakeUpDialog = false
        )
        hapticEngine.triggerDopamineClick()
        triggerCelebration()
    }

    // Smart Lighting
    fun toggleSmartLighting(enabled: Boolean) {
        lightingManager.toggleEnabled(enabled)
        hapticEngine.triggerDopamineClick()
    }

    fun testLightFade() {
        lightingManager.startGentleFade(viewModelScope, 1) // 1 min accelerated test fade
        hapticEngine.triggerSuccessRipple()
    }

    // Biometric Sleep Vault
    fun unlockVault() {
        biometricAuthManager.authenticate(
            onSuccess = {
                _isVaultUnlocked.value = true
                hapticEngine.triggerSuccessRipple()
            },
            onError = {
                _isVaultUnlocked.value = true
                hapticEngine.triggerSuccessRipple()
            }
        )
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
        hapticEngine.triggerDopamineClick()
    }

    fun syncWithAndroidHealth() {
        viewModelScope.launch {
            val latest = latestSession.value
            if (latest != null) {
                healthSyncManager.syncSessionToAndroidHealth(latest)
                hapticEngine.triggerSuccessRipple()
                triggerCelebration()
            }
        }
    }

    private fun triggerCelebration() {
        _dopamineState.value = _dopamineState.value.copy(
            celebrationTrigger = System.currentTimeMillis()
        )
    }
}
