package com.example.aura.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.WorkoutDayPlan
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.engine.ProgressAnalyticsEngine
import com.example.aura.engine.ProgressionEngine
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.ui.AuraViewModel
import com.example.aura.ui.coach.AuraCoachEngine
import com.example.aura.ui.coach.CoachFeedback
import com.example.aura.ui.components.AuraCard
import com.example.aura.ui.components.AuraPillButton
import com.example.aura.ui.components.AuraRing
import com.example.aura.ui.components.AuraStaggeredEntrance
import com.example.aura.ui.components.rememberReducedMotion
import com.example.aura.ui.theme.AuraTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeekDotDay(
    val shortLabel: String,
    val dateKey: String,
    val isDone: Boolean,
    val isToday: Boolean,
    val isPlanned: Boolean
)

fun buildCurrentWeekDots(
    completedDateKeys: Set<String>,
    plannedWeekdays: Set<Int>,
    todayKey: String
): List<WeekDotDay> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val cal = Calendar.getInstance()
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val offsetFromMon = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    cal.add(Calendar.DAY_OF_YEAR, -offsetFromMon)

    return List(7) { idx ->
        val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, idx) }
        val key = sdf.format(dayCal.time)
        WeekDotDay(
            shortLabel = labels[idx],
            dateKey = key,
            isDone = key in completedDateKeys,
            isToday = key == todayKey,
            isPlanned = (idx + 1) in plannedWeekdays
        )
    }
}

