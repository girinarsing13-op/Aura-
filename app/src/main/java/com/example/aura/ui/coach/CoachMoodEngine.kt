package com.example.aura.ui.coach

import com.example.aura.data.model.Blueprint
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.SetLog
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutDayPlan
import com.example.aura.data.model.WorkoutSessionRecord
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Real Emotional Coach System for AURA
 * Evaluates real biometric, workload, consistency, recovery, and progression data.
 * Does NOT praise fake progress. Does NOT manufacture arbitrary numbers or cheerful platitudes.
 */
object CoachMoodEngine {
    fun evaluateUserState(
        profile: UserProfile?,
        blueprint: Blueprint?,
        history: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>,
        isSessionActive: Boolean = false,
        sessionLogs: Map<Int, List<SetLog>> = emptyMap(),
        activeDayNumber: Int = 1
    ): CoachFeedback {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // 1. REAL-TIME ACTIVE SESSION CHECKS (Priority 1)
        if (isSessionActive && sessionLogs.isNotEmpty() && blueprint != null) {
            val liveSafety = evaluateExerciseProgress(sessionLogs, blueprint, activeDayNumber, history)
            if (liveSafety != null) return liveSafety

            return CoachFeedback(
                mood = CoachMood.DETERMINED,
                expression = "determined",
                headline = "Session in Progress",
                message = "Focus on controlled execution, tempo, and clean mechanical tension.",
                reason = "Active workout session currently logged.",
                severity = "none",
                suggestedAction = "Record each set immediately after completion.",
                tag = "ACTIVE SESSION",
                priority = 1
            )
        }

        // 2. UNREALISTIC GOAL TIMELINE CHECK (Priority 2)
        val goalFeedback = evaluateGoalReality(profile)
        if (goalFeedback != null) return goalFeedback

        // 3. UNHEALTHY STREAK / RECOVERY CONFLICT CHECK (Priority 3)
        val streakSafety = evaluateStreakSafety(history, todayKey)
        if (streakSafety != null) return streakSafety

        // 4. EARNED STREAK MILESTONES (Priority 4)
        val earnedStreak = evaluateStreak(history, todayKey)
        if (earnedStreak != null) return earnedStreak

        // 5. TODAY'S WORKOUT EVALUATION (Priority 5)
        val todayWorkoutFeedback = evaluateWorkout(history, blueprint, todayKey)
        if (todayWorkoutFeedback != null) return todayWorkoutFeedback

        // 6. STRENGTH & REPETITION TREND / STAGNATION (Priority 6)
        val strengthTrendFeedback = evaluateStrengthTrend(history)
        if (strengthTrendFeedback != null) return strengthTrendFeedback

        // 7. CONSISTENCY & BREAK CHECK (Priority 7)
        val consistencyFeedback = evaluateConsistency(history, blueprint)
        if (consistencyFeedback != null) return consistencyFeedback

        // 8. REST DAY & RECOVERY STATUS (Priority 8)
        val recoveryFeedback = evaluateRecovery(blueprint, activeDayNumber, habits, todayKey)
        if (recoveryFeedback != null) return recoveryFeedback

        // 9. HABIT & SLEEP IMPACT (Priority 9)
        val sleepFeedback = evaluateSleep(habits, todayKey)
        if (sleepFeedback != null) return sleepFeedback

        val hydrationFeedback = evaluateHydration(habits, todayKey)
        if (hydrationFeedback != null) return hydrationFeedback

        val nutritionFeedback = evaluateNutrition(habits, todayKey)
        if (nutritionFeedback != null) return nutritionFeedback

        // 10. DAILY BASELINE STATE / NO DATA CHECK (Priority 10)
        return evaluateDailyState(history, blueprint, activeDayNumber, profile)
    }

    /**
     * Checks if goal target and timeframe are realistic.
     */
    fun evaluateGoalReality(profile: UserProfile?): CoachFeedback? {
        if (profile != null && profile.targetWeight != null) {
            val weightDiff = abs(profile.targetWeight - profile.weight)
            val months = profile.durationMonths.coerceAtLeast(1)
            val rateKgPerMonth = weightDiff / months

            // Trying to shift > 4.5kg per month is dangerously aggressive
            if (rateKgPerMonth > 4.5 && weightDiff >= 5.0) {
                return CoachFeedback(
                    mood = CoachMood.SKEPTICAL,
                    expression = "skeptical",
                    headline = "Ambitious Target Pace",
                    message = "That's ambitious. The timeline may be too aggressive.",
                    reason = "Target pace of ${String.format(Locale.US, "%.1f", rateKgPerMonth)} kg/month exceeds safe physiological adaptation rate of 4.0 kg/month.",
                    severity = "medium",
                    suggestedAction = "Let's aim for measurable progress rather than rushing the result.",
                    tag = "GOAL REALITY",
                    priority = 2
                )
            }
        }
        return null
    }

