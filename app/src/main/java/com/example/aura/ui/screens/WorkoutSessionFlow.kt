package com.example.aura.ui.screens

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.data.model.Exercise
import com.example.aura.data.model.SetLog
import com.example.aura.engine.ExerciseCoachingPlan
import com.example.aura.engine.ProgressionEngine
import com.example.aura.ui.PersonalBestEvent
import com.example.aura.ui.components.AuraCard
import com.example.aura.ui.components.AuraPillButton
import com.example.aura.ui.components.AuraStaggeredEntrance
import com.example.aura.ui.components.ExerciseDemo
import com.example.aura.ui.components.rememberReducedMotion
import com.example.aura.ui.theme.AuraTheme
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.delay

data class RestUpNextInfo(
    val exercise: BlueprintExercise,
    val setNumber: Int,
    val totalSets: Int,
    val weightKg: Double,
    val reps: Int,
    val isNewExercise: Boolean
)

data class WorkoutCompletionSummary(
    val focusTitle: String,
    val durationMinutes: Int,
    val totalVolumeKg: Double,
    val totalCompletedSets: Int,
    val totalReps: Int,
    val isBodyweightOnly: Boolean,
    val newPersonalBest: PersonalBestEvent?,
    val bestSetLabel: String?,
    val streakDays: Int
)

