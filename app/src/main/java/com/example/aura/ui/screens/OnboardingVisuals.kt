package com.example.aura.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.ui.components.AuraCard
import com.example.aura.ui.components.AuraToggleSwitch
import com.example.aura.ui.components.rememberReducedMotion
import com.example.aura.ui.theme.AuraTheme
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Welcome screen three concentric gold rings pulsing (scale 1 -> 1.12, 2.4s, staggered).
 */
@Composable
fun WelcomePulseRings(
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = rememberReducedMotion()
) {
    val accent = AuraTheme.colors.accent
    val phase = if (reducedMotion) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "welcomeRings")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "welcomePhase"
        )
        value
    }

    Canvas(modifier = modifier.size(160.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val strokePx = 3.dp.toPx()
        val scaleUnit = size.minDimension / 160f

        data class RingSpec(val radius: Float, val baseAlpha: Float, val delayFraction: Float)
        val rings = listOf(
            RingSpec(70f, 0.30f, 0.25f), // 0.6s / 2.4s
            RingSpec(48f, 0.60f, 0.125f), // 0.3s / 2.4s
            RingSpec(26f, 1.00f, 0f)
        )

        rings.forEach { spec ->
            val shifted = (phase - spec.delayFraction + 1f) % 1f
            // Smooth 0 -> 1 -> 0 wave matching CSS 0%/100% scale(1) to 50% scale(1.12)
            val wave = if (reducedMotion) 0f else ((1f - kotlin.math.cos(shifted * 2f * PI.toFloat())) / 2f)
            val currentScale = 1f + (0.12f * wave)
            val currentAlpha = (spec.baseAlpha * (0.7f + 0.3f * wave)).coerceIn(0f, 1f)

            drawCircle(
                color = accent.copy(alpha = currentAlpha),
                radius = spec.radius * scaleUnit * currentScale,
                center = center,
                style = Stroke(width = strokePx)
            )
        }
    }
}

/**
 * Interactive horizontal swipeable ruler for Height (cm) and Weight (kg):
 * - Ticks slide in from -40dp on entrance (1.2s ease-out).
 * - Horizontal drag adjusts value with crisp haptic ticks per integer step.
 * - Center gold marker (3dp rounded stroke).
 */
