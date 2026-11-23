package com.example.aura.ui.components

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.ui.theme.AuraTheme
import kotlinx.coroutines.delay

private val AuraFadeUpEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/**
 * Checks Android's system reduced-motion setting (ANIMATOR_DURATION_SCALE == 0f).
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * Staggered fade-up entrance matching `.p > * { animation: up .6s cubic-bezier(.2,.8,.2,1) both }`
 * with 14dp rise and 80ms stagger per child index. Respects reduced-motion.
 */
@Composable
fun AuraStaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    animationKey: Any = Unit,
    reducedMotion: Boolean = rememberReducedMotion(),
    content: @Composable () -> Unit
) {
    val progress = remember(animationKey, index) {
        Animatable(if (reducedMotion) 1f else 0f)
    }

    LaunchedEffect(animationKey, index, reducedMotion) {
        if (reducedMotion) {
            progress.snapTo(1f)
        } else {
            progress.snapTo(0f)
            val delayMs = (index.coerceAtMost(8) * 80L)
            if (delayMs > 0L) delay(delayMs)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = AuraFadeUpEasing)
            )
        }
    }

    val risePx = with(LocalDensity.current) { 14.dp.toPx() }
    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * risePx
        }
    ) {
        content()
    }
}

/**
 * Standard AURA card matching `.c`:
 * - Radius 24dp, padding 16dp by default.
 * - Selected state: accent tint (#1A1812 dark) with 2dp gold border.
 * - Highlighted state: accent tint with subtle gold border (#E8B64C55).
 * - Default state: surface (#151518 dark) with 1dp white 8% border.
 */
@Composable
fun AuraCard(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "auraCardScale"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            selected || highlighted -> colors.accentTint
            else -> colors.surface
        },
        animationSpec = tween(180),
        label = "auraCardBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            selected -> colors.accent
            highlighted -> colors.accent.copy(alpha = 0.35f)
            else -> colors.border
        },
        animationSpec = tween(180),
        label = "auraCardBorder"
    )
    val borderWidth = if (selected) 2.dp else 1.dp
    val shape = RoundedCornerShape(24.dp)

    Surface(
        shape = shape,
        color = containerColor,
        border = BorderStroke(borderWidth, borderColor),
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(shape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClick()
                            }
                        )
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Full-width primary or secondary rounded pill CTA matching `.b`:
 * - Height 52dp, CircleShape (fully rounded), bold 16sp text.
 * - Gold accent (#E8B64C) with dark #111 text for primary; surface3 (#26262B) for secondary.
 */
@Composable
fun AuraPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    height: Dp = 52.dp
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "auraPillBtnScale"
    )

    val bgColor = when {
        !enabled -> colors.surface2
        isPrimary -> colors.accent
        else -> colors.surface3
    }
    val fgColor = when {
        !enabled -> colors.mutedText
        isPrimary -> colors.accentContrast
        else -> colors.primaryText
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fgColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Selectable rounded pill chip matching `.a`:
 * - Selected: gold fill (#E8B64C), dark text (#111), SemiBold 13sp.
 * - Unselected: surface2 (#1E1E22), primary text (#F4F4F5), Normal 13sp.
 * - Meets 48dp touch target via minimumInteractiveComponentSize().
 */
@Composable
fun AuraChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    val bgColor by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.surface2,
        animationSpec = tween(160),
        label = "auraChipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.accentContrast else colors.primaryText,
        animationSpec = tween(160),
        label = "auraChipText"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(40.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                role = Role.Checkbox,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

/**
 * Circular numeric pill selector (e.g., Days per week 2..7, Meals per day 3..6).
 */
@Composable
fun AuraNumberChip(
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    val bgColor by animateColorAsState(
        targetValue = if (selected) colors.accent else colors.surface2,
        animationSpec = tween(160),
        label = "auraNumChipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.accentContrast else colors.primaryText,
        animationSpec = tween(160),
        label = "auraNumChipText"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(44.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                role = Role.RadioButton,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Animated progress ring matching `.rg` (64dp size, 6dp rounded stroke, 1.4s ease-out sweep from 0).
 */
@Composable
fun AuraRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp,
    trackColor: Color = AuraTheme.colors.trackMuted,
    reducedMotion: Boolean = rememberReducedMotion(),
    centerContent: (@Composable BoxScope.() -> Unit)? = null
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animatedSweep = remember { Animatable(if (reducedMotion) clamped else 0f) }
    val hasAnimatedInitially = remember { androidx.compose.runtime.mutableStateOf(false) }

    LaunchedEffect(clamped, reducedMotion) {
        if (reducedMotion) {
            animatedSweep.snapTo(clamped)
            hasAnimatedInitially.value = true
        } else if (!hasAnimatedInitially.value) {
            hasAnimatedInitially.value = true
            animatedSweep.snapTo(0f)
            animatedSweep.animateTo(
                targetValue = clamped,
                animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
            )
        } else {
            animatedSweep.animateTo(
                targetValue = clamped,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedSweep.value,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        if (centerContent != null) {
            centerContent()
        }
    }
}

/**
 * Minimal AURA toggle pill matching the reminder switch in Schedule (46x26dp, 20dp knob).
 */
@Composable
fun AuraToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescriptionText: String,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.accent else colors.trackMuted,
        animationSpec = tween(180),
        label = "auraToggleTrack"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 23.dp else 3.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
        label = "auraToggleThumb"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .semantics { contentDescription = contentDescriptionText }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(width = 46.dp, height = 26.dp)
                .clip(CircleShape)
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset, y = 3.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (checked) colors.accentContrast else colors.secondaryText)
            )
        }
    }
}
