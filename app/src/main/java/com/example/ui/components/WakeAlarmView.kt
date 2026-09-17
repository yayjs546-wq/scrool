package com.example.ui.components

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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AlarmUiState
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CanvasCard
import com.example.ui.theme.CanvasCardBorder
import com.example.ui.theme.CanvasCardElevated
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MoonSubtle
import com.example.ui.theme.MoonWhite

@Composable
fun WakeAlarmView(
    alarmState: AlarmUiState,
    onSetTime: (hour: Int, minute: Int, isAm: Boolean) -> Unit,
    onToggleAlarm: (Boolean) -> Unit,
    onTestHapticAlarm: () -> Unit,
    onDismissAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CanvasCard)
            .border(1.dp, CanvasCardBorder, RoundedCornerShape(6.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Alarm Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gentle Morning Alarm",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MoonWhite
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (alarmState.isAlarmEnabled) "Scheduled for tomorrow morning" else "Alarm paused",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (alarmState.isAlarmEnabled) CalmSage else MoonMuted,
                        fontSize = 12.sp
                    )
                )
            }

            Switch(
                checked = alarmState.isAlarmEnabled,
                onCheckedChange = { onToggleAlarm(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CanvasCard,
                    checkedTrackColor = CalmSage,
                    uncheckedThumbColor = MoonMuted,
                    uncheckedTrackColor = CanvasCardElevated,
                    uncheckedBorderColor = CanvasCardBorder
                ),
                modifier = Modifier.testTag("alarm_toggle_switch")
            )
        }

        // Clean Digital Time Setter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hour Spinner
                TimeSpinUnit(
                    value = String.format("%02d", alarmState.wakeHour),
                    onIncrement = {
                        val next = if (alarmState.wakeHour >= 12) 1 else alarmState.wakeHour + 1
                        onSetTime(next, alarmState.wakeMinute, alarmState.isAm)
                    },
                    onDecrement = {
                        val prev = if (alarmState.wakeHour <= 1) 12 else alarmState.wakeHour - 1
                        onSetTime(prev, alarmState.wakeMinute, alarmState.isAm)
                    },
                    tagPrefix = "hour"
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MoonWhite
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Minute Spinner
                TimeSpinUnit(
                    value = String.format("%02d", alarmState.wakeMinute),
                    onIncrement = {
                        val next = if (alarmState.wakeMinute >= 55) 0 else alarmState.wakeMinute + 5
                        onSetTime(alarmState.wakeHour, next, alarmState.isAm)
                    },
                    onDecrement = {
                        val prev = if (alarmState.wakeMinute <= 0) 55 else alarmState.wakeMinute - 5
                        onSetTime(alarmState.wakeHour, prev, alarmState.isAm)
                    },
                    tagPrefix = "minute"
                )

                Spacer(modifier = Modifier.width(20.dp))

                // Rectangular AM / PM Toggle
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CanvasCard)
                        .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                        .padding(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (alarmState.isAm) MoonWhite else Color.Transparent)
                            .clickable { onSetTime(alarmState.wakeHour, alarmState.wakeMinute, true) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("am_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (alarmState.isAm) CanvasCard else MoonMuted
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (!alarmState.isAm) MoonWhite else Color.Transparent)
                            .clickable { onSetTime(alarmState.wakeHour, alarmState.wakeMinute, false) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("pm_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (!alarmState.isAm) CanvasCard else MoonMuted
                            )
                        )
                    }
                }
            }
        }

        // Haptic Crescendo Settings Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = MoonWhite,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Progressive Haptic Crescendo",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MoonWhite
                        )
                    )
                    Text(
                        text = "Vibration amplitude steps up every 30 seconds",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MoonMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            OutlinedButton(
                onClick = onTestHapticAlarm,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MoonWhite),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CanvasCardBorder)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("test_haptic_alarm_button")
            ) {
                Text(
                    text = "Test",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun TimeSpinUnit(
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    tagPrefix: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(32.dp)
                .testTag("${tagPrefix}_increment")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = MoonMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MoonWhite,
                letterSpacing = 1.sp
            )
        )

        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(32.dp)
                .testTag("${tagPrefix}_decrement")
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = MoonMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