@Composable
fun HomeScreen(
    viewModel: AuraViewModel,
    onNavigateToWorkout: () -> Unit,
    onOpenProgress: () -> Unit = {},
    onOpenColorModal: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val draft by viewModel.draftProfile.collectAsState()
    val user = profile ?: draft

    val blueprint by viewModel.blueprint.collectAsState()
    val sessions by viewModel.workoutSessions.collectAsState()
    val habits by viewModel.dailyHabits.collectAsState()
    val exerciseLogs by viewModel.exerciseLogs.collectAsState()
    val checkIns by viewModel.bodyCheckIns.collectAsState()
    val selectedDayIndex by viewModel.selectedWorkoutDay.collectAsState()
    val activeSession by viewModel.activeWorkoutSession.collectAsState()

    val colors = AuraTheme.colors
    val reducedMotion = rememberReducedMotion()
    val haptics = LocalHapticFeedback.current

    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val todayDateLabel = remember { SimpleDateFormat("EEEE, MMM d", Locale.US).format(Date()) }
    val greetingPrefix = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 4..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val todayHabit = habits[todayKey] ?: DailyHabitStatus(dateKey = todayKey)
    val streak = remember(user, sessions, habits) { viewModel.calculateCurrentStreak() }
    val completedDateKeys = remember(sessions, habits) {
        ProgressionEngine.extractCompletedWorkoutDateKeys(sessions, habits)
    }
    val plannedWeekdays = remember(user.preferredWorkoutDays, user.daysPerWeek) {
        WorkoutScheduleEngine.resolveWorkoutDays(user).toSet()
    }
    val weekDots = remember(completedDateKeys, plannedWeekdays, todayKey) {
        buildCurrentWeekDots(completedDateKeys, plannedWeekdays, todayKey)
    }
    val weeklySummary = remember(user, blueprint, sessions, habits, exerciseLogs) {
        ProgressAnalyticsEngine.buildWeeklySummary(user, blueprint, sessions, habits, exerciseLogs)
    }
    val deloadRec = remember(sessions, habits, user, todayKey) {
        ProgressionEngine.evaluateDeloadNeed(sessions, habits, user, todayKey)
    }
    val weighInDue = remember(checkIns, todayKey) {
        ProgressAnalyticsEngine.isWeighInDue(checkIns, todayKey)
    }

    val weekPlans = blueprint?.workout?.week ?: emptyList()
    val todayPlan: WorkoutDayPlan? = remember(weekPlans, todayKey, selectedDayIndex) {
        val todayShort = SimpleDateFormat("EEE", Locale.US).format(Date())
        weekPlans.find { it.dateKey == todayKey }
            ?: weekPlans.find { it.scheduledDate.startsWith(todayShort, ignoreCase = true) }
            ?: weekPlans.getOrNull(selectedDayIndex)
            ?: weekPlans.firstOrNull()
    }
    val isTodayWorkout = viewModel.isTodayWorkoutDay(user, blueprint)
    val isTodayDone = todayHabit.workout || (todayPlan != null && sessions.any {
        val recDate = ProgressionEngine.normalizeDateKey(it.date)
        recDate == todayKey && it.dayNumber == todayPlan.dayNumber && it.isFullyCompleted
    })

    val targetWaterMl = remember(user.weight, isTodayWorkout) {
        FitnessCalcEngine.calcWaterTargetMl(user.weight, isTodayWorkout)
    }
    val bmr = blueprint?.metrics?.bmr ?: FitnessCalcEngine.calcBMR(user.weight, user.height, user.age, user.gender)
    val tdee = blueprint?.metrics?.tdee ?: FitnessCalcEngine.calcTDEE(bmr, user.daysPerWeek)
    val calorieSafety = remember(tdee, user.goal, bmr, user.weight, user.targetWeight, user.durationMonths) {
        FitnessCalcEngine.calcSafeCalories(
            tdee = tdee,
            goal = user.goal,
            bmr = bmr,
            weightKg = user.weight,
            targetWeightKg = user.targetWeight,
            durationMonths = user.durationMonths
        )
    }
    val targetCals = blueprint?.metrics?.goalTargetCals ?: calorieSafety.targetCals
    val targetProteinG = blueprint?.metrics?.macroTargets?.protein
        ?: FitnessCalcEngine.calcMacroTargets(targetCals, user.weight, user.goal).protein
    val safetyAdvisory = remember(user) { FitnessCalcEngine.evaluateSafetyAdvisory(user) }
    val warningMessage = when {
        safetyAdvisory.messages.isNotEmpty() -> safetyAdvisory.messages.first()
        blueprint?.metrics?.pace?.isWarning == true -> blueprint?.metrics?.pace?.message
        calorieSafety.note != null -> calorieSafety.note
        else -> null
    }
    val preferredTimeLabel = remember(user.preferredWorkoutMinutes) {
        WorkoutScheduleEngine.formatTimeMinutes(user.preferredWorkoutMinutes)
    }
    val coachFeedback = remember(user, blueprint, sessions, habits) {
        AuraCoachEngine.evaluate(
            profile = user,
            blueprint = blueprint,
            history = sessions,
            habits = habits
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 108.dp)
        ) {
            // 0. Header Greeting + Date
            item(key = "home_header") {
                AuraStaggeredEntrance(index = 0, reducedMotion = reducedMotion) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = todayDateLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.mutedText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$greetingPrefix, ${user.name.ifBlank { "Athlete" }}",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onOpenColorModal,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.surface)
                                .border(1.dp, colors.border, CircleShape)
                                .testTag("quick_color_modal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = "Accent color",
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 1. Today's Workout Hero Card
            if (todayPlan != null) {
                item(key = "home_today_workout") {
                    AuraStaggeredEntrance(index = 1, reducedMotion = reducedMotion) {
                        HomeTodayWorkoutCard(
                            dayPlan = todayPlan,
                            isCompleted = isTodayDone,
                            hasActiveSession = activeSession != null,
                            scheduledTimeText = preferredTimeLabel,
                            estimatedMinutes = user.sessionDurationMinutes,
                            onStartWorkout = {
                                val targetIdx = weekPlans.indexOfFirst { it.dayNumber == todayPlan.dayNumber }
                                    .takeIf { it >= 0 } ?: selectedDayIndex
                                viewModel.setSelectedWorkoutDay(targetIdx)
                                onNavigateToWorkout()
                            },
                            onMoveToTomorrow = { viewModel.moveTodayWorkoutToTomorrow() }
                        )
                    }
                }
            }

            // 2. Three Animated Rings (Calories, Protein, Water) + +250 ml quick log
            item(key = "home_rings") {
                AuraStaggeredEntrance(index = 2, reducedMotion = reducedMotion) {
                    HomeDailyRingsCard(
                        calories = todayHabit.loggedCalories,
                        targetCalories = targetCals,
                        proteinG = todayHabit.loggedProteinG,
                        targetProteinG = targetProteinG,
                        waterMl = todayHabit.waterMl,
                        targetWaterMl = targetWaterMl,
                        warningMessage = warningMessage,
                        reducedMotion = reducedMotion,
                        onQuickAddWater = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.addWaterMl(250, targetWaterMl)
                        }
                    )
                }
            }

            // 3. Streak Card with 7 Week Dots (tap to open Progress)
            item(key = "home_streak") {
                AuraStaggeredEntrance(index = 3, reducedMotion = reducedMotion) {
                    HomeStreakCard(
                        streakDays = streak,
                        workoutsDone = weeklySummary.workoutsDone,
                        workoutsPlanned = weeklySummary.workoutsPlanned,
                        weekDots = weekDots,
                        weighInDue = weighInDue,
                        deloadActive = user.deloadActive,
                        deloadOffered = deloadRec.shouldOffer,
                        onClick = onOpenProgress
                    )
                }
            }

            // 4. Coach Tip Card
            item(key = "home_coach_tip") {
                AuraStaggeredEntrance(index = 4, reducedMotion = reducedMotion) {
                    HomeCoachTipCard(
                        feedback = coachFeedback,
                        onClick = onOpenProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTodayWorkoutCard(
    dayPlan: WorkoutDayPlan,
    isCompleted: Boolean,
    hasActiveSession: Boolean,
    scheduledTimeText: String,
    estimatedMinutes: Int,
    onStartWorkout: () -> Unit,
    onMoveToTomorrow: () -> Unit
) {
    val colors = AuraTheme.colors
    val totalSets = remember(dayPlan.exercises) { dayPlan.exercises.sumOf { it.sets } }

    AuraCard(
        highlighted = !dayPlan.isRest,
        contentPadding = PaddingValues(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_workout_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (dayPlan.isRest) colors.mutedText else colors.accent)
                )
                Text(
                    text = if (dayPlan.isRest) "REST DAY" else "TODAY'S WORKOUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (dayPlan.isRest) colors.mutedText else colors.accent,
                    letterSpacing = 0.8.sp
                )
            }

            if (isCompleted) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Done",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                }
            } else if (!dayPlan.isRest) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.surface2)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = scheduledTimeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = dayPlan.focus,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (dayPlan.isRest) {
                "Recovery, hydration, and light walking today"
            } else {
                "${dayPlan.exercises.size} exercises • $totalSets sets • ~${estimatedMinutes} min"
            },
            fontSize = 14.sp,
            color = colors.secondaryText
        )

        Spacer(modifier = Modifier.height(18.dp))

        val ctaText = when {
            hasActiveSession -> "Resume workout"
            isCompleted -> "View workout"
            dayPlan.isRest -> "View recovery"
            else -> "Start workout"
        }

        AuraPillButton(
            text = ctaText,
            onClick = onStartWorkout,
            isPrimary = !dayPlan.isRest,
            modifier = Modifier.testTag("start_workout_cta")
        )

        if (!dayPlan.isRest && !isCompleted && !hasActiveSession) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onMoveToTomorrow)
                    .padding(vertical = 6.dp)
                    .testTag("move_workout_tomorrow_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Move today's workout to tomorrow",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.mutedText
                )
            }
        }
    }
}

