package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SleepSession
import com.example.ui.theme.CalmSage
import com.example.ui.theme.CanvasCard
import com.example.ui.theme.CanvasCardBorder
import com.example.ui.theme.CanvasCardElevated
import com.example.ui.theme.MoonMuted
import com.example.ui.theme.MoonSubtle
import com.example.ui.theme.MoonWhite
import com.example.ui.theme.MoonlightAmber
import com.example.ui.theme.SoftIris
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SleepStatisticsDashboard(
    allSessions: List<SleepSession>,
    modifier: Modifier = Modifier
) {
    if (allSessions.isEmpty()) return

    // Ensure they are ordered chronologically for the trend graph
    val sortedSessions = allSessions.sortedBy { it.startTimeMillis }
    val lastSevenSessions = sortedSessions.takeLast(7)

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationTriggered = true
    }

    val barProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "Bar Animation"
    )

    val donutProgress by animateFloatAsState(
        targetValue = if (animationTriggered) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "Donut Animation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CanvasCard)
            .border(1.dp, CanvasCardBorder, RoundedCornerShape(6.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sleep Statistics Dashboard",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MoonWhite
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "D3-inspired analytics engine (Native Canvas)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MoonMuted,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // Top Row: KPI Cards
        val avgEfficiency = if (allSessions.isNotEmpty()) allSessions.map { it.efficiencyPercentage }.average().toInt() else 0
        val totalScrolled = allSessions.sumOf { it.videosScrolled }
        val avgDurationMins = if (allSessions.isNotEmpty()) allSessions.map { it.totalDurationMinutes }.average().toInt() else 0
        val avgDurationStr = "${avgDurationMins / 60}h ${avgDurationMins % 60}m"
        val avgDeepMins = if (allSessions.isNotEmpty()) allSessions.map { it.deepSleepMinutes }.average().toInt() else 0
        val deepSleepPercent = if (avgDurationMins > 0) ((avgDeepMins.toFloat() / avgDurationMins) * 100).toInt() else 0

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "AVG EFFICIENCY",
                    value = "$avgEfficiency%",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "AVG DURATION",
                    value = avgDurationStr,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "DEEP SLEEP",
                    value = "$deepSleepPercent%",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "VIDEOS SCROLLED",
                    value = "$totalScrolled",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sleep Duration Trend Chart (Bar Chart)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "7-Day Sleep Duration Trend",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MoonMuted,
                    letterSpacing = 1.sp
                )
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    if (lastSevenSessions.isNotEmpty()) {
                        val maxDuration = lastSevenSessions.maxOf { it.totalDurationMinutes }.toFloat().coerceAtLeast(1f)
                        val barCount = lastSevenSessions.size
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            val slotWidth = canvasWidth / barCount
                            val barWidth = slotWidth * 0.5f

                            // Draw horizontal grid lines
                            for (i in 0..3) {
                                val y = canvasHeight - (canvasHeight * (i / 3f))
                                drawLine(
                                    color = CanvasCardBorder,
                                    start = Offset(0f, y),
                                    end = Offset(canvasWidth, y),
                                    strokeWidth = 1f
                                )
                            }

                            lastSevenSessions.forEachIndexed { index, session ->
                                val durationFloat = session.totalDurationMinutes.toFloat()
                                val normalizedHeight = (durationFloat / maxDuration) * canvasHeight
                                
                                // Animate height
                                val animatedHeight = normalizedHeight * barProgress
                                
                                val x = (index * slotWidth) + (slotWidth - barWidth) / 2
                                val y = canvasHeight - animatedHeight

                                drawRoundRect(
                                    color = SoftIris,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, animatedHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }
                    }
                }
                
                // X-Axis Labels
                if (lastSevenSessions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val formatter = remember { java.text.SimpleDateFormat("d MMM", java.util.Locale.getDefault()) }
                        lastSevenSessions.forEachIndexed { _, session ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                val dateStr = formatter.format(java.util.Date(session.startTimeMillis))
                                Text(
                                    text = dateStr.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MoonMuted,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Aggregate Stage Breakdown (Donut Chart)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(CanvasCardElevated)
                .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Aggregate Stage Architecture",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MoonMuted,
                    letterSpacing = 1.sp
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Donut Chart
                val totalDeep = allSessions.sumOf { it.deepSleepMinutes }.toFloat()
                val totalRem = allSessions.sumOf { it.remMinutes }.toFloat()
                val totalLight = allSessions.sumOf { it.lightSleepMinutes }.toFloat()
                val totalAwake = allSessions.sumOf { it.awakeMinutes }.toFloat()
                val totalMins = (totalDeep + totalRem + totalLight + totalAwake).coerceAtLeast(1f)

                val deepAngle = (totalDeep / totalMins) * 360f
                val remAngle = (totalRem / totalMins) * 360f
                val lightAngle = (totalLight / totalMins) * 360f
                val awakeAngle = (totalAwake / totalMins) * 360f

                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 16.dp.toPx()
                        val halfStroke = strokeWidth / 2
                        val chartSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(halfStroke, halfStroke)

                        var startAngle = -90f
                        val gapAngle = 3f

                        val sections = listOf(
                            Triple(deepAngle, CalmSage, "Deep"),
                            Triple(remAngle, SoftIris, "REM"),
                            Triple(lightAngle, MoonWhite.copy(alpha = 0.5f), "Light"),
                            Triple(awakeAngle, MoonlightAmber, "Awake")
                        )

                        sections.forEach { (sweep, color, _) ->
                            if (sweep > gapAngle) {
                                val animatedSweep = sweep * donutProgress
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = animatedSweep - gapAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = chartSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            startAngle += sweep
                        }
                    }
                    Text(
                        text = "Total\n${(totalMins / 60).toInt()}h",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MoonWhite,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }

                // Legend
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem("Deep", CalmSage, "${((totalDeep / totalMins) * 100).toInt()}%")
                    LegendItem("REM", SoftIris, "${((totalRem / totalMins) * 100).toInt()}%")
                    LegendItem("Light", MoonWhite.copy(alpha = 0.5f), "${((totalLight / totalMins) * 100).toInt()}%")
                    LegendItem("Awake", MoonlightAmber, "${((totalAwake / totalMins) * 100).toInt()}%")
                }
            }
        }
    }
}

@Composable
private fun KpiCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CanvasCardElevated)
            .border(1.dp, CanvasCardBorder, RoundedCornerShape(4.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MoonSubtle,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MoonWhite
            )
        )
    }
}

@Composable
private fun LegendItem(name: String, color: Color, percentage: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.width(90.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, shape = RoundedCornerShape(2.dp))
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MoonMuted,
                    fontSize = 12.sp
                )
            )
        }
        Text(
            text = percentage,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MoonWhite,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
