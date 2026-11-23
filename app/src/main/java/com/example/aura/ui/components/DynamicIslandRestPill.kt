package com.example.aura.ui.components

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.ui.theme.AuraTheme
import kotlinx.coroutines.delay

class RestTimerController {
    var isVisible by mutableStateOf(false)
    var isExpanded by mutableStateOf(false)
    var secondsLeft by mutableIntStateOf(90)
    var targetSeconds by mutableIntStateOf(90)
    var isComplete by mutableStateOf(false)

    fun start(seconds: Int = 90) {
        targetSeconds = seconds
        secondsLeft = seconds
        isComplete = false
        isVisible = true
        isExpanded = true
    }

    fun adjust(delta: Int) {
        secondsLeft = (secondsLeft + delta).coerceAtLeast(5)
    }

    fun stop() {
        isVisible = false
        isExpanded = false
        isComplete = false
    }

    fun toggleExpand() {
        isExpanded = !isExpanded
    }
}

@Composable
fun rememberRestTimerController(): RestTimerController {
    return remember { RestTimerController() }
}

@Composable
fun DynamicIslandRestPill(
    controller: RestTimerController,
    hapticsEnabled: Boolean = true,
    soundEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accent = AuraTheme.colors.accent

    val toneGenerator = remember(soundEnabled) {
        if (!soundEnabled) null
        else try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (_: Exception) {
            null
        }
    }
    DisposableEffect(toneGenerator) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    // Vibrator + optional beep helper
    fun triggerCompletionFeedback() {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 220)
            } catch (_: Exception) {
                // ignore
            }
        }
        if (hapticsEnabled) {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val timings = longArrayOf(0, 120, 80, 160)
                        val amplitudes = intArrayOf(0, 200, 0, 255)
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(250)
                    }
                }
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    // Countdown timer coroutine
    LaunchedEffect(controller.isVisible, controller.secondsLeft) {
        if (controller.isVisible && controller.secondsLeft > 0 && !controller.isComplete) {
            delay(1000)
            controller.secondsLeft -= 1
            if (controller.secondsLeft <= 0) {
                controller.isComplete = true
                triggerCompletionFeedback()
                delay(3000)
                controller.stop()
            }
        }
    }

    // Auto-collapse after 4 seconds of inactivity if expanded
    LaunchedEffect(controller.isExpanded, controller.secondsLeft) {
        if (controller.isExpanded && !controller.isComplete) {
            delay(4000)
            controller.isExpanded = false
        }
    }

    AnimatedVisibility(
        visible = controller.isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .statusBarsPadding()
            .padding(top = 8.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            val minutes = controller.secondsLeft / 60
            val seconds = controller.secondsLeft % 60
            val formattedTime = String.format("%02d:%02d", minutes, seconds)

            // Dynamic Island Container
            Box(
                modifier = Modifier
                    .widthIn(max = 410.dp)
                    .animateContentSize(
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                    .shadow(
                        elevation = 20.dp,
                        shape = if (controller.isExpanded) RoundedCornerShape(26.dp) else CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.85f),
                        ambientColor = accent.copy(alpha = 0.35f)
                    )
                    .clip(if (controller.isExpanded) RoundedCornerShape(26.dp) else CircleShape)
                    .background(Color(0xFF0B0B14))
                    .border(
                        width = 1.dp,
                        color = if (controller.isExpanded) accent.copy(alpha = 0.5f) else Color(0x24FFFFFF),
                        shape = if (controller.isExpanded) RoundedCornerShape(26.dp) else CircleShape
                    )
                    .clickable { controller.toggleExpand() }
                    .testTag("dynamic_island_rest_pill")
            ) {
                if (!controller.isExpanded) {
                    // --- COMPACT VIEW ---
                    Row(
                        modifier = Modifier
                            .width(180.dp)
                            .height(42.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(accent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (controller.isComplete) "DONE" else "REST",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = AuraTheme.colors.textMuted
                            )
                        }
                        Text(
                            text = if (controller.isComplete) "00:00" else formattedTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = accent
                        )
                        // Waveform animation
                        WaveformMiniBars(accent = accent)
                    }
                } else {
                    // --- EXPANDED VIEW ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .heightIn(min = 72.dp)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = SportStopwatchFilledIcon,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (controller.isComplete) "REST COMPLETE" else "REST INTERVAL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = accent
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Target: ${controller.targetSeconds}s",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = AuraTheme.colors.textMuted
                                    )
                                }
                                Text(
                                    text = if (controller.isComplete) "00:00" else formattedTime,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // +15s button
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .background(Color(0x1AFFFFFF))
                                    .border(1.dp, Color(0x1AFFFFFF), CircleShape)
                                    .clickable { controller.adjust(15) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "+15s",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            // Dismiss button
                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x33EF4444))
                                    .border(1.dp, Color(0x66EF4444), RoundedCornerShape(10.dp))
                                    .clickable { controller.stop() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss rest timer",
                                    tint = Color(0xFFFCA5A5),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaveformMiniBars(accent: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h1.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h2.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(h3.dp)
                .clip(CircleShape)
                .background(accent)
        )
    }
}
