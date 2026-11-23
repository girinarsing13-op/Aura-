package com.example.aura.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.db.ExerciseLogEntity
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.data.model.SetLog
import com.example.aura.data.model.WorkoutDayPlan
import com.example.aura.engine.AuraTrainingMemoryEngine
import com.example.aura.engine.ExerciseCoachingPlan
import com.example.aura.engine.ProgressionEngine
import com.example.aura.engine.WorkoutBlueprintEngine
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.ui.AuraViewModel
import com.example.aura.ui.PersonalBestEvent
import com.example.aura.ui.character.AuraEventBus
import com.example.aura.ui.character.FitnessEvent
import com.example.aura.ui.components.AuraCard
import com.example.aura.ui.components.AuraPillButton
import com.example.aura.ui.components.AuraStaggeredEntrance
import com.example.aura.ui.components.ExerciseDemo
import com.example.aura.ui.components.ExerciseGuideBottomSheet
import com.example.aura.ui.components.RestTimerController
import com.example.aura.ui.components.rememberReducedMotion
import com.example.aura.ui.theme.AuraTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private enum class WorkoutViewStage {
    WEEK,
    DAY_DETAIL,
    SESSION,
    REST,
    COMPLETE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: AuraViewModel,
    restTimerController: RestTimerController,
    onNavigateBack: () -> Unit,
    openTodayDirectlyTrigger: Int = 0,
    onFullScreenChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val draft by viewModel.draftProfile.collectAsState()
    val user = profile ?: draft
    val blueprint by viewModel.blueprint.collectAsState()
    val selectedDayIdx by viewModel.selectedWorkoutDay.collectAsState()
    val habits by viewModel.dailyHabits.collectAsState()
    val activeSession by viewModel.activeWorkoutSession.collectAsState()
    val recordedSessions by viewModel.workoutSessions.collectAsState()
    val exerciseLogs by viewModel.exerciseLogs.collectAsState()
    val dayExerciseSwaps by viewModel.dayExerciseSwaps.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()

    val colors = AuraTheme.colors
    val reducedMotion = rememberReducedMotion()
    val haptics = LocalHapticFeedback.current

    val weekPlans = blueprint?.workout?.week ?: emptyList()
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayShort = remember { SimpleDateFormat("EEE", Locale.US).format(Date()) }
    val todayPlanIdx = remember(weekPlans, todayDateStr, todayShort) {
        weekPlans.indexOfFirst { it.dateKey == todayDateStr }
            .takeIf { it >= 0 }
            ?: weekPlans.indexOfFirst { it.scheduledDate.startsWith(todayShort, ignoreCase = true) }
                .takeIf { it >= 0 }
            ?: (WorkoutScheduleEngine.calendarToWeekdayIndex() - 1).coerceIn(0, (weekPlans.lastIndex).coerceAtLeast(0))
    }

    var stageName by rememberSaveable { mutableStateOf(WorkoutViewStage.WEEK.name) }
    val stage = remember(stageName) {
        try {
            WorkoutViewStage.valueOf(stageName)
        } catch (_: Exception) {
            WorkoutViewStage.WEEK
        }
    }

    // Track trigger from Home "Start workout" so it opens Today's workout (or active session) directly
    var handledTrigger by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(openTodayDirectlyTrigger, activeSession) {
        if (openTodayDirectlyTrigger > handledTrigger) {
            handledTrigger = openTodayDirectlyTrigger
            if (activeSession != null) {
                viewModel.setSelectedWorkoutDay(activeSession!!.dayIndex)
                stageName = WorkoutViewStage.SESSION.name
            } else {
                viewModel.setSelectedWorkoutDay(todayPlanIdx)
                stageName = WorkoutViewStage.DAY_DETAIL.name
            }
        }
    }

    // Notify parent AuraApp to hide bottom nav bar on DAY_DETAIL, SESSION, REST, COMPLETE
    LaunchedEffect(stage) {
        onFullScreenChanged(stage != WorkoutViewStage.WEEK)
        if (stage == WorkoutViewStage.REST || stage == WorkoutViewStage.COMPLETE) {
            restTimerController.stop()
        }
    }
    DisposableEffect(Unit) {
        onDispose { onFullScreenChanged(false) }
    }

    val activeDayPlan: WorkoutDayPlan? = weekPlans.getOrNull(selectedDayIdx) ?: weekPlans.firstOrNull()
    val displayedExercises = remember(activeDayPlan, dayExerciseSwaps) {
        activeDayPlan?.exercises?.mapIndexed { idx, ex ->
            dayExerciseSwaps[activeDayPlan.dayNumber to idx] ?: ex
        } ?: emptyList()
    }

    val completedDateKeys = remember(recordedSessions, habits) {
        ProgressionEngine.extractCompletedWorkoutDateKeys(recordedSessions, habits)
    }

    fun isDayPlanCompleted(plan: WorkoutDayPlan, idx: Int): Boolean {
        val isPlanToday = idx == todayPlanIdx || plan.dateKey == todayDateStr
        if (isPlanToday && habits[todayDateStr]?.workout == true) return true
        return recordedSessions.any { rec ->
            val recDate = ProgressionEngine.normalizeDateKey(rec.date)
            rec.dayNumber == plan.dayNumber &&
                (rec.isFullyCompleted || rec.setsCompleted > 0) &&
                (recDate == plan.dateKey || (isPlanToday && recDate == todayDateStr))
        }
    }

    val coachingMap = remember(displayedExercises, exerciseLogs, user.deloadActive, todayDateStr) {
        displayedExercises.mapIndexed { idx, ex ->
            idx to ProgressionEngine.buildExerciseCoaching(
                exercise = ex,
                allLogs = exerciseLogs,
                isDeloadActive = user.deloadActive,
                todayDateKey = todayDateStr
            )
        }.toMap()
    }

    fun createInitialSets(ex: BlueprintExercise, plan: ExerciseCoachingPlan? = null): MutableList<SetLog> {
        if (plan != null && plan.prefilledSets.isNotEmpty()) {
            return plan.prefilledSets.toMutableList()
        }
        val defaultReps = if (ex.isTimedHold()) ex.targetHoldSeconds()
        else Regex("\\d+").find(ex.reps)?.value?.toIntOrNull() ?: 10
        return MutableList(ex.sets) { sIdx ->
            SetLog(
                setNumber = sIdx + 1,
                prescribedWeightKg = ex.recommendedLoadKg,
                prescribedReps = ex.reps,
                completed = false,
                loggedWeightKg = ex.recommendedLoadKg,
                loggedReps = defaultReps
            )
        }
    }

    val touchedExerciseSlots = remember(activeDayPlan?.dayNumber) { mutableStateMapOf<Int, Boolean>() }
    val exerciseSetsMap = remember(activeDayPlan?.dayNumber) {
        val map = mutableStateMapOf<Int, MutableList<SetLog>>()
        displayedExercises.forEachIndexed { idx, ex ->
            map[idx] = createInitialSets(ex, coachingMap[idx])
        }
        map
    }

    LaunchedEffect(coachingMap, displayedExercises) {
        displayedExercises.forEachIndexed { idx, ex ->
            if (touchedExerciseSlots[idx] != true) {
                exerciseSetsMap[idx] = createInitialSets(ex, coachingMap[idx])
            }
        }
    }

    // Session flow state
    var currentExerciseIdx by rememberSaveable { mutableIntStateOf(0) }
    var pendingNextExerciseIdx by rememberSaveable { mutableIntStateOf(0) }
    var restDurationSeconds by rememberSaveable { mutableIntStateOf(90) }
    var restUpNextInfo by remember { mutableStateOf<RestUpNextInfo?>(null) }
    var sessionPrCount by rememberSaveable { mutableIntStateOf(0) }
    var sessionBestPb by remember { mutableStateOf<PersonalBestEvent?>(null) }
    var completionSummary by remember { mutableStateOf<WorkoutCompletionSummary?>(null) }

    // Guide & Swap sheet state
    var activeGuideExerciseId by rememberSaveable { mutableStateOf<String?>(null) }
    var swapTargetIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    val activeGuideExercise = activeGuideExerciseId?.let { id ->
        displayedExercises.firstOrNull { it.id == id }
    }
    val swapTargetExercise = swapTargetIndex?.let { idx ->
        displayedExercises.getOrNull(idx)?.let { idx to it }
    }

    // Keep screen awake during active session & rest
    val context = LocalContext.current
    val view = LocalView.current
    val shouldKeepScreenOn = (stage == WorkoutViewStage.SESSION || stage == WorkoutViewStage.REST) && keepScreenOn
    DisposableEffect(shouldKeepScreenOn, view, context) {
        val window = (context as? Activity)?.window
        view.keepScreenOn = shouldKeepScreenOn
        if (shouldKeepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            view.keepScreenOn = false
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun startOrResumeSession(plan: WorkoutDayPlan) {
        val startIdx = displayedExercises.indices.firstOrNull { idx ->
            exerciseSetsMap[idx]?.any { !it.completed } == true
        } ?: 0
        currentExerciseIdx = startIdx
        if (activeSession == null || activeSession?.dayNumber != plan.dayNumber) {
            sessionPrCount = 0
            sessionBestPb = null
            viewModel.startWorkoutSession(
                dayIndex = selectedDayIdx,
                dayNumber = plan.dayNumber,
                dayTitle = plan.focus
            )
        }
        stageName = WorkoutViewStage.SESSION.name
    }

    fun finishAndRecordWorkout(plan: WorkoutDayPlan) {
        val logs = mutableListOf<ExerciseLogEntity>()
        var completedSets = 0
        var totalVolume = 0.0
        var totalReps = 0
        var allBodyweight = true
        var topSetScore = -1.0
        var topSetLabel: String? = null

        displayedExercises.forEachIndexed { idx, ex ->
            if (!ex.isBodyweight) allBodyweight = false
            val sets = exerciseSetsMap[idx] ?: emptyList()
            sets.forEach { s ->
                val w = s.loggedWeightKg ?: s.prescribedWeightKg
                val r = s.loggedReps ?: 10
                if (s.completed) {
                    completedSets++
                    totalReps += r
                    if (!ex.isBodyweight && w > 0.0) {
                        totalVolume += w * r.toDouble()
                    }
                    val score = if (w > 0.0) ProgressionEngine.calculateEstimated1Rm(w, r) else r.toDouble()
                    if (score > topSetScore) {
                        topSetScore = score
                        topSetLabel = if (!ex.isBodyweight && w > 0.0) {
                            "${ex.name} • ${ProgressionEngine.formatKg(w)} kg × $r reps"
                        } else {
                            "${ex.name} • $r ${if (ex.isTimedHold()) "sec" else "reps"}"
                        }
                    }
                }
                logs.add(
                    ExerciseLogEntity(
                        sessionId = 0,
                        date = todayDateStr,
                        exerciseId = ex.id,
                        exerciseName = ex.name,
                        equipment = ex.equipment,
                        variation = ex.detail,
                        setNumber = s.setNumber,
                        weightKg = w,
                        reps = r,
                        completed = s.completed
                    )
                )
            }
        }

        val totalSets = displayedExercises.indices.sumOf { idx ->
            exerciseSetsMap[idx]?.size ?: coachingMap[idx]?.targetSets ?: displayedExercises[idx].sets
        }
        val elapsedMins = activeSession?.let {
            max(1, ((System.currentTimeMillis() - it.startTimeMillis) / 60000L).toInt())
        } ?: max(1, (completedSets * 2))

        viewModel.recordWorkoutSession(
            dayNumber = plan.dayNumber,
            dayTitle = plan.focus,
            setsCompleted = completedSets,
            totalSets = totalSets,
            exerciseLogs = logs,
            volumeKg = totalVolume,
            prCount = sessionPrCount
        )

        val updatedStreak = max(1, viewModel.calculateCurrentStreak() + if (habits[todayDateStr]?.workout == true) 0 else 1)
        completionSummary = WorkoutCompletionSummary(
            focusTitle = plan.focus,
            durationMinutes = elapsedMins,
            totalVolumeKg = totalVolume,
            totalCompletedSets = completedSets,
            totalReps = totalReps,
            isBodyweightOnly = allBodyweight,
            newPersonalBest = sessionBestPb,
            bestSetLabel = topSetLabel,
            streakDays = updatedStreak
        )
        stageName = WorkoutViewStage.COMPLETE.name
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        if (weekPlans.isEmpty() || activeDayPlan == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Building your weekly workout plan...",
                    color = colors.secondaryText,
                    fontSize = 14.sp
                )
            }
            return
        }

        when (stage) {
            WorkoutViewStage.WEEK -> {
                WorkoutWeekListView(
                    splitName = blueprint?.workout?.splitName ?: user.trainingSplit,
                    weekPlans = weekPlans,
                    todayPlanIdx = todayPlanIdx,
                    deloadActive = user.deloadActive,
                    activeSessionTitle = activeSession?.dayTitle,
                    isDayCompleted = { plan, idx -> isDayPlanCompleted(plan, idx) },
                    onResumeActive = {
                        activeSession?.let {
                            viewModel.setSelectedWorkoutDay(it.dayIndex)
                            stageName = WorkoutViewStage.SESSION.name
                        }
                    },
                    onDaySelected = { idx ->
                        viewModel.setSelectedWorkoutDay(idx)
                        stageName = WorkoutViewStage.DAY_DETAIL.name
                    }
                )
            }

            WorkoutViewStage.DAY_DETAIL -> {
                val isSelectedToday = selectedDayIdx == todayPlanIdx || activeDayPlan.dateKey == todayDateStr
                val isSelectedCompleted = isDayPlanCompleted(activeDayPlan, selectedDayIdx)
                val isSelectedActive = activeSession != null && activeSession?.dayNumber == activeDayPlan.dayNumber
                val scheduledTimeLabel = remember(user.preferredWorkoutMinutes) {
                    WorkoutScheduleEngine.formatTimeMinutes(user.preferredWorkoutMinutes)
                }

                WorkoutDayDetailView(
                    dayPlan = activeDayPlan,
                    exercises = displayedExercises,
                    coachingMap = coachingMap,
                    isToday = isSelectedToday,
                    isCompleted = isSelectedCompleted,
                    isSessionActive = isSelectedActive,
                    scheduledTimeLabel = scheduledTimeLabel,
                    estimatedMinutes = user.sessionDurationMinutes,
                    onBack = { stageName = WorkoutViewStage.WEEK.name },
                    onExerciseClicked = { ex -> activeGuideExerciseId = ex.id },
                    onSwapExercise = { idx -> swapTargetIndex = idx },
                    onStartWorkout = {
                        if (activeDayPlan.isRest) {
                            viewModel.recordWorkoutSession(
                                dayNumber = activeDayPlan.dayNumber,
                                dayTitle = activeDayPlan.focus,
                                setsCompleted = 0,
                                totalSets = 0
                            )
                            stageName = WorkoutViewStage.WEEK.name
                        } else {
                            startOrResumeSession(activeDayPlan)
                        }
                    }
                )
            }

            WorkoutViewStage.SESSION -> {
                val safeExIdx = currentExerciseIdx.coerceIn(0, (displayedExercises.lastIndex).coerceAtLeast(0))
                val currentEx = displayedExercises.getOrNull(safeExIdx)
                if (currentEx == null) {
                    stageName = WorkoutViewStage.DAY_DETAIL.name
                } else {
                    val currentSets = exerciseSetsMap[safeExIdx] ?: createInitialSets(currentEx, coachingMap[safeExIdx])
                    val remainingTotalSets = displayedExercises.indices.sumOf { idx ->
                        (exerciseSetsMap[idx] ?: emptyList()).count { !it.completed }
                    }

                    WorkoutSessionView(
                        exercise = currentEx,
                        exerciseIndex = safeExIdx,
                        totalExercises = displayedExercises.size,
                        sets = currentSets,
                        coachingPlan = coachingMap[safeExIdx],
                        startTimeMillis = activeSession?.startTimeMillis ?: System.currentTimeMillis(),
                        isFinalRemainingSet = remainingTotalSets <= 1,
                        onSelectExercise = { newIdx ->
                            currentExerciseIdx = newIdx.coerceIn(0, displayedExercises.lastIndex)
                        },
                        onEditSet = { setIdx, newWeight, newReps ->
                            touchedExerciseSlots[safeExIdx] = true
                            val list = exerciseSetsMap[safeExIdx]?.toMutableList() ?: currentSets.toMutableList()
                            if (setIdx in list.indices) {
                                val updated = list[setIdx].copy(
                                    loggedWeightKg = newWeight,
                                    loggedReps = newReps
                                )
                                list[setIdx] = updated
                                exerciseSetsMap[safeExIdx] = list
                                if (updated.completed) {
                                    viewModel.logSetImmediate(
                                        exercise = currentEx,
                                        setNumber = updated.setNumber,
                                        weightKg = newWeight,
                                        reps = newReps,
                                        completed = true
                                    )
                                }
                            }
                        },
                        onCompleteSet = { setIdx ->
                            touchedExerciseSlots[safeExIdx] = true
                            val list = exerciseSetsMap[safeExIdx]?.toMutableList() ?: currentSets.toMutableList()
                            if (setIdx in list.indices) {
                                val target = list[setIdx]
                                val effectiveW = target.loggedWeightKg ?: target.prescribedWeightKg
                                val effectiveR = target.loggedReps ?: 10
                                list[setIdx] = target.copy(
                                    completed = true,
                                    loggedWeightKg = effectiveW,
                                    loggedReps = effectiveR
                                )
                                exerciseSetsMap[safeExIdx] = list

                                val hitPb = viewModel.logSetImmediate(
                                    exercise = currentEx,
                                    setNumber = target.setNumber,
                                    weightKg = effectiveW,
                                    reps = effectiveR,
                                    completed = true
                                )
                                val feedback = AuraTrainingMemoryEngine.compareSet(
                                    exerciseName = currentEx.name,
                                    setNumber = target.setNumber,
                                    weightKg = effectiveW,
                                    reps = effectiveR,
                                    equipment = currentEx.equipment,
                                    currentTargetRepRange = currentEx.reps
                                )
                                if (hitPb || feedback.isPersonalRecord) {
                                    sessionPrCount++
                                    val candidatePb = PersonalBestEvent(
                                        exerciseName = currentEx.name,
                                        weightKg = effectiveW,
                                        reps = effectiveR,
                                        estimated1RmKg = ProgressionEngine.calculateEstimated1Rm(effectiveW, effectiveR)
                                    )
                                    if (sessionBestPb == null || candidatePb.estimated1RmKg >= (sessionBestPb?.estimated1RmKg ?: 0.0)) {
                                        sessionBestPb = candidatePb
                                    }
                                }

                                AuraEventBus.post(
                                    FitnessEvent.SetCompleted(
                                        exerciseName = currentEx.name,
                                        setIndex = target.setNumber,
                                        totalSets = list.size,
                                        isPR = hitPb || feedback.isPersonalRecord,
                                        isDecline = feedback.isPerformanceDecline,
                                        isExcessiveWeightJump = feedback.isWeightJumpExcessive
                                    )
                                )

                                // Find next uncompleted set
                                val nextInCurrent = list.indexOfFirst { !it.completed }
                                if (nextInCurrent >= 0) {
                                    val nextSet = list[nextInCurrent]
                                    pendingNextExerciseIdx = safeExIdx
                                    restDurationSeconds = currentEx.restSeconds.coerceIn(30, 240)
                                    restUpNextInfo = RestUpNextInfo(
                                        exercise = currentEx,
                                        setNumber = nextSet.setNumber,
                                        totalSets = list.size,
                                        weightKg = nextSet.loggedWeightKg ?: nextSet.prescribedWeightKg,
                                        reps = nextSet.loggedReps ?: 10,
                                        isNewExercise = false
                                    )
                                    stageName = WorkoutViewStage.REST.name
                                } else {
                                    // Find next exercise with any uncompleted set
                                    val orderedCandidateIndices =
                                        ((safeExIdx + 1) until displayedExercises.size).toList() +
                                            (0 until safeExIdx).toList()
                                    val nextExIdx = orderedCandidateIndices.firstOrNull { candidateIdx ->
                                        (exerciseSetsMap[candidateIdx] ?: emptyList()).any { !it.completed }
                                    }
                                    if (nextExIdx != null) {
                                        val nextEx = displayedExercises[nextExIdx]
                                        val nextExSets = exerciseSetsMap[nextExIdx] ?: emptyList()
                                        val firstSetIdx = nextExSets.indexOfFirst { !it.completed }.coerceAtLeast(0)
                                        val nextSet = nextExSets.getOrNull(firstSetIdx)
                                        pendingNextExerciseIdx = nextExIdx
                                        restDurationSeconds = currentEx.restSeconds.coerceIn(45, 240)
                                        restUpNextInfo = RestUpNextInfo(
                                            exercise = nextEx,
                                            setNumber = nextSet?.setNumber ?: 1,
                                            totalSets = nextExSets.size.coerceAtLeast(1),
                                            weightKg = nextSet?.loggedWeightKg ?: nextEx.recommendedLoadKg,
                                            reps = nextSet?.loggedReps ?: 10,
                                            isNewExercise = true
                                        )
                                        stageName = WorkoutViewStage.REST.name
                                    } else {
                                        finishAndRecordWorkout(activeDayPlan)
                                    }
                                }
                            }
                        },
                        onOpenGuide = { activeGuideExerciseId = currentEx.id },
                        onOpenSwap = { swapTargetIndex = safeExIdx },
                        onApplyHarderVariation = { variation ->
                            val replaced = viewModel.swapExerciseForDayOnly(
                                dayNumber = activeDayPlan.dayNumber,
                                exerciseIndex = safeExIdx,
                                replacement = variation
                            )
                            val updatedCoaching = ProgressionEngine.buildExerciseCoaching(
                                exercise = replaced,
                                allLogs = exerciseLogs,
                                isDeloadActive = user.deloadActive,
                                todayDateKey = todayDateStr
                            )
                            touchedExerciseSlots[safeExIdx] = true
                            exerciseSetsMap[safeExIdx] = createInitialSets(replaced, updatedCoaching)
                        },
                        onFinishEarly = { finishAndRecordWorkout(activeDayPlan) },
                        onExitSession = { stageName = WorkoutViewStage.DAY_DETAIL.name }
                    )
                }
            }

            WorkoutViewStage.REST -> {
                WorkoutRestView(
                    initialSeconds = restDurationSeconds,
                    upNext = restUpNextInfo,
                    hapticsEnabled = hapticsEnabled,
                    soundEnabled = soundEnabled,
                    onFinishRest = {
                        currentExerciseIdx = pendingNextExerciseIdx.coerceIn(0, (displayedExercises.lastIndex).coerceAtLeast(0))
                        stageName = WorkoutViewStage.SESSION.name
                    }
                )
            }

            WorkoutViewStage.COMPLETE -> {
                val summary = completionSummary ?: WorkoutCompletionSummary(
                    focusTitle = activeDayPlan.focus,
                    durationMinutes = user.sessionDurationMinutes,
                    totalVolumeKg = 0.0,
                    totalCompletedSets = exerciseSetsMap.values.flatten().count { it.completed },
                    totalReps = exerciseSetsMap.values.flatten().filter { it.completed }.sumOf { it.loggedReps ?: 0 },
                    isBodyweightOnly = false,
                    newPersonalBest = sessionBestPb,
                    bestSetLabel = null,
                    streakDays = max(1, viewModel.calculateCurrentStreak())
                )
                val plannedWeekdays = remember(user.preferredWorkoutDays, user.daysPerWeek) {
                    WorkoutScheduleEngine.resolveWorkoutDays(user).toSet()
                }
                val weekDotsWithToday = remember(completedDateKeys, plannedWeekdays, todayDateStr) {
                    buildCurrentWeekDots(completedDateKeys + todayDateStr, plannedWeekdays, todayDateStr)
                }
                WorkoutCompleteView(
                    summary = summary,
                    weekDots = weekDotsWithToday,
                    onDone = {
                        completionSummary = null
                        stageName = WorkoutViewStage.WEEK.name
                        onNavigateBack()
                    }
                )
            }
        }

        // How to do it Bottom Sheet
        ExerciseGuideBottomSheet(
            exercise = activeGuideExercise,
            onDismiss = { activeGuideExerciseId = null }
        )

        // Swap Exercise Bottom Sheet
        if (swapTargetExercise != null) {
            val (targetIdx, targetEx) = swapTargetExercise
            val excludedIds = remember(displayedExercises, targetEx) {
                displayedExercises.map { it.id }.toSet()
            }
            val allowedAlternatives = remember(targetEx, user, excludedIds) {
                WorkoutBlueprintEngine.findSwapAlternatives(
                    targetExercise = targetEx,
                    excludedIdsOrNames = excludedIds,
                    profile = user,
                    limit = 4
                )
            }
            val swapSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = { swapTargetIndex = null },
                sheetState = swapSheetState,
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
                        .testTag("swap_exercise_bottom_sheet"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Swap for today",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText
                            )
                            Text(
                                text = "${targetEx.name} • ${targetEx.getEffectivePrimary()}",
                                fontSize = 13.sp,
                                color = colors.secondaryText
                            )
                        }
                        IconButton(
                            onClick = { swapTargetIndex = null },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.surface2)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close swap sheet",
                                tint = colors.primaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (allowedAlternatives.isEmpty()) {
                        AuraCard(
                            contentPadding = PaddingValues(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No other ${targetEx.getEffectivePrimary().lowercase()} exercises match your selected equipment and injury filters.",
                                fontSize = 13.sp,
                                color = colors.secondaryText
                            )
                        }
                    } else {
                        allowedAlternatives.forEachIndexed { altIdx, alt ->
                            AuraCard(
                                contentPadding = PaddingValues(12.dp),
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val replaced = viewModel.swapExerciseForDayOnly(
                                        dayNumber = activeDayPlan.dayNumber,
                                        exerciseIndex = targetIdx,
                                        replacement = alt
                                    )
                                    val updatedCoaching = ProgressionEngine.buildExerciseCoaching(
                                        exercise = replaced,
                                        allLogs = exerciseLogs,
                                        isDeloadActive = user.deloadActive,
                                        todayDateKey = todayDateStr
                                    )
                                    touchedExerciseSlots[targetIdx] = true
                                    exerciseSetsMap[targetIdx] = createInitialSets(replaced, updatedCoaching)
                                    viewModel.recordSubstitution(targetEx.id, alt.id, alt.name)
                                    swapTargetIndex = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("swap_alternative_item_$altIdx")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ExerciseDemo(
                                        exerciseName = alt.name,
                                        exerciseId = alt.id,
                                        primaryMuscle = alt.getEffectivePrimary(),
                                        equipment = alt.equipment.joinToString(" / "),
                                        movementPattern = alt.movementPattern,
                                        compact = true,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = alt.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.primaryText
                                        )
                                        Text(
                                            text = "${alt.getEffectivePrimary()} • ${alt.equipment.firstOrNull() ?: "Bodyweight"}",
                                            fontSize = 12.5.sp,
                                            color = colors.secondaryText
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.SwapHoriz,
                                        contentDescription = "Swap to ${alt.name}",
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp)
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
 * Step 3 — Workout Week List (`week`)
 * Done = gold check, Today = gold border + "Today" chip, Rest day dimmed.
 */
@Composable
private fun WorkoutWeekListView(
    splitName: String,
    weekPlans: List<WorkoutDayPlan>,
    todayPlanIdx: Int,
    deloadActive: Boolean,
    activeSessionTitle: String?,
    isDayCompleted: (WorkoutDayPlan, Int) -> Boolean,
    onResumeActive: () -> Unit,
    onDaySelected: (Int) -> Unit
) {
    val colors = AuraTheme.colors
    val reducedMotion = rememberReducedMotion()

    val plannedCount = remember(weekPlans) { weekPlans.count { !it.isRest } }
    val doneCount = remember(weekPlans) {
        weekPlans.indices.count { idx -> !weekPlans[idx].isRest && isDayCompleted(weekPlans[idx], idx) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag("workout_week_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 108.dp)
    ) {
        item(key = "week_header") {
            AuraStaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Workout",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.border, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$doneCount / $plannedCount done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.accent
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (deloadActive) "$splitName • Deload week (-40% sets)" else splitName,
                        fontSize = 14.sp,
                        color = colors.secondaryText
                    )
                }
            }
        }

        if (!activeSessionTitle.isNullOrBlank()) {
            item(key = "active_session_banner") {
                AuraStaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                    AuraCard(
                        selected = true,
                        contentPadding = PaddingValues(16.dp),
                        onClick = onResumeActive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("week_resume_active_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "IN PROGRESS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = activeSessionTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primaryText
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.accent)
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = colors.accentContrast,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Resume",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentContrast
                                )
                            }
                        }
                    }
                }
            }
        }

        itemsIndexed(
            items = weekPlans,
            key = { idx, plan -> "week_day_${plan.dayNumber}_$idx" }
        ) { idx, plan ->
            val isToday = idx == todayPlanIdx
            val isDone = isDayCompleted(plan, idx)
            val dayShortLabel = plan.scheduledDate
                .substringBefore(",")
                .ifBlank { "Day ${plan.dayNumber}" }

            AuraStaggeredEntrance(index = (idx + 1).coerceAtMost(8), reducedMotion = reducedMotion) {
                AuraCard(
                    selected = isToday,
                    contentPadding = PaddingValues(16.dp),
                    onClick = { onDaySelected(idx) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (plan.isRest && !isToday) 0.52f else 1f)
                        .testTag("week_day_card_$idx")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Left status indicator (Done = gold check, Rest = moon, Workout = day badge)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isDone -> colors.accent
                                        isToday -> colors.accent.copy(alpha = 0.16f)
                                        plan.isRest -> colors.surface2
                                        else -> colors.surface2
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        isDone -> colors.accent
                                        isToday -> colors.accent.copy(alpha = 0.55f)
                                        else -> colors.border
                                    },
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isDone -> {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Completed",
                                        tint = colors.accentContrast,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                plan.isRest -> {
                                    Icon(
                                        imageVector = Icons.Filled.Bedtime,
                                        contentDescription = "Rest day",
                                        tint = colors.mutedText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = dayShortLabel.take(3),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) colors.accent else colors.primaryText
                                    )
                                }
                            }
                        }

                        // Middle text
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = plan.focus,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (plan.isRest && !isToday) colors.secondaryText else colors.primaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (isToday) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(colors.accent)
                                            .padding(horizontal = 9.dp, vertical = 3.dp)
                                            .testTag("week_today_chip")
                                    ) {
                                        Text(
                                            text = "Today",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accentContrast
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            val subtitle = when {
                                plan.isRest -> "Rest day • Active recovery"
                                isDone -> "$dayShortLabel • Completed"
                                else -> "$dayShortLabel • ${plan.exercises.size} exercises • ${plan.exercises.sumOf { it.sets }} sets"
                            }
                            Text(
                                text = subtitle,
                                fontSize = 13.sp,
                                color = if (isDone) colors.accent else colors.mutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!plan.isRest || isToday) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Open ${plan.focus}",
                                tint = if (isToday) colors.accent else colors.mutedText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 3 — Today's Workout Detail (`day`)
 * Nav bar hidden. Back arrow, exercise list with thumbnails & sets x reps, Start workout button.
 */
@Composable
private fun WorkoutDayDetailView(
    dayPlan: WorkoutDayPlan,
    exercises: List<BlueprintExercise>,
    coachingMap: Map<Int, ExerciseCoachingPlan>,
    isToday: Boolean,
    isCompleted: Boolean,
    isSessionActive: Boolean,
    scheduledTimeLabel: String,
    estimatedMinutes: Int,
    onBack: () -> Unit,
    onExerciseClicked: (BlueprintExercise) -> Unit,
    onSwapExercise: (Int) -> Unit,
    onStartWorkout: () -> Unit
) {
    val colors = AuraTheme.colors
    val reducedMotion = rememberReducedMotion()
    var showWarmup by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    val totalSets = remember(exercises, coachingMap) {
        exercises.indices.sumOf { idx -> coachingMap[idx]?.targetSets ?: exercises[idx].sets }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("workout_day_detail_screen")
    ) {
        // Top Header with Back Arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .border(1.dp, colors.border, CircleShape)
                    .testTag("workout_day_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to week",
                    tint = colors.primaryText,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isToday && !dayPlan.isRest) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = scheduledTimeLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                }
            }
        }

        // Scrollable Exercise List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 20.dp)
        ) {
            item(key = "day_title_block") {
                AuraStaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isToday) "TODAY'S WORKOUT" else dayPlan.scheduledDate.uppercase().ifBlank { "DAY ${dayPlan.dayNumber}" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                letterSpacing = 0.8.sp
                            )
                            if (isCompleted) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(colors.accent.copy(alpha = 0.16f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Done",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.accent
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = dayPlan.focus,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (dayPlan.isRest) {
                                "Active recovery, hydration, and light mobility"
                            } else {
                                "${exercises.size} exercises • $totalSets sets • ~${estimatedMinutes} min"
                            },
                            fontSize = 14.sp,
                            color = colors.secondaryText
                        )
                    }
                }
            }

            if (dayPlan.isRest) {
                item(key = "rest_day_detail_card") {
                    AuraCard(
                        contentPadding = PaddingValues(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Recovery focus",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Muscles grow while you recover. Aim for a 20–30 minute walk, hit your protein and water targets, and get solid sleep tonight.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = colors.secondaryText
                        )
                    }
                }
            } else {
                // Collapsible Warm-up Toggle
                item(key = "warmup_toggle") {
                    AuraCard(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        onClick = { showWarmup = !showWarmup },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Warm-up & mobility",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primaryText
                                )
                                Text(
                                    text = "5 min dynamic prep before lifting",
                                    fontSize = 12.sp,
                                    color = colors.mutedText
                                )
                            }
                            Icon(
                                imageVector = if (showWarmup) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "Toggle warm-up",
                                tint = colors.secondaryText
                            )
                        }
                        AnimatedVisibility(visible = showWarmup) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                WorkoutWarmupSection(dayPlan = dayPlan)
                            }
                        }
                    }
                }

                // Exercise Cards with Thumbnails & Sets x Reps
                itemsIndexed(
                    items = exercises,
                    key = { idx, ex -> "day_ex_${dayPlan.dayNumber}_${idx}_${ex.id}" }
                ) { idx, ex ->
                    val coaching = coachingMap[idx]
                    val targetSets = coaching?.targetSets ?: ex.sets
                    val loadKg = coaching?.prefilledSets?.firstOrNull()?.loggedWeightKg ?: ex.recommendedLoadKg
                    val setsRepsLine = if (ex.isBodyweight || loadKg <= 0.0) {
                        "$targetSets sets × ${ex.reps}"
                    } else {
                        "$targetSets sets × ${ex.reps} • ${ProgressionEngine.formatKg(loadKg)} kg"
                    }

                    AuraStaggeredEntrance(index = (idx + 1).coerceAtMost(8), reducedMotion = reducedMotion) {
                        AuraCard(
                            contentPadding = PaddingValues(12.dp),
                            onClick = { onExerciseClicked(ex) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("day_exercise_row_$idx")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ExerciseDemo(
                                    exerciseName = ex.name,
                                    exerciseId = ex.id,
                                    primaryMuscle = ex.getEffectivePrimary(),
                                    equipment = ex.equipment,
                                    movementPattern = ex.getEffectivePattern(),
                                    compact = true,
                                    modifier = Modifier.size(64.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = ex.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.primaryText,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = setsRepsLine,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.secondaryText
                                    )
                                    if (coaching != null && coaching.hasChange && coaching.shortNote.isNotBlank()) {
                                        Text(
                                            text = coaching.shortNote,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colors.accent,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    IconButton(
                                        onClick = { onSwapExercise(idx) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .size(40.dp)
                                            .testTag("day_swap_ex_$idx")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.SwapHoriz,
                                            contentDescription = "Swap ${ex.name}",
                                            tint = colors.mutedText,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onExerciseClicked(ex) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .size(40.dp)
                                            .testTag("day_guide_ex_$idx")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                            contentDescription = "How to do ${ex.name}",
                                            tint = colors.accent,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fixed Bottom Start Workout Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            val buttonLabel = when {
                dayPlan.isRest -> if (isCompleted) "Recovery logged" else "Mark recovery done"
                isSessionActive -> "Resume workout"
                isCompleted -> "Do workout again"
                else -> "Start workout"
            }
            AuraPillButton(
                text = buttonLabel,
                onClick = onStartWorkout,
                enabled = !(dayPlan.isRest && isCompleted),
                isPrimary = true,
                modifier = Modifier.testTag("start_workout_session_button")
            )
        }
    }
}
