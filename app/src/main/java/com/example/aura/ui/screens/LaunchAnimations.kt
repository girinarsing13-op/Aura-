package com.example.aura.ui.screens

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.aura.ui.components.rememberReducedMotion
import com.example.aura.ui.theme.InterFontFamily
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val LaunchBlack = Color(0xFF010204)
private val MutedBlueGrey = Color(0xFF9AA3B5)
private val ButtonFill = Color(0xFFE8EEFF)
private val ButtonTextDark = Color(0xFF0A1020)
private val RingStrokeColor = Color(0xFF7FB0FF)
private val RingGlowColor = Color(0xFF3D7BFF)
private val FlareCoreColor = Color(0xFF4678FF)
private val FlareMidColor = Color(0xFF2850FF)
private val EmberCoreColor = Color(0xFFBFD8FF)
private val EmberGlowColor = Color(0xFF5C94FF)
private val LoadingTrackColor = Color(0xFF1B2438)
private val LoadingFillStart = Color(0xFF4C86FF)
private val LoadingFillEnd = Color(0xFFE8EEFF)

private val EaseOutQuart = CubicBezierEasing(0.2f, 0.7f, 0.2f, 1.0f)
private val EaseOut = CubicBezierEasing(0.0f, 0.0f, 0.58f, 1.0f)
private val EaseIn = CubicBezierEasing(0.42f, 0.0f, 1.0f, 1.0f)
private val EaseInOut = CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f)

private data class EmberSpec(
    val xFraction: Float,
    val delaySec: Float,
    val durationSec: Float
)

private val DefaultEmbers = listOf(
    EmberSpec(40f / 390f, 0.0f, 4.5f),
    EmberSpec(63f / 390f, 0.9f, 5.3f),
    EmberSpec(86f / 390f, 1.8f, 6.1f),
    EmberSpec(109f / 390f, 2.7f, 6.9f),
    EmberSpec(132f / 390f, 3.6f, 4.5f),
    EmberSpec(155f / 390f, 4.5f, 5.3f),
    EmberSpec(178f / 390f, 5.4f, 6.1f),
    EmberSpec(201f / 390f, 0.3f, 6.9f),
    EmberSpec(224f / 390f, 1.2f, 4.5f),
    EmberSpec(247f / 390f, 2.1f, 5.3f),
    EmberSpec(270f / 390f, 3.0f, 6.1f),
    EmberSpec(293f / 390f, 3.9f, 6.9f),
    EmberSpec(316f / 390f, 4.8f, 4.5f),
    EmberSpec(339f / 390f, 5.7f, 5.3f)
)

// AGSL shader for API 33+: slow two-octave noise displacement applied only to the outer flames,
// keeping the central silhouette figure and the AURA wordmark sharp and still.
private const val FLAME_SWAY_AGSL = """
uniform shader composable;
uniform float2 uResolution;
uniform float uTime;
uniform float uScale;

float hash21(float2 p) {
    p = fract(p * float2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float vnoise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float2 u = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + float2(1.0, 0.0));
    float c = hash21(i + float2(0.0, 1.0));
    float d = hash21(i + float2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(float2 p) {
    float v = 0.0;
    v += 0.65 * vnoise(p);
    v += 0.35 * vnoise(p * 2.05 + float2(13.7, 7.1));
    return v;
}

half4 main(float2 fragCoord) {
    float2 uv = fragCoord / max(uResolution, float2(1.0));

    // Elliptical mask matching CSS: ellipse 30% 38% at 50% 54%, transparent 60% -> opaque 100%
    float2 ed = float2((uv.x - 0.50) / 0.30, (uv.y - 0.54) / 0.38);
    float ellipseMask = smoothstep(0.60, 1.00, length(ed));

    // Keep the AURA text band completely crisp and motionless
    float textX = smoothstep(0.14, 0.20, uv.x) * (1.0 - smoothstep(0.80, 0.86, uv.x));
    float textY = smoothstep(0.60, 0.64, uv.y) * (1.0 - smoothstep(0.76, 0.80, uv.y));
    float textGuard = 1.0 - (textX * textY);

    float swayMask = clamp(ellipseMask * textGuard, 0.0, 1.0);
    if (swayMask <= 0.001) {
        return half4(0.0);
    }

    // Slow breathing frequency modulation over a 9s cycle
    float phase = 0.5 - 0.5 * cos(uTime * 0.6981317);
    float2 freq = mix(float2(3.36, 7.84), float2(5.04, 11.20), phase);
    float2 drift = float2(uTime * 0.14, -uTime * 0.28);

    float nx = fbm(uv * freq + drift) - 0.5;
    float ny = fbm(uv * freq + drift + float2(5.2, 9.4)) - 0.5;

    float2 offset = float2(nx, ny) * uScale * 2.0;
    half4 displaced = composable.eval(fragCoord + offset);
    return displaced * half(swayMask);
}
"""

