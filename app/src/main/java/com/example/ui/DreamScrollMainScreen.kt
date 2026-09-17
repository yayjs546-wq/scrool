package com.example.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MotionPhotosAuto
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SleepCycleHealthView
import com.example.ui.components.SleepStatisticsDashboard
import com.example.ui.components.SleepTimerView
import com.example.ui.components.SunriseWakeDialog
import com.example.ui.components.TikTokBackgroundControllerCard
import com.example.ui.components.WakeAlarmView
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CanvasCard
import com.example.ui.theme.CanvasCardBorder
import com.example.ui.theme.CanvasCardElevated
import com.example.ui.theme.CanvasDeep
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MoonSubtle
import com.example.ui.theme.MoonWhite
import com.example.ui.theme.MoonlightAmber
import kotlinx.coroutines.launch

@Composable
fun DreamScrollMainScreen(
    viewModel: DreamScrollViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // ViewModel States
    val autoScrollState by viewModel.autoScrollState.collectAsState()
    val sleepTimerState by viewModel.sleepTimerState.collectAsState()
    val alarmState by viewModel.alarmState.collectAsState()
    val lightingState by viewModel.lightingState.collectAsState()
    val healthSyncStatus by viewModel.healthSyncStatus.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val latestSession by viewModel.latestSession.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CanvasDeep
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Utilitarian Human App Header & System Status
                item(key = "header") {
                    Spacer(modifier = Modifier.height(12.dp))
                    AppSystemHeader(
                        isServiceActive = autoScrollState.isAccessibilityServiceConnected,
                        onOpenSettings = { viewModel.openAccessibilitySettings(context) }
                    )
                }

                // Section 1: TikTok Background Auto-Scroll Controller
                item(key = "section_autoscroll") {
                    SectionLabel("BACKGROUND AUTO-SCROLL", "Automated upward swipe engine")
                    Spacer(modifier = Modifier.height(6.dp))
                    TikTokBackgroundControllerCard(
                        autoScrollState = autoScrollState,
                        onToggleAutoScroll = { viewModel.toggleAutoScroll() },
                        onSetDelaySeconds = { viewModel.setScrollDelay(it) },
                        onToggleSkipLives = { viewModel.toggleSkipLives(it) },
                        onToggleUniversalMode = { viewModel.toggleUniversalMode(it) },
                        onToggleIncludeShortsAndReels = { viewModel.toggleIncludeShortsAndReels(it) },
                        onTestSwipe = { viewModel.performTestSwipe() },
                        onLaunchTikTok = { viewModel.launchTikTok(it) },
                        onOpenSettings = { viewModel.openAccessibilitySettings(it) },
                        onDismissLiveNotice = { viewModel.dismissLiveNotice() },
                        onDismissTestNotice = { viewModel.dismissTestNotice() }
                    )
                }

                // Section 2: Sleep Wind-Down & Timer
                item(key = "section_sleeptimer") {
                    SectionLabel("SLEEP WIND-DOWN", "Media cutoff & circadian room dimming")
                    Spacer(modifier = Modifier.height(6.dp))
                    SleepTimerView(
                        timerState = sleepTimerState,
                        lightingState = lightingState,
                        onStartTimer = { viewModel.startSleepTimer(it) },
                        onStopTimer = { viewModel.stopSleepTimer() },
                        onToggleLighting = { viewModel.toggleSmartLighting(it) },
                        onTestLightFade = { viewModel.testLightFade() },
                        onToggleBatterySaver = { viewModel.toggleBatterySaver(it) }
                    )
                }

                // Section 3: Gentle Morning Alarm
                item(key = "section_alarm") {
                    SectionLabel("MORNING WAKE", "Low-cortisol crescendo haptic alarm")
                    Spacer(modifier = Modifier.height(6.dp))
                    WakeAlarmView(
                        alarmState = alarmState,
                        onSetTime = { h, m, isAm -> viewModel.setWakeTime(h, m, isAm) },
                        onToggleAlarm = { viewModel.toggleAlarm(it) },
                        onTestHapticAlarm = { viewModel.triggerTestHapticAlarm() },
                        onDismissAlarm = { viewModel.dismissWakeAlarm() }
                    )
                }

                // Section 4: Sleep Architecture & Health
                item(key = "section_health") {
                    SectionLabel("SLEEP ARCHITECTURE", "Encrypted nocturnal stages & health sync")
                    Spacer(modifier = Modifier.height(6.dp))
                    SleepCycleHealthView(
                        isVaultUnlocked = isVaultUnlocked,
                        latestSession = latestSession,
                        allSessions = allSessions,
                        healthSyncStatus = healthSyncStatus,
                        onUnlockVault = { viewModel.unlockVault() },
                        onLockVault = { viewModel.lockVault() },
                        onSyncHealth = { viewModel.syncWithAndroidHealth() }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SleepStatisticsDashboard(
                        allSessions = allSessions
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }

            // Morning Wake Dialog
            if (alarmState.showWakeUpDialog) {
                SunriseWakeDialog(
                    alarmState = alarmState,
                    onDismiss = { viewModel.dismissWakeAlarm() }
                )
            }
        }
    }
}

/**
 * Standard utility header - clean title, version, and structured status bar.
 * No floating pills, no neon gradients, no generic AI templates.
 */
@Composable
private fun AppSystemHeader(
    isServiceActive: Boolean,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CanvasCard)
            .border(1.dp, CanvasCardBorder, RoundedCornerShape(6.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DreamScroll",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MoonWhite,
                        fontSize = 18.sp,
                        letterSpacing = (-0.3).sp
                    )
                )
                Text(
                    text = "Bedtime video pacing & sleep utility",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MoonMuted,
                        fontSize = 12.sp
                    )
                )
            }

            Text(
                text = "v2.4.0",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MoonSubtle,
                    fontSize = 11.sp
                )
            )
        }

        HorizontalDivider(thickness = 1.dp, color = CanvasCardBorder)

        // Standard functional system status row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSettings() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isServiceActive) CalmSage else MoonlightAmber,
                            shape = RoundedCornerShape(1.dp)
                        )
                )
                Text(
                    text = if (isServiceActive) "Accessibility Service: Running" else "Accessibility Service: Not Configured",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isServiceActive) MoonWhite else MoonlightAmber,
                        fontSize = 12.sp
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isServiceActive) "Settings" else "Configure",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MoonMuted,
                        fontSize = 11.sp
                    )
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MoonMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MoonMuted,
                letterSpacing = 1.2.sp,
                fontSize = 11.sp
            )
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MoonSubtle,
                fontSize = 12.sp
            )
        )
    }
}