    /**
     * Evaluates live exercise progression or dangerous weight jumps.
     */
    fun evaluateExerciseProgress(
        sessionLogs: Map<Int, List<SetLog>>,
        blueprint: Blueprint,
        activeDayNumber: Int,
        history: List<WorkoutSessionRecord>
    ): CoachFeedback? {
        val week = blueprint.workout.week
        val day = week.find { it.dayNumber == activeDayNumber } ?: return null

        for ((exIdx, sets) in sessionLogs) {
            val ex = day.exercises.getOrNull(exIdx)
            val exPrescribed = ex?.recommendedLoadKg ?: 0.0

            sets.forEach { set ->
                val base = if (set.prescribedWeightKg > 0.0) set.prescribedWeightKg else exPrescribed
                val logged = set.loggedWeightKg

                // Dangerous excessive sudden jump (> 35% jump on uncompleted/failed sets)
                if (!set.completed && logged != null && base > 0.0 && logged > base) {
                    val pctIncrease = (logged - base) / base
                    if (pctIncrease >= 0.35) {
                        return CoachFeedback(
                            mood = CoachMood.WORRIED,
                            expression = "worried",
                            headline = "Excessive Load Jump",
                            message = "That's a big jump. Don't sacrifice form just to move more weight.",
                            reason = "+${(pctIncrease * 100).roundToInt()}% sudden weight increase on ${ex?.name ?: "exercise"}.",
                            severity = "high",
                            suggestedAction = "Earn the next weight gradually. Step back down to +5-10% to protect joint integrity.",
                            tag = "SAFETY WARNING",
                            priority = 1
                        )
                    }
                }
            }

            // Real Personal Record check
            val maxLoggedThisSession = sets.mapNotNull { it.loggedWeightKg }.maxOrNull()
            if (maxLoggedThisSession != null && maxLoggedThisSession > 0.0) {
                val historicalMax = history.filter { it.dayTitle.contains(ex?.name ?: "___", ignoreCase = true) }
                    .maxOfOrNull { it.setsCompleted * 1.0 } ?: 0.0
                if (maxLoggedThisSession > exPrescribed * 1.15 && maxLoggedThisSession > historicalMax) {
                    return CoachFeedback(
                        mood = CoachMood.CELEBRATING,
                        expression = "celebrating",
                        headline = "New PR Verified",
                        message = "NEW PR. You earned that one.",
                        reason = "Logged ${maxLoggedThisSession}kg exceeds previous working weight baseline (${exPrescribed}kg).",
                        severity = "none",
                        suggestedAction = "Maintain strict tempo and recover properly.",
                        tag = "PERSONAL RECORD",
                        priority = 1
                    )
                }
            }

            // Small success: 1 more rep or incremental weight
            val completedSets = sets.filter { it.completed }
            if (completedSets.isNotEmpty()) {
                val lastCompleted = completedSets.last()
                val loggedReps = lastCompleted.loggedReps
                val prescribedReps = ex?.reps?.split("-")?.firstOrNull()?.trim()?.toIntOrNull() ?: 10
                if (loggedReps != null && loggedReps == prescribedReps + 1) {
                    return CoachFeedback(
                        mood = CoachMood.PLEASED,
                        expression = "pleased",
                        headline = "Rep Increment",
                        message = "One more rep. That's progress.",
                        reason = "Exceeded prescribed rep target by 1 clean repetition.",
                        severity = "none",
                        suggestedAction = "Keep building momentum across remaining sets.",
                        tag = "MICRO PROGRESS",
                        priority = 1
                    )
                }
            }
        }
        return null
    }

