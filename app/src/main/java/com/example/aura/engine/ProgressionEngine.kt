package com.example.aura.engine

import com.example.aura.data.db.ExerciseLogEntity
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.Exercise
import com.example.aura.data.model.SetLog
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutSessionRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class ProgressionAction {
    BASELINE,
    MAINTAIN_PREFILL,
    INCREASE_WEIGHT,
    INCREASE_REPS,
    SUGGEST_VARIATION,
    REDUCE_WEIGHT,
    HOLD_REBUILD,
    DELOAD
}

data class ExerciseCoachingPlan(
    val action: ProgressionAction,
    val targetSets: Int,
    val prefilledSets: List<SetLog>,
    val shortNote: String,
    val hasChange: Boolean,
    val suggestedVariation: Exercise? = null,
    val lastSessionWeightKg: Double? = null,
    val lastSessionReps: Int? = null,
    val bestEstimated1RmKg: Double = 0.0,
    val bestWeightKg: Double = 0.0,
    val bestReps: Int = 0
)

data class DeloadRecommendation(
    val shouldOffer: Boolean,
    val reason: String,
    val triggerType: String // "hard_block", "missed_sessions", "none"
)

object ProgressionEngine {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Chain of bodyweight / calisthenics progressions matching ExerciseDB entries.
     */
    private val BODYWEIGHT_VARIATION_MAP: Map<String, String> = mapOf(
        "c3" to "t4",       // Standard Push-Up -> Diamond Push-Up
        "t4" to "cal_ap1",  // Diamond Push-Up -> Archer Push-Up
        "cal_ap1" to "cal_pp1", // Archer Push-Up -> Pseudo-Planche Push-Up
        "b2" to "cal_mu1",  // Pull-Up / Chin-Up -> Chest-to-Bar High Pull-Up
        "req_b2" to "cal_mu1", // Pull-Up -> Chest-to-Bar High Pull-Up
        "a6" to "cal_mu1",  // Chin-Up -> Chest-to-Bar High Pull-Up
        "cal_mu1" to "cal_mu2", // Chest-to-Bar High Pull-Up -> Muscle-Up Negatives
        "q4" to "q3",       // Bodyweight Air Squats -> Bulgarian Split Squat
        "q3" to "cal_ps1",  // Bulgarian Split Squat -> Box-Assisted Pistol Squat
        "cal_ps1" to "cal_ps2", // Box-Assisted Pistol Squat -> Pistol Squat
        "hm_gl1" to "hm_gl3", // Bodyweight Glute Bridge -> Bodyweight Hip Thrust
        "hm_gl3" to "hm_gl4", // Bodyweight Hip Thrust -> Single-Leg Hip Thrust
        "cr_b1" to "cal_hl1", // Hanging Knee / Leg Raise -> Hanging Straight-Leg Raise
        "cal_hl1" to "cal_hl2", // Hanging Straight-Leg Raise -> Hanging Windshield Wipers
        "cal_ls1" to "cal_ls2", // Tuck L-Sit Hold -> Full L-Sit Hold
        "cal_fl1" to "cal_fl2", // Tuck Front Lever -> Advanced Tuck Front Lever
        "cal_fl2" to "cal_fl3", // Advanced Tuck Front Lever -> Straddle Front Lever
        "s3" to "cal_hs1",  // Pike Push-Up -> Handstand Wall Hold
        "ca1" to "ca4",     // Standing Ledge Calf Raise -> Single-Leg Bodyweight Calf Raise
        "cr1" to "hm_cr3"   // Hollow Body Floor Hold -> Hollow Body Rock
    )

    fun parseRepRange(repsSpec: String, isTimed: Boolean = false): Pair<Int, Int> {
        val nums = Regex("\\d+").findAll(repsSpec).mapNotNull { it.value.toIntOrNull() }.toList()
        return when {
            nums.size >= 2 -> nums[0].coerceAtLeast(1) to nums[1].coerceAtLeast(nums[0])
            nums.size == 1 -> nums[0].coerceAtLeast(1) to nums[0].coerceAtLeast(1)
            isTimed -> 45 to 45
            else -> 8 to 12
        }
    }

