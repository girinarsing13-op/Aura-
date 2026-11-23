package com.example.aura.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.data.model.Exercise
import com.example.aura.data.model.SetLog
import com.example.aura.engine.AuraTrainingMemoryEngine
import com.example.aura.engine.ExerciseCoachingPlan
import com.example.aura.engine.ExerciseGuideEngine
import com.example.aura.engine.ProgressionAction
import com.example.aura.engine.ProgressionEngine
import com.example.aura.engine.SetComparisonResult
import com.example.aura.ui.components.ExerciseDemo
import com.example.aura.ui.components.SportStopwatchOutlineIcon
import com.example.aura.ui.theme.AuraTheme
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutExerciseCard(
    exIdx: Int,
    ex: BlueprintExercise,
    activeSets: List<SetLog>,
    isCurrentSessionDay: Boolean,
    isExpanded: Boolean,
    activeSetFeedback: Map<Pair<Int, Int>, SetComparisonResult>,
    accent: Color,
    accentContrast: Color,
    isWorkoutActive: Boolean = false,
    hapticsEnabled: Boolean = true,
    soundEnabled: Boolean = true,
    coachingPlan: ExerciseCoachingPlan? = null,
    onSwitchVariation: ((Exercise) -> Unit)? = null,
    onToggleCardExpand: () -> Unit,
    onOpenGuide: () -> Unit,
    onSwapClick: () -> Unit,
    onRestShortcutClick: () -> Unit,
    onWeightChanged: (setIdx: Int, weight: Double) -> Unit,
    onRepsChanged: (setIdx: Int, reps: Int) -> Unit,
    onToggleDone: (setIdx: Int) -> Unit
) {
    val guide = remember(ex.id, ex.name) {
        ExerciseGuideEngine.getGuide(ex.name, ex.muscle, ex.equipment, ex.detail, ex.id)
    }
    val lastPerf = remember(ex.name) { AuraTrainingMemoryEngine.getLastPerformance(ex.name) }
    val primaryMuscleDisplay = ex.getEffectivePrimary().ifBlank { ex.muscle.replaceFirstChar { it.uppercase() } }
    val isTimedExercise = remember(ex.id, ex.name, ex.reps) { ex.isTimedHold() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AuraTheme.colors.card)
            .border(
                1.dp,
                if (isCurrentSessionDay) accent.copy(alpha = 0.40f) else AuraTheme.colors.cardBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .testTag("exercise_card_$exIdx")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // --- 0. Exercise Demo Animation at top of card ---
            ExerciseDemo(
                exerciseName = ex.name,
                exerciseId = ex.id,
                primaryMuscle = primaryMuscleDisplay,
                equipment = ex.equipment,
                movementPattern = ex.getEffectivePattern(),
                caption = guide.formCue,
                modifier = Modifier.fillMaxWidth()
            )

            // --- 1. Exercise Name & Swap Icon Button ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = ex.name,
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AuraTheme.colors.primaryText,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 2
                    )

                    if (ex.isPriority) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.18f))
                                .border(1.dp, accent.copy(alpha = 0.40f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "★ PRIORITY",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Swap IconButton & Rest Timer Shortcut
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSwapClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AuraTheme.colors.surfaceSecondary)
                            .testTag("swap_exercise_btn_$exIdx")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Swap exercise",
                            tint = accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .clickable { onRestShortcutClick() }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("rest_shortcut_btn_$exIdx")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = SportStopwatchOutlineIcon,
                                contentDescription = "Rest Timer",
                                tint = accent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${ex.restSeconds}s",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.primaryText,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // --- 2. Target Muscle, Secondary Muscles & Equipment Tags ---
            val secondaryMusclesList = ex.getEffectiveSecondary()
            val bodyRegion = when (ex.muscle.lowercase()) {
                "chest", "back", "shoulders", "biceps", "triceps" -> "UPPER BODY"
                "quads", "hamstrings", "glutes", "calves", "legs" -> "LOWER BODY"
                "core", "abs", "abdominals" -> "CORE"
                else -> if (ex.muscle.contains("leg", ignoreCase = true) || ex.muscle.contains("lower", ignoreCase = true)) "LOWER BODY" else "UPPER BODY"
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AuraTheme.colors.surfaceSecondary)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = bodyRegion,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AuraTheme.colors.secondaryText,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.20f))
                        .border(1.dp, accent.copy(alpha = 0.50f), CircleShape)
                        .padding(horizontal = 9.dp, vertical = 3.5.dp)
                ) {
                    Text(
                        text = primaryMuscleDisplay.uppercase(),
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (secondaryMusclesList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AuraTheme.colors.surfaceSecondary)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "+ ${secondaryMusclesList.joinToString(", ") { it.uppercase() }}",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = AuraTheme.colors.secondaryText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AuraTheme.colors.surfaceSecondary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = ex.equipment.uppercase(),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AuraTheme.colors.textMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // --- 3. Form Cue Strip (Opens Exercise Guide Bottom Sheet) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraTheme.colors.surfaceSecondary)
                    .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                    .clickable { onOpenGuide() }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("open_guide_btn_$exIdx")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = guide.formCue,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (AuraTheme.colors.isDark) Color(0xFFFCD34D) else Color(0xFFB45309),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    Text(
                        text = "Guide ▸",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // --- 3b. Smart Progression / Deload Coach Note ---
            if (coachingPlan != null && coachingPlan.shortNote.isNotBlank()) {
                val isReduction = coachingPlan.action == ProgressionAction.REDUCE_WEIGHT ||
                    coachingPlan.action == ProgressionAction.HOLD_REBUILD
                val noteTint = when {
                    coachingPlan.action == ProgressionAction.DELOAD -> AuraTheme.colors.chartSecondary
                    isReduction -> AuraTheme.colors.warning
                    else -> accent
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(noteTint.copy(alpha = 0.12f))
                        .border(1.dp, noteTint.copy(alpha = 0.32f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("exercise_coaching_note_$exIdx"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = coachingPlan.shortNote,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuraTheme.colors.primaryText,
                        lineHeight = 16.sp
                    )
                    val variation = coachingPlan.suggestedVariation
                    if (variation != null && onSwitchVariation != null) {
                        Button(
                            onClick = { onSwitchVariation(variation) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = noteTint.copy(alpha = 0.22f),
                                contentColor = AuraTheme.colors.primaryText
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .minimumInteractiveComponentSize()
                                .testTag("switch_harder_variation_btn_$exIdx")
                        ) {
                            Text(
                                text = "Switch to ${variation.name}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- 4. Timed-Exercise Hold Countdown Ring Timer ---
            if (isTimedExercise) {
                TimedHoldTimerPanel(
                    exIdx = exIdx,
                    ex = ex,
                    activeSets = activeSets,
                    isWorkoutActive = isWorkoutActive,
                    hapticsEnabled = hapticsEnabled,
                    soundEnabled = soundEnabled,
                    accent = accent,
                    accentContrast = accentContrast,
                    onHoldCompleted = {
                        val nextUncompletedIdx = activeSets.indexOfFirst { !it.completed }
                        if (isWorkoutActive && nextUncompletedIdx >= 0) {
                            onToggleDone(nextUncompletedIdx)
                        } else {
                            onRestShortcutClick()
                        }
                    }
                )
            }

            // --- 5. Target Metrics & Set Logging (Visible ONLY After Starting Active Workout Session) ---
            if (isWorkoutActive) {
                val effectiveSetsCount = coachingPlan?.targetSets ?: ex.sets
                val prefilledFirstWeight = coachingPlan?.prefilledSets?.firstOrNull()?.prescribedWeightKg ?: ex.recommendedLoadKg
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val loadLabel = if (ex.isBodyweight) "Bodyweight" else "${ProgressionEngine.formatKg(prefilledFirstWeight)} kg"
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.09f))
                            .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Column {
                            Text(
                                text = "TARGET PRESCRIPTION",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$effectiveSetsCount Sets • ${ex.reps} • $loadLabel",
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                color = AuraTheme.colors.primaryText,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Column {
                            Text(
                                text = "PREVIOUS / BEST",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.textMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val perfText = when {
                                coachingPlan != null && coachingPlan.lastSessionWeightKg != null -> {
                                    val wStr = ProgressionEngine.formatKg(coachingPlan.lastSessionWeightKg)
                                    val rStr = coachingPlan.lastSessionReps ?: 0
                                    val bestStr = if (coachingPlan.bestEstimated1RmKg > 0.0) {
                                        " • 1RM ${ProgressionEngine.formatKg(coachingPlan.bestEstimated1RmKg)}k"
                                    } else ""
                                    "Last: ${wStr}k×$rStr$bestStr"
                                }
                                lastPerf != null -> "Last: ${lastPerf.lastWeightKg.toInt()}k×${lastPerf.lastReps} • PR: ${lastPerf.bestWeightKg.toInt()}k"
                                else -> "Baseline Target"
                            }
                            Text(
                                text = perfText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.primaryText,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                val completedSetsCount = activeSets.count { it.completed }
                val totalSetsCount = activeSets.size
                val allCompleted = totalSetsCount > 0 && completedSetsCount == totalSetsCount
                val hasStarted = completedSetsCount > 0
                val successColor = AuraTheme.colors.success

                if (!isExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                when {
                                    allCompleted -> successColor.copy(alpha = 0.10f)
                                    hasStarted -> accent.copy(alpha = 0.12f)
                                    else -> AuraTheme.colors.surfaceSecondary
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    allCompleted -> successColor.copy(alpha = 0.35f)
                                    hasStarted -> accent.copy(alpha = 0.40f)
                                    else -> AuraTheme.colors.cardBorder
                                },
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onToggleCardExpand() }
                            .padding(horizontal = 14.dp, vertical = 11.dp)
                            .testTag("exercise_summary_pill_$exIdx")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                allCompleted -> successColor.copy(alpha = 0.20f)
                                                hasStarted -> accent.copy(alpha = 0.22f)
                                                else -> AuraTheme.colors.cardBorder
                                            }
                                        )
                                        .padding(horizontal = 9.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when {
                                            allCompleted -> "✓ $completedSetsCount/$totalSetsCount"
                                            hasStarted -> "$completedSetsCount/$totalSetsCount DONE"
                                            else -> "$totalSetsCount SETS"
                                        },
                                        fontSize = 9.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            allCompleted -> successColor
                                            hasStarted -> accent
                                            else -> AuraTheme.colors.primaryText
                                        },
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    val loadLabel = if (ex.isBodyweight) "Bodyweight" else "${ex.recommendedLoadKg}kg"
                                    Text(
                                        text = "${ex.sets} Sets • ${ex.reps} • $loadLabel",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraTheme.colors.primaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = when {
                                            allCompleted -> "All sets completed • Great work!"
                                            hasStarted -> "In progress • Tap to log next set"
                                            else -> "Tap to expand set logging"
                                        },
                                        fontSize = 10.sp,
                                        color = when {
                                            allCompleted -> successColor
                                            hasStarted -> accent
                                            else -> AuraTheme.colors.textMuted
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            allCompleted -> successColor.copy(alpha = 0.14f)
                                            hasStarted -> accent
                                            else -> accent.copy(alpha = 0.18f)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (allCompleted) successColor.copy(alpha = 0.30f) else accent.copy(alpha = 0.45f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 11.dp, vertical = 6.dp)
                                    .testTag("expand_sets_btn_$exIdx")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = when {
                                            allCompleted -> "Review"
                                            hasStarted -> "Resume"
                                            else -> "Start Set"
                                        },
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            allCompleted -> successColor
                                            hasStarted -> accentContrast
                                            else -> AuraTheme.colors.primaryText
                                        },
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = if (allCompleted) Icons.Default.ExpandMore else Icons.Default.PlayArrow,
                                        contentDescription = "Expand Set Logging",
                                        tint = when {
                                            allCompleted -> successColor
                                            hasStarted -> accentContrast
                                            else -> accent
                                        },
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (allCompleted) successColor.copy(alpha = 0.20f) else accent.copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (allCompleted) "✓ COMPLETED $completedSetsCount/$totalSetsCount" else "LOGGING $completedSetsCount/$totalSetsCount SETS",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (allCompleted) successColor else accent,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AuraTheme.colors.surfaceSecondary)
                                    .clickable { onToggleCardExpand() }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("collapse_sets_btn_$exIdx")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Collapse",
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = AuraTheme.colors.textMuted,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.ExpandLess,
                                        contentDescription = "Collapse Set Rows",
                                        tint = AuraTheme.colors.textMuted,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SET",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(44.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "WEIGHT (KG)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = if (isTimedExercise) "SEC" else "REPS",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "STATUS",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = AuraTheme.colors.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(58.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        val defaultHoldSec = remember(ex.id, ex.reps) {
                            if (isTimedExercise) ex.targetHoldSeconds() else 10
                        }
                        activeSets.forEachIndexed { sIdx, setLog ->
                            val isDone = setLog.completed
                            val currentW = setLog.loggedWeightKg ?: setLog.prescribedWeightKg
                            val currentReps = setLog.loggedReps ?: (Regex("\\d+").find(setLog.prescribedReps)?.value?.toIntOrNull() ?: defaultHoldSec)

                            InteractiveSetRow(
                                setNumber = setLog.setNumber,
                                isDone = isDone,
                                currentW = currentW,
                                currentReps = currentReps,
                                accent = accent,
                                accentContrast = accentContrast,
                                onWeightChanged = { num -> onWeightChanged(sIdx, num) },
                                onRepsChanged = { num -> onRepsChanged(sIdx, num) },
                                onToggleDone = { onToggleDone(sIdx) }
                            )

                            val feedback = activeSetFeedback[Pair(exIdx, sIdx)]
                            if (isDone && feedback != null) {
                                val prColor = if (AuraTheme.colors.isDark) Color(0xFFFFD700) else AuraTheme.colors.warning
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (feedback.isPersonalRecord) prColor.copy(alpha = 0.16f) else AuraTheme.colors.surfaceSecondary)
                                        .border(
                                            1.dp,
                                            if (feedback.isPersonalRecord) prColor else accent.copy(alpha = 0.35f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (feedback.isPersonalRecord) "🏆 ${feedback.headline}" else "⚡ ${feedback.headline}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (feedback.isPersonalRecord) prColor else accent
                                    )
                                    Text(
                                        text = feedback.message,
                                        fontSize = 11.sp,
                                        color = AuraTheme.colors.primaryText
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

@Composable
private fun TimedHoldTimerPanel(
    exIdx: Int,
    ex: BlueprintExercise,
    activeSets: List<SetLog>,
    isWorkoutActive: Boolean,
    hapticsEnabled: Boolean = true,
    soundEnabled: Boolean = true,
    accent: Color,
    accentContrast: Color,
    onHoldCompleted: () -> Unit
) {
    val context = LocalContext.current
    val targetSeconds = remember(ex.id, ex.reps) { ex.targetHoldSeconds() }
    var remainingSeconds by remember(ex.id, ex.reps) { mutableIntStateOf(targetSeconds) }
    var isRunning by remember(ex.id, ex.reps) { mutableStateOf(false) }

    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
        } catch (_: Throwable) {
            null
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (_: Throwable) {
            }
        }
    }

    val trackColor = AuraTheme.colors.cardBorder
    val progressFraction = if (targetSeconds > 0) {
        (remainingSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            val next = remainingSeconds - 1
            remainingSeconds = next
            if (next <= 0) {
                isRunning = false
                if (hapticsEnabled) {
                    try {
                        @Suppress("DEPRECATION")
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(250L, VibrationEffect.DEFAULT_AMPLITUDE)
                                )
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(250L)
                            }
                        }
                    } catch (_: Exception) {
                        // Ignore vibration permissions / headless environment
                    }
                }
                if (soundEnabled) {
                    try {
                        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 180)
                    } catch (_: Throwable) {
                    }
                }
                onHoldCompleted()
                remainingSeconds = targetSeconds
            }
        }
    }

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeText = String.format(Locale.US, "%02d:%02d", mins, secs)
    val completedCount = activeSets.count { it.completed }
    val allDone = activeSets.isNotEmpty() && completedCount == activeSets.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AuraTheme.colors.surfaceSecondary)
            .border(1.dp, accent.copy(alpha = if (isRunning) 0.55f else 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("timed_hold_panel_$exIdx"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(136.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle = 360f * progressFraction,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeText,
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuraTheme.colors.primaryText
                )
                Text(
                    text = "${targetSeconds}s hold",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AuraTheme.colors.textMuted
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = accentContrast
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("timed_hold_toggle_btn_$exIdx")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause hold timer" else "Start hold timer",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isRunning) "Pause" else "Start",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            IconButton(
                onClick = {
                    isRunning = false
                    remainingSeconds = targetSeconds
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AuraTheme.colors.card)
                    .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                    .testTag("timed_hold_reset_btn_$exIdx")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset hold timer",
                    tint = AuraTheme.colors.primaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isWorkoutActive && allDone) {
            Text(
                text = "All hold sets completed",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AuraTheme.colors.success
            )
        }
    }
}

@Composable
fun InteractiveSetRow(
    setNumber: Int,
    isDone: Boolean,
    currentW: Double,
    currentReps: Int,
    accent: Color,
    accentContrast: Color,
    onWeightChanged: (Double) -> Unit,
    onRepsChanged: (Int) -> Unit,
    onToggleDone: () -> Unit
) {
    var localWeight by remember(currentW) {
        mutableStateOf(
            if (currentW > 0) {
                if (currentW % 1.0 == 0.0) currentW.toInt().toString() else currentW.toString()
            } else ""
        )
    }
    var localReps by remember(currentReps) {
        mutableStateOf(if (currentReps > 0) currentReps.toString() else "")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDone) accent.copy(alpha = 0.08f) else AuraTheme.colors.surfaceSecondary)
            .border(1.dp, if (isDone) accent.copy(alpha = 0.40f) else AuraTheme.colors.cardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Symmetrical Set # Column
            Text(
                text = "#$setNumber",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isDone) accent else AuraTheme.colors.primaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(44.dp)
            )

            // Symmetrical Weight Column with smooth editing
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuraTheme.colors.input)
                    .border(1.dp, if (isDone) accent.copy(alpha = 0.25f) else AuraTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (localWeight.isEmpty()) {
                    Text(
                        text = "0",
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AuraTheme.colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
                BasicTextField(
                    value = localWeight,
                    onValueChange = { inputStr ->
                        val clean = inputStr.filter { ch -> ch.isDigit() || ch == '.' }
                        localWeight = clean
                        clean.toDoubleOrNull()?.let { num ->
                            onWeightChanged(num)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AuraTheme.colors.primaryText,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Symmetrical Reps Column with smooth editing
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AuraTheme.colors.input)
                    .border(1.dp, if (isDone) accent.copy(alpha = 0.25f) else AuraTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (localReps.isEmpty()) {
                    Text(
                        text = "0",
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AuraTheme.colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
                BasicTextField(
                    value = localReps,
                    onValueChange = { inputStr ->
                        val clean = inputStr.filter { ch -> ch.isDigit() }
                        localReps = clean
                        clean.toIntOrNull()?.let { num ->
                            onRepsChanged(num)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(
                        fontSize = 12.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AuraTheme.colors.primaryText,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Symmetrical Done / Log Button Column (minimum 48dp touch target)
            Box(
                modifier = Modifier
                    .widthIn(min = 58.dp)
                    .heightIn(min = 48.dp)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDone) accent else AuraTheme.colors.cardBorder)
                    .clickable { onToggleDone() }
                    .padding(horizontal = 10.dp)
                    .testTag("set_toggle_done_btn_$setNumber"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDone) "✓" else "LOG",
                    fontSize = if (isDone) 14.sp else 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDone) accentContrast else AuraTheme.colors.primaryText,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
