package com.example.aura.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.engine.ExerciseStrengthPoint
import com.example.aura.engine.HeatmapDayCell
import com.example.aura.engine.ProgressionEngine
import com.example.aura.engine.WeightTrendPoint
import com.example.aura.ui.PersonalBestEvent
import com.example.aura.ui.theme.AuraTheme
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.delay

@Composable
fun PersonalBestConfettiOverlay(
    pbEvent: PersonalBestEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = AuraTheme.colors.accent
    val progress = remember(pbEvent?.timestampMillis) { Animatable(0f) }

    LaunchedEffect(pbEvent?.timestampMillis) {
        if (pbEvent != null) {
            progress.snapTo(0f)
            progress.animateTo(1f, animationSpec = tween(durationMillis = 2400, easing = LinearEasing))
            delay(1800)
            onDismiss()
        }
    }

    val particles = remember(pbEvent?.timestampMillis) {
        val palette = listOf(
            Color(0xFFFFD700),
            Color(0xFF34D399),
            Color(0xFF3898FF),
            Color(0xFFF472B6),
            Color(0xFFF5A623)
        )
        List(44) { idx ->
            val seed = (idx * 73 + 17) % 100
            ConfettiParticle(
                xFraction = (seed / 100f),
                speedY = 0.7f + ((idx * 19) % 70) / 100f,
                drift = ((idx % 9) - 4) * 0.08f,
                sizeDp = 5f + (idx % 5) * 1.6f,
                color = palette[idx % palette.size],
                phase = idx * 0.6f
            )
        }
    }

    AnimatedVisibility(
        visible = pbEvent != null,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Non-blocking confetti Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = progress.value
                val alpha = (1f - (p - 0.7f).coerceAtLeast(0f) / 0.3f).coerceIn(0f, 1f)
                particles.forEach { particle ->
                    val y = -30f + p * size.height * particle.speedY
                    val x = (particle.xFraction * size.width) +
                        sin(p * 12f + particle.phase) * 28f +
                        particle.drift * size.width * p
                    if (y in 0f..size.height) {
                        val r = particle.sizeDp.dp.toPx()
                        drawCircle(
                            color = particle.color.copy(alpha = alpha),
                            radius = r * 0.55f,
                            center = Offset(x, y)
                        )
                        drawLine(
                            color = particle.color.copy(alpha = alpha * 0.85f),
                            start = Offset(x, y),
                            end = Offset(
                                x + cos(p * 10f + particle.phase) * r,
                                y + sin(p * 10f + particle.phase) * r
                            ),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            if (pbEvent != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp, start = 20.dp, end = 20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AuraTheme.colors.card)
                        .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(18.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("personal_best_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x28FFD700)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Personal Best",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "NEW PERSONAL BEST!",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700),
                                letterSpacing = 1.sp
                            )
                            val wLabel = if (pbEvent.weightKg > 0.0) {
                                "${ProgressionEngine.formatKg(pbEvent.weightKg)} kg × ${pbEvent.reps} reps"
                            } else {
                                "${pbEvent.reps} reps"
                            }
                            Text(
                                text = "${pbEvent.exerciseName}: $wLabel",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.primaryText
                            )
                            if (pbEvent.estimated1RmKg > 0.0) {
                                Text(
                                    text = "Estimated 1RM: ${ProgressionEngine.formatKg(pbEvent.estimated1RmKg)} kg",
                                    fontSize = 11.sp,
                                    color = accent
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss PB banner",
                            tint = AuraTheme.colors.secondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class ConfettiParticle(
    val xFraction: Float,
    val speedY: Float,
    val drift: Float,
    val sizeDp: Float,
    val color: Color,
    val phase: Float
)

@Composable
fun WeightTrendLineChartCanvas(
    points: List<WeightTrendPoint>,
    goalWeightKg: Double?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val gridColor = AuraTheme.colors.cardBorder
    val mutedText = AuraTheme.colors.textMuted
    val cardColor = AuraTheme.colors.card
    val goalColor = Color(0xFF34D399)

    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AuraTheme.colors.surfaceSecondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log a weigh-in to start your weight chart",
                fontSize = 12.sp,
                color = AuraTheme.colors.secondaryText
            )
        }
        return
    }

    val allValues = points.map { it.weightKg } + listOfNotNull(goalWeightKg)
    val rawMin = allValues.minOrNull() ?: 60.0
    val rawMax = allValues.maxOrNull() ?: 80.0
    val paddingRange = max(1.5, (rawMax - rawMin) * 0.25)
    val minY = rawMin - paddingRange
    val maxY = rawMax + paddingRange
    val rangeY = max(0.5, maxY - minY)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${ProgressionEngine.formatKg(maxY)} kg",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = mutedText
            )
            if (goalWeightKg != null && goalWeightKg > 0.0) {
                Text(
                    text = "Goal: ${ProgressionEngine.formatKg(goalWeightKg)} kg",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = goalColor
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .testTag("weight_trend_canvas")
        ) {
            val leftPad = 12f
            val rightPad = 12f
            val topPad = 14f
            val bottomPad = 14f
            val w = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val h = (size.height - topPad - bottomPad).coerceAtLeast(1f)

            // Horizontal subtle grid lines
            for (i in 0..2) {
                val y = topPad + h * (i / 2f)
                drawLine(
                    color = gridColor.copy(alpha = 0.55f),
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Goal dashed line
            if (goalWeightKg != null && goalWeightKg > 0.0) {
                val goalFrac = ((goalWeightKg - minY) / rangeY).toFloat().coerceIn(0f, 1f)
                val goalY = topPad + h * (1f - goalFrac)
                drawLine(
                    color = goalColor.copy(alpha = 0.85f),
                    start = Offset(leftPad, goalY),
                    end = Offset(size.width - rightPad, goalY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                )
            }

            val coords = points.mapIndexed { idx, pt ->
                val xFrac = if (points.size == 1) 0.5f else idx.toFloat() / (points.size - 1).toFloat()
                val yFrac = ((pt.weightKg - minY) / rangeY).toFloat().coerceIn(0f, 1f)
                Offset(
                    x = leftPad + w * xFrac,
                    y = topPad + h * (1f - yFrac)
                )
            }

            if (coords.size >= 2) {
                val linePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        lineTo(coords[i].x, coords[i].y)
                    }
                }
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(coords.last().x, topPad + h)
                    lineTo(coords.first().x, topPad + h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = topPad,
                        endY = topPad + h
                    )
                )
                drawPath(
                    path = linePath,
                    color = accentColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            coords.forEach { pt ->
                drawCircle(color = cardColor, radius = 6.dp.toPx(), center = pt)
                drawCircle(color = accentColor, radius = 4.dp.toPx(), center = pt)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = points.first().label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = mutedText
            )
            Text(
                text = "${ProgressionEngine.formatKg(minY)} kg",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = mutedText
            )
            Text(
                text = points.last().label,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = mutedText
            )
        }
    }
}

@Composable
fun ExerciseStrengthLineChartCanvas(
    points: List<ExerciseStrengthPoint>,
    showEstimated1Rm: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val gridColor = AuraTheme.colors.cardBorder
    val mutedText = AuraTheme.colors.textMuted
    val cardColor = AuraTheme.colors.card

    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AuraTheme.colors.surfaceSecondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log sets for this lift to see your strength curve",
                fontSize = 12.sp,
                color = AuraTheme.colors.secondaryText
            )
        }
        return
    }

    val values = points.map { pt ->
        if (showEstimated1Rm) pt.estimated1RmKg
        else if (pt.bestWeightKg > 0.0) pt.bestWeightKg else pt.bestReps.toDouble()
    }
    val rawMin = values.minOrNull() ?: 0.0
    val rawMax = values.maxOrNull() ?: 10.0
    val pad = max(2.0, (rawMax - rawMin) * 0.22)
    val minY = max(0.0, rawMin - pad)
    val maxY = rawMax + pad
    val rangeY = max(1.0, maxY - minY)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Peak: ${ProgressionEngine.formatKg(rawMax)} ${if (points.all { it.bestWeightKg <= 0.0 } && !showEstimated1Rm) "reps" else "kg"}",
                fontSize = 10.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = "${points.size} session${if (points.size == 1) "" else "s"}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = mutedText
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .testTag("exercise_strength_canvas")
        ) {
            val leftPad = 12f
            val rightPad = 12f
            val topPad = 12f
            val bottomPad = 12f
            val w = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val h = (size.height - topPad - bottomPad).coerceAtLeast(1f)

            for (i in 0..2) {
                val y = topPad + h * (i / 2f)
                drawLine(
                    color = gridColor.copy(alpha = 0.5f),
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val coords = values.mapIndexed { idx, v ->
                val xFrac = if (values.size == 1) 0.5f else idx.toFloat() / (values.size - 1).toFloat()
                val yFrac = ((v - minY) / rangeY).toFloat().coerceIn(0f, 1f)
                Offset(leftPad + w * xFrac, topPad + h * (1f - yFrac))
            }

            if (coords.size >= 2) {
                val path = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) lineTo(coords[i].x, coords[i].y)
                }
                val area = Path().apply {
                    addPath(path)
                    lineTo(coords.last().x, topPad + h)
                    lineTo(coords.first().x, topPad + h)
                    close()
                }
                drawPath(
                    path = area,
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.25f), Color.Transparent),
                        startY = topPad,
                        endY = topPad + h
                    )
                )
                drawPath(
                    path = path,
                    color = accentColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            coords.forEach { pt ->
                drawCircle(color = cardColor, radius = 5.5.dp.toPx(), center = pt)
                drawCircle(color = accentColor, radius = 3.8.dp.toPx(), center = pt)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = points.first().label, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = mutedText)
            Text(text = points.last().label, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = mutedText)
        }
    }
}

