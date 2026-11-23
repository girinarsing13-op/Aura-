package com.example.aura.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.MovementPattern
import com.example.aura.ui.theme.AuraTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class StickPose(
    val head: Offset,
    val shoulder: Offset,
    val elbowL: Offset,
    val handL: Offset,
    val elbowR: Offset,
    val handR: Offset,
    val hip: Offset,
    val kneeL: Offset,
    val footL: Offset,
    val kneeR: Offset,
    val footR: Offset
)

private fun lerpOffset(a: Offset, b: Offset, t: Float): Offset =
    Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

private fun lerpPose(a: StickPose, b: StickPose, t: Float): StickPose = StickPose(
    head = lerpOffset(a.head, b.head, t),
    shoulder = lerpOffset(a.shoulder, b.shoulder, t),
    elbowL = lerpOffset(a.elbowL, b.elbowL, t),
    handL = lerpOffset(a.handL, b.handL, t),
    elbowR = lerpOffset(a.elbowR, b.elbowR, t),
    handR = lerpOffset(a.handR, b.handR, t),
    hip = lerpOffset(a.hip, b.hip, t),
    kneeL = lerpOffset(a.kneeL, b.kneeL, t),
    footL = lerpOffset(a.footL, b.footL, t),
    kneeR = lerpOffset(a.kneeR, b.kneeR, t),
    footR = lerpOffset(a.footR, b.footR, t)
)

/**
 * Clean line-art animated stick figure drawn in Compose Canvas with rounded joints,
 * faint equipment hints, and 14 biomechanically distinct keyframe-interpolated patterns.
 */