@Composable
private fun HomeDailyRingsCard(
    calories: Int,
    targetCalories: Int,
    proteinG: Int,
    targetProteinG: Int,
    waterMl: Int,
    targetWaterMl: Int,
    warningMessage: String?,
    reducedMotion: Boolean,
    onQuickAddWater: () -> Unit
) {
    val colors = AuraTheme.colors
    val calProgress = if (targetCalories > 0) calories.toFloat() / targetCalories.toFloat() else 0f
    val proteinProgress = if (targetProteinG > 0) proteinG.toFloat() / targetProteinG.toFloat() else 0f
    val waterProgress = if (targetWaterMl > 0) waterMl.toFloat() / targetWaterMl.toFloat() else 0f

    val waterFormatted = remember(waterMl) {
        String.format(Locale.US, "%.2f", waterMl / 1000.0)
    }
    val waterTargetFormatted = remember(targetWaterMl) {
        String.format(Locale.US, "%.1fL", targetWaterMl / 1000.0)
    }

    AuraCard(
        contentPadding = PaddingValues(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_targets_rings_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today's fuel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )
                Text(
                    text = "Calories, protein & hydration",
                    fontSize = 12.sp,
                    color = colors.mutedText
                )
            }

            // +250 ml quick-log pill button
            Row(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .background(colors.waterBlue.copy(alpha = 0.16f))
                    .border(1.dp, colors.waterBlue.copy(alpha = 0.35f), CircleShape)
                    .clickable(onClick = onQuickAddWater)
                    .padding(horizontal = 14.dp, vertical = 9.dp)
                    .testTag("home_add_water_250_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add 250 ml water",
                    tint = colors.waterBlue,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "250 ml",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.waterBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            HomeRingMetric(
                label = "Calories",
                centerValue = "$calories",
                subLabel = "of $targetCalories kcal",
                progress = calProgress,
                color = colors.accent,
                reducedMotion = reducedMotion,
                modifier = Modifier.weight(1f)
            )
            HomeRingMetric(
                label = "Protein",
                centerValue = "${proteinG}g",
                subLabel = "of ${targetProteinG}g",
                progress = proteinProgress,
                color = colors.proteinGreen,
                reducedMotion = reducedMotion,
                modifier = Modifier.weight(1f)
            )
            HomeRingMetric(
                label = "Water",
                centerValue = "${waterFormatted}L",
                subLabel = "of $waterTargetFormatted",
                progress = waterProgress,
                color = colors.waterBlue,
                reducedMotion = reducedMotion,
                modifier = Modifier.weight(1f)
            )
        }

        if (!warningMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.warningCoral.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("home_calorie_pace_warning"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = colors.warningCoral,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = warningMessage,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = colors.primaryText
                )
            }
        }
    }
}

