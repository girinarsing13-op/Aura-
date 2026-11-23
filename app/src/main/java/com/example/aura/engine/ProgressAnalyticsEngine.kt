package com.example.aura.engine

import com.example.aura.data.db.ExerciseLogEntity
import com.example.aura.data.db.MealLogEntity
import com.example.aura.data.model.Blueprint
import com.example.aura.data.model.BodyCheckIn
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutSessionRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class WeightTrendPoint(
    val dateKey: String,
    val label: String,
    val weightKg: Double
)

data class ExerciseStrengthPoint(
    val dateKey: String,
    val label: String,
    val estimated1RmKg: Double,
    val bestWeightKg: Double,
    val bestReps: Int,
    val totalVolumeKg: Double
)

data class ExercisePersonalBest(
    val exerciseId: String,
    val exerciseName: String,
    val bestWeightKg: Double,
    val bestReps: Int,
    val estimated1RmKg: Double,
    val dateKey: String
)

data class WeeklySummaryData(
    val isSunday: Boolean,
    val weekLabel: String,
    val workoutsDone: Int,
    val workoutsPlanned: Int,
    val totalVolumeKg: Double,
    val proteinConsistencyPct: Int,
    val waterConsistencyPct: Int,
    val coachInsight: String,
    val nextWeekFocus: String,
    val weeklyNutrition: WeeklyNutritionSummary = WeeklyNutritionSummary(
        days = emptyList(),
        avgCalories = 0,
        avgProteinG = 0,
        targetCalories = 2200,
        targetProteinG = 140,
        loggedDaysCount = 0
    )
)

data class HeatmapDayCell(
    val dateKey: String,
    val intensity: Int, // 0 = empty, 1 = diet/water habit, 2 = partial session, 3 = full workout
    val isPlannedRest: Boolean,
    val isToday: Boolean
)

object ProgressAnalyticsEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val shortLabelFormat = SimpleDateFormat("MMM d", Locale.US)

    fun formatShortLabel(dateKey: String): String {
        return try {
            val d = dateFormat.parse(dateKey.take(10)) ?: return dateKey
            shortLabelFormat.format(d)
        } catch (_: Exception) {
            dateKey
        }
    }

    fun buildWeightTrend(
        checkIns: List<BodyCheckIn>,
        daysWindow: Int,
        todayKey: String = dateFormat.format(Date())
    ): List<WeightTrendPoint> {
        val sorted = checkIns
            .groupBy { it.dateKey }
            .mapNotNull { (dateKey, entries) ->
                entries.maxByOrNull { it.timestampMillis }?.let { dateKey to it }
            }
            .sortedBy { it.first }

        return sorted
            .filter { (dateKey, _) ->
                val diff = ProgressionEngine.daysBetween(dateKey, todayKey)
                diff in 0..daysWindow
            }
            .map { (dateKey, entry) ->
                WeightTrendPoint(
                    dateKey = dateKey,
                    label = formatShortLabel(dateKey),
                    weightKg = entry.weightKg
                )
            }
    }

    fun getLoggedExerciseNames(logs: List<ExerciseLogEntity>): List<String> {
        return logs
            .filter { it.completed }
            .groupBy { it.exerciseName }
            .entries
            .sortedByDescending { it.value.size }
            .map { it.key }
    }

    fun buildExerciseStrengthSeries(
        exerciseName: String,
        logs: List<ExerciseLogEntity>
    ): List<ExerciseStrengthPoint> {
        val filtered = logs.filter {
            it.completed && it.exerciseName.equals(exerciseName, ignoreCase = true)
        }
        return filtered
            .groupBy { it.date }
            .entries
            .sortedBy { it.key }
            .map { (dateKey, sets) ->
                val bestSet = sets.maxWithOrNull(
                    compareBy<ExerciseLogEntity> { ProgressionEngine.calculateEstimated1Rm(it.weightKg, it.reps) }
                        .thenBy { it.weightKg }
                        .thenBy { it.reps }
                ) ?: sets.first()
                val e1Rm = ProgressionEngine.calculateEstimated1Rm(bestSet.weightKg, bestSet.reps)
                val vol = sets.sumOf { it.weightKg * it.reps.toDouble() }
                ExerciseStrengthPoint(
                    dateKey = dateKey,
                    label = formatShortLabel(dateKey),
                    estimated1RmKg = if (e1Rm > 0.0) e1Rm else bestSet.reps.toDouble(),
                    bestWeightKg = bestSet.weightKg,
                    bestReps = bestSet.reps,
                    totalVolumeKg = vol
                )
            }
    }

    fun buildPersonalBests(logs: List<ExerciseLogEntity>): List<ExercisePersonalBest> {
        return logs
            .filter { it.completed && (it.weightKg > 0.0 || it.reps > 0) }
            .groupBy { it.exerciseName.trim().lowercase(Locale.ROOT) }
            .values
            .mapNotNull { group ->
                val best = group.maxWithOrNull(
                    compareBy<ExerciseLogEntity> { ProgressionEngine.calculateEstimated1Rm(it.weightKg, it.reps) }
                        .thenBy { it.weightKg }
                        .thenBy { it.reps }
                        .thenBy { it.date }
                ) ?: return@mapNotNull null

                ExercisePersonalBest(
                    exerciseId = best.exerciseId,
                    exerciseName = best.exerciseName,
                    bestWeightKg = best.weightKg,
                    bestReps = best.reps,
                    estimated1RmKg = ProgressionEngine.calculateEstimated1Rm(best.weightKg, best.reps),
                    dateKey = best.date
                )
            }
            .sortedWith(compareByDescending<ExercisePersonalBest> { it.dateKey }.thenByDescending { it.estimated1RmKg })
    }

    fun isNewPersonalBestSet(
        exerciseName: String,
        weightKg: Double,
        reps: Int,
        existingLogs: List<ExerciseLogEntity>,
        todayKey: String
    ): Boolean {
        if (reps <= 0) return false
        val priorLogs = existingLogs.filter {
            it.completed && it.exerciseName.equals(exerciseName, ignoreCase = true) && it.date < todayKey
        }
        // Only celebrate when beating a real previous record
        if (priorLogs.isEmpty()) return false
        val newE1Rm = ProgressionEngine.calculateEstimated1Rm(weightKg, reps)
        val bestPrior = priorLogs.maxWithOrNull(
            compareBy<ExerciseLogEntity> { ProgressionEngine.calculateEstimated1Rm(it.weightKg, it.reps) }
                .thenBy { it.weightKg }
                .thenBy { it.reps }
        ) ?: return false

        val bestPriorE1Rm = ProgressionEngine.calculateEstimated1Rm(bestPrior.weightKg, bestPrior.reps)
        return when {
            weightKg > 0.0 && bestPriorE1Rm > 0.0 -> newE1Rm > bestPriorE1Rm || weightKg > bestPrior.weightKg
            else -> reps > bestPrior.reps
        }
    }

    /**
     * Builds the Sunday / Current Week summary with workouts done vs planned, total volume,
     * protein & water consistency %, one plain coach insight, and one focus for next week.
     */
    fun buildWeeklySummary(
        profile: UserProfile,
        blueprint: Blueprint?,
        sessions: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>,
        exerciseLogs: List<ExerciseLogEntity>,
        mealLogs: List<MealLogEntity> = emptyList(),
        nowCal: Calendar = Calendar.getInstance()
    ): WeeklySummaryData {
        val isSunday = nowCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        val daysToMonday = (WorkoutScheduleEngine.calendarToWeekdayIndex(nowCal) - 1).coerceIn(0, 6)
        val mondayCal = (nowCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -daysToMonday)
        }

        val weekDateKeys = (0..daysToMonday).map { offset ->
            val c = (mondayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            dateFormat.format(c.time)
        }
        val weekSet = weekDateKeys.toSet()

        val completedDatesThisWeek = ProgressionEngine.extractCompletedWorkoutDateKeys(sessions, habits)
            .filter { it in weekSet }
            .toSet()

        val workoutsDone = completedDatesThisWeek.size
        val workoutsPlanned = profile.daysPerWeek.coerceIn(2, 7)

        val totalVolumeKg = exerciseLogs
            .filter { it.completed && it.date in weekSet }
            .sumOf { it.weightKg * it.reps.toDouble() }

        val targetProteinG = blueprint?.metrics?.macroTargets?.protein
            ?: FitnessCalcEngine.calcMacroTargets(
                blueprint?.metrics?.goalTargetCals ?: 2200,
                profile.weight,
                profile.goal
            ).protein
        val restWaterTargetMl = FitnessCalcEngine.calcWaterTargetMl(profile.weight, isWorkoutDay = false)
        val trainWaterTargetMl = FitnessCalcEngine.calcWaterTargetMl(profile.weight, isWorkoutDay = true)

        val daysElapsed = weekDateKeys.size.coerceAtLeast(1)
        val proteinHitDays = weekDateKeys.count { k ->
            val h = habits[k]
            h != null && (h.diet || (targetProteinG > 0 && h.loggedProteinG >= (targetProteinG * 0.8).toInt()))
        }
        val waterHitDays = weekDateKeys.count { k ->
            val h = habits[k]
            val target = if (k in completedDatesThisWeek) trainWaterTargetMl else restWaterTargetMl
            h != null && (h.water || h.waterMl >= (target * 0.8).toInt())
        }

        val proteinConsistencyPct = ((proteinHitDays * 100.0) / daysElapsed).roundToInt().coerceIn(0, 100)
        val waterConsistencyPct = ((waterHitDays * 100.0) / daysElapsed).roundToInt().coerceIn(0, 100)

        val coachInsight = when {
            workoutsDone == 0 ->
                "No workouts logged this week yet — even one short session gets momentum back."
            workoutsDone >= workoutsPlanned && proteinConsistencyPct >= 75 ->
                "Strong week: you hit all $workoutsDone planned sessions and stayed steady on protein."
            workoutsDone >= workoutsPlanned ->
                "You completed every planned workout ($workoutsDone/$workoutsPlanned) — training consistency is dialed in."
            workoutsDone >= (workoutsPlanned / 2.0) ->
                "Solid effort with $workoutsDone of $workoutsPlanned sessions done and ${ProgressionEngine.formatKg(totalVolumeKg)} kg moved."
            else ->
                "Training dipped to $workoutsDone of $workoutsPlanned planned sessions this week."
        }

        val nextWeekFocus = when {
            waterConsistencyPct < 60 ->
                "Hit your daily water target early each day (${String.format(Locale.US, "%.2f", restWaterTargetMl / 1000.0)} L+)."
            proteinConsistencyPct < 60 ->
                "Prioritize hitting ${targetProteinG}g protein on training days to support recovery."
            workoutsDone < workoutsPlanned ->
                "Lock in ${workoutsPlanned} scheduled sessions — move missed days rather than skipping."
            else ->
                "Keep pushing top sets cleanly toward the top of your rep range next week."
        }

        val weeklyNutrition = NutritionTrackingEngine.buildWeeklyNutritionSummary(
            profile = profile,
            blueprint = blueprint,
            mealLogs = mealLogs,
            habits = habits,
            nowCal = nowCal
        )
        val startLabel = formatShortLabel(weekDateKeys.first())
        val endLabel = formatShortLabel(weekDateKeys.last())
        return WeeklySummaryData(
            isSunday = isSunday,
            weekLabel = "$startLabel – $endLabel",
            workoutsDone = workoutsDone,
            workoutsPlanned = workoutsPlanned,
            totalVolumeKg = totalVolumeKg,
            proteinConsistencyPct = proteinConsistencyPct,
            waterConsistencyPct = waterConsistencyPct,
            coachInsight = coachInsight,
            nextWeekFocus = nextWeekFocus,
            weeklyNutrition = weeklyNutrition
        )
    }

    /**
     * Generates a 12-week (84-day) consistency heatmap aligned in 7-row (Mon..Sun) columns.
     */
    fun build12WeekHeatmap(
        profile: UserProfile,
        sessions: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>,
        todayCal: Calendar = Calendar.getInstance()
    ): List<List<HeatmapDayCell>> {
        val todayKey = dateFormat.format(todayCal.time)
        val plannedDays = WorkoutScheduleEngine.resolveWorkoutDays(profile).toSet()
        val fullDates = ProgressionEngine.extractCompletedWorkoutDateKeys(sessions, habits)
        val partialDates = sessions
            .filter { !it.isFullyCompleted && it.setsCompleted > 0 }
            .mapNotNull { ProgressionEngine.normalizeDateKey(it.date) }
            .toSet()

        // Find Monday of current week, then step back 11 weeks so total = 12 weeks (84 slots)
        val todayWeekdayOffset = (WorkoutScheduleEngine.calendarToWeekdayIndex(todayCal) - 1).coerceIn(0, 6)
        val startMondayCal = (todayCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -todayWeekdayOffset - (11 * 7))
        }

        return (0 until 12).map { weekIdx ->
            (0 until 7).map { dayIdx ->
                val c = (startMondayCal.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, weekIdx * 7 + dayIdx)
                }
                val key = dateFormat.format(c.time)
                val wd = WorkoutScheduleEngine.calendarToWeekdayIndex(c)
                val habit = habits[key]
                val intensity = when {
                    key > todayKey -> 0
                    key in fullDates || habit?.workout == true -> 3
                    key in partialDates -> 2
                    habit != null && (habit.diet || habit.water || habit.waterMl >= 500) -> 1
                    else -> 0
                }
                HeatmapDayCell(
                    dateKey = key,
                    intensity = intensity,
                    isPlannedRest = wd !in plannedDays,
                    isToday = key == todayKey
                )
            }
        }
    }

    /**
     * Streak allowing 1 rest day without breaking, evaluated strictly against the user's planned training days.
     * - Planned rest days do not break the streak.
     * - 1 missed planned workout day is absorbed by the 1-day rest grace; a 2nd missed planned day breaks the streak.
     */
    fun calculateSmartStreak(
        profile: UserProfile,
        sessions: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>,
        todayCal: Calendar = Calendar.getInstance()
    ): Int {
        val completedDates = ProgressionEngine.extractCompletedWorkoutDateKeys(sessions, habits)
        if (completedDates.isEmpty()) return 0

        val plannedWeekdays = WorkoutScheduleEngine.resolveWorkoutDays(profile).toSet()
        val todayKey = dateFormat.format(todayCal.time)

        var streak = 0
        var consecutiveMissedPlannedDays = 0
        var usedGraceRestDay = false

        // Today counts if done; if not done yet today, don't penalize yet.
        val todayDone = todayKey in completedDates || habits[todayKey]?.let { it.workout } == true
        if (todayDone) {
            streak++
        }

        val checkCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        for (i in 0 until 120) {
            val key = dateFormat.format(checkCal.time)
            val wd = WorkoutScheduleEngine.calendarToWeekdayIndex(checkCal)
            val isPlannedWorkout = wd in plannedWeekdays
            val habit = habits[key]
            val workoutDone = key in completedDates || habit?.workout == true

            if (workoutDone) {
                streak++
                consecutiveMissedPlannedDays = 0
            } else if (isPlannedWorkout) {
                consecutiveMissedPlannedDays++
                if (!usedGraceRestDay && consecutiveMissedPlannedDays == 1) {
                    // Allow 1 missed planned workout day without breaking
                    usedGraceRestDay = true
                } else {
                    break
                }
            } else {
                // Planned rest day: preserves streak without breaking
            }

            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return streak
    }

    fun isWeighInDue(
        checkIns: List<BodyCheckIn>,
        todayKey: String = dateFormat.format(Date())
    ): Boolean {
        val latest = checkIns.maxByOrNull { it.dateKey } ?: return true
        return ProgressionEngine.daysBetween(latest.dateKey, todayKey) >= 7
    }
}
