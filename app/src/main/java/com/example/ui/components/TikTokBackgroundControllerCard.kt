package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AutoScrollUiState
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CanvasCard
import com.example.ui.theme.CanvasCardBorder
import com.example.ui.theme.CanvasCardElevated
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MoonSubtle
import com.example.ui.theme.MoonWhite
import com.example.ui.theme.MoonlightAmber

@Composable
fun TikTokBackgroundControllerCard(
    autoScrollState: AutoScrollUiState,
    onToggleAutoScroll: () -> Unit,
    onSetDelaySeconds: (Int) -> Unit,
    onToggleSkipLives: (Boolean) -> Unit,
    onToggleUniversalMode: (Boolean) -> Unit,
    onToggleIncludeShortsAndReels: (Boolean) -> Unit,
    onTestSwipe: () -> Unit,
    onLaunchTikTok: (Context) -> Unit,
    onOpenSettings: (Context) -> Unit,
    onDismissLiveNotice: () -> Unit,
    onDismissTestNotice: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRunning = autoScrollState.isAutoScrollRunning
    val isServiceActive = autoScrollState.isAccessibilityServiceConnected

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CanvasCard)
            .border(1.dp, CanvasCardBorder, RoundedCornerShape(6.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hands-Free Auto-Scroll",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MoonWhite
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isRunning && isServiceActive) "Running in background • Upward swipe active"
                    else if (!isServiceActive) "Accessibility permission required"
                    else "Paused",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isRunning && isServiceActive) CalmSage else MoonMuted,
                        fontSize = 12.sp
                    )
                )
            }

            Switch(
                checked = isRunning,
                onCheckedChange = { onToggleAutoScroll() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CanvasCard,
                    checkedTrackColor = CalmSage,
                    uncheckedThumbColor = MoonMuted,
                    uncheckedTrackColor = CanvasCardElevated,
                    uncheckedBorderColor = CanvasCardBorder
                ),
                modifier = Modifier.testTag("auto_scroll_master_switch")
            )
        }

        // Accessibility Prompt Bar (Flat, rectangular, utilitarian)
        if (!isServiceActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(CanvasCardElevated)
                    .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                    .clickable { onOpenSettings(context) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MoonlightAmber,
                    modifier = Modifier.size(16.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Accessibility Service",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MoonlightAmber
                        )
                    )
                    Text(
                        text = "System Settings > Accessibility > Installed Apps > DreamScroll",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MoonMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        // Telemetry Data Table
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(vertical = 10.dp, horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TelemetryColumn(
                label = "INTERVAL",
                value = "${autoScrollState.scrollDelaySeconds}s"
            )
            TelemetryColumn(
                label = "VIDEOS SCROLLED",
                value = "${autoScrollState.videosScrolled}"
            )
            TelemetryColumn(
                label = "LIVES SKIPPED",
                value = "${autoScrollState.livesSkipped}"
            )
        }

        // Interval Setting
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scroll Interval",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MoonWhite
                    )
                )
                Text(
                    text = "${autoScrollState.scrollDelaySeconds}s",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MoonWhite,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Utilitarian Stepped Selector
            val intervals = listOf(5, 8, 12, 18, 30)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                intervals.forEach { sec ->
                    val isSelected = autoScrollState.scrollDelaySeconds == sec
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) MoonWhite else CanvasCardElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MoonWhite else CanvasCardBorder,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onSetDelaySeconds(sec) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${sec}s",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CanvasCard else MoonWhite
                            )
                        )
                    }
                }
            }

            Slider(
                value = autoScrollState.scrollDelaySeconds.toFloat(),
                onValueChange = { onSetDelaySeconds(it.toInt()) },
                valueRange = 5f..45f,
                steps = 39,
                colors = SliderDefaults.colors(
                    thumbColor = MoonWhite,
                    activeTrackColor = MoonWhite,
                    inactiveTrackColor = CanvasCardElevated
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Preference Rows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
        ) {
            SettingRow(
                title = "Skip TikTok LIVE Streams",
                subtitle = "Detects broadcast screen elements and immediately swipes past",
                checked = autoScrollState.skipLivesEnabled,
                onCheckedChange = { onToggleSkipLives(it) }
            )
            HorizontalDivider(thickness = 1.dp, color = CanvasCardBorder)
            SettingRow(
                title = "Enable for Shorts & Reels",
                subtitle = "Applies upward scroll gestures to YouTube Shorts and Instagram",
                checked = autoScrollState.includeShortsAndReels,
                onCheckedChange = { onToggleIncludeShortsAndReels(it) }
            )
        }

        // Dismissible Live Notice
        AnimatedVisibility(
            visible = autoScrollState.lastSkippedLiveNotice != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            autoScrollState.lastSkippedLiveNotice?.let { notice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(CanvasCardElevated)
                        .border(1.dp, MoonlightAmber.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notice,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MoonlightAmber,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismissLiveNotice,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MoonlightAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Dismissible Test Gesture Notice
        AnimatedVisibility(
            visible = autoScrollState.testSwipeNotice != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            autoScrollState.testSwipeNotice?.let { testNotice ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(CanvasCardElevated)
                        .border(
                            1.dp,
                            if (testNotice.startsWith("✓")) CalmSage.copy(alpha = 0.6f) else MoonlightAmber.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = testNotice,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (testNotice.startsWith("✓")) CalmSage else MoonlightAmber,
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismissTestNotice,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = if (testNotice.startsWith("✓")) CalmSage else MoonlightAmber,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onLaunchTikTok(context) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoonWhite,
                    contentColor = CanvasCard
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .height(42.dp)
                    .testTag("launch_tiktok_button")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Open TikTok",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            OutlinedButton(
                onClick = onTestSwipe,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MoonWhite
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CanvasCardBorder)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("test_swipe_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SwipeUp,
                    contentDescription = null,
                    tint = MoonMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Test Swipe",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
private fun TelemetryColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MoonSubtle,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MoonWhite
            )
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MoonWhite,
                    fontSize = 13.sp
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MoonMuted,
                    fontSize = 11.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CanvasCard,
                checkedTrackColor = CalmSage,
                uncheckedThumbColor = MoonMuted,
                uncheckedTrackColor = CanvasCardElevated,
                uncheckedBorderColor = CanvasCardBorder
            )
        )
    }
}