    fun calculateEstimated1Rm(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0.0 || reps <= 0) return 0.0
        if (reps == 1) return roundOneDecimal(weightKg)
        val epley = weightKg * (1.0 + reps.toDouble() / 30.0)
        return roundOneDecimal(epley)
    }

    fun buildExerciseCoaching(
        exercise: BlueprintExercise,
        allLogs: List<ExerciseLogEntity>,
        isDeloadActive: Boolean = false,
        todayDateKey: String? = null
    ): ExerciseCoachingPlan {
        val isTimed = exercise.isTimedHold()
        val (minRep, maxRep) = parseRepRange(exercise.reps, isTimed)

        val matchingLogs = allLogs.filter { log ->
            log.completed && (log.exerciseId == exercise.id || log.exerciseName.equals(exercise.name, ignoreCase = true))
        }

        val todaySavedLogs = if (!todayDateKey.isNullOrBlank()) {
            matchingLogs.filter { it.date == todayDateKey }.sortedBy { it.setNumber }
        } else {
            emptyList()
        }

        val priorLogs = if (!todayDateKey.isNullOrBlank()) {
            matchingLogs.filter { it.date < todayDateKey }
        } else {
            matchingLogs
        }

        // Chronological sessions (by date + sessionId)
        val priorSessions = priorLogs
            .groupBy { it.date to it.sessionId }
            .entries
            .sortedWith(compareBy<Map.Entry<Pair<String, Long>, List<ExerciseLogEntity>>> { it.key.first }.thenBy { it.key.second })
            .map { entry -> entry.value.sortedBy { it.setNumber } }

        val overallBestSet = matchingLogs.maxWithOrNull(
            compareBy<ExerciseLogEntity> { calculateEstimated1Rm(it.weightKg, it.reps) }
                .thenBy { it.weightKg }
                .thenBy { it.reps }
        )
        val bestE1Rm = overallBestSet?.let { calculateEstimated1Rm(it.weightKg, it.reps) } ?: 0.0
        val bestW = overallBestSet?.weightKg ?: 0.0
        val bestR = overallBestSet?.reps ?: 0

        val deloadSets = max(1, (exercise.sets * 0.6f).roundToInt())
        val baseSetCount = if (isDeloadActive) deloadSets else exercise.sets

        if (priorSessions.isEmpty()) {
            val defaultReps = if (isTimed) exercise.targetHoldSeconds() else minRep
            val initialSets = buildPrefilledSetList(
                setCount = baseSetCount,
                prescribedReps = exercise.reps,
                fallbackWeightKg = exercise.recommendedLoadKg,
                fallbackReps = defaultReps,
                sourceSets = emptyList(),
                todayCompletedSets = todaySavedLogs
            )
            return ExerciseCoachingPlan(
                action = if (isDeloadActive) ProgressionAction.DELOAD else ProgressionAction.BASELINE,
                targetSets = baseSetCount,
                prefilledSets = initialSets,
                shortNote = if (isDeloadActive) {
                    "Deload week: ${baseSetCount} sets (-40% volume) to help you recover."
                } else "",
                hasChange = isDeloadActive,
                bestEstimated1RmKg = bestE1Rm,
                bestWeightKg = bestW,
                bestReps = bestR
            )
        }

        val lastSession = priorSessions.last()
        val prevSession = if (priorSessions.size >= 2) priorSessions[priorSessions.size - 2] else null
        val lastPrimaryWeight = lastSession.maxOfOrNull { it.weightKg } ?: exercise.recommendedLoadKg
        val lastBestReps = lastSession.maxOfOrNull { it.reps } ?: minRep
        val lastMinReps = lastSession.minOfOrNull { it.reps } ?: minRep

        if (isDeloadActive) {
            val sets = buildPrefilledSetList(
                setCount = deloadSets,
                prescribedReps = exercise.reps,
                fallbackWeightKg = lastPrimaryWeight,
                fallbackReps = lastBestReps,
                sourceSets = lastSession,
                todayCompletedSets = todaySavedLogs
            )
            return ExerciseCoachingPlan(
                action = ProgressionAction.DELOAD,
                targetSets = deloadSets,
                prefilledSets = sets,
                shortNote = "Deload week: ${deloadSets} sets (-40% volume) — keep weights crisp and sub-maximal.",
                hasChange = true,
                lastSessionWeightKg = lastPrimaryWeight,
                lastSessionReps = lastBestReps,
                bestEstimated1RmKg = bestE1Rm,
                bestWeightKg = bestW,
                bestReps = bestR
            )
        }

        val isDumbbellOnly = exercise.equipment.contains("Dumbbell", ignoreCase = true) &&
            !exercise.equipment.contains("Barbell", ignoreCase = true) &&
            !exercise.equipment.contains("Machine", ignoreCase = true) &&
            !exercise.equipment.contains("Cable", ignoreCase = true)

        val incrementKg = when {
            isDumbbellOnly && lastPrimaryWeight < 14.0 -> 1.0
            isDumbbellOnly -> 2.0
            else -> 2.5
        }

        val hitTopOnAllSets = lastSession.isNotEmpty() && lastSession.all { it.reps >= maxRep }
        val missedLowLastSession = lastSession.isNotEmpty() && lastSession.any { it.reps < minRep }
        val missedLowPrevSession = prevSession != null && prevSession.isNotEmpty() && prevSession.any { it.reps < minRep }

        // 1. Double progression: hit top of rep range on all sets
        if (hitTopOnAllSets) {
            if (!exercise.isBodyweight && lastPrimaryWeight > 0.0) {
                val nextWeight = roundToHalfKg(lastPrimaryWeight + incrementKg)
                val sets = buildUniformSetList(
                    setCount = baseSetCount,
                    weightKg = nextWeight,
                    reps = minRep,
                    prescribedReps = exercise.reps,
                    todayCompletedSets = todaySavedLogs
                )
                return ExerciseCoachingPlan(
                    action = ProgressionAction.INCREASE_WEIGHT,
                    targetSets = baseSetCount,
                    prefilledSets = sets,
                    shortNote = "Up ${formatKg(incrementKg)} kg: you hit $maxRep reps on all sets.",
                    hasChange = true,
                    lastSessionWeightKg = lastPrimaryWeight,
                    lastSessionReps = lastBestReps,
                    bestEstimated1RmKg = bestE1Rm,
                    bestWeightKg = bestW,
                    bestReps = bestR
                )
            } else {
                // Bodyweight progression: increase reps first, then suggest a harder variation
                val masteryRepThreshold = if (isTimed) maxRep + 15 else maxRep + 3
                val masteredReps = lastSession.all { it.reps >= masteryRepThreshold }
                val variation = if (masteredReps) findHarderVariation(exercise) else null

                if (variation != null) {
                    val sets = buildPrefilledSetList(
                        setCount = baseSetCount,
                        prescribedReps = exercise.reps,
                        fallbackWeightKg = 0.0,
                        fallbackReps = lastMinReps,
                        sourceSets = lastSession,
                        todayCompletedSets = todaySavedLogs
                    )
                    return ExerciseCoachingPlan(
                        action = ProgressionAction.SUGGEST_VARIATION,
                        targetSets = baseSetCount,
                        prefilledSets = sets,
                        shortNote = "Ready for a harder move: you hit $lastMinReps+ reps cleanly — try ${variation.name}.",
                        hasChange = true,
                        suggestedVariation = variation,
                        lastSessionWeightKg = lastPrimaryWeight,
                        lastSessionReps = lastBestReps,
                        bestEstimated1RmKg = bestE1Rm,
                        bestWeightKg = bestW,
                        bestReps = bestR
                    )
                } else {
                    val repBoost = if (isTimed) 5 else 2
                    val nextReps = lastMinReps + repBoost
                    val sets = buildUniformSetList(
                        setCount = baseSetCount,
                        weightKg = lastPrimaryWeight,
                        reps = nextReps,
                        prescribedReps = exercise.reps,
                        todayCompletedSets = todaySavedLogs
                    )
                    return ExerciseCoachingPlan(
                        action = ProgressionAction.INCREASE_REPS,
                        targetSets = baseSetCount,
                        prefilledSets = sets,
                        shortNote = if (isTimed) {
                            "Hold for ${nextReps}s: you completed all ${maxRep}s holds last session."
                        } else {
                            "Aim for $nextReps reps: you hit $maxRep reps on all sets last time."
                        },
                        hasChange = true,
                        lastSessionWeightKg = lastPrimaryWeight,
                        lastSessionReps = lastBestReps,
                        bestEstimated1RmKg = bestE1Rm,
                        bestWeightKg = bestW,
                        bestReps = bestR
                    )
                }
            }
        }

        // 2. Missed lower rep range two sessions in a row -> reduce or hold weight and explain cleanly
        if (missedLowLastSession && missedLowPrevSession) {
            if (!exercise.isBodyweight && lastPrimaryWeight >= 5.0) {
                val reducedWeight = roundToHalfKg((lastPrimaryWeight - incrementKg).coerceAtLeast(2.5))
                val actualDrop = roundToHalfKg(lastPrimaryWeight - reducedWeight)
                val note = if (actualDrop > 0.0) {
                    "Down ${formatKg(actualDrop)} kg: you missed $minRep reps two sessions in a row."
                } else {
                    "Keeping ${formatKg(lastPrimaryWeight)} kg: you missed $minRep reps two sessions in a row."
                }
                val sets = buildUniformSetList(
                    setCount = baseSetCount,
                    weightKg = reducedWeight,
                    reps = minRep,
                    prescribedReps = exercise.reps,
                    todayCompletedSets = todaySavedLogs
                )
                return ExerciseCoachingPlan(
                    action = if (actualDrop > 0.0) ProgressionAction.REDUCE_WEIGHT else ProgressionAction.HOLD_REBUILD,
                    targetSets = baseSetCount,
                    prefilledSets = sets,
                    shortNote = note,
                    hasChange = true,
                    lastSessionWeightKg = lastPrimaryWeight,
                    lastSessionReps = lastBestReps,
                    bestEstimated1RmKg = bestE1Rm,
                    bestWeightKg = bestW,
                    bestReps = bestR
                )
            } else {
                val targetRebuildReps = (lastBestReps).coerceIn(3, minRep)
                val sets = buildUniformSetList(
                    setCount = baseSetCount,
                    weightKg = lastPrimaryWeight,
                    reps = targetRebuildReps,
                    prescribedReps = exercise.reps,
                    todayCompletedSets = todaySavedLogs
                )
                return ExerciseCoachingPlan(
                    action = ProgressionAction.HOLD_REBUILD,
                    targetSets = baseSetCount,
                    prefilledSets = sets,
                    shortNote = "Keeping weight steady: you missed $minRep reps two sessions in a row — rebuild clean reps.",
                    hasChange = true,
                    lastSessionWeightKg = lastPrimaryWeight,
                    lastSessionReps = lastBestReps,
                    bestEstimated1RmKg = bestE1Rm,
                    bestWeightKg = bestW,
                    bestReps = bestR
                )
            }
        }

        // 3. Standard prefill with last used weight & reps
        val sets = buildPrefilledSetList(
            setCount = baseSetCount,
            prescribedReps = exercise.reps,
            fallbackWeightKg = lastPrimaryWeight,
            fallbackReps = lastBestReps,
            sourceSets = lastSession,
            todayCompletedSets = todaySavedLogs
        )
        return ExerciseCoachingPlan(
            action = ProgressionAction.MAINTAIN_PREFILL,
            targetSets = baseSetCount,
            prefilledSets = sets,
            shortNote = "",
            hasChange = false,
            lastSessionWeightKg = lastPrimaryWeight,
            lastSessionReps = lastBestReps,
            bestEstimated1RmKg = bestE1Rm,
            bestWeightKg = bestW,
            bestReps = bestR
        )
    }

    fun findHarderVariation(exercise: BlueprintExercise): Exercise? {
        val targetId = BODYWEIGHT_VARIATION_MAP[exercise.id] ?: when {
            exercise.name.equals("Standard Push-Up", true) || exercise.name.equals("Push-Up", true) -> "cal_ap1"
            exercise.name.contains("Diamond Push-Up", true) -> "cal_ap1"
            exercise.name.contains("Pull-Up", true) && !exercise.name.contains("Chest-to-Bar", true) -> "cal_mu1"
            exercise.name.contains("Air Squat", true) -> "q3"
            exercise.name.contains("Bulgarian Split Squat", true) -> "cal_ps1"
            else -> null
        } ?: return null
        return ExerciseDB.list.find { it.id == targetId }
    }

    fun evaluateDeloadNeed(
        sessions: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>,
        profile: UserProfile,
        todayKey: String = dateFormat.format(Date())
    ): DeloadRecommendation {
        if (profile.deloadActive) {
            return DeloadRecommendation(
                shouldOffer = false,
                reason = "Deload week is currently active (-40% sets).",
                triggerType = "active"
            )
        }

        val currentWeekKey = getIsoWeekKey(todayKey)
        if (profile.deloadDismissedAtWeekKey == currentWeekKey) {
            return DeloadRecommendation(false, "", "dismissed")
        }

        val completedDates = extractCompletedWorkoutDateKeys(sessions, habits).sorted()
        if (completedDates.size < 3) {
            return DeloadRecommendation(false, "", "none")
        }

        // Check gap since last workout (skipped several sessions)
        val latestDate = completedDates.last()
        val gapDays = daysBetween(latestDate, todayKey)
        if (gapDays >= 7) {
            return DeloadRecommendation(
                shouldOffer = true,
                reason = "You've been away for $gapDays days. Start back with an easier week (-40% volume).",
                triggerType = "missed_sessions"
            )
        }

        // Check if skipped >= 3 planned workout days in the last 14 days
        val plannedWeekdays = WorkoutScheduleEngine.resolveWorkoutDays(profile).toSet()
        val completedSet = completedDates.toSet()
        var missedPlannedLast14 = 0
        val cal = parseCal(todayKey)
        for (offset in 1..14) {
            val c = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
            val k = dateFormat.format(c.time)
            val wd = WorkoutScheduleEngine.calendarToWeekdayIndex(c)
            if (wd in plannedWeekdays && k !in completedSet && k >= profile.startDate) {
                missedPlannedLast14++
            }
        }
        if (missedPlannedLast14 >= 3) {
            return DeloadRecommendation(
                shouldOffer = true,
                reason = "Several sessions were missed recently. Take an easier week (-40% volume) to rebuild momentum.",
                triggerType = "missed_sessions"
            )
        }

        // Check 4-6 hard weeks without a deload
        val activeAfterDeload = if (profile.deloadStartDateKey.isNotBlank()) {
            completedDates.filter { it >= profile.deloadStartDateKey }
        } else {
            completedDates
        }
        val distinctWeeks = activeAfterDeload.map { getIsoWeekKey(it) }.distinct().size
        val minSessionsNeeded = (profile.daysPerWeek.coerceAtLeast(2) * 4).coerceAtLeast(10)
        if (distinctWeeks >= 4 && activeAfterDeload.size >= minSessionsNeeded) {
            return DeloadRecommendation(
                shouldOffer = true,
                reason = "You've pushed hard for $distinctWeeks weeks straight. Take a deload week (-40% volume) to recover and grow stronger.",
                triggerType = "hard_block"
            )
        }

        return DeloadRecommendation(false, "", "none")
    }

    fun extractCompletedWorkoutDateKeys(
        sessions: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>
    ): Set<String> {
        val keys = mutableSetOf<String>()
        habits.forEach { (dateKey, habit) ->
            if (habit.workout && dateKey.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                keys.add(dateKey)
            }
        }
        sessions.filter { it.setsCompleted > 0 || it.isFullyCompleted }.forEach { rec ->
            normalizeDateKey(rec.date)?.let { keys.add(it) }
        }
        return keys
    }

    fun normalizeDateKey(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}.*"))) {
            return trimmed.take(10)
        }
        val parsers = listOf(
            SimpleDateFormat("MMM d, yyyy", Locale.US),
            SimpleDateFormat("EEE, MMM d", Locale.US)
        )
        for (p in parsers) {
            try {
                val parsed = p.parse(trimmed) ?: continue
                return dateFormat.format(parsed)
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun getIsoWeekKey(dateKey: String): String {
        val cal = parseCal(dateKey)
        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        return String.format(Locale.US, "%04d-W%02d", year, week)
    }

    fun daysBetween(fromDateKey: String, toDateKey: String): Int {
        val from = parseCal(fromDateKey)
        val to = parseCal(toDateKey)
        from.set(Calendar.HOUR_OF_DAY, 0)
        from.set(Calendar.MINUTE, 0)
        from.set(Calendar.SECOND, 0)
        from.set(Calendar.MILLISECOND, 0)
        to.set(Calendar.HOUR_OF_DAY, 0)
        to.set(Calendar.MINUTE, 0)
        to.set(Calendar.SECOND, 0)
        to.set(Calendar.MILLISECOND, 0)
        return ((to.timeInMillis - from.timeInMillis) / (1000L * 60L * 60L * 24L)).toInt()
    }

    private fun parseCal(dateKey: String): Calendar {
        return Calendar.getInstance().apply {
            try {
                time = dateFormat.parse(dateKey.take(10)) ?: Date()
            } catch (_: Exception) {
                time = Date()
            }
        }
    }

    private fun buildPrefilledSetList(
        setCount: Int,
        prescribedReps: String,
        fallbackWeightKg: Double,
        fallbackReps: Int,
        sourceSets: List<ExerciseLogEntity>,
        todayCompletedSets: List<ExerciseLogEntity>
    ): List<SetLog> {
        return List(setCount) { idx ->
            val setNumber = idx + 1
            val todayDone = todayCompletedSets.find { it.setNumber == setNumber }
            val source = sourceSets.find { it.setNumber == setNumber } ?: sourceSets.getOrNull(idx) ?: sourceSets.lastOrNull()
            val w = todayDone?.weightKg ?: source?.weightKg ?: fallbackWeightKg
            val r = todayDone?.reps ?: source?.reps ?: fallbackReps
            SetLog(
                setNumber = setNumber,
                prescribedWeightKg = w,
                prescribedReps = prescribedReps,
                completed = todayDone?.completed ?: false,
                loggedWeightKg = w,
                loggedReps = r
            )
        }
    }

    private fun buildUniformSetList(
        setCount: Int,
        weightKg: Double,
        reps: Int,
        prescribedReps: String,
        todayCompletedSets: List<ExerciseLogEntity>
    ): List<SetLog> {
        return List(setCount) { idx ->
            val setNumber = idx + 1
            val todayDone = todayCompletedSets.find { it.setNumber == setNumber }
            SetLog(
                setNumber = setNumber,
                prescribedWeightKg = weightKg,
                prescribedReps = prescribedReps,
                completed = todayDone?.completed ?: false,
                loggedWeightKg = todayDone?.weightKg ?: weightKg,
                loggedReps = todayDone?.reps ?: reps
            )
        }
    }

    private fun roundToHalfKg(value: Double): Double = (value * 2.0).roundToInt() / 2.0

    private fun roundOneDecimal(value: Double): Double = (value * 10.0).roundToInt() / 10.0

    fun formatKg(kg: Double): String {
        return if (kg % 1.0 == 0.0) kg.toInt().toString() else String.format(Locale.US, "%.1f", kg)
    }
}