/**
 * Step 4 — Focused 1-exercise-at-a-time active workout session view.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutSessionView(
    exercise: BlueprintExercise,
    exerciseIndex: Int,
    totalExercises: Int,
    sets: List<SetLog>,
    coachingPlan: ExerciseCoachingPlan?,
    startTimeMillis: Long,
    isFinalRemainingSet: Boolean,
    onSelectExercise: (Int) -> Unit,
    onEditSet: (setIdx: Int, weightKg: Double, reps: Int) -> Unit,
    onCompleteSet: (setIdx: Int) -> Unit,
    onOpenGuide: () -> Unit,
    onOpenSwap: () -> Unit,
    onApplyHarderVariation: (Exercise) -> Unit,
    onFinishEarly: () -> Unit,
    onExitSession: () -> Unit
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    var showExitConfirm by remember { mutableStateOf(false) }
    var editingSetIndex by remember { mutableStateOf<Int?>(null) }

    // Live elapsed timer
    var elapsedSeconds by remember(startTimeMillis) {
        mutableIntStateOf(((System.currentTimeMillis() - startTimeMillis) / 1000L).toInt().coerceAtLeast(0))
    }
    LaunchedEffect(startTimeMillis) {
        while (true) {
            elapsedSeconds = ((System.currentTimeMillis() - startTimeMillis) / 1000L).toInt().coerceAtLeast(0)
            delay(1000L)
        }
    }

    val firstIncompleteIdx = remember(sets) { sets.indexOfFirst { !it.completed } }
    val activeSetIdx = if (firstIncompleteIdx >= 0) firstIncompleteIdx else (sets.lastIndex.coerceAtLeast(0))
    val allSetsDoneForExercise = sets.isNotEmpty() && firstIncompleteIdx == -1

    BackHandler { showExitConfirm = true }

    val progressFraction by animateFloatAsState(
        targetValue = ((exerciseIndex + 1).toFloat() / totalExercises.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "sessionExerciseProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("workout_session_screen")
    ) {
        // Top Progress Header: Close + "3 of 6" + Progress Bar + Elapsed
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showExitConfirm = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .testTag("session_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Exit workout",
                        tint = colors.primaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${exerciseIndex + 1} of $totalExercises",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText,
                        modifier = Modifier.testTag("session_progress_counter")
                    )
                    Text(
                        text = formatElapsedTime(elapsedSeconds),
                        fontSize = 12.sp,
                        color = colors.mutedText
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { if (exerciseIndex > 0) onSelectExercise(exerciseIndex - 1) },
                        enabled = exerciseIndex > 0,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border, CircleShape)
                            .testTag("session_prev_exercise")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous exercise",
                            tint = if (exerciseIndex > 0) colors.primaryText else colors.mutedText.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { if (exerciseIndex < totalExercises - 1) onSelectExercise(exerciseIndex + 1) },
                        enabled = exerciseIndex < totalExercises - 1,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.border, CircleShape)
                            .testTag("session_next_exercise")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next exercise",
                            tint = if (exerciseIndex < totalExercises - 1) colors.primaryText else colors.mutedText.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Gold progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape)
                    .background(colors.trackMuted)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                )
            }
        }

        // Scrollable Exercise Body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Demo animation on top
            AnimatedContent(
                targetState = exercise.id,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                label = "sessionExerciseDemoTransition"
            ) {
                ExerciseDemo(
                    exerciseName = exercise.name,
                    exerciseId = exercise.id,
                    primaryMuscle = exercise.getEffectivePrimary(),
                    equipment = exercise.equipment,
                    movementPattern = exercise.getEffectivePattern(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("session_exercise_demo")
                )
            }

            // Exercise Title & Target Meta
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = exercise.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText,
                    lineHeight = 30.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${sets.size} sets × ${exercise.reps}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                    Text(text = "•", color = colors.mutedText, fontSize = 14.sp)
                    Text(
                        text = exercise.getEffectivePrimary(),
                        fontSize = 14.sp,
                        color = colors.secondaryText
                    )
                }
            }

            // "Last time" hint & coaching note
            val lastTimeText = remember(coachingPlan, exercise) {
                val isTimed = exercise.isTimedHold()
                when {
                    coachingPlan?.lastSessionWeightKg != null && coachingPlan.lastSessionReps != null -> {
                        val w = coachingPlan.lastSessionWeightKg
                        val r = coachingPlan.lastSessionReps
                        if (exercise.isBodyweight || w <= 0.0) {
                            "Last time: $r ${if (isTimed) "sec" else "reps"}"
                        } else {
                            "Last time: ${ProgressionEngine.formatKg(w)} kg × $r reps"
                        }
                    }
                    else -> {
                        if (exercise.isBodyweight || exercise.recommendedLoadKg <= 0.0) {
                            "Target: ${exercise.reps} • Rest ${exercise.restSeconds}s"
                        } else {
                            "Suggested start: ${ProgressionEngine.formatKg(exercise.recommendedLoadKg)} kg × ${exercise.reps}"
                        }
                    }
                }
            }

            AuraCard(
                highlighted = coachingPlan?.hasChange == true,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("session_last_time_hint")
            ) {
                Text(
                    text = lastTimeText,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryText
                )
                if (coachingPlan != null && coachingPlan.shortNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = coachingPlan.shortNote,
                        fontSize = 12.5.sp,
                        color = colors.accent
                    )
                }
                val suggestedVar = coachingPlan?.suggestedVariation
                if (suggestedVar != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.16f))
                            .border(1.dp, colors.accent.copy(alpha = 0.45f), CircleShape)
                            .clickable { onApplyHarderVariation(suggestedVar) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Switch to ${suggestedVar.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent
                        )
                    }
                }
            }

            // Set Chips (tap any chip to edit weight x reps; current set highlighted in gold)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sets",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                    Text(
                        text = "Tap a set to edit",
                        fontSize = 12.sp,
                        color = colors.mutedText
                    )
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    sets.forEachIndexed { idx, setLog ->
                        val isCurrent = !allSetsDoneForExercise && idx == activeSetIdx
                        val weight = setLog.loggedWeightKg ?: setLog.prescribedWeightKg
                        val reps = setLog.loggedReps ?: 10
                        val setLabel = formatSetChipValue(
                            weightKg = weight,
                            reps = reps,
                            isBodyweight = exercise.isBodyweight,
                            isTimed = exercise.isTimedHold()
                        )
                        SessionSetChip(
                            setNumber = setLog.setNumber,
                            valueText = setLabel,
                            isCompleted = setLog.completed,
                            isCurrent = isCurrent,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                editingSetIndex = idx
                            },
                            modifier = Modifier.testTag("session_set_chip_$idx")
                        )
                    }
                }
            }

            // Action chips: "How to do it" & "Swap"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenGuide()
                        }
                        .padding(horizontal = 14.dp)
                        .testTag("session_how_to_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "How to do it",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onOpenSwap()
                        }
                        .padding(horizontal = 14.dp)
                        .testTag("session_swap_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = null,
                        tint = colors.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Swap",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Fixed Bottom Primary Action
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            val ctaLabel = when {
                allSetsDoneForExercise && exerciseIndex < totalExercises - 1 -> "Next exercise"
                allSetsDoneForExercise -> "Finish workout"
                isFinalRemainingSet -> "Complete final set"
                else -> "Complete set ${activeSetIdx + 1}"
            }
            AuraPillButton(
                text = ctaLabel,
                onClick = {
                    if (allSetsDoneForExercise) {
                        if (exerciseIndex < totalExercises - 1) {
                            onSelectExercise(exerciseIndex + 1)
                        } else {
                            onFinishEarly()
                        }
                    } else {
                        onCompleteSet(activeSetIdx)
                    }
                },
                isPrimary = true,
                modifier = Modifier.testTag("session_complete_set_button")
            )
        }
    }

    // Edit Set Modal Sheet
    val targetEditIdx = editingSetIndex
    if (targetEditIdx != null && targetEditIdx in sets.indices) {
        val targetSet = sets[targetEditIdx]
        EditSetBottomSheet(
            setNumber = targetSet.setNumber,
            initialWeightKg = targetSet.loggedWeightKg ?: targetSet.prescribedWeightKg,
            initialReps = targetSet.loggedReps ?: 10,
            isBodyweight = exercise.isBodyweight,
            isTimed = exercise.isTimedHold(),
            equipment = exercise.equipment,
            onSave = { w, r ->
                onEditSet(targetEditIdx, w, r)
                editingSetIndex = null
            },
            onDismiss = { editingSetIndex = null }
        )
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            containerColor = colors.surface,
            titleContentColor = colors.primaryText,
            textContentColor = colors.secondaryText,
            title = {
                Text(
                    text = "Leave workout?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Your completed sets are already saved. You can finish now or resume later today.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirm = false
                        onFinishEarly()
                    }
                ) {
                    Text("Finish & save", color = colors.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitConfirm = false
                        onExitSession()
                    }
                ) {
                    Text("Pause", color = colors.secondaryText)
                }
            }
        )
    }
}

@Composable
private fun SessionSetChip(
    setNumber: Int,
    valueText: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    val bg by animateColorAsState(
        targetValue = when {
            isCurrent -> colors.accentTint
            isCompleted -> colors.accent.copy(alpha = 0.14f)
            else -> colors.surface2
        },
        animationSpec = tween(180),
        label = "setChipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isCurrent -> colors.accent
            isCompleted -> colors.accent.copy(alpha = 0.45f)
            else -> colors.border
        },
        animationSpec = tween(180),
        label = "setChipBorder"
    )

    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(if (isCurrent) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> colors.accent
                        isCurrent -> colors.accent
                        else -> colors.surface3
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Set $setNumber completed",
                    tint = colors.accentContrast,
                    modifier = Modifier.size(13.dp)
                )
            } else {
                Text(
                    text = "$setNumber",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) colors.accentContrast else colors.secondaryText
                )
            }
        }
        Text(
            text = valueText,
            fontSize = 13.5.sp,
            fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Medium,
            color = if (isCurrent) colors.accent else colors.primaryText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSetBottomSheet(
    setNumber: Int,
    initialWeightKg: Double,
    initialReps: Int,
    isBodyweight: Boolean,
    isTimed: Boolean,
    equipment: String,
    onSave: (weightKg: Double, reps: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = AuraTheme.colors
    val haptics = LocalHapticFeedback.current
    val weightStep = remember(equipment) {
        if (equipment.contains("Dumbbell", ignoreCase = true)) 1.0 else 2.5
    }
    var weight by remember(initialWeightKg) { mutableDoubleStateOf(initialWeightKg.coerceAtLeast(0.0)) }
    var reps by remember(initialReps) { mutableIntStateOf(initialReps.coerceAtLeast(1)) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        contentColor = colors.primaryText,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.border) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
                .testTag("edit_set_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Edit Set $setNumber",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText
            )

            if (!isBodyweight) {
                StepperRowCard(
                    label = "Weight (kg)",
                    valueText = "${ProgressionEngine.formatKg(weight)} kg",
                    stepLabel = "±${ProgressionEngine.formatKg(weightStep)} kg",
                    onDecrement = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        weight = ((weight - weightStep).coerceAtLeast(0.0) * 2.0).roundToInt() / 2.0
                    },
                    onIncrement = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        weight = ((weight + weightStep).coerceAtMost(400.0) * 2.0).roundToInt() / 2.0
                    }
                )
            }

            StepperRowCard(
                label = if (isTimed) "Hold duration (sec)" else "Reps",
                valueText = if (isTimed) "${reps}s" else "$reps reps",
                stepLabel = if (isTimed) "±5s" else "±1",
                onDecrement = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val delta = if (isTimed) 5 else 1
                    reps = (reps - delta).coerceAtLeast(1)
                },
                onIncrement = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val delta = if (isTimed) 5 else 1
                    reps = (reps + delta).coerceAtMost(300)
                }
            )

            AuraPillButton(
                text = "Save set",
                onClick = { onSave(weight, reps) },
                isPrimary = true,
                modifier = Modifier.testTag("edit_set_save_button")
            )
        }
    }
}

@Composable
private fun StepperRowCard(
    label: String,
    valueText: String,
    stepLabel: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    val colors = AuraTheme.colors
    AuraCard(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    color = colors.secondaryText
                )
                Text(
                    text = valueText,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
                Text(
                    text = stepLabel,
                    fontSize = 11.sp,
                    color = colors.mutedText
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .border(1.dp, colors.border, CircleShape)
                        .clickable(onClick = onDecrement),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease $label",
                        tint = colors.primaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .clickable(onClick = onIncrement),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase $label",
                        tint = colors.accentContrast,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Step 4 — Full-screen Rest Timer with real countdown ring, -15s / +15s / Skip,
 * Up-Next preview card, vibration at zero, and automatic advance.
 */
