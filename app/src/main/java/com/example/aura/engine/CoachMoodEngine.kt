package com.example.aura.engine

import com.example.aura.data.model.Blueprint
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.SetLog
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutSessionRecord
import com.example.aura.ui.coach.CoachFeedback
import com.example.aura.ui.coach.CoachMood
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Return structure containing mood, expression, message, reason, and severity.
 */
data class CoachMoodResult(
    val mood: CoachMood,
    val expression: String = mood.name.lowercase(),
    val message: String,
    val reason: String = "",
    val severity: String = "none" // "none", "low", "medium", "high"
) {
    fun toCoachFeedback(
        headline: String = "Coach Evaluation",
        suggestedAction: String? = null,
        tag: String = "AURA COACH",
        priority: Int = 5
    ): CoachFeedback = CoachFeedback(
        mood = mood,
        expression = expression,
        headline = headline,
        message = message,
        reason = reason,
        severity = severity,
        suggestedAction = suggestedAction,
        tag = tag,
        priority = priority
    )
}

typealias CoachMoodEvaluation = CoachMoodResult
typealias CoachMoodResponse = CoachMoodResult

/**
 * Real Emotional Coach Engine for evaluating workout execution, strength trends,
 * and training consistency.
 */
open class CoachMoodEngine {
    /**
     * Evaluates a completed or active workout session against target volume and expectations.
     */
    fun evaluateWorkout(
        completedSets: Int,
        totalSets: Int,
        isPR: Boolean = false,
        workoutTitle: String = "Workout"
    ): CoachMoodResult {
        if (totalSets <= 0) {
            return CoachMoodResult(
                mood = CoachMood.WAITING,
                expression = "waiting",
                message = "Ready when you are. Review today's movements and log your sets.",
                reason = "No prescribed sets logged yet.",
                severity = "none"
            )
        }

        if (completedSets == 0) {
            return CoachMoodResult(
                mood = CoachMood.DISAPPOINTED,
                expression = "disappointed",
                message = "Prescribed session had 0 sets logged. Discipline is built on showing up even when motivation is low.",
                reason = "0 out of $totalSets prescribed sets completed.",
                severity = "medium"
            )
        }

        if (isPR) {
            return CoachMoodResult(
                mood = CoachMood.CELEBRATING,
                expression = "celebrating",
                message = "Personal Record shattered! Mechanical tension and progressive overload verified.",
                reason = "New personal best load or reps logged for $workoutTitle.",
                severity = "none"
            )
        }

        val completionRate = completedSets.toFloat() / totalSets.toFloat()
        return when {
            completionRate >= 1.0f -> {
                CoachMoodResult(
                    mood = CoachMood.PROUD,
                    expression = "proud",
                    message = "All $totalSets prescribed sets locked in. High-quality execution without cutting corners.",
                    reason = "Completed 100% of prescribed sets for $workoutTitle.",
                    severity = "none"
                )
            }
            completionRate >= 0.75f -> {
                CoachMoodResult(
                    mood = CoachMood.PLEASED,
                    expression = "pleased",
                    message = "Solid effort. Completed $completedSets of $totalSets sets. Great work maintaining focus.",
                    reason = "Achieved ${(completionRate * 100).toInt()}% target volume.",
                    severity = "none"
                )
            }
            completionRate >= 0.5f -> {
                CoachMoodResult(
                    mood = CoachMood.OBSERVING,
                    expression = "observing",
                    message = "Partial session completed ($completedSets/$totalSets sets). Notice if fatigue or time was the bottleneck.",
                    reason = "Moderate volume shortfall recorded.",
                    severity = "low"
                )
            }
            else -> {
                CoachMoodResult(
                    mood = CoachMood.CONCERNED,
                    expression = "concerned",
                    message = "Session was cut short at $completedSets of $totalSets sets. If fatigue is compounding, examine sleep and nutrition.",
                    reason = "Completed under 50% of prescribed volume.",
                    severity = "medium"
                )
            }
        }
    }

