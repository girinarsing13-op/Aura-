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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.aura.ui.theme.AuraTheme

/**
 * AURA Official Brand Mark
 * Clean, modern athletic monogram "A" with neon accent lighting and bevel contour.
 * Character/mascot completely removed per design specification.
 */
@Composable
fun AuraAppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showGlow: Boolean = true,
    shapeRadius: Dp = 14.dp
) {
    val accent = AuraTheme.colors.accent
    val infiniteTransition = rememberInfiniteTransition(label = "aura_logo_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                if (showGlow) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = pulseGlow * 0.40f),
                                accent.copy(alpha = pulseGlow * 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(this.size.width / 2f, this.size.height / 2f),
                            radius = this.size.maxDimension * 0.75f
                        )
                    )
                }
            }
            .clip(RoundedCornerShape(shapeRadius))
            .background(Color(0xFF0C0C12))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.8f),
                        accent.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(shapeRadius)
            )
            .testTag("aura_app_logo"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.65f)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = w * 0.14f

            // Sleek athletic "A" chevron vector
            val leftLeg = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                lineTo(w * 0.18f, h * 0.88f)
            }
            val rightLeg = Path().apply {
                moveTo(w * 0.5f, h * 0.12f)
                lineTo(w * 0.82f, h * 0.88f)
            }
            val crossBar = Path().apply {
                moveTo(w * 0.32f, h * 0.62f)
                lineTo(w * 0.68f, h * 0.62f)
            }

            // Draw primary athletic "A" monogram
            drawPath(
                path = leftLeg,
                color = accent,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = rightLeg,
                color = accent,
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = crossBar,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round)
            )

            // Dynamic peak accent dot
            drawCircle(
                color = Color.White,
                radius = strokeW * 0.40f,
                center = Offset(w * 0.5f, h * 0.12f)
            )
        }
    }
}