    /**
     * Evaluates if streak is dangerous/unhealthy (e.g. 7+ days with 0 rest).
     */
    private fun evaluateStreakSafety(history: List<WorkoutSessionRecord>, todayKey: String): CoachFeedback? {
        val streak = calculateConsecutiveWorkoutDays(history, todayKey)
        // If user trains > 7 consecutive days with zero rest, warn them!
        if (streak > 7) {
            val last7DaysHasRest = history.takeLast(7).any { it.dayTitle.contains("Rest", ignoreCase = true) }
            if (!last7DaysHasRest) {
                return CoachFeedback(
                    mood = CoachMood.WORRIED,
                    expression = "worried",
                    headline = "Recovery Deficit",
                    message = "Your consistency is good. But your recovery isn't.",
                    reason = "$streak consecutive days of heavy lifting without scheduled systemic rest.",
                    severity = "high",
                    suggestedAction = "Take a scheduled rest day. Muscle protein synthesis occurs during recovery.",
                    tag = "OVERTRAINING RISK",
                    priority = 3
                )
            }
        }
        return null
    }

    /**
     * Evaluates today's completed workout with honest appraisal.
     */
    fun evaluateWorkout(
        history: List<WorkoutSessionRecord>,
        blueprint: Blueprint?,
        todayKey: String
    ): CoachFeedback? {
        val todaySessions = history.filter { it.date.startsWith(todayKey) && it.isFullyCompleted }
        if (todaySessions.isEmpty()) return null

        val latest = todaySessions.last()

        // Multiple signals positive evaluation
        val recentCompleted = history.count { it.isFullyCompleted }
        val allSetsHit = latest.setsCompleted >= latest.totalSets && latest.totalSets > 0

        return when {
            allSetsHit && recentCompleted >= 8 -> {
                CoachFeedback(
                    mood = CoachMood.PROUD,
                    expression = "proud",
                    headline = "Intelligent Overload",
                    message = "You're not just showing up. You're progressing intelligently.",
                    reason = "Completed all ${latest.totalSets} prescribed sets with consistent weekly adherence.",
                    severity = "none",
                    suggestedAction = "Hydrate and hit your target protein threshold.",
                    tag = "SESSION VERIFIED",
                    priority = 4
                )
            }
            allSetsHit -> {
                CoachFeedback(
                    mood = CoachMood.HAPPY,
                    expression = "happy",
                    headline = "Session Completed",
                    message = "That's a solid session. All sets completed as prescribed.",
                    reason = "100% prescribed volume completed (${latest.setsCompleted}/${latest.totalSets} sets).",
                    severity = "none",
                    suggestedAction = "Log your nutrition fuel and rest.",
                    tag = "SESSION VERIFIED",
                    priority = 4
                )
            }
            else -> {
                CoachFeedback(
                    mood = CoachMood.OBSERVING,
                    expression = "observing",
                    headline = "Partial Volume Logged",
                    message = "Session concluded. ${latest.setsCompleted} of ${latest.totalSets} sets logged.",
                    reason = "Partial volume completion.",
                    severity = "low",
                    suggestedAction = "Ensure rest is adequate before the next scheduled session.",
                    tag = "VOLUME NOTE",
                    priority = 4
                )
            }
        }
    }

