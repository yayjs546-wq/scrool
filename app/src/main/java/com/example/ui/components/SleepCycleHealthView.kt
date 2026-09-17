package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SleepSession
import com.example.service.HealthSyncStatus
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CanvasCard
import com.example.ui.theme.CanvasCardBorder
import com.example.ui.theme.CanvasCardElevated
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MoonSubtle
import com.example.ui.theme.MoonWhite
import com.example.ui.theme.MoonlightAmber
import com.example.ui.theme.SoftIris

@Composable
fun SleepCycleHealthView(
    isVaultUnlocked: Boolean,
    latestSession: SleepSession?,
    allSessions: List<SleepSession>,
    healthSyncStatus: HealthSyncStatus,
    onUnlockVault: () -> Unit,
    onLockVault: () -> Unit,
    onSyncHealth: () -> Unit,
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
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sleep Architecture",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MoonWhite
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Nocturnal recovery & sleep cycle efficiency",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MoonMuted,
                        fontSize = 12.sp
                    )
                )
            }

            // Vault Lock Action
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CanvasCardElevated)
                    .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                    .clickable {
                        if (isVaultUnlocked) onLockVault() else onUnlockVault()
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag(if (isVaultUnlocked) "lock_vault_action" else "unlock_vault_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isVaultUnlocked) CalmSage else MoonMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (isVaultUnlocked) "Lock Vault" else "Unlock Vault",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isVaultUnlocked) CalmSage else MoonMuted,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Clean Score Summary Table
        val efficiency = latestSession?.efficiencyPercentage ?: 92

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EFFICIENCY RATING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MoonSubtle,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$efficiency%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MoonWhite
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (efficiency >= 90) "Optimal" else "Normal",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CalmSage,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TOTAL DURATION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MoonSubtle,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = latestSession?.durationFormatted ?: "7h 35m",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MoonWhite
                    )
                )
            }
        }

        // Sleep Architecture Stages
        val deep = latestSession?.deepSleepMinutes ?: 110
        val rem = latestSession?.remMinutes ?: 95
        val light = latestSession?.lightSleepMinutes ?: 180
        val awake = latestSession?.awakeMinutes ?: 25
        val total = (deep + rem + light + awake).toFloat().coerceAtLeast(1f)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Stage Breakdown",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MoonWhite,
                    fontSize = 13.sp
                )
            )

            // Segmented Stage Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(2.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight((deep / total).coerceAtLeast(0.05f))
                        .fillMaxSize()
                        .background(CalmSage)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight((rem / total).coerceAtLeast(0.05f))
                        .fillMaxSize()
                        .background(SoftIris)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight((light / total).coerceAtLeast(0.05f))
                        .fillMaxSize()
                        .background(MoonWhite.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight((awake / total).coerceAtLeast(0.05f))
                        .fillMaxSize()
                        .background(MoonlightAmber)
                )
            }

            // Clean Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StageMetric("DEEP", "${deep}m", CalmSage)
                StageMetric("REM", "${rem}m", SoftIris)
                StageMetric("LIGHT", "${light}m", MoonWhite.copy(alpha = 0.5f))
                StageMetric("AWAKE", "${awake}m", MoonlightAmber)
            }
        }

        // Health Connect Integration Row
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = healthSyncStatus.serviceName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MoonWhite,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = healthSyncStatus.syncStatusMessage,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MoonMuted,
                        fontSize = 11.sp
                    )
                )
            }

            OutlinedButton(
                onClick = onSyncHealth,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MoonWhite),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(CanvasCardBorder)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("sync_health_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = MoonMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sync",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
            }
        }

        // Historical Encrypted Sessions (Shown when unlocked)
        if (isVaultUnlocked) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Logged Sessions (Encrypted)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = MoonWhite,
                        fontSize = 13.sp
                    )
                )

                allSessions.take(3).forEach { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(CanvasCardElevated)
                            .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${session.durationFormatted} • ${session.efficiencyPercentage}% efficiency",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MoonWhite,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = "${session.videosScrolled} videos • ${session.livesSkipped} lives skipped",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MoonMuted,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        Text(
                            text = "AES-GCM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MoonSubtle,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StageMetric(name: String, duration: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, shape = RoundedCornerShape(1.dp))
        )
        Text(
            text = "$name $duration",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MoonMuted,
                fontSize = 10.sp
            )
        )
    }
}