// ============================================================================
// ANIMATION 1: WelcomeScreen (First launch only)
// ============================================================================

@Composable
fun WelcomeScreen(
    backupMessage: String?,
    onGetStarted: () -> Unit,
    onExistingAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotion()
    var elapsedSec by remember { mutableFloatStateOf(if (reducedMotion) 10f else 0f) }

    LaunchedEffect(reducedMotion) {
        if (reducedMotion) {
            elapsedSec = 10f
            return@LaunchedEffect
        }
        val startNanos = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                elapsedSec = (now - startNanos) / 1_000_000_000f
            }
        }
    }

    val t = elapsedSec

    // 0..0.8s black; 0.8..2.8s fade in
    val wrapperAlpha = if (reducedMotion) 1f else segment(t, 0.8f, 2.8f, EaseOut)

    // 0.8..3.4s scale 1.14 -> 1.0, then slow push-in 1.0 -> 1.035 while waiting
    val imageScale = when {
        reducedMotion -> 1f
        t < 0.8f -> 1.14f
        t < 3.4f -> lerp(1.14f, 1.0f, segment(t, 0.8f, 3.4f, EaseOutQuart))
        t < 3.9f -> 1.0f
        else -> lerp(1.0f, 1.035f, segment(t, 3.9f, 10.6f))
    }

    // 0.8..3.4s brightness 15% -> 110%, then settles to 100% by 3.9s
    val imageBrightness = when {
        reducedMotion -> 1f
        t < 0.8f -> 0.15f
        t < 3.4f -> lerp(0.15f, 1.10f, segment(t, 0.8f, 3.4f, EaseOutQuart))
        t < 3.9f -> lerp(1.10f, 1.00f, segment(t, 3.4f, 3.9f, EaseOut))
        else -> 1.0f
    }

    // 0.9..1.9s radial blue flare grows 0.3 -> 1.0 at 85% alpha, settles to 30% alpha & scale 1.2
    val flareAlpha = when {
        reducedMotion -> 0.30f
        t < 0.9f -> 0f
        t < 1.9f -> lerp(0f, 0.85f, segment(t, 0.9f, 1.9f, EaseOut))
        t < 3.4f -> lerp(0.85f, 0.30f, segment(t, 1.9f, 3.4f, EaseOut))
        else -> 0.30f
    }
    val flareScale = when {
        reducedMotion -> 1.2f
        t < 0.9f -> 0.3f
        t < 1.9f -> lerp(0.3f, 1.0f, segment(t, 0.9f, 1.9f, EaseOut))
        t < 3.4f -> lerp(1.0f, 1.2f, segment(t, 1.9f, 3.4f, EaseOut))
        else -> 1.2f
    }

    // 2.4s..3.7s expanding blue ring (scale 0.25 -> 2.6, fade out)
    val ringProgress = if (reducedMotion || t < 2.4f || t > 3.7f) -1f else segment(t, 2.4f, 3.7f, EaseOut)
    val ringScale = if (ringProgress < 0f) 0.25f else lerp(0.25f, 2.6f, ringProgress)
    val ringAlpha = if (ringProgress < 0f) 0f else lerp(0.80f, 0f, ringProgress)

    // Light sweep across AURA text at 3.9s and 8.2s (0.9s each)
    val sweep1 = if (!reducedMotion && t in 3.9f..4.8f) segment(t, 3.9f, 4.8f, EaseInOut) else -1f
    val sweep2 = if (!reducedMotion && t in 8.2f..9.1f) segment(t, 8.2f, 9.1f, EaseInOut) else -1f
    val sweepProgress = when {
        sweep1 >= 0f -> sweep1
        sweep2 >= 0f -> sweep2
        else -> -1f
    }

    // Text & buttons stagger: 4.3s tagline, 5.0s Get started, 5.6s I already have an account
    val taglineProgress = if (reducedMotion) 1f else segment(t, 4.3f, 5.1f, EaseOut)
    val ctaProgress = if (reducedMotion) 1f else segment(t, 5.0f, 5.8f, EaseOut)
    val existingProgress = if (reducedMotion) 1f else segment(t, 5.6f, 6.3f, EaseOut)

    // Soft blue glow pulse on Get started button every 2.6s
    val buttonGlowStrength = if (reducedMotion || ctaProgress <= 0f) {
        0f
    } else {
        val pulsePhase = ((t - 5.0f).coerceAtLeast(0f) % 2.6f) / 2.6f
        (0.5f - 0.5f * cos(pulsePhase * 2f * PI.toFloat())) * ctaProgress
    }

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(LaunchBlack)
            .clipToBounds()
    ) {
        val screenHeight = maxHeight

        // Artwork + flames sway + flare + ring + text sweep + rising embers
        AuraLaunchVisualLayer(
            elapsedSec = t,
            reducedMotion = reducedMotion,
            wrapperAlpha = wrapperAlpha,
            imageScale = imageScale,
            imageBrightness = imageBrightness,
            flareAlpha = flareAlpha,
            flareScale = flareScale,
            ringAlpha = ringAlpha,
            ringScale = ringScale,
            sweepProgress = sweepProgress,
            showEmbers = !reducedMotion
        )

        // Tagline at 640dp from top (clamped cleanly on shorter viewports so it never overlaps CTA)
        val taglineTop = minOf(640.dp, (screenHeight - 204.dp).coerceAtLeast(460.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .padding(top = taglineTop, start = 24.dp, end = 24.dp)
                .graphicsLayer {
                    alpha = taglineProgress
                    translationY = with(density) { (16.dp * (1f - taglineProgress)).toPx() }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your personal coach.\nFree, in your pocket.",
                color = MutedBlueGrey,
                fontFamily = InterFontFamily,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center
            )
            if (!backupMessage.isNullOrBlank()) {
                Text(
                    text = backupMessage,
                    color = RingStrokeColor,
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Bottom CTA stack
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Get started pill button with soft 2.6s pulsing blue glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = ctaProgress
                        translationY = with(density) { (16.dp * (1f - ctaProgress)).toPx() }
                    }
                    .drawBehind {
                        if (buttonGlowStrength > 0.01f) {
                            val glowAlpha = 0.42f * buttonGlowStrength
                            val spreadX = 24.dp.toPx()
                            val spreadY = 20.dp.toPx()
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colorStops = arrayOf(
                                        0f to Color(0xFF5A8CFF).copy(alpha = glowAlpha),
                                        0.55f to Color(0xFF5A8CFF).copy(alpha = glowAlpha * 0.45f),
                                        1f to Color.Transparent
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.width * 0.62f
                                ),
                                topLeft = Offset(-spreadX, -spreadY),
                                size = Size(size.width + spreadX * 2f, size.height + spreadY * 2f),
                                cornerRadius = CornerRadius(size.height, size.height)
                            )
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(ButtonFill)
                        .clickable(
                            enabled = ctaProgress > 0.25f,
                            role = Role.Button,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color(0xFF3D7BFF)),
                            onClick = onGetStarted
                        )
                        .testTag("onboarding_get_started"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Get started",
                        color = ButtonTextDark,
                        fontFamily = InterFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // "I already have an account" muted action with 48dp min touch target
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .graphicsLayer {
                        alpha = existingProgress
                        translationY = with(density) { (16.dp * (1f - existingProgress)).toPx() }
                    }
                    .clip(RoundedCornerShape(999.dp))
                    .clickable(
                        enabled = existingProgress > 0.25f,
                        role = Role.Button,
                        onClick = onExistingAccount
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("onboarding_restore_account"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "I already have an account",
                    color = MutedBlueGrey,
                    fontFamily = InterFontFamily,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// ANIMATION 2: LaunchSplash (Every other launch, ~2 seconds total)
// ============================================================================

@Composable
fun LaunchSplash(
    isAppReady: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = rememberReducedMotion()
    var elapsedSec by remember { mutableFloatStateOf(if (reducedMotion) 1.8f else 0f) }
    var exitStartSec by remember { mutableStateOf<Float?>(null) }
    var finishedDispatched by remember { mutableStateOf(false) }

    // Reduced-motion path: render final settled frame briefly, then navigate as soon as ready
    LaunchedEffect(reducedMotion, isAppReady) {
        if (reducedMotion && isAppReady && !finishedDispatched) {
            delay(180L)
            finishedDispatched = true
            onFinished()
        }
    }

    // Frame-accurate animation clock
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) return@LaunchedEffect
        val startNanos = withFrameNanos { it }
        while (isActive && !finishedDispatched) {
            withFrameNanos { now ->
                elapsedSec = (now - startNanos) / 1_000_000_000f
            }
        }
    }

    // Start exit transition as soon as app is ready AND at least 1.8s has elapsed
    LaunchedEffect(isAppReady, elapsedSec, reducedMotion) {
        if (reducedMotion || finishedDispatched) return@LaunchedEffect
        if (exitStartSec == null && isAppReady && elapsedSec >= 1.8f) {
            exitStartSec = elapsedSec
        }
        val start = exitStartSec
        if (start != null && elapsedSec >= start + 0.4f) {
            finishedDispatched = true
            onFinished()
        }
    }

    val t = elapsedSec
    val exitProgress = exitStartSec?.let { segment(t, it, it + 0.4f, EaseIn) } ?: 0f

    // 0..0.3s black; 0.3..0.9s fade in; fade out over 0.4s when exiting
    val enterAlpha = if (reducedMotion) 1f else segment(t, 0.3f, 0.9f, EaseOut)
    val wrapperAlpha = if (reducedMotion) 1f else enterAlpha * (1f - exitProgress)

    // 0.3..1.2s scale 1.1 -> 1.0, brightness 180% -> 100% (fast ease-out), then exit -> scale 1.12, brightness 140%
    val baseScale = when {
        reducedMotion -> 1f
        t < 0.3f -> 1.10f
        t < 1.2f -> lerp(1.10f, 1.0f, segment(t, 0.3f, 1.2f, EaseOutQuart))
        else -> lerp(1.0f, 1.03f, segment(t, 1.2f, 3.7f))
    }
    val imageScale = if (exitProgress > 0f) lerp(baseScale, 1.12f, exitProgress) else baseScale

    val baseBrightness = when {
        reducedMotion -> 1f
        t < 0.3f -> 1.80f
        t < 1.2f -> lerp(1.80f, 1.00f, segment(t, 0.3f, 1.2f, EaseOutQuart))
        else -> 1.00f
    }
    val imageBrightness = if (exitProgress > 0f) lerp(baseBrightness, 1.40f, exitProgress) else baseBrightness

    // Radial blue flare: 0.3..1.4s settles 0.8 -> 0.25 alpha, scale 0.6 -> 1.15
    val flareAlpha = when {
        reducedMotion -> 0.25f
        t < 0.3f -> 0f
        t < 1.4f -> lerp(0.80f, 0.25f, segment(t, 0.3f, 1.4f, EaseOut))
        else -> 0.25f
    }
    val flareScale = when {
        reducedMotion -> 1.15f
        t < 0.3f -> 0.60f
        t < 1.4f -> lerp(0.60f, 1.15f, segment(t, 0.3f, 1.4f, EaseOut))
        else -> 1.15f
    }

    // 0.55s..1.6s expanding blue ring once (0.25 -> 2.4, fade out)
    val ringProgress = if (reducedMotion || t < 0.55f || t > 1.6f) -1f else segment(t, 0.55f, 1.6f, EaseOut)
    val ringScale = if (ringProgress < 0f) 0.25f else lerp(0.25f, 2.4f, ringProgress)
    val ringAlpha = if (ringProgress < 0f) 0f else lerp(0.70f, 0f, ringProgress)

    // 1.4s..2.2s one light sweep across AURA text (0.8s)
    val sweepProgress = if (!reducedMotion && t in 1.4f..2.2f) segment(t, 1.4f, 2.2f, EaseInOut) else -1f

    // 0.9s..3.5s loading line fills left to right (completes cleanly if exiting early)
    val rawBarProgress = if (reducedMotion) 1f else segment(t, 0.9f, 3.5f, FastOutSlowInEasing)
    val loadingProgress = if (exitProgress > 0f) {
        lerp(rawBarProgress, 1f, (exitProgress * 1.5f).coerceIn(0f, 1f))
    } else {
        rawBarProgress
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LaunchBlack)
            .clipToBounds()
            .testTag("launch_splash_screen")
    ) {
        AuraLaunchVisualLayer(
            elapsedSec = t,
            reducedMotion = reducedMotion,
            wrapperAlpha = wrapperAlpha,
            imageScale = imageScale,
            imageBrightness = imageBrightness,
            flareAlpha = flareAlpha,
            flareScale = flareScale,
            ringAlpha = ringAlpha,
            ringScale = ringScale,
            sweepProgress = sweepProgress,
            showEmbers = !reducedMotion
        )

        // Thin loading line at bottom (120dp wide, 3dp tall, 70dp from bottom edge)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 70.dp)
                .graphicsLayer { alpha = wrapperAlpha }
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(LoadingTrackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(loadingProgress.coerceIn(0f, 1f))
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(LoadingFillStart, LoadingFillEnd)
                            )
                        )
                )
            }
        }
    }
}