    /**
     * Evaluates plateau, progression, or regression across recent sessions.
     */
    fun evaluateStrengthTrend(history: List<WorkoutSessionRecord>): CoachFeedback? {
        val completed = history.filter { it.isFullyCompleted }
        if (completed.size < 2) return null

        val recent = completed.takeLast(5)

        // 1. Check for performance regression
        if (recent.size >= 2) {
            val last = recent.last()
            val prev = recent[recent.size - 2]
            if (last.totalSets > 0 && prev.totalSets > 0) {
                val dropRatio = last.setsCompleted.toFloat() / prev.setsCompleted.toFloat()
                if (dropRatio <= 0.65f) {
                    // One bad session vs repeated decline
                    val prior = recent.getOrNull(recent.size - 3)
                    val isRepeatedDecline = prior != null && prev.setsCompleted < prior.setsCompleted
                    return if (isRepeatedDecline) {
                        CoachFeedback(
                            mood = CoachMood.CONCERNED,
                            expression = "concerned",
                            headline = "Downward Performance Trend",
                            message = "Your performance has been trending down. Let's check your recovery before pushing harder.",
                            reason = "Volume output declined across consecutive training sessions.",
                            severity = "medium",
                            suggestedAction = "Check your sleep, hydration, and nutrition intake.",
                            tag = "TREND ANALYSIS",
                            priority = 5
                        )
                    } else {
                        CoachFeedback(
                            mood = CoachMood.CONCERNED,
                            expression = "concerned",
                            headline = "Single Session Dip",
                            message = "Your performance dropped today. One weaker session doesn't define your progress.",
                            reason = "Completed ${last.setsCompleted} sets compared to ${prev.setsCompleted} in the prior session.",
                            severity = "low",
                            suggestedAction = "Rest and return fresh for the next session.",
                            tag = "SESSION FEEDBACK",
                            priority = 5
                        )
                    }
                }
            }
        }

        // 2. Check for plateau / stagnation (4+ sessions with exact same volume/performance)
        if (recent.size >= 4) {
            val setCounts = recent.takeLast(4).map { it.setsCompleted }
            val allIdentical = setCounts.distinct().size == 1
            if (allIdentical) {
                return CoachFeedback(
                    mood = CoachMood.THINKING,
                    expression = "thinking",
                    headline = "Performance Plateau",
                    message = "Your performance has plateaued. You're maintaining, not progressing.",
                    reason = "Same volume (${setCounts.first()} sets) across 4 consecutive sessions.",
                    severity = "medium",
                    suggestedAction = "Let's look at recovery, nutrition, form and progression before increasing the weight.",
                    tag = "PLATEAU DETECTED",
                    priority = 5
                )
            }
        }

        // 3. Stagnation for 2-3 sessions
        if (recent.size in 2..3) {
            val setCounts = recent.map { it.setsCompleted }
            if (setCounts.distinct().size == 1) {
                return CoachFeedback(
                    mood = CoachMood.THINKING,
                    expression = "thinking",
                    headline = "Volume Maintained",
                    message = "You're maintaining the same performance. That's not necessarily bad, but there isn't measurable progression yet.",
                    reason = "Steady workload sustained across recent sessions.",
                    severity = "low",
                    suggestedAction = "Aim to add 1 rep on your top set next session.",
                    tag = "MAINTENANCE NOTE",
                    priority = 5
                )
            }
        }

        return null
    }

    /**
     * Evaluates attendance gaps, missed workouts, or returns from a long break.
     */
    fun evaluateConsistency(
        history: List<WorkoutSessionRecord>,
        blueprint: Blueprint?
    ): CoachFeedback? {
        if (history.isEmpty()) return null

        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val now = Calendar.getInstance()
        val todayStr = dateFmt.format(now.time)
        val latestRecord = history.maxByOrNull { it.date } ?: return null
        val latestDateStr = latestRecord.date.take(10)

        val daysBetween = try {
            val dLatest = dateFmt.parse(latestDateStr)
            val dNow = dateFmt.parse(todayStr)
            if (dLatest != null && dNow != null) {
                ((dNow.time - dLatest.time) / (1000 * 60 * 60 * 24)).toInt()
            } else 0
        } catch (_: Exception) { 0 }

        // Returned today after long break (> 5 days)
        if (daysBetween == 0 && history.size >= 2) {
            val priorRecord = history.sortedBy { it.date }.dropLast(1).lastOrNull()
            if (priorRecord != null) {
                val priorDaysGap = try {
                    val dPrior = dateFmt.parse(priorRecord.date.take(10))
                    val dLatest = dateFmt.parse(latestDateStr)
                    if (dPrior != null && dLatest != null) {
                        ((dLatest.time - dPrior.time) / (1000 * 60 * 60 * 24)).toInt()
                    } else 0
                } catch (_: Exception) { 0 }
                if (priorDaysGap >= 5) {
                    return CoachFeedback(
                        mood = CoachMood.RELIEVED,
                        expression = "relieved",
                        headline = "Welcome Back",
                        message = "You're back. Let's restart from where you are instead of pretending nothing happened.",
                        reason = "Resumed training after $priorDaysGap days away.",
                        severity = "none",
                        suggestedAction = "Rebuild consistency with controlled volume this week.",
                        tag = "RETURN AFTER BREAK",
                        priority = 6
                    )
                }
            }
        }

        // Long break (> 7 days)
        if (daysBetween >= 7) {
            return CoachFeedback(
                mood = CoachMood.SERIOUS,
                expression = "serious",
                headline = "Extended Break Detected",
                message = "You've been away for a while. Let's restart from where you are instead of pretending nothing happened.",
                reason = "No workout logged in $daysBetween days.",
                severity = "medium",
                suggestedAction = "Complete a moderate session today to reactivate neural motor pathways.",
                tag = "INACTIVITY ALERT",
                priority = 6
            )
        }

        // Missed workouts
        if (daysBetween in 2..3) {
            return CoachFeedback(
                mood = CoachMood.DISAPPOINTED,
                expression = "disappointed",
                headline = "Consistency Slipping",
                message = "Your consistency is slipping.",
                reason = "$daysBetween days since your last logged workout.",
                severity = "low",
                suggestedAction = "Get today's session in to protect your training rhythm.",
                tag = "CONSISTENCY CHECK",
                priority = 6
            )
        }

        return null
    }