    /**
     * Overload: Evaluates today's session from the session record and history.
     */
    fun evaluateWorkout(
        session: WorkoutSessionRecord?,
        history: List<WorkoutSessionRecord> = emptyList()
    ): CoachMoodResult {
        if (session == null) {
            return CoachMoodResult(
                mood = CoachMood.WAITING,
                expression = "waiting",
                message = "No active or completed session found for today. Prepare your gear and warm up.",
                reason = "Session record is null.",
                severity = "none"
            )
        }
        return evaluateWorkout(
            completedSets = session.setsCompleted,
            totalSets = session.totalSets,
            isPR = false,
            workoutTitle = session.dayTitle
        )
    }

    /**
     * Overload: Evaluates workouts based on history list and optional blueprint/todayKey.
     */
    fun evaluateWorkout(
        history: List<WorkoutSessionRecord>,
        blueprint: Blueprint? = null,
        todayKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    ): CoachMoodResult {
        val todaySessions = history.filter { it.date.startsWith(todayKey) && it.isFullyCompleted }
        if (todaySessions.isEmpty()) {
            val pendingSession = history.firstOrNull { it.date.startsWith(todayKey) }
            if (pendingSession != null) {
                return evaluateWorkout(pendingSession.setsCompleted, pendingSession.totalSets, workoutTitle = pendingSession.dayTitle)
            }
            return CoachMoodResult(
                mood = CoachMood.WAITING,
                expression = "waiting",
                message = "Workout pending for today. Ready when you are.",
                reason = "No completed session found for today ($todayKey).",
                severity = "none"
            )
        }
        val latest = todaySessions.last()
        val allSetsHit = latest.setsCompleted >= latest.totalSets && latest.totalSets > 0
        val recentCompleted = history.count { it.isFullyCompleted }
        return when {
            allSetsHit && recentCompleted >= 8 -> CoachMoodResult(
                mood = CoachMood.PROUD,
                expression = "proud",
                message = "You're not just showing up. You're progressing intelligently.",
                reason = "Completed all ${latest.totalSets} prescribed sets with consistent weekly adherence.",
                severity = "none"
            )
            allSetsHit -> CoachMoodResult(
                mood = CoachMood.HAPPY,
                expression = "happy",
                message = "Session completed cleanly. Every set accounted for.",
                reason = "Completed all ${latest.totalSets} prescribed sets for today.",
                severity = "none"
            )
            else -> evaluateWorkout(latest.setsCompleted, latest.totalSets, workoutTitle = latest.dayTitle)
        }
    }

    /**
     * Evaluates strength progression, stagnation, or performance drops.
     */
    @JvmName("evaluateStrengthTrendFromHistory")
    fun evaluateStrengthTrend(history: List<WorkoutSessionRecord>): CoachMoodResult {
        val completed = history.filter { it.isFullyCompleted }
        if (completed.size < 2) {
            return CoachMoodResult(
                mood = CoachMood.CURIOUS,
                expression = "curious",
                message = "I don't have enough data yet. Complete a few workouts to establish your strength baseline.",
                reason = "Need at least 2 completed sessions to compute a strength trend.",
                severity = "none"
            )
        }
        val recent = completed.takeLast(5)
        val last = recent.last()
        val prev = recent[recent.size - 2]

        // Check for sudden drop in performance
        if (last.totalSets > 0 && prev.totalSets > 0) {
            val dropRatio = last.setsCompleted.toFloat() / prev.setsCompleted.toFloat()
            if (dropRatio <= 0.65f) {
                val prior = recent.getOrNull(recent.size - 3)
                val isRepeatedDecline = prior != null && prev.setsCompleted < prior.setsCompleted
                return if (isRepeatedDecline) {
                    CoachMoodResult(
                        mood = CoachMood.CONCERNED,
                        expression = "concerned",
                        message = "Your performance has been trending down across consecutive sessions. Check sleep and recovery.",
                        reason = "Volume output declined consecutively (${prev.setsCompleted} -> ${last.setsCompleted} sets).",
                        severity = "medium"
                    )
                } else {
                    CoachMoodResult(
                        mood = CoachMood.CONCERNED,
                        expression = "concerned",
                        message = "Your volume dipped in this session. One off-day is normal, but watch your recovery.",
                        reason = "Completed ${last.setsCompleted} sets compared to ${prev.setsCompleted} in prior session.",
                        severity = "low"
                    )
                }
            }
        }

        // Check for 4+ sessions plateau
        if (recent.size >= 4) {
            val setCounts = recent.takeLast(4).map { it.setsCompleted }
            if (setCounts.distinct().size == 1) {
                return CoachMoodResult(
                    mood = CoachMood.THINKING,
                    expression = "thinking",
                    message = "Your performance has plateaued. You're maintaining, not progressing. Look at nutrition, form, and recovery.",
                    reason = "Identical volume (${setCounts.first()} sets) across 4 consecutive sessions.",
                    severity = "medium"
                )
            }
        }

        // Check for positive progression
        if (last.setsCompleted >= prev.setsCompleted && last.setsCompleted > 0) {
            return CoachMoodResult(
                mood = CoachMood.DETERMINED,
                expression = "determined",
                message = "Strength trend is stable and moving in the right direction. Keep adhering to progressive overload.",
                reason = "Maintained or exceeded volume (${last.setsCompleted} sets vs ${prev.setsCompleted} previous).",
                severity = "none"
            )
        }

        return CoachMoodResult(
            mood = CoachMood.OBSERVING,
            expression = "observing",
            message = "Performance is holding within normal physiological variance.",
            reason = "Standard baseline volume distribution.",
            severity = "none"
        )
    }