// ============================================================================
// SHARED VISUAL LAYER (Artwork + Radial Mask + AGSL Sway + Flare + Ring + Sweep + Embers)
// ============================================================================

@Composable
private fun AuraLaunchVisualLayer(
    elapsedSec: Float,
    reducedMotion: Boolean,
    wrapperAlpha: Float,
    imageScale: Float,
    imageBrightness: Float,
    flareAlpha: Float,
    flareScale: Float,
    ringAlpha: Float,
    ringScale: Float,
    sweepProgress: Float,
    showEmbers: Boolean
) {
    val brightnessFilter = remember(imageBrightness) {
        val m = ColorMatrix().apply {
            setToScale(imageBrightness, imageBrightness, imageBrightness, 1f)
        }
        ColorFilter.colorMatrix(m)
    }

    val painter = painterResource(id = R.drawable.aura_splash)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = wrapperAlpha.coerceIn(0f, 1f) }
    ) {
        // 560dp square artwork centered horizontally, 70dp from the top, cropped by screen bounds
        Box(
            modifier = Modifier
                .requiredSize(560.dp)
                .align(Alignment.TopCenter)
                .offset(y = 70.dp)
                .graphicsLayer {
                    scaleX = imageScale
                    scaleY = imageScale
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    // Radial alpha mask: opaque to 52% of farthest-corner radius, transparent at 71%
                    val farthestCornerRadius = hypot(size.width / 2f, size.height / 2f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Black,
                                0.52f to Color.Black,
                                0.71f to Color.Transparent,
                                1.00f to Color.Transparent
                            ),
                            center = center,
                            radius = farthestCornerRadius
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            // Base crisp artwork
            Image(
                painter = painter,
                contentDescription = "AURA",
                contentScale = ContentScale.Crop,
                colorFilter = brightnessFilter,
                modifier = Modifier.fillMaxSize()
            )

            // API 33+ AGSL flame sway overlay (figure & AURA text masked still)
            if (!reducedMotion && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                FlameSwayOverlayApi33(
                    painter = painter,
                    colorFilter = brightnessFilter,
                    elapsedSec = elapsedSec
                )
            }

            // Radial blue flare behind figure (BlendMode.Screen)
            if (flareAlpha > 0.005f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                ) {
                    val baseRadius = 320.dp.toPx() * flareScale
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.00f to FlareCoreColor.copy(alpha = 0.75f * flareAlpha),
                                0.40f to FlareMidColor.copy(alpha = 0.25f * flareAlpha),
                                0.68f to Color.Transparent,
                                1.00f to Color.Transparent
                            ),
                            center = center,
                            radius = baseRadius
                        ),
                        radius = baseRadius,
                        center = center,
                        blendMode = BlendMode.Screen
                    )
                }
            }

            // Expanding blue ring with outer/inner glow
            if (ringAlpha > 0.005f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = 150.dp.toPx() * ringScale
                    val strokePx = 3.dp.toPx()
                    // Outer & inner soft blue glow passes
                    drawCircle(
                        color = RingGlowColor.copy(alpha = 0.22f * ringAlpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 24.dp.toPx())
                    )
                    drawCircle(
                        color = RingGlowColor.copy(alpha = 0.42f * ringAlpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 10.dp.toPx())
                    )
                    drawCircle(
                        color = RingStrokeColor.copy(alpha = ringAlpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokePx)
                    )
                }
            }

            // Soft light sweep across AURA text band (top 62%, height 16%, ColorDodge, feathered top & bottom)
            if (sweepProgress in 0f..1f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                ) {
                    val bandLeft = size.width * 0.03f
                    val bandWidth = size.width * 0.94f
                    val bandTop = size.height * 0.62f
                    val bandHeight = size.height * 0.16f

                    // Fade smoothly in at start and out at end of the sweep
                    val edgeFade = when {
                        sweepProgress < 0.08f -> sweepProgress / 0.08f
                        sweepProgress > 0.92f -> (1f - sweepProgress) / 0.08f
                        else -> 1f
                    }.coerceIn(0f, 1f)

                    val sweepCenterX = bandLeft + lerp(-bandWidth * 0.15f, bandWidth * 1.15f, sweepProgress)
                    val halfBeam = bandWidth * 0.16f
                    val slantPx = bandHeight * 0.18f

                    drawRect(
                        brush = Brush.linearGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Transparent,
                                0.50f to Color(0xFF78A0FF).copy(alpha = 0.55f * edgeFade),
                                1.00f to Color.Transparent
                            ),
                            start = Offset(sweepCenterX - halfBeam, bandTop),
                            end = Offset(sweepCenterX + halfBeam + slantPx, bandTop + bandHeight)
                        ),
                        topLeft = Offset(bandLeft, bandTop),
                        size = Size(bandWidth, bandHeight),
                        blendMode = BlendMode.ColorDodge
                    )

                    // Vertical feather mask: transparent -> 35% solid -> 65% solid -> transparent
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.00f to Color.Transparent,
                                0.35f to Color.Black,
                                0.65f to Color.Black,
                                1.00f to Color.Transparent
                            ),
                            startY = bandTop,
                            endY = bandTop + bandHeight
                        ),
                        topLeft = Offset(bandLeft, bandTop),
                        size = Size(bandWidth, bandHeight),
                        blendMode = BlendMode.DstIn
                    )
                }
            }
        }

        // 14 rising blue-white embers
        if (showEmbers) {
            RisingEmbersCanvas(
                elapsedSec = elapsedSec,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun FlameSwayOverlayApi33(
    painter: androidx.compose.ui.graphics.painter.Painter,
    colorFilter: ColorFilter,
    elapsedSec: Float
) {
    val shader = remember {
        runCatching { RuntimeShader(FLAME_SWAY_AGSL) }.getOrNull()
    } ?: return

    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        colorFilter = colorFilter,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (size.width > 0f && size.height > 0f) {
                    shader.setFloatUniform("uResolution", size.width, size.height)
                    shader.setFloatUniform("uTime", elapsedSec)
                    // Scale ~18px at 560px reference box
                    shader.setFloatUniform("uScale", 18f * (size.width / 560f))
                    renderEffect = AndroidRenderEffect
                        .createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
            }
    )
}

@Composable
private fun RisingEmbersCanvas(
    elapsedSec: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val startY = size.height - 150.dp.toPx()
        val risePx = 480.dp.toPx()
        val driftXPx = 18.dp.toPx()
        val dotRadius = 2.dp.toPx() // 4dp diameter
        val glowRadius = 8.dp.toPx()

        for (ember in DefaultEmbers) {
            val cycle = ((elapsedSec + ember.delaySec) % ember.durationSec) / ember.durationSec
            val alpha = when {
                cycle < 0.15f -> lerp(0f, 0.90f, cycle / 0.15f)
                else -> lerp(0.90f, 0f, (cycle - 0.15f) / 0.85f)
            }.coerceIn(0f, 1f)

            if (alpha <= 0.01f) continue

            val scale = lerp(1.0f, 0.3f, cycle)
            val x = size.width * ember.xFraction + driftXPx * cycle + sin(cycle * 6.283f + ember.delaySec) * 3.dp.toPx()
            val y = startY - risePx * cycle
            val center = Offset(x, y)

            // Soft blue glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        EmberGlowColor.copy(alpha = 0.70f * alpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = glowRadius * scale
                ),
                radius = glowRadius * scale,
                center = center
            )

            // Crisp blue-white core dot
            drawCircle(
                color = EmberCoreColor.copy(alpha = alpha),
                radius = dotRadius * scale,
                center = center
            )
        }
    }
}

private fun segment(
    t: Float,
    start: Float,
    end: Float,
    easing: androidx.compose.animation.core.Easing? = null
): Float {
    if (end <= start) return if (t >= end) 1f else 0f
    val raw = ((t - start) / (end - start)).coerceIn(0f, 1f)
    return easing?.transform(raw) ?: raw
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
