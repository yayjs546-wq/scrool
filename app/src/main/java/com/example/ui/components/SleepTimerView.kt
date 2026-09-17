package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.service.SmartLightingState
import com.example.ui.SleepTimerUiState
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CanvasCard
import com.example.ui.theme.CanvasCardBorder
import com.example.ui.theme.CanvasCardElevated
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MoonSubtle
import com.example.ui.theme.MoonWhite
import com.example.ui.theme.MoonlightAmber

@Composable
fun SleepTimerView(
    timerState: SleepTimerUiState,
    lightingState: SmartLightingState,
    onStartTimer: (Int) -> Unit,
    onStopTimer: () -> Unit,
    onToggleLighting: (Boolean) -> Unit,
    onTestLightFade: () -> Unit,
    onToggleBatterySaver: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isRunning = timerState.isTimerActive

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CanvasCard)
            .border(1.dp, CanvasCardBorder, RoundedCornerShape(6.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sleep Wind-Down",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MoonWhite
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isRunning) "Halts video playback when countdown finishes"
                    else "Set duration before falling asleep",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isRunning) MoonlightAmber else MoonMuted,
                        fontSize = 12.sp
                    )
                )
            }

            // Minimalist Square Status Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CanvasCardElevated)
                    .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isRunning) "RUNNING" else "STANDBY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isRunning) MoonlightAmber else MoonSubtle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Digital Counter Block
        val minutes = timerState.remainingSeconds / 60
        val seconds = timerState.remainingSeconds % 60

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MoonWhite,
                        letterSpacing = 2.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isRunning) "Remaining time until playback shutdown" else "Selected timer duration",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MoonSubtle,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Stepped Duration Selector
        val presets = listOf(15, 30, 45, 60, 90)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { mins ->
                val isSelected = timerState.durationMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected && !isRunning) MoonWhite else CanvasCardElevated)
                        .border(
                            width = 1.dp,
                            color = if (isSelected && !isRunning) MoonWhite else CanvasCardBorder,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { onStartTimer(mins) }
                        .padding(vertical = 8.dp)
                        .testTag("timer_preset_${mins}m"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mins}m",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected && !isRunning) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected && !isRunning) CanvasCard else MoonWhite
                        )
                    )
                }
            }
        }

        // Start / Stop Button
        if (isRunning) {
            OutlinedButton(
                onClick = onStopTimer,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MoonlightAmber
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CanvasCardBorder)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("stop_timer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cancel Sleep Timer",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        } else {
            Button(
                onClick = { onStartTimer(timerState.durationMinutes) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MoonWhite,
                    contentColor = CanvasCard
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("start_timer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Start ${timerState.durationMinutes}m Wind-Down",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Circadian Lighting Control Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_smart_light),
                        contentDescription = "Smart Light",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = "Circadian Lighting Dimmer",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MoonWhite
                            )
                        )
                        Text(
                            text = if (lightingState.isEnabled) "2200K warm fade active" else "Smart light fade disabled",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MoonMuted,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Switch(
                    checked = lightingState.isEnabled,
                    onCheckedChange = { onToggleLighting(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CanvasCard,
                        checkedTrackColor = CalmSage,
                        uncheckedThumbColor = MoonMuted,
                        uncheckedTrackColor = CanvasCard,
                        uncheckedBorderColor = CanvasCardBorder
                    ),
                    modifier = Modifier.testTag("lighting_toggle_switch")
                )
            }

            if (lightingState.isEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onTestLightFade,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MoonWhite),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(CanvasCardBorder)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "Preview Fade",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Battery Saver Toggle Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BatterySaver,
                    contentDescription = null,
                    tint = if (timerState.isBatterySaverActive) CalmSage else MoonSubtle,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        text = "Overnight Battery Guard",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MoonWhite
                        )
                    )
                    Text(
                        text = "Exits video feed on timer expiry so display goes to sleep",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MoonMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Switch(
                checked = timerState.isBatterySaverActive,
                onCheckedChange = { onToggleBatterySaver(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CanvasCard,
                    checkedTrackColor = CalmSage,
                    uncheckedThumbColor = MoonMuted,
                    uncheckedTrackColor = CanvasCard,
                    uncheckedBorderColor = CanvasCardBorder
                ),
                modifier = Modifier.testTag("battery_saver_toggle_switch")
            )
        }
    }
}