@Composable
private fun HomeRingMetric(
    label: String,
    centerValue: String,
    subLabel: String,
    progress: Float,
    color: Color,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuraRing(
            progress = progress,
            color = color,
            size = 74.dp,
            strokeWidth = 6.dp,
            reducedMotion = reducedMotion
        ) {
            Text(
                text = centerValue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.primaryText
        )
        Text(
            text = subLabel,
            fontSize = 11.sp,
            color = colors.mutedText,
            maxLines = 1
        )
    }
}

@Composable
private fun HomeStreakCard(
    streakDays: Int,
    workoutsDone: Int,
    workoutsPlanned: Int,
    weekDots: List<WeekDotDay>,
    weighInDue: Boolean,
    deloadActive: Boolean,
    deloadOffered: Boolean,
    onClick: () -> Unit
) {
    val colors = AuraTheme.colors

    AuraCard(
        contentPadding = PaddingValues(18.dp),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_streak_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.accentTint)
                        .border(1.dp, colors.accent.copy(alpha = 0.4f), CircleShape),
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
                        text = if (streakDays > 0) "$streakDays day streak" else "Build your streak",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryText
                    )
                    Text(
                        text = "$workoutsDone of $workoutsPlanned workouts this week",
                        fontSize = 12.5.sp,
                        color = colors.mutedText
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.testTag("home_open_progress_button")
            ) {
                Text(
                    text = "Progress",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open progress",
                    tint = colors.accent,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        WeekStreakDotsRow(weekDots = weekDots)

        val statusHint = when {
            deloadActive -> "Deload week active — volume trimmed 40% to recover."
            deloadOffered -> "Deload week suggested — tap Progress to ease volume."
            weighInDue -> "Weekly check-in due — tap to log your weight."
            else -> null
        }
        if (statusHint != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusHint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.accent
            )
        }
    }
}

@Composable
fun WeekStreakDotsRow(
    weekDots: List<WeekDotDay>,
    modifier: Modifier = Modifier
) {
    val colors = AuraTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        weekDots.forEach { dot ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                dot.isDone -> colors.accent
                                dot.isToday -> colors.accentTint
                                else -> colors.surface2
                            }
                        )
                        .border(
                            width = if (dot.isToday && !dot.isDone) 1.5.dp else 1.dp,
                            color = when {
                                dot.isDone -> colors.accent
                                dot.isToday -> colors.accent
                                dot.isPlanned -> colors.border
                                else -> Color.Transparent
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (dot.isDone) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = colors.accentContrast,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (dot.isToday) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                        )
                    }
                }
                Text(
                    text = dot.shortLabel,
                    fontSize = 11.sp,
                    fontWeight = if (dot.isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (dot.isToday || dot.isDone) colors.primaryText else colors.mutedText
                )
            }
        }
    }
}

@Composable
private fun HomeCoachTipCard(
    feedback: CoachFeedback,
    onClick: () -> Unit
) {
    val colors = AuraTheme.colors
    val indicatorColor = when (feedback.mood.category) {
        "Negative", "Concerned" -> colors.warningCoral
        "Recovery" -> colors.waterBlue
        "Positive", "Progress" -> colors.proteinGreen
        else -> colors.accent
    }

    AuraCard(
        contentPadding = PaddingValues(18.dp),
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_coach_card")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Text(
                    text = "COACH TIP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = indicatorColor,
                    letterSpacing = 0.8.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(indicatorColor.copy(alpha = 0.14f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = feedback.mood.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = indicatorColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = feedback.headline,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = feedback.message,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            color = colors.secondaryText
        )

        val action = feedback.suggestedAction ?: feedback.secondaryAdvice
        if (!action.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = action,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent
            )
        }
    }
}