    /**
     * Evaluates rest day schedule and recovery state.
     */
    fun evaluateRecovery(
        blueprint: Blueprint?,
        activeDayNumber: Int,
        habits: Map<String, DailyHabitStatus>,
        todayKey: String
    ): CoachFeedback? {
        val week = blueprint?.workout?.week ?: return null
        val currentDayPlan = week.find { it.dayNumber == activeDayNumber }
        if (currentDayPlan != null && currentDayPlan.isRest) {
            return CoachFeedback(
                mood = CoachMood.CALM,
                expression = "calm",
                headline = "Scheduled Recovery",
                message = "Rest day. Recovery is part of your program.",
                reason = "Prescribed rest day in weekly blueprint.",
                severity = "none",
                suggestedAction = "Prioritize hydration, mobility, and 8 hours of sleep.",
                tag = "ACTIVE REST",
                priority = 7
            )
        }
        return null
    }

    /**
     * Evaluates sleep habit metrics.
     */
    fun evaluateSleep(habits: Map<String, DailyHabitStatus>, todayKey: String): CoachFeedback? {
        val today = habits[todayKey]
        if (today != null && !today.sleep && (today.workout || today.water || today.diet)) {
            return CoachFeedback(
                mood = CoachMood.TIRED,
                expression = "tired",
                headline = "Sleep Debt",
                message = "Your sleep was low. Manage lifting intensity accordingly.",
                reason = "7h+ Sleep habit unmet on check-in.",
                severity = "low",
                suggestedAction = "Avoid heavy maximal singles when sleep deprived.",
                tag = "RECOVERY SIGNAL",
                priority = 8
            )
        }
        return null
    }

    /**
     * Evaluates hydration habit metrics.
     */
    fun evaluateHydration(habits: Map<String, DailyHabitStatus>, todayKey: String): CoachFeedback? {
        val today = habits[todayKey]
        if (today != null && !today.water && today.workout) {
            return CoachFeedback(
                mood = CoachMood.CAUTIOUS,
                expression = "cautious",
                headline = "Hydration Lagging",
                message = "Hydration is low. Muscle contraction and joint lubrication require water.",
                reason = "Water intake goal incomplete.",
                severity = "low",
                suggestedAction = "Drink 500ml water before starting training.",
                tag = "HYDRATION NOTE",
                priority = 8
            )
        }
        return null
    }

    /**
     * Evaluates diet and nutrition fueling.
     */
    fun evaluateNutrition(habits: Map<String, DailyHabitStatus>, todayKey: String): CoachFeedback? {
        val today = habits[todayKey]
        if (today != null && !today.diet && today.workout) {
            return CoachFeedback(
                mood = CoachMood.THINKING,
                expression = "thinking",
                headline = "Fueling Check",
                message = "Your training data alone doesn't explain the drop if nutrition is off.",
                reason = "Diet fuel target unchecked today.",
                severity = "low",
                suggestedAction = "Hit your protein threshold within 2 hours post-workout.",
                tag = "NUTRITION NOTE",
                priority = 8
            )
        }
        return null
    }

