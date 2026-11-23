package com.example.aura.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.ui.theme.AuraTheme
import kotlin.math.roundToInt

data class NavTabItem(
    val id: String,
    val title: String,
    val outlineIcon: ImageVector,
    val filledIcon: ImageVector
)

// ============================================================================
// AURA MINIMAL LINE ICONS (Matching approved 390x844 HTML design preview)
// ============================================================================

val NavHomeOutlineIcon: ImageVector = ImageVector.Builder(
    name = "AuraNavHome",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(3f, 11f)
        lineTo(12f, 3f)
        lineTo(21f, 11f)
        lineTo(21f, 20f)
        arcTo(1f, 1f, 0f, false, true, 20f, 21f)
        lineTo(15f, 21f)
        lineTo(15f, 15f)
        lineTo(9f, 15f)
        lineTo(9f, 21f)
        lineTo(4f, 21f)
        arcTo(1f, 1f, 0f, false, true, 3f, 20f)
        close()
    }
}.build()

val NavHomeFilledIcon: ImageVector = NavHomeOutlineIcon

val NavWorkoutOutlineIcon: ImageVector = ImageVector.Builder(
    name = "AuraNavWorkout",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(6f, 7f)
        lineTo(6f, 17f)
        moveTo(18f, 7f)
        lineTo(18f, 17f)
        moveTo(3f, 10f)
        lineTo(3f, 14f)
        moveTo(21f, 10f)
        lineTo(21f, 14f)
        moveTo(6f, 12f)
        lineTo(18f, 12f)
    }
}.build()

val NavWorkoutFilledIcon: ImageVector = NavWorkoutOutlineIcon

val NavDietOutlineIcon: ImageVector = ImageVector.Builder(
    name = "AuraNavDiet",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // Apple outline from HTML preview
        moveTo(12f, 7f)
        curveTo(9f, 4f, 4f, 7f, 6f, 13f)
        curveTo(7f, 16f, 9f, 19f, 12f, 18f)
        curveTo(15f, 19f, 17f, 16f, 18f, 13f)
        curveTo(20f, 7f, 15f, 4f, 12f, 7f)
        close()
        moveTo(12f, 7f)
        curveTo(12f, 5f, 13f, 4f, 15f, 3f)
    }
}.build()

val NavDietFilledIcon: ImageVector = NavDietOutlineIcon

val NavProfileOutlineIcon: ImageVector = ImageVector.Builder(
    name = "AuraNavProfile",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        // Head circle (cx=12, cy=8, r=4)
        moveTo(12f, 4f)
        arcTo(4f, 4f, 0f, true, true, 11.99f, 4f)
        close()
        // Shoulders arc
        moveTo(4f, 21f)
        curveTo(5f, 17f, 8f, 15f, 12f, 15f)
        curveTo(16f, 15f, 19f, 17f, 20f, 21f)
    }
}.build()

val NavProfileFilledIcon: ImageVector = NavProfileOutlineIcon

// ============================================================================
// PRESERVED UTILITY SPORT ICONS (Referenced by existing workout/streak screens)
// ============================================================================

val SportStopwatchFilledIcon: ImageVector = ImageVector.Builder(
    name = "SportStopwatchFilled",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round
    ) {
        moveTo(10f, 2.5f)
        lineTo(14f, 2.5f)
        moveTo(12f, 2.5f)
        lineTo(12f, 5f)
    }
    path(fill = SolidColor(Color.White)) {
        moveTo(12f, 5f)
        arcTo(8.5f, 8.5f, 0f, true, true, 11.99f, 5f)
        close()
    }
    path(
        stroke = SolidColor(Color(0xFF0F172A)),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round
    ) {
        moveTo(12f, 13.5f)
        lineTo(12f, 8.5f)
        moveTo(12f, 13.5f)
        lineTo(15.2f, 13.5f)
    }
}.build()

val NavStreaksFilledIcon: ImageVector = ImageVector.Builder(
    name = "NavStreaksFilled",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(12f, 2f)
        curveTo(14.5f, 6f, 18.5f, 8.5f, 18.5f, 13.5f)
        arcTo(6.5f, 6.5f, 0f, false, true, 5.5f, 13.5f)
        curveTo(5.5f, 10.5f, 7.2f, 8.2f, 8.5f, 7f)
        curveTo(8.5f, 9f, 9.8f, 10f, 10.8f, 10f)
        curveTo(10.5f, 7.5f, 11f, 4.5f, 12f, 2f)
        close()
    }
}.build()

val SportStopwatchOutlineIcon: ImageVector = ImageVector.Builder(
    name = "SportStopwatchOutline",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.White),
        strokeLineWidth = 1.9f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(10.5f, 2.5f)
        lineTo(13.5f, 2.5f)
        moveTo(12f, 2.5f)
        lineTo(12f, 5.2f)
        moveTo(12f, 5.2f)
        arcTo(8.3f, 8.3f, 0f, true, true, 11.99f, 5.2f)
        close()
        moveTo(12f, 9.2f)
        lineTo(12f, 13.5f)
        lineTo(15.2f, 13.5f)
    }
}.build()