@Composable
fun WorkoutRestView(
    initialSeconds: Int,
    upNext: RestUpNextInfo?,
    hapticsEnabled: Boolean,
    soundEnabled: Boolean,
    onFinishRest: () -> Unit
) {
    val colors = AuraTheme.colors
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    val safeInitial = initialSeconds.coerceIn(15, 300)
    var totalSeconds by remember(safeInitial) { mutableIntStateOf(safeInitial) }
    var remainingSeconds by remember(safeInitial) { mutableIntStateOf(safeInitial) }
    var finished by remember(safeInitial) { mutableStateOf(false) }

    val toneGenerator = remember(soundEnabled) {
        if (!soundEnabled) null
        else try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
        } catch (_: Exception) {
            null
        }
    }
    DisposableEffect(toneGenerator) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (_: Exception) {
            }
        }
    }

    fun triggerRestZeroFeedback() {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 260)
            } catch (_: Exception) {
            }
        }
        if (hapticsEnabled) {
            vibrateRestComplete(context)
        }
    }

    LaunchedEffect(remainingSeconds, finished) {
        if (finished) return@LaunchedEffect
        if (remainingSeconds <= 0) {
            finished = true
            triggerRestZeroFeedback()
            delay(350L)
            onFinishRest()
        } else {
            delay(1000L)
            remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
        }
    }

    BackHandler {
        if (!finished) {
            finished = true
            onFinishRest()
        }
    }

    val targetFraction = (remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val smoothFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 850, easing = LinearEasing),
        label = "restRingCountdown"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("workout_rest_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                text = "REST",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Catch your breath",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText
            )
        }

        // Countdown Ring
        Box(
            modifier = Modifier
                .size(236.dp)
                .testTag("rest_timer_ring"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = 11.dp.toPx()
                val inset = strokePx / 2f
                val arcSize = Size(size.width - strokePx, size.height - strokePx)
                val topLeft = Offset(inset, inset)

                drawArc(
                    color = colors.trackMuted,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                drawArc(
                    color = colors.accent,
                    startAngle = -90f,
                    sweepAngle = 360f * smoothFraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatCountdown(remainingSeconds),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText,
                    modifier = Modifier.testTag("rest_timer_digits")
                )
                Text(
                    text = "remaining",
                    fontSize = 13.sp,
                    color = colors.mutedText
                )
            }
        }

        // Bottom Controls + Up Next Card
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // -15s / +15s / Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RestControlPill(
                    label = "-15s",
                    isPrimary = false,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val updated = (remainingSeconds - 15).coerceAtLeast(0)
                        remainingSeconds = updated
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rest_minus_15_button")
                )
                RestControlPill(
                    label = "+15s",
                    isPrimary = false,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        remainingSeconds = (remainingSeconds + 15).coerceAtMost(600)
                        if (remainingSeconds > totalSeconds) {
                            totalSeconds = remainingSeconds
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rest_plus_15_button")
                )
                RestControlPill(
                    label = "Skip",
                    isPrimary = true,
                    onClick = {
                        if (!finished) {
                            finished = true
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFinishRest()
                        }
                    },
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("rest_skip_button")
                )
            }

            // Up Next Card
            if (upNext != null) {
                AuraCard(
                    highlighted = upNext.isNewExercise,
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rest_up_next_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExerciseDemo(
                            exerciseName = upNext.exercise.name,
                            exerciseId = upNext.exercise.id,
                            primaryMuscle = upNext.exercise.getEffectivePrimary(),
                            equipment = upNext.exercise.equipment,
                            movementPattern = upNext.exercise.getEffectivePattern(),
                            compact = true,
                            modifier = Modifier.size(56.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (upNext.isNewExercise) "NEXT EXERCISE" else "UP NEXT",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = upNext.exercise.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val targetLine = formatSetChipValue(
                                weightKg = upNext.weightKg,
                                reps = upNext.reps,
                                isBodyweight = upNext.exercise.isBodyweight,
                                isTimed = upNext.exercise.isTimedHold()
                            )
                            Text(
                                text = "Set ${upNext.setNumber} of ${upNext.totalSets} • $targetLine",
                                fontSize = 13.sp,
                                color = colors.secondaryText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestControlPill(
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .height(50.dp)
            .clip(CircleShape)
            .background(if (isPrimary) colors.accent else colors.surface2)
            .border(
                width = 1.dp,
                color = if (isPrimary) colors.accent else colors.border,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) colors.accentContrast else colors.primaryText
        )
    }
}

/**
 * Step 4 — Celebratory Workout Complete view with pop-in check, falling confetti,
 * count-up stats, personal best card, streak + week dots, and Done button.
 */
@Composable
fun WorkoutCompleteView(
    summary: WorkoutCompletionSummary,
    weekDots: List<WeekDotDay>,
    onDone: () -> Unit
) {
    val colors = AuraTheme.colors
    val reducedMotion = rememberReducedMotion()

    // Pop-in check scale
    val checkScale = remember { Animatable(if (reducedMotion) 1f else 0f) }
    // Count-up stat progress
    val countProgress = remember { Animatable(if (reducedMotion) 1f else 0f) }
    // Confetti fall progress
    val confettiProgress = remember { Animatable(if (reducedMotion) 1f else 0f) }

    LaunchedEffect(reducedMotion) {
        if (!reducedMotion) {
            checkScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }
    LaunchedEffect(reducedMotion) {
        if (!reducedMotion) {
            delay(180L)
            countProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing)
            )
        }
    }
    LaunchedEffect(reducedMotion) {
        if (!reducedMotion) {
            confettiProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
            )
        }
    }

    val particles = remember {
        val palette = listOf(
            Color(0xFFE8B64C),
            Color(0xFF7DD3A8),
            Color(0xFF6CB4F5),
            Color(0xFFF58C6C),
            Color(0xFFF4F4F5)
        )
        List(52) { idx ->
            DoneConfettiParticle(
                xFraction = ((idx * 67 + 13) % 100) / 100f,
                startOffsetY = -((idx * 23) % 240).toFloat(),
                speedY = 0.85f + ((idx * 17) % 65) / 100f,
                drift = ((idx % 9) - 4) * 0.06f,
                sizeDp = 5f + (idx % 4) * 2.2f,
                color = palette[idx % palette.size],
                phase = idx * 0.55f
            )
        }
    }

    BackHandler { onDone() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .testTag("workout_complete_screen")
    ) {
        // Falling Confetti Canvas
        if (!reducedMotion && confettiProgress.value < 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = confettiProgress.value
                val fadeAlpha = (1f - ((p - 0.72f).coerceAtLeast(0f) / 0.28f)).coerceIn(0f, 1f)
                particles.forEach { particle ->
                    val y = particle.startOffsetY + p * (size.height + 260f) * particle.speedY
                    val x = (particle.xFraction * size.width) +
                        sin(p * 11f + particle.phase) * 26f +
                        particle.drift * size.width * p
                    if (y in -20f..(size.height + 20f)) {
                        val r = particle.sizeDp.dp.toPx()
                        drawLine(
                            color = particle.color.copy(alpha = fadeAlpha),
                            start = Offset(x, y),
                            end = Offset(
                                x + cos(p * 12f + particle.phase) * r * 1.4f,
                                y + sin(p * 12f + particle.phase) * r * 1.4f
                            ),
                            strokeWidth = (r * 0.55f).coerceAtLeast(2.5f),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Pop-in Gold Check
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .graphicsLayer {
                        scaleX = checkScale.value
                        scaleY = checkScale.value
                    }
                    .clip(CircleShape)
                    .background(colors.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Workout complete",
                    tint = colors.accentContrast,
                    modifier = Modifier.size(44.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Workout complete!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary.focusTitle,
                    fontSize = 15.sp,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center
                )
            }

            // Count-up Stats Row
            val animatedDuration = max(1, (summary.durationMinutes * countProgress.value).roundToInt())
            val animatedVolume = (summary.totalVolumeKg * countProgress.value).roundToInt()
            val animatedReps = (summary.totalReps * countProgress.value).roundToInt()
            val animatedSets = (summary.totalCompletedSets * countProgress.value).roundToInt()

            AuraStaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_complete_stats_row"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DoneStatTile(
                        value = "$animatedDuration min",
                        label = "Duration",
                        modifier = Modifier.weight(1f)
                    )
                    DoneStatTile(
                        value = if (summary.isBodyweightOnly || summary.totalVolumeKg <= 0.0) {
                            "$animatedReps"
                        } else {
                            "$animatedVolume kg"
                        },
                        label = if (summary.isBodyweightOnly || summary.totalVolumeKg <= 0.0) "Total reps" else "Volume",
                        modifier = Modifier.weight(1f)
                    )
                    DoneStatTile(
                        value = "$animatedSets",
                        label = "Sets done",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Personal Best / Best Set Card
            val pb = summary.newPersonalBest
            if (pb != null || !summary.bestSetLabel.isNullOrBlank()) {
                AuraStaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                    AuraCard(
                        highlighted = pb != null,
                        contentPadding = PaddingValues(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_complete_pb_card")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Personal best",
                                    tint = colors.accent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (pb != null) "NEW PERSONAL BEST" else "TOP SET TODAY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                if (pb != null) {
                                    val loadLabel = if (pb.weightKg > 0.0) {
                                        "${ProgressionEngine.formatKg(pb.weightKg)} kg × ${pb.reps} reps"
                                    } else {
                                        "${pb.reps} reps"
                                    }
                                    Text(
                                        text = pb.exerciseName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primaryText
                                    )
                                    Text(
                                        text = if (pb.estimated1RmKg > 0.0) {
                                            "$loadLabel • Est. 1RM ${ProgressionEngine.formatKg(pb.estimated1RmKg)} kg"
                                        } else {
                                            loadLabel
                                        },
                                        fontSize = 13.sp,
                                        color = colors.secondaryText
                                    )
                                } else {
                                    Text(
                                        text = summary.bestSetLabel.orEmpty(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Streak Card with Week Dots
            AuraStaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                AuraCard(
                    contentPadding = PaddingValues(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("workout_complete_streak_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(colors.accentTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${max(1, summary.streakDays)} day streak",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                            Text(
                                text = "Consistency builds results",
                                fontSize = 12.5.sp,
                                color = colors.mutedText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    WeekStreakDotsRow(weekDots = weekDots)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AuraPillButton(
                text = "Done",
                onClick = onDone,
                isPrimary = true,
                modifier = Modifier.testTag("finish_workout_summary_button")
            )
        }
    }
}

@Composable
private fun DoneStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    AuraCard(
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = colors.mutedText,
                maxLines = 1
            )
        }
    }
}

private data class DoneConfettiParticle(
    val xFraction: Float,
    val startOffsetY: Float,
    val speedY: Float,
    val drift: Float,
    val sizeDp: Float,
    val color: Color,
    val phase: Float
)

private fun formatSetChipValue(
    weightKg: Double,
    reps: Int,
    isBodyweight: Boolean,
    isTimed: Boolean
): String {
    return when {
        isTimed -> "${reps}s"
        isBodyweight || weightKg <= 0.0 -> "$reps reps"
        else -> "${ProgressionEngine.formatKg(weightKg)} kg × $reps"
    }
}

private fun formatCountdown(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    val mins = safe / 60
    val secs = safe % 60
    return String.format("%d:%02d", mins, secs)
}

private fun formatElapsedTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}

private fun vibrateRestComplete(context: Context) {
    try {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 140, 80, 180)
                val amplitudes = intArrayOf(0, 220, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(280L)
            }
        }
    } catch (_: Exception) {
        // ignore if device has no vibrator permission/hardware
    }
}