    /**
     * Evaluates streak milestones honestly.
     */
    fun evaluateStreak(history: List<WorkoutSessionRecord>, todayKey: String): CoachFeedback? {
        val streak = calculateConsecutiveWorkoutDays(history, todayKey)
        return when (streak) {
            30 -> CoachFeedback(
                mood = CoachMood.CELEBRATING,
                expression = "celebrating",
                headline = "30-Day Milestone",
                message = "30 days of disciplined execution.",
                reason = "30 consecutive days of verified training activity.",
                severity = "none",
                suggestedAction = "Consolidate this discipline as an identity.",
                tag = "STREAK MILESTONE",
                priority = 9
            )
            14 -> CoachFeedback(
                mood = CoachMood.JOYFUL,
                expression = "joyful",
                headline = "14-Day Streak",
                message = "Two consecutive weeks of consistent execution.",
                reason = "14 days active discipline.",
                severity = "none",
                suggestedAction = "Keep this cadence rolling.",
                tag = "STREAK MILESTONE",
                priority = 9
            )
            7 -> CoachFeedback(
                mood = CoachMood.JOYFUL,
                expression = "joyful",
                headline = "7-Day Streak",
                message = "7 days locked in. Habit formation in action.",
                reason = "7 days active discipline.",
                severity = "none",
                suggestedAction = "Maintain proper recovery as volume accumulates.",
                tag = "STREAK MILESTONE",
                priority = 4
            )
            3 -> CoachFeedback(
                mood = CoachMood.HAPPY,
                expression = "happy",
                headline = "3-Day Cadence",
                message = "Three consecutive days locked down.",
                reason = "3 days active discipline.",
                severity = "none",
                suggestedAction = "Build the streak day by day.",
                tag = "STREAK MILESTONE",
                priority = 9
            )
            else -> null
        }
    }

    /**
     * Default daily baseline state.
     * No data = No claim!
     */
    fun evaluateDailyState(
        history: List<WorkoutSessionRecord>,
        blueprint: Blueprint?,
        activeDayNumber: Int,
        profile: UserProfile? = null
    ): CoachFeedback {
        // If onboarding is genuinely incomplete
        val isOnboardingIncomplete = profile != null && !profile.isOnboardingComplete()
        if (isOnboardingIncomplete) {
            return CoachFeedback(
                mood = CoachMood.CURIOUS,
                expression = "curious",
                headline = "Calibrating Athlete Profile",
                message = "I don't have enough data yet. Complete your onboarding to calibrate your weekly protocol.",
                reason = "Onboarding calibration incomplete.",
                severity = "none",
                suggestedAction = "Tap to complete setup.",
                tag = "SETUP GUIDE",
                priority = 10
            )
        }

        val week = blueprint?.workout?.week
        val day = week?.find { it.dayNumber == activeDayNumber }

        // Once onboarding is completed, show ready to train status
        if (history.isEmpty()) {
            return CoachFeedback(
                mood = CoachMood.PLEASED,
                expression = "pleased",
                headline = if (day != null) "Ready for Day ${day.dayNumber}" else "Ready to Train",
                message = if (day != null) "Scheduled: ${day.focus}. Ready when you are." else "Ready when you are. Review today's movements.",
                reason = "Profile calibrated. Baseline protocol active.",
                severity = "none",
                suggestedAction = "Warm up dynamically before starting your working sets.",
                tag = "READY",
                priority = 10
            )
        }

        return CoachFeedback(
            mood = CoachMood.WAITING,
            expression = "waiting",
            headline = day?.dayTitle ?: "Today's Protocol",
            message = if (day != null) "Scheduled: ${day.focus}. Ready when you are." else "Ready when you are. Review today's movements.",
            reason = "Workout pending for today's schedule.",
            severity = "none",
            suggestedAction = "Warm up dynamically before starting your working sets.",
            tag = "READY",
            priority = 10
        )
    }

    private fun calculateConsecutiveWorkoutDays(history: List<WorkoutSessionRecord>, todayKey: String): Int {
        if (history.isEmpty()) return 0
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dates = history.filter { it.isFullyCompleted }.map { it.date.take(10) }.distinct().sortedDescending()
        if (dates.isEmpty()) return 0

        val latestDateStr = dates.first()
        val latestCal = Calendar.getInstance().apply {
            try {
                time = dateFmt.parse(latestDateStr) ?: Date()
            } catch (e: Exception) {
                time = Date()
            }
        }
        val todayCal = Calendar.getInstance()
        val daysDiffFromToday = ((todayCal.timeInMillis - latestCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        val anchorCal = if (daysDiffFromToday in 0..1) todayCal else latestCal
        var streak = 0
        for (i in 0 until 60) {
            val checkCal = (anchorCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
            val k = dateFmt.format(checkCal.time)
            val hasWorkout = dates.contains(k)
            if (hasWorkout) {
                streak++
            } else if (i > 0 || anchorCal != todayCal) {
                break
            }
        }
        return streak
    }
}