@Composable
fun ConsistencyHeatmapGrid(
    weeks: List<List<HeatmapDayCell>>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val emptyCellColor = AuraTheme.colors.surfaceSecondary
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.width(14.dp)
            ) {
                dayLabels.forEach { lbl ->
                    Box(
                        modifier = Modifier.height(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lbl,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AuraTheme.colors.textMuted
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weeks.forEach { weekCol ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekCol.forEach { cell ->
                            val fill = when (cell.intensity) {
                                3 -> accentColor
                                2 -> accentColor.copy(alpha = 0.58f)
                                1 -> accentColor.copy(alpha = 0.28f)
                                else -> emptyCellColor
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(3.5.dp))
                                    .background(fill)
                                    .then(
                                        if (cell.isToday) {
                                            Modifier.border(1.dp, AuraTheme.colors.primaryText, RoundedCornerShape(3.5.dp))
                                        } else Modifier
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "12 weeks ago",
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                color = AuraTheme.colors.textMuted
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Less", fontSize = 9.5.sp, color = AuraTheme.colors.textMuted)
                listOf(
                    emptyCellColor,
                    accentColor.copy(alpha = 0.28f),
                    accentColor.copy(alpha = 0.58f),
                    accentColor
                ).forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(c)
                    )
                }
                Text("Trained", fontSize = 9.5.sp, color = AuraTheme.colors.textMuted)
            }
        }
    }
}