    /**
     * Overload: Evaluates strength trend from numeric weight progression.
     */
    @JvmName("evaluateStrengthTrendFromWeights")
    fun evaluateStrengthTrend(weights: List<Double>): CoachMoodResult {
        if (weights.size < 2) {
            return CoachMoodResult(
                mood = CoachMood.CURIOUS,
                expression = "curious",
                message = "Log more sessions with weight values to track your strength curve.",
                reason = "Fewer than 2 weight points recorded.",
                severity = "none"
            )
        }
        val last = weights.last()
        val prev = weights[weights.size - 2]
        val diff = last - prev
        return when {
            diff > 0 -> CoachMoodResult(
                mood = CoachMood.DETERMINED,
                expression = "determined",
                message = "Weight progression detected (+${String.format(Locale.US, "%.1f", diff)} kg/lbs). Progressive overload active.",
                reason = "Top working set weight increased from $prev to $last.",
                severity = "none"
            )
            diff < 0 -> CoachMoodResult(
                mood = CoachMood.CONCERNED,
                expression = "concerned",
                message = "Working load decreased (${String.format(Locale.US, "%.1f", diff)}). Prioritize restorative sleep and fueling.",
                reason = "Weight dropped from $prev to $last.",
                severity = "low"
            )
            else -> CoachMoodResult(
                mood = CoachMood.THINKING,
                expression = "thinking",
                message = "Working load unchanged at $last. Aim to add 1 rep before pushing the weight up.",
                reason = "Load sustained at $last across consecutive sessions.",
                severity = "none"
            )
        }
    }