val SportScoreboardFilledIcon: ImageVector = ImageVector.Builder(
    name = "SportScoreboardFilled",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = SolidColor(Color.White)) {
        moveTo(4f, 6f)
        lineTo(20f, 6f)
        arcTo(2f, 2f, 0f, false, true, 22f, 8f)
        lineTo(22f, 16f)
        arcTo(2f, 2f, 0f, false, true, 20f, 18f)
        lineTo(4f, 18f)
        arcTo(2f, 2f, 0f, false, true, 2f, 16f)
        lineTo(2f, 8f)
        arcTo(2f, 2f, 0f, false, true, 4f, 6f)
        close()
    }
    path(
        stroke = SolidColor(Color(0xFF0F172A)),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(6f, 10.5f)
        lineTo(7.2f, 9.5f)
        lineTo(7.2f, 14.5f)
        moveTo(6f, 14.5f)
        lineTo(8.4f, 14.5f)
        moveTo(12f, 10.5f)
        lineTo(12f, 11f)
        moveTo(12f, 13f)
        lineTo(12f, 13.5f)
        moveTo(15.5f, 10.5f)
        curveTo(15.5f, 9.5f, 18f, 9.5f, 18f, 11.2f)
        lineTo(15.5f, 14.5f)
        lineTo(18.5f, 14.5f)
    }
}.build()

val NAV_TABS = listOf(
    NavTabItem("home", "Home", NavHomeOutlineIcon, NavHomeFilledIcon),
    NavTabItem("workout", "Workout", NavWorkoutOutlineIcon, NavWorkoutFilledIcon),
    NavTabItem("diet", "Diet", NavDietOutlineIcon, NavDietFilledIcon),
    NavTabItem("profile", "Profile", NavProfileOutlineIcon, NavProfileFilledIcon)
)

/**
 * Floating pill navigation bar matching `.n`:
 * - 64dp tall, 20dp from sides and bottom (+ navigationBarsPadding).
 * - Translucent #222226 (dark) / crisp white (light), 1dp border, soft shadow.
 * - 4 tabs: Home, Workout, Diet, Profile.
 * - Active tab shows a spring-sliding gold pill (#E8B64C) with icon and label;
 *   inactive tabs show icon only.
 */
@Composable
fun AuraPillNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    navTransparency: Float = 0.05f
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val reducedMotion = rememberReducedMotion()

    val effectiveTab = if (NAV_TABS.any { it.id == activeTab }) activeTab else "home"
    val alphaFactor = (0.92f - (navTransparency * 0.20f)).coerceIn(0.76f, 0.96f)
    val pillBackground = colors.navPillBg.copy(alpha = alphaFactor)

    // Track each tab slot's x-offset and width in pixels so the gold pill slides smoothly
    val slotBounds = remember { mutableStateMapOf<String, Pair<Float, Float>>() }
    val activeBounds = slotBounds[effectiveTab]

    val targetOffsetDp = with(density) { (activeBounds?.first ?: 0f).toDp() }
    val targetWidthDp = with(density) { (activeBounds?.second ?: 0f).toDp() }

    val pillSpring = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = 0.78f,
        stiffness = Spring.StiffnessMediumLow
    )
    val animatedOffsetX by animateDpAsState(
        targetValue = targetOffsetDp,
        animationSpec = pillSpring,
        label = "navPillOffset"
    )
    val animatedWidth by animateDpAsState(
        targetValue = targetWidthDp,
        animationSpec = pillSpring,
        label = "navPillWidth"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            .testTag("aura_bottom_navigation_bar"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = pillBackground,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, colors.navPillBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Sliding gold active pill indicator
                if (activeBounds != null && animatedWidth > 0.dp) {
                    val xPx = with(density) {
                        (if (reducedMotion) targetOffsetDp else animatedOffsetX).toPx().roundToInt()
                    }
                    val pillW = if (reducedMotion) targetWidthDp else animatedWidth
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = xPx, y = 0) }
                            .width(pillW)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                    )
                }

                // 4 Navigation destinations
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NAV_TABS.forEach { tab ->
                        val isSelected = effectiveTab == tab.id
                        Row(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .height(44.dp)
                                .onGloballyPositioned { coords ->
                                    slotBounds[tab.id] =
                                        coords.positionInParent().x to coords.size.width.toFloat()
                                }
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    role = Role.Tab
                                ) {
                                    if (!isSelected) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTabSelected(tab.id)
                                    }
                                }
                                .padding(horizontal = if (isSelected) 16.dp else 11.dp)
                                .testTag("nav_tab_${tab.id}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.filledIcon else tab.outlineIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) colors.accentContrast else colors.navigationIconInactive,
                                modifier = Modifier.size(22.dp)
                            )
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn() + expandHorizontally(),
                                exit = fadeOut() + shrinkHorizontally()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tab.title,
                                        color = colors.accentContrast,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Backward-compatible entry point delegating to AuraPillNavBar.
 */
@Composable
fun LiquidGlassNavBar(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    navTransparency: Float = 0.05f,
    navBlur: Float = 0.80f
) {
    AuraPillNavBar(
        activeTab = activeTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
        navTransparency = navTransparency
    )
}