@Composable
fun AnimatedExerciseFigure(
    movementPattern: MovementPattern,
    equipment: String = "",
    caption: String? = null,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val accent = AuraTheme.colors.accent
    val equipHintColor = AuraTheme.colors.textMuted.copy(alpha = 0.28f)
    val floorColor = AuraTheme.colors.cardBorder.copy(alpha = 0.55f)
    val headFillColor = if (AuraTheme.colors.isDark) Color(0xFF0A0A12) else AuraTheme.colors.surfaceSecondary
    val infiniteTransition = rememberInfiniteTransition(label = "figure_${movementPattern.name}")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pose_t"
    )

    val (startPose, endPose) = keyframesFor(movementPattern, equipment)
    val effectiveCaption = caption?.takeIf { it.isNotBlank() } ?: movementPattern.defaultCaption

    Box(
        modifier = modifier.testTag("animated_exercise_figure_${movementPattern.name}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (compact) 4.dp else 14.dp,
                    end = if (compact) 4.dp else 14.dp,
                    top = if (compact) 4.dp else 10.dp,
                    bottom = if (compact) 4.dp else 30.dp
                )
        ) {
            val pose = lerpPose(startPose, endPose, animPhase)
            val w = size.width
            val h = size.height
            val strokeMain = if (compact) 2.3.dp.toPx() else 3.8.dp.toPx()
            val strokeEquip = if (compact) 1.6.dp.toPx() else 2.6.dp.toPx()
            val jointRadius = if (compact) 2.2.dp.toPx() else 3.8.dp.toPx()
            val headRadius = if (compact) 3.8.dp.toPx() else 7.2.dp.toPx()

            fun pt(norm: Offset) = Offset(norm.x * w, norm.y * h)

            // Floor baseline
            drawLine(
                color = floorColor,
                start = Offset(w * 0.10f, h * 0.88f),
                end = Offset(w * 0.90f, h * 0.88f),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Faint equipment hint behind figure
            drawEquipmentHint(
                pattern = movementPattern,
                equipment = equipment,
                pose = pose,
                pt = ::pt,
                hintColor = equipHintColor,
                strokeWidth = strokeEquip,
                compact = compact
            )

            // Breathing ring pulse on isometric holds
            if (movementPattern == MovementPattern.PLANK_HOLD) {
                val hipPt = pt(pose.hip)
                val shoulderPt = pt(pose.shoulder)
                val coreMid = Offset((hipPt.x + shoulderPt.x) * 0.5f, (hipPt.y + shoulderPt.y) * 0.5f)
                val pulseR = (if (compact) 7.dp else 16.dp).toPx() * (0.85f + 0.35f * animPhase)
                drawCircle(
                    color = accent.copy(alpha = 0.24f * (1f - 0.5f * animPhase)),
                    radius = pulseR,
                    center = coreMid,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Back limbs (slightly softer alpha for depth)
            val secondaryLimb = accent.copy(alpha = 0.60f)
            drawLimb(pt(pose.shoulder), pt(pose.elbowR), secondaryLimb, strokeMain)
            drawLimb(pt(pose.elbowR), pt(pose.handR), secondaryLimb, strokeMain)
            drawLimb(pt(pose.hip), pt(pose.kneeR), secondaryLimb, strokeMain)
            drawLimb(pt(pose.kneeR), pt(pose.footR), secondaryLimb, strokeMain)

            // Torso + Neck
            drawLimb(pt(pose.hip), pt(pose.shoulder), accent, strokeMain * 1.12f)
            drawLimb(pt(pose.shoulder), pt(pose.head), accent, strokeMain * 0.85f)

            // Front limbs
            drawLimb(pt(pose.shoulder), pt(pose.elbowL), accent, strokeMain)
            drawLimb(pt(pose.elbowL), pt(pose.handL), accent, strokeMain)
            drawLimb(pt(pose.hip), pt(pose.kneeL), accent, strokeMain)
            drawLimb(pt(pose.kneeL), pt(pose.footL), accent, strokeMain)

            // Rounded joints
            val joints = listOf(
                pose.shoulder, pose.elbowL, pose.handL,
                pose.hip, pose.kneeL, pose.footL
            )
            joints.forEach { j ->
                drawCircle(
                    color = accent,
                    radius = jointRadius,
                    center = pt(j)
                )
            }

            // Head (hollow athletic ring)
            val headCenter = pt(pose.head)
            drawCircle(
                color = headFillColor,
                radius = headRadius,
                center = headCenter
            )
            drawCircle(
                color = accent,
                radius = headRadius,
                center = headCenter,
                style = Stroke(width = strokeMain * 0.8f)
            )
        }

        if (!compact) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clip(CircleShape)
                    .background(Color(0xD00B0B14))
                    .border(1.dp, accent.copy(alpha = 0.30f), CircleShape)
                    .padding(horizontal = 11.dp, vertical = 4.dp)
            ) {
                Text(
                    text = effectiveCaption,
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun DrawScope.drawLimb(start: Offset, end: Offset, color: Color, strokeWidth: Float) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawEquipmentHint(
    pattern: MovementPattern,
    equipment: String,
    pose: StickPose,
    pt: (Offset) -> Offset,
    hintColor: Color,
    strokeWidth: Float,
    compact: Boolean
) {
    val eq = equipment.lowercase()
    val hasBarbell = eq.contains("barbell") || eq.contains("ez-bar") || eq.contains("smith")
    val hasDumbbell = eq.contains("dumbbell")
    val hasPullupBar = eq.contains("pull-up") || pattern == MovementPattern.VERTICAL_PULL
    val hasCable = eq.contains("cable")

    when (pattern) {
        MovementPattern.HORIZONTAL_PUSH -> {
            if (!eq.contains("bodyweight")) {
                // Flat bench silhouette
                drawLine(hintColor, pt(Offset(0.24f, 0.72f)), pt(Offset(0.76f, 0.72f)), strokeWidth * 1.4f, StrokeCap.Round)
                drawLine(hintColor, pt(Offset(0.32f, 0.72f)), pt(Offset(0.32f, 0.87f)), strokeWidth, StrokeCap.Round)
                drawLine(hintColor, pt(Offset(0.68f, 0.72f)), pt(Offset(0.68f, 0.87f)), strokeWidth, StrokeCap.Round)
                drawWeightedHandObjects(pt(pose.handL), pt(pose.handR), hasBarbell || !hasDumbbell, hintColor, strokeWidth, compact)
            }
        }
        MovementPattern.INCLINE_PUSH -> {
            // Incline bench silhouette
            drawLine(hintColor, pt(Offset(0.25f, 0.80f)), pt(Offset(0.50f, 0.76f)), strokeWidth * 1.3f, StrokeCap.Round)
            drawLine(hintColor, pt(Offset(0.50f, 0.76f)), pt(Offset(0.72f, 0.50f)), strokeWidth * 1.4f, StrokeCap.Round)
            drawLine(hintColor, pt(Offset(0.64f, 0.60f)), pt(Offset(0.64f, 0.87f)), strokeWidth, StrokeCap.Round)
            drawWeightedHandObjects(pt(pose.handL), pt(pose.handR), hasBarbell || !hasDumbbell, hintColor, strokeWidth, compact)
        }
        MovementPattern.VERTICAL_PUSH -> {
            drawWeightedHandObjects(pt(pose.handL), pt(pose.handR), hasBarbell, hintColor, strokeWidth, compact)
        }
        MovementPattern.VERTICAL_PULL -> {
            if (hasPullupBar || !hasCable) {
                drawLine(hintColor, pt(Offset(0.20f, 0.14f)), pt(Offset(0.80f, 0.14f)), strokeWidth * 1.4f, StrokeCap.Round)
            } else {
                drawLine(hintColor, pt(Offset(0.50f, 0.08f)), pt(pose.handL).let { pt(pose.handL) }, strokeWidth * 0.9f, StrokeCap.Round)
            }
        }
        MovementPattern.HORIZONTAL_PULL -> {
            if (hasCable || eq.contains("row") || eq.contains("band")) {
                drawLine(hintColor, pt(Offset(0.16f, 0.25f)), pt(Offset(0.16f, 0.87f)), strokeWidth, StrokeCap.Round)
                val hand = pt(pose.handL)
                drawLine(hintColor, pt(Offset(0.16f, 0.52f)), hand, strokeWidth * 0.9f, StrokeCap.Round)
            } else {
                drawWeightedHandObjects(pt(pose.handL), pt(pose.handR), hasBarbell, hintColor, strokeWidth, compact)
            }
        }
        MovementPattern.SQUAT -> {
            if (hasBarbell) {
                val sPt = pt(pose.shoulder)
                val r = if (compact) 4.dp.toPx() else 8.dp.toPx()
                drawCircle(hintColor, r, sPt, style = Stroke(width = strokeWidth))
            } else if (hasDumbbell) {
                drawDumbbellPlate(pt(pose.handL), hintColor, strokeWidth, compact)
            }
        }
        MovementPattern.LUNGE_SPLIT_SQUAT -> {
            if (hasDumbbell) {
                drawDumbbellPlate(pt(pose.handL), hintColor, strokeWidth, compact)
                drawDumbbellPlate(pt(pose.handR), hintColor, strokeWidth, compact)
            }
        }
        MovementPattern.HIP_HINGE -> {
            if (hasBarbell || !eq.contains("bodyweight")) {
                val hL = pt(pose.handL)
                drawLine(hintColor, Offset(hL.x - 16.dp.toPx(), hL.y), Offset(hL.x + 16.dp.toPx(), hL.y), strokeWidth * 1.1f, StrokeCap.Round)
                val pr = if (compact) 4.dp.toPx() else 7.dp.toPx()
                drawCircle(hintColor, pr, hL, style = Stroke(width = strokeWidth))
            }
        }
        MovementPattern.CURL, MovementPattern.RAISE -> {
            if (hasCable) {
                drawLine(hintColor, pt(Offset(0.18f, 0.20f)), pt(Offset(0.18f, 0.87f)), strokeWidth, StrokeCap.Round)
                drawLine(hintColor, pt(Offset(0.18f, 0.78f)), pt(pose.handL), strokeWidth * 0.85f, StrokeCap.Round)
            } else {
                drawDumbbellPlate(pt(pose.handL), hintColor, strokeWidth, compact)
                drawDumbbellPlate(pt(pose.handR), hintColor, strokeWidth, compact)
            }
        }
        MovementPattern.TRICEP_EXT_DIP -> {
            if (hasCable) {
                drawLine(hintColor, pt(Offset(0.24f, 0.14f)), pt(Offset(0.24f, 0.87f)), strokeWidth, StrokeCap.Round)
                drawLine(hintColor, pt(Offset(0.24f, 0.22f)), pt(pose.handL), strokeWidth * 0.9f, StrokeCap.Round)
            } else if (eq.contains("dip") || eq.contains("bodyweight")) {
                drawLine(hintColor, pt(Offset(0.32f, 0.62f)), pt(Offset(0.32f, 0.87f)), strokeWidth, StrokeCap.Round)
                drawLine(hintColor, pt(Offset(0.68f, 0.62f)), pt(Offset(0.68f, 0.87f)), strokeWidth, StrokeCap.Round)
            } else {
                drawDumbbellPlate(pt(pose.handL), hintColor, strokeWidth, compact)
            }
        }
        else -> Unit
    }
}

private fun DrawScope.drawWeightedHandObjects(
    handL: Offset,
    handR: Offset,
    asBarbell: Boolean,
    color: Color,
    strokeWidth: Float,
    compact: Boolean
) {
    if (asBarbell) {
        val barEnd1 = Offset(handL.x - (if (compact) 8.dp else 18.dp).toPx(), handL.y)
        val barEnd2 = Offset(handL.x + (if (compact) 8.dp else 18.dp).toPx(), handL.y)
        drawLine(color, barEnd1, barEnd2, strokeWidth, StrokeCap.Round)
        val pr = (if (compact) 3.dp else 6.5.dp).toPx()
        drawCircle(color, pr, handL, style = Stroke(width = strokeWidth))
    } else {
        drawDumbbellPlate(handL, color, strokeWidth, compact)
        drawDumbbellPlate(handR, color, strokeWidth, compact)
    }
}

private fun DrawScope.drawDumbbellPlate(center: Offset, color: Color, strokeWidth: Float, compact: Boolean) {
    val span = (if (compact) 4.dp else 8.dp).toPx()
    val halfH = (if (compact) 2.5.dp else 5.dp).toPx()
    drawLine(color, Offset(center.x - span, center.y), Offset(center.x + span, center.y), strokeWidth, StrokeCap.Round)
    drawLine(color, Offset(center.x - span, center.y - halfH), Offset(center.x - span, center.y + halfH), strokeWidth * 1.2f, StrokeCap.Round)
    drawLine(color, Offset(center.x + span, center.y - halfH), Offset(center.x + span, center.y + halfH), strokeWidth * 1.2f, StrokeCap.Round)
}

private fun keyframesFor(pattern: MovementPattern, equipment: String): Pair<StickPose, StickPose> {
    val eq = equipment.lowercase()
    return when (pattern) {
        MovementPattern.HORIZONTAL_PUSH -> {
            if (eq.contains("bodyweight")) {
                // Push-up: lowered chest vs top lockout
                val down = StickPose(
                    head = Offset(0.75f, 0.72f),
                    shoulder = Offset(0.65f, 0.77f),
                    elbowL = Offset(0.56f, 0.66f),
                    handL = Offset(0.66f, 0.87f),
                    elbowR = Offset(0.58f, 0.68f),
                    handR = Offset(0.68f, 0.87f),
                    hip = Offset(0.42f, 0.80f),
                    kneeL = Offset(0.30f, 0.83f),
                    footL = Offset(0.18f, 0.87f),
                    kneeR = Offset(0.31f, 0.84f),
                    footR = Offset(0.19f, 0.87f)
                )
                val up = StickPose(
                    head = Offset(0.74f, 0.45f),
                    shoulder = Offset(0.66f, 0.52f),
                    elbowL = Offset(0.66f, 0.70f),
                    handL = Offset(0.66f, 0.87f),
                    elbowR = Offset(0.68f, 0.70f),
                    handR = Offset(0.68f, 0.87f),
                    hip = Offset(0.42f, 0.68f),
                    kneeL = Offset(0.30f, 0.77f),
                    footL = Offset(0.18f, 0.87f),
                    kneeR = Offset(0.31f, 0.78f),
                    footR = Offset(0.19f, 0.87f)
                )
                down to up
            } else {
                // Supine bench press: bar at chest -> lockout
                val bottom = StickPose(
                    head = Offset(0.68f, 0.62f),
                    shoulder = Offset(0.58f, 0.66f),
                    elbowL = Offset(0.56f, 0.74f),
                    handL = Offset(0.56f, 0.57f),
                    elbowR = Offset(0.60f, 0.73f),
                    handR = Offset(0.59f, 0.57f),
                    hip = Offset(0.36f, 0.68f),
                    kneeL = Offset(0.24f, 0.68f),
                    footL = Offset(0.22f, 0.87f),
                    kneeR = Offset(0.27f, 0.69f),
                    footR = Offset(0.25f, 0.87f)
                )
                val top = bottom.copy(
                    elbowL = Offset(0.57f, 0.50f),
                    handL = Offset(0.57f, 0.34f),
                    elbowR = Offset(0.60f, 0.50f),
                    handR = Offset(0.60f, 0.34f)
                )
                bottom to top
            }
        }
        MovementPattern.INCLINE_PUSH -> {
            val bottom = StickPose(
                head = Offset(0.68f, 0.38f),
                shoulder = Offset(0.61f, 0.48f),
                elbowL = Offset(0.55f, 0.58f),
                handL = Offset(0.53f, 0.41f),
                elbowR = Offset(0.59f, 0.59f),
                handR = Offset(0.56f, 0.42f),
                hip = Offset(0.42f, 0.70f),
                kneeL = Offset(0.28f, 0.73f),
                footL = Offset(0.25f, 0.87f),
                kneeR = Offset(0.31f, 0.74f),
                footR = Offset(0.28f, 0.87f)
            )
            val top = bottom.copy(
                elbowL = Offset(0.56f, 0.34f),
                handL = Offset(0.53f, 0.20f),
                elbowR = Offset(0.58f, 0.35f),
                handR = Offset(0.56f, 0.21f)
            )
            bottom to top
        }
        MovementPattern.VERTICAL_PUSH -> {
            val start = StickPose(
                head = Offset(0.50f, 0.24f),
                shoulder = Offset(0.50f, 0.36f),
                elbowL = Offset(0.38f, 0.44f),
                handL = Offset(0.38f, 0.31f),
                elbowR = Offset(0.62f, 0.44f),
                handR = Offset(0.62f, 0.31f),
                hip = Offset(0.50f, 0.58f),
                kneeL = Offset(0.45f, 0.73f),
                footL = Offset(0.43f, 0.87f),
                kneeR = Offset(0.55f, 0.73f),
                footR = Offset(0.57f, 0.87f)
            )
            val end = start.copy(
                elbowL = Offset(0.41f, 0.24f),
                handL = Offset(0.43f, 0.12f),
                elbowR = Offset(0.59f, 0.24f),
                handR = Offset(0.57f, 0.12f)
            )
            start to end
        }
        MovementPattern.VERTICAL_PULL -> {
            val hang = StickPose(
                head = Offset(0.50f, 0.44f),
                shoulder = Offset(0.50f, 0.52f),
                elbowL = Offset(0.41f, 0.33f),
                handL = Offset(0.36f, 0.14f),
                elbowR = Offset(0.59f, 0.33f),
                handR = Offset(0.64f, 0.14f),
                hip = Offset(0.50f, 0.70f),
                kneeL = Offset(0.47f, 0.81f),
                footL = Offset(0.47f, 0.87f),
                kneeR = Offset(0.53f, 0.81f),
                footR = Offset(0.53f, 0.87f)
            )
            val peak = StickPose(
                head = Offset(0.50f, 0.23f),
                shoulder = Offset(0.50f, 0.32f),
                elbowL = Offset(0.35f, 0.36f),
                handL = Offset(0.36f, 0.14f),
                elbowR = Offset(0.65f, 0.36f),
                handR = Offset(0.64f, 0.14f),
                hip = Offset(0.50f, 0.54f),
                kneeL = Offset(0.47f, 0.68f),
                footL = Offset(0.47f, 0.78f),
                kneeR = Offset(0.53f, 0.68f),
                footR = Offset(0.53f, 0.78f)
            )
            hang to peak
        }
        MovementPattern.HORIZONTAL_PULL -> {
            val stretch = StickPose(
                head = Offset(0.62f, 0.38f),
                shoulder = Offset(0.56f, 0.45f),
                elbowL = Offset(0.42f, 0.50f),
                handL = Offset(0.29f, 0.53f),
                elbowR = Offset(0.44f, 0.52f),
                handR = Offset(0.31f, 0.55f),
                hip = Offset(0.44f, 0.66f),
                kneeL = Offset(0.32f, 0.73f),
                footL = Offset(0.24f, 0.87f),
                kneeR = Offset(0.35f, 0.75f),
                footR = Offset(0.27f, 0.87f)
            )
            val contracted = stretch.copy(
                head = Offset(0.66f, 0.35f),
                shoulder = Offset(0.58f, 0.43f),
                elbowL = Offset(0.68f, 0.53f),
                handL = Offset(0.53f, 0.55f),
                elbowR = Offset(0.70f, 0.55f),
                handR = Offset(0.55f, 0.57f)
            )
            stretch to contracted
        }
        MovementPattern.SQUAT -> {
            val standing = StickPose(
                head = Offset(0.52f, 0.22f),
                shoulder = Offset(0.50f, 0.34f),
                elbowL = Offset(0.56f, 0.44f),
                handL = Offset(0.60f, 0.36f),
                elbowR = Offset(0.46f, 0.44f),
                handR = Offset(0.50f, 0.36f),
                hip = Offset(0.47f, 0.56f),
                kneeL = Offset(0.50f, 0.72f),
                footL = Offset(0.48f, 0.87f),
                kneeR = Offset(0.47f, 0.72f),
                footR = Offset(0.45f, 0.87f)
            )
            val bottom = StickPose(
                head = Offset(0.55f, 0.42f),
                shoulder = Offset(0.49f, 0.51f),
                elbowL = Offset(0.56f, 0.58f),
                handL = Offset(0.62f, 0.52f),
                elbowR = Offset(0.46f, 0.58f),
                handR = Offset(0.52f, 0.52f),
                hip = Offset(0.36f, 0.70f),
                kneeL = Offset(0.56f, 0.69f),
                footL = Offset(0.50f, 0.87f),
                kneeR = Offset(0.52f, 0.70f),
                footR = Offset(0.47f, 0.87f)
            )
            standing to bottom
        }
        MovementPattern.LUNGE_SPLIT_SQUAT -> {
            val high = StickPose(
                head = Offset(0.50f, 0.22f),
                shoulder = Offset(0.50f, 0.34f),
                elbowL = Offset(0.47f, 0.45f),
                handL = Offset(0.48f, 0.56f),
                elbowR = Offset(0.53f, 0.45f),
                handR = Offset(0.54f, 0.56f),
                hip = Offset(0.50f, 0.55f),
                kneeL = Offset(0.62f, 0.70f),
                footL = Offset(0.64f, 0.87f),
                kneeR = Offset(0.37f, 0.71f),
                footR = Offset(0.30f, 0.87f)
            )
            val low = StickPose(
                head = Offset(0.52f, 0.34f),
                shoulder = Offset(0.51f, 0.45f),
                elbowL = Offset(0.48f, 0.56f),
                handL = Offset(0.49f, 0.66f),
                elbowR = Offset(0.54f, 0.56f),
                handR = Offset(0.55f, 0.66f),
                hip = Offset(0.49f, 0.66f),
                kneeL = Offset(0.66f, 0.67f),
                footL = Offset(0.66f, 0.87f),
                kneeR = Offset(0.38f, 0.84f),
                footR = Offset(0.25f, 0.87f)
            )
            high to low
        }
        MovementPattern.HIP_HINGE -> {
            val hinged = StickPose(
                head = Offset(0.68f, 0.44f),
                shoulder = Offset(0.60f, 0.50f),
                elbowL = Offset(0.61f, 0.64f),
                handL = Offset(0.61f, 0.76f),
                elbowR = Offset(0.58f, 0.64f),
                handR = Offset(0.58f, 0.76f),
                hip = Offset(0.36f, 0.56f),
                kneeL = Offset(0.44f, 0.72f),
                footL = Offset(0.46f, 0.87f),
                kneeR = Offset(0.41f, 0.72f),
                footR = Offset(0.43f, 0.87f)
            )
            val lockout = StickPose(
                head = Offset(0.49f, 0.22f),
                shoulder = Offset(0.48f, 0.34f),
                elbowL = Offset(0.50f, 0.46f),
                handL = Offset(0.52f, 0.57f),
                elbowR = Offset(0.46f, 0.46f),
                handR = Offset(0.48f, 0.57f),
                hip = Offset(0.49f, 0.56f),
                kneeL = Offset(0.50f, 0.72f),
                footL = Offset(0.49f, 0.87f),
                kneeR = Offset(0.46f, 0.72f),
                footR = Offset(0.45f, 0.87f)
            )
            hinged to lockout
        }
        MovementPattern.CURL -> {
            val down = StickPose(
                head = Offset(0.48f, 0.22f),
                shoulder = Offset(0.48f, 0.35f),
                elbowL = Offset(0.50f, 0.49f),
                handL = Offset(0.53f, 0.62f),
                elbowR = Offset(0.45f, 0.49f),
                handR = Offset(0.47f, 0.62f),
                hip = Offset(0.47f, 0.58f),
                kneeL = Offset(0.49f, 0.73f),
                footL = Offset(0.49f, 0.87f),
                kneeR = Offset(0.45f, 0.73f),
                footR = Offset(0.45f, 0.87f)
            )
            val up = down.copy(
                handL = Offset(0.58f, 0.36f),
                handR = Offset(0.53f, 0.36f)
            )
            down to up
        }
        MovementPattern.TRICEP_EXT_DIP -> {
            val flexed = StickPose(
                head = Offset(0.50f, 0.24f),
                shoulder = Offset(0.48f, 0.36f),
                elbowL = Offset(0.54f, 0.47f),
                handL = Offset(0.62f, 0.39f),
                elbowR = Offset(0.43f, 0.47f),
                handR = Offset(0.51f, 0.39f),
                hip = Offset(0.47f, 0.58f),
                kneeL = Offset(0.49f, 0.73f),
                footL = Offset(0.49f, 0.87f),
                kneeR = Offset(0.45f, 0.73f),
                footR = Offset(0.45f, 0.87f)
            )
            val extended = flexed.copy(
                handL = Offset(0.55f, 0.62f),
                handR = Offset(0.44f, 0.62f)
            )
            flexed to extended
        }
        MovementPattern.RAISE -> {
            val down = StickPose(
                head = Offset(0.50f, 0.22f),
                shoulder = Offset(0.50f, 0.35f),
                elbowL = Offset(0.40f, 0.47f),
                handL = Offset(0.39f, 0.59f),
                elbowR = Offset(0.60f, 0.47f),
                handR = Offset(0.61f, 0.59f),
                hip = Offset(0.50f, 0.58f),
                kneeL = Offset(0.45f, 0.73f),
                footL = Offset(0.44f, 0.87f),
                kneeR = Offset(0.55f, 0.73f),
                footR = Offset(0.56f, 0.87f)
            )
            val raised = down.copy(
                elbowL = Offset(0.34f, 0.35f),
                handL = Offset(0.20f, 0.34f),
                elbowR = Offset(0.66f, 0.35f),
                handR = Offset(0.80f, 0.34f)
            )
            down to raised
        }
        MovementPattern.CRUNCH_LEG_RAISE -> {
            val open = StickPose(
                head = Offset(0.76f, 0.79f),
                shoulder = Offset(0.65f, 0.82f),
                elbowL = Offset(0.73f, 0.74f),
                handL = Offset(0.76f, 0.79f),
                elbowR = Offset(0.70f, 0.76f),
                handR = Offset(0.74f, 0.80f),
                hip = Offset(0.46f, 0.84f),
                kneeL = Offset(0.32f, 0.67f),
                footL = Offset(0.22f, 0.86f),
                kneeR = Offset(0.35f, 0.69f),
                footR = Offset(0.26f, 0.86f)
            )
            val tucked = StickPose(
                head = Offset(0.62f, 0.57f),
                shoulder = Offset(0.56f, 0.67f),
                elbowL = Offset(0.59f, 0.55f),
                handL = Offset(0.63f, 0.57f),
                elbowR = Offset(0.57f, 0.57f),
                handR = Offset(0.61f, 0.58f),
                hip = Offset(0.44f, 0.83f),
                kneeL = Offset(0.38f, 0.62f),
                footL = Offset(0.28f, 0.70f),
                kneeR = Offset(0.41f, 0.64f),
                footR = Offset(0.31f, 0.72f)
            )
            open to tucked
        }
        MovementPattern.PLANK_HOLD -> {
            val holdA = StickPose(
                head = Offset(0.77f, 0.64f),
                shoulder = Offset(0.69f, 0.69f),
                elbowL = Offset(0.69f, 0.85f),
                handL = Offset(0.77f, 0.85f),
                elbowR = Offset(0.66f, 0.85f),
                handR = Offset(0.74f, 0.85f),
                hip = Offset(0.45f, 0.72f),
                kneeL = Offset(0.31f, 0.79f),
                footL = Offset(0.18f, 0.86f),
                kneeR = Offset(0.32f, 0.80f),
                footR = Offset(0.20f, 0.86f)
            )
            val holdB = holdA.copy(
                hip = Offset(0.45f, 0.705f),
                shoulder = Offset(0.69f, 0.68f)
            )
            holdA to holdB
        }
        MovementPattern.CARDIO_JUMP -> {
            val base = StickPose(
                head = Offset(0.50f, 0.25f),
                shoulder = Offset(0.50f, 0.37f),
                elbowL = Offset(0.44f, 0.49f),
                handL = Offset(0.42f, 0.60f),
                elbowR = Offset(0.56f, 0.49f),
                handR = Offset(0.58f, 0.60f),
                hip = Offset(0.50f, 0.59f),
                kneeL = Offset(0.47f, 0.73f),
                footL = Offset(0.46f, 0.87f),
                kneeR = Offset(0.53f, 0.73f),
                footR = Offset(0.54f, 0.87f)
            )
            val peak = StickPose(
                head = Offset(0.50f, 0.19f),
                shoulder = Offset(0.50f, 0.31f),
                elbowL = Offset(0.36f, 0.24f),
                handL = Offset(0.39f, 0.14f),
                elbowR = Offset(0.64f, 0.24f),
                handR = Offset(0.61f, 0.14f),
                hip = Offset(0.50f, 0.52f),
                kneeL = Offset(0.40f, 0.66f),
                footL = Offset(0.34f, 0.80f),
                kneeR = Offset(0.60f, 0.66f),
                footR = Offset(0.66f, 0.80f)
            )
            base to peak
        }
    }
}