    /**
     * Evaluates attendance consistency, training streaks, missed sessions, and returns after breaks.
     */
    fun evaluateConsistency(
        history: List<WorkoutSessionRecord>,
        blueprint: Blueprint? = null
    ): CoachMoodResult {
        if (history.isEmpty()) {
            return CoachMoodResult(
                mood = CoachMood.CURIOUS,
                expression = "curious",
                message = "I don't have enough data yet. Complete a few workouts to establish your consistency baseline.",
                reason = "Zero workout history recorded.",
                severity = "none"
            )
        }
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val now = Calendar.getInstance()
        val todayStr = dateFmt.format(now.time)
        val latestRecord = history.maxByOrNull { it.date } ?: return CoachMoodResult(
            mood = CoachMood.CALM,
            expression = "calm",
            message = "Ready to train.",
            reason = "No prior records.",
            severity = "none"
        )
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
                    return CoachMoodResult(
                        mood = CoachMood.RELIEVED,
                        expression = "relieved",
                        message = "You're back. Let's restart from where you are instead of pretending nothing happened.",
                        reason = "Resumed training after $priorDaysGap days away.",
                        severity = "none"
                    )
                }
            }
        }

        // Extended break (> 7 days)
        if (daysBetween >= 7) {
            return CoachMoodResult(
                mood = CoachMood.SERIOUS,
                expression = "serious",
                message = "You've been away for $daysBetween days. Let's restart from where you are instead of pretending nothing happened.",
                reason = "No workout logged in $daysBetween days.",
                severity = "high"
            )
        }

        // Consistency slipping (2-3 days gap without rest day designated)
        if (daysBetween in 2..3) {
            return CoachMoodResult(
                mood = CoachMood.DISAPPOINTED,
                expression = "disappointed",
                message = "Your consistency is slipping. Gaps between sessions reduce training momentum.",
                reason = "$daysBetween days since last logged session.",
                severity = "medium"
            )
        }

        // Active streak check
        val streak = calculateConsecutiveWorkoutDays(history, todayStr)
        if (streak >= 7) {
            return CoachMoodResult(
                mood = CoachMood.JOYFUL,
                expression = "joyful",
                message = "Unstoppable momentum: $streak days locked in! Respect your recovery needs as well.",
                reason = "Maintained $streak consecutive training days.",
                severity = "none"
            )
        }
        if (streak in 3..6) {
            return CoachMoodResult(
                mood = CoachMood.PLEASED,
                expression = "pleased",
                message = "$streak-day streak locked. Solid athletic cadence in effect.",
                reason = "$streak consecutive workout days recorded.",
                severity = "none"
            )
        }

        return CoachMoodResult(
            mood = CoachMood.CALM,
            expression = "calm",
            message = "Training cadence is steady. Stay on schedule for your next session.",
            reason = "Normal interval between workouts.",
            severity = "none"
        )
    }

    /**
     * Overload: Evaluates consistency from days missed count and consecutive days trained.
     */
    fun evaluateConsistency(daysMissed: Int, consecutiveDaysTrained: Int = 0): CoachMoodResult {
        return when {
            daysMissed >= 7 -> CoachMoodResult(
                mood = CoachMood.SERIOUS,
                expression = "serious",
                message = "Extended absence ($daysMissed days). Let's resume with calibrated weights.",
                reason = "Inactivity period exceeds 1 week.",
                severity = "high"
            )
            daysMissed in 2..3 -> CoachMoodResult(
                mood = CoachMood.DISAPPOINTED,
                expression = "disappointed",
                message = "Consistency is slipping. $daysMissed missed days begin to compound.",
                reason = "$daysMissed missed sessions recorded.",
                severity = "medium"
            )
            consecutiveDaysTrained >= 7 -> CoachMoodResult(
                mood = CoachMood.JOYFUL,
                expression = "joyful",
                message = "$consecutiveDaysTrained-day streak! Phenomenal discipline.",
                reason = "Multi-day streak sustained.",
                severity = "none"
            )
            consecutiveDaysTrained >= 3 -> CoachMoodResult(
                mood = CoachMood.PLEASED,
                expression = "pleased",
                message = "Solid $consecutiveDaysTrained-day streak active. Keep the standard high.",
                reason = "Consecutive training days logged.",
                severity = "none"
            )
            else -> CoachMoodResult(
                mood = CoachMood.CALM,
                expression = "calm",
                message = "Cadence is on track. Consistency is built day by day.",
                reason = "Standard training rhythm.",
                severity = "none"
            )
        }
    }

    private fun calculateConsecutiveWorkoutDays(history: List<WorkoutSessionRecord>, todayKey: String): Int {
        var streak = 0
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val now = Calendar.getInstance()
        for (i in 0 until 60) {
            val checkCal = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -i) }
            val k = dateFmt.format(checkCal.time)
            val hasWorkout = history.any { it.date.startsWith(k) && it.isFullyCompleted }
            if (hasWorkout) {
                streak++
            } else if (i > 0) {
                break
            }
        }
        return streak
    }

    companion object : CoachMoodEngine() {
        val instance = CoachMoodEngine()
    }
}