@Composable
fun AuraHorizontalRuler(
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = rememberReducedMotion()
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val tickSpacingPx = with(density) { 8.dp.toPx() }

    val slideInOffset = remember { Animatable(if (reducedMotion) 0f else -40f) }
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            slideInOffset.snapTo(0f)
        } else {
            slideInOffset.snapTo(-40f)
            slideInOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
            )
        }
    }

    val currentValueState by rememberUpdatedState(value)
    val onValueChangeState by rememberUpdatedState(onValueChange)
    var dragRemainderPx by remember { mutableFloatStateOf(0f) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .pointerInput(minValue, maxValue, tickSpacingPx) {
                detectHorizontalDragGestures(
                    onDragStart = { dragRemainderPx = 0f },
                    onDragEnd = { dragRemainderPx = 0f },
                    onDragCancel = { dragRemainderPx = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    dragRemainderPx += dragAmount
                    val deltaSteps = (-dragRemainderPx / tickSpacingPx).toInt()
                    if (deltaSteps != 0) {
                        dragRemainderPx += deltaSteps * tickSpacingPx
                        val next = (currentValueState + deltaSteps).coerceIn(minValue, maxValue)
                        if (next != currentValueState) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onValueChangeState(next)
                        }
                    }
                }
            }
    ) {
        val centerX = size.width / 2f
        val slidePx = slideInOffset.value.dp.toPx()
        val halfTicks = ((size.width / 2f) / tickSpacingPx).toInt() + 2
        val majorTop = 18.dp.toPx()
        val minorTop = 30.dp.toPx()
        val bottomY = 44.dp.toPx()
        val tickStroke = 2.dp.toPx()

        for (offset in -halfTicks..halfTicks) {
            val tickVal = value + offset
            if (tickVal in minValue..maxValue) {
                val x = centerX + (offset * tickSpacingPx) + slidePx
                if (x in 0f..size.width) {
                    val isMajor = tickVal % 5 == 0
                    val distRatio = (kotlin.math.abs(x - centerX) / (size.width / 2f)).coerceIn(0f, 1f)
                    val tickAlpha = (1f - distRatio * 0.55f).coerceIn(0.25f, 1f)
                    drawLine(
                        color = colors.tickColor.copy(alpha = tickAlpha),
                        start = Offset(x, if (isMajor) majorTop else minorTop),
                        end = Offset(x, bottomY),
                        strokeWidth = tickStroke,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Fixed gold center needle
        drawLine(
            color = colors.accent,
            start = Offset(centerX, 2.dp.toPx()),
            end = Offset(centerX, 46.dp.toPx()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * 24-hour day timeline showing Sleep, Busy (Work/College), and Free time blocks,
 * with an optional pulsing recommended workout block on the Schedule step.
 */
@Composable
fun AuraDayTimelineCard(
    wakeMinutes: Int,
    sleepMinutes: Int,
    hasBusyHours: Boolean,
    busyStartMinutes: Int,
    busyEndMinutes: Int,
    busyLabel: String = "Work / College",
    highlightWorkoutMinutes: Int? = null,
    workoutDurationMinutes: Int = 60,
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = rememberReducedMotion()
) {
    val colors = AuraTheme.colors
    val pulseScale = if (highlightWorkoutMinutes != null && !reducedMotion) {
        val transition = rememberInfiniteTransition(label = "schedSlotPulse")
        val v by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "schedPulseVal"
        )
        v
    } else {
        0.5f
    }

    AuraCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        val topSpace = if (highlightWorkoutMinutes != null) 14.dp else 4.dp
        Spacer(modifier = Modifier.height(topSpace))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (highlightWorkoutMinutes != null) 36.dp else 20.dp)
        ) {
            val totalMins = 1440f
            val barH = 20.dp.toPx()
            val barTop = (size.height - barH) / 2f
            val radius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            val gap = 2.dp.toPx()

            val wake = wakeMinutes.coerceIn(0, 1439)
            val sleep = sleepMinutes.coerceIn(0, 1440)
            val busyStart = busyStartMinutes.coerceIn(wake, sleep.coerceAtLeast(wake))
            val busyEnd = busyEndMinutes.coerceIn(busyStart, sleep.coerceAtLeast(busyStart))

            fun drawSeg(startM: Int, endM: Int, color: Color) {
                if (endM <= startM) return
                val x1 = (startM / totalMins) * size.width
                val x2 = (endM / totalMins) * size.width
                val w = (x2 - x1 - gap).coerceAtLeast(2f)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x1, barTop),
                    size = Size(w, barH),
                    cornerRadius = radius
                )
            }

            // 00:00 -> wake (sleep)
            drawSeg(0, wake, colors.sleepBlockColor)

            // wake -> sleep (free + optional busy)
            val effectiveSleep = if (sleep > wake) sleep else 1440
            if (hasBusyHours && busyEnd > busyStart) {
                drawSeg(wake, busyStart, colors.accent.copy(alpha = 0.33f))
                drawSeg(busyStart, busyEnd, colors.busyBlockColor)
                drawSeg(busyEnd, effectiveSleep, colors.accent.copy(alpha = 0.33f))
            } else {
                drawSeg(wake, effectiveSleep, colors.accent.copy(alpha = 0.33f))
            }

            // sleep -> 24:00 (sleep)
            if (effectiveSleep < 1440) {
                drawSeg(effectiveSleep, 1440, colors.sleepBlockColor)
            }

            // Optional pulsing recommended workout slot block
            if (highlightWorkoutMinutes != null) {
                val slotStart = ((highlightWorkoutMinutes % 1440) + 1440) % 1440
                val slotWidth = ((workoutDurationMinutes.coerceIn(30, 120) / totalMins) * size.width)
                    .coerceAtLeast(14.dp.toPx())
                val centerX = ((slotStart / totalMins) * size.width) + (slotWidth / 2f)
                val clampedCenterX = centerX.coerceIn(slotWidth / 2f, size.width - slotWidth / 2f)

                val scale = if (reducedMotion) 1f else 1f + 0.12f * pulseScale
                val alpha = if (reducedMotion) 1f else 0.72f + 0.28f * pulseScale
                val drawW = slotWidth * scale
                val drawH = size.height * (0.90f + 0.10f * pulseScale)

                drawRoundRect(
                    color = colors.accent.copy(alpha = alpha),
                    topLeft = Offset(clampedCenterX - drawW / 2f, (size.height - drawH) / 2f),
                    size = Size(drawW, drawH),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("12 AM", "6", "12 PM", "6", "12").forEach { label ->
                Text(
                    text = label,
                    color = colors.mutedText,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimelineLegendItem(dotColor = colors.legendSleepColor, label = "Sleep")
            TimelineLegendItem(dotColor = colors.legendBusyColor, label = busyLabel)
            TimelineLegendItem(dotColor = colors.accent, label = "Free time")
        }
    }
}

@Composable
private fun TimelineLegendItem(dotColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = AuraTheme.colors.mutedText,
            fontSize = 12.sp
        )
    }
}

/**
 * Rotating spinner arc for the Building Plan screen (150dp, 8dp stroke, 1.3s linear rotation).
 */
@Composable
fun BuildingPlanSpinner(
    modifier: Modifier = Modifier,
    reducedMotion: Boolean = rememberReducedMotion()
) {
    val colors = AuraTheme.colors
    val angle = if (reducedMotion) {
        45f
    } else {
        val transition = rememberInfiniteTransition(label = "buildPlanSpinner")
        val a by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1300, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spinAngle"
        )
        a
    }

    Canvas(modifier = modifier.size(150.dp)) {
        val strokePx = 8.dp.toPx()
        val inset = strokePx / 2f + 11.dp.toPx()
        val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
        val topLeft = Offset(inset, inset)

        drawArc(
            color = colors.trackMuted,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx)
        )

        rotate(degrees = angle) {
            drawArc(
                color = colors.accent,
                startAngle = -90f,
                sweepAngle = 105f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Clean 30-minute increment time picker dialog for Wake up / Sleep selection.
 */
@Composable
fun OnboardingTimePickerDialog(
    title: String,
    selectedMinutes: Int,
    minHour: Int,
    maxHour: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AuraTheme.colors
    val options = remember(minHour, maxHour) {
        val list = mutableListOf<Int>()
        var m = minHour * 60
        while (m <= maxHour * 60) {
            list.add(m % 1440)
            m += 30
        }
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                text = title,
                color = colors.primaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(options, key = { it }) { minutes ->
                    val isSelected = minutes == selectedMinutes
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) colors.accentTint else colors.surface2)
                            .clickable {
                                onSelect(minutes)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = WorkoutScheduleEngine.formatTimeMinutes(minutes),
                            color = if (isSelected) colors.accent else colors.primaryText,
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = colors.accent, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

/**
 * Dialog for configuring busy hours (Work/College start & end, or flexible day).
 */
@Composable
fun BusyHoursEditorDialog(
    hasBusyHours: Boolean,
    startMinutes: Int,
    endMinutes: Int,
    onSave: (Boolean, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AuraTheme.colors
    var enabled by remember { mutableStateOf(hasBusyHours) }
    var start by remember { mutableIntStateOf(startMinutes) }
    var end by remember { mutableIntStateOf(endMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                text = "Busy hours",
                color = colors.primaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fixed work or college hours",
                        color = colors.primaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    AuraToggleSwitch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        contentDescriptionText = "Toggle fixed work or college hours"
                    )
                }

                if (enabled) {
                    TimeStepperRow(
                        label = "Starts at",
                        minutes = start,
                        onStep = { delta ->
                            val next = (start + delta).coerceIn(6 * 60, 18 * 60)
                            start = next
                            if (end <= start + 60) end = (start + 120).coerceAtMost(22 * 60)
                        }
                    )
                    TimeStepperRow(
                        label = "Ends at",
                        minutes = end,
                        onStep = { delta ->
                            val next = (end + delta).coerceIn(start + 60, 22 * 60)
                            end = next
                        }
                    )
                } else {
                    Text(
                        text = "We will treat your full waking day as open when suggesting workout times.",
                        color = colors.mutedText,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(enabled, start, end)
                    onDismiss()
                },
                modifier = Modifier.testTag("busy_hours_save_button")
            ) {
                Text("Save", color = colors.accent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.mutedText)
            }
        }
    )
}

@Composable
private fun TimeStepperRow(
    label: String,
    minutes: Int,
    onStep: (Int) -> Unit
) {
    val colors = AuraTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface2)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, color = colors.mutedText, fontSize = 12.sp)
            Text(
                text = WorkoutScheduleEngine.formatTimeMinutes(minutes),
                color = colors.primaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StepMiniButton(label = "−", onClick = { onStep(-30) })
            StepMiniButton(label = "+", onClick = { onStep(30) })
        }
    }
}

@Composable
internal fun StepMiniButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.surface3)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = colors.primaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
