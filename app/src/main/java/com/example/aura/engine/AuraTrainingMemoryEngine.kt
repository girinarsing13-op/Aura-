package com.example.aura.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data model for a logged exercise set in memory
 */
data class LoggedSetData(
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val completed: Boolean
)

/**
 * Result of evaluating a set against historical performance
 */
data class SetComparisonResult(
    val isPersonalRecord: Boolean = false,
    val expression: String = "calm",
    val headline: String = "",
    val message: String = "",
    val detail: String = "",
    val isWeightJumpExcessive: Boolean = false,
    val isPerformanceDecline: Boolean = false,
    val deltaReps: Int = 0,
    val deltaWeightKg: Double = 0.0,
    val isRepeatedStagnant: Boolean = false
)

data class RecommendedTarget(
    val coachingCue: String,
    val targetLoadKg: Double = 0.0,
    val targetReps: String = ""
)

/**
 * Historical performance summary for an exercise
 */
data class PerformanceStats(
    val exerciseName: String,
    val bestWeightKg: Double,
    val bestReps: Int,
    val lastWeightKg: Double,
    val lastReps: Int,
    val totalVolumeKg: Double,
    val sessionCount: Int,
    val lastSessionDate: String
)

/**
 * Authoritative Local AURA Training Memory Engine
 * Runs completely locally inside the application with zero external AI dependencies.
 * Remembers all user exercise history, PRs, progression, regressions, and substitutions.
 */
object AuraTrainingMemoryEngine {
    // Map: Exercise Name (normalized) -> List of chronological set entries
    private val exerciseHistory = mutableMapOf<String, MutableList<ExerciseHistoryEntry>>()
    // Map: Original Exercise ID -> Substituted Exercise Name
    private val substitutions = mutableMapOf<String, String>()
    // Tracking overall PRs
    private val personalRecords = mutableMapOf<String, PRRecord>()

    data class ExerciseHistoryEntry(
        val sessionId: String,
        val date: String,
        val exerciseId: String,
        val exerciseName: String,
        val equipment: String,
        val variation: String,
        val sets: List<LoggedSetData>,
        val notes: String
    )

    data class PRRecord(
        val exerciseName: String,
        val weightKg: Double,
        val reps: Int,
        val achievedDate: String
    )

    @Synchronized
    fun recordExerciseSets(
        sessionId: Any,
        date: String,
        exerciseId: String,
        exerciseName: String,
        equipment: String,
        variation: String,
        sets: List<LoggedSetData>,
        notes: String = ""
    ) {
        val sId = sessionId.toString()
        val key = exerciseName.trim().lowercase(Locale.ROOT)
        val list = exerciseHistory.getOrPut(key) { mutableListOf() }
        // Remove duplicate entry for same session and date if already present
        list.removeAll { it.sessionId == sId && it.date == date }
        val entry = ExerciseHistoryEntry(
            sessionId = sId,
            date = date,
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            equipment = equipment,
            variation = variation,
            sets = sets,
            notes = notes
        )
        list.add(entry)

        // Check for new PR
        val completedSets = sets.filter { it.completed }
        for (s in completedSets) {
            val curPR = personalRecords[key]
            if (curPR == null || s.weightKg > curPR.weightKg || (s.weightKg == curPR.weightKg && s.reps > curPR.reps)) {
                personalRecords[key] = PRRecord(
                    exerciseName = exerciseName,
                    weightKg = s.weightKg,
                    reps = s.reps,
                    achievedDate = date
                )
            }
        }
    }

    @Synchronized
    fun recordSubstitution(originalExerciseId: String, substitutedExerciseName: String) {
        substitutions[originalExerciseId] = substitutedExerciseName
    }

    @Synchronized
    fun getSubstitution(originalExerciseId: String): String? {
        return substitutions[originalExerciseId]
    }

    @Synchronized
    fun getLastPerformance(exerciseName: String): PerformanceStats? {
        val key = exerciseName.trim().lowercase(Locale.ROOT)
        val history = exerciseHistory[key] ?: return null
        if (history.isEmpty()) return null
        val lastEntry = history.last()
        val completedSets = lastEntry.sets.filter { it.completed }
        if (completedSets.isEmpty()) return null

        val maxWeightSet = completedSets.maxByOrNull { it.weightKg } ?: completedSets.first()
        val allCompletedSets = history.flatMap { it.sets.filter { s -> s.completed } }
        val overallBest = allCompletedSets.maxWithOrNull(
            compareBy<LoggedSetData> { it.weightKg }.thenBy { it.reps }
        ) ?: maxWeightSet

        val totalVol = history.sumOf { h ->
            h.sets.filter { it.completed }.sumOf { it.weightKg * it.reps }
        }

        return PerformanceStats(
            exerciseName = exerciseName,
            bestWeightKg = overallBest.weightKg,
            bestReps = overallBest.reps,
            lastWeightKg = maxWeightSet.weightKg,
            lastReps = maxWeightSet.reps,
            totalVolumeKg = totalVol,
            sessionCount = history.size,
            lastSessionDate = lastEntry.date
        )
    }

    @Synchronized
    fun getBestPerformance(exerciseName: String): PerformanceStats? {
        return getLastPerformance(exerciseName)
    }

    @Synchronized
    fun getRecommendedNextTarget(exerciseName: String, recommendedLoadKg: Double, repsRange: String): RecommendedTarget {
        val last = getLastPerformance(exerciseName)
        if (last == null) {
            return RecommendedTarget(
                coachingCue = "Target: $recommendedLoadKg kg × $repsRange. Establish clean baseline.",
                targetLoadKg = recommendedLoadKg,
                targetReps = repsRange
            )
        }

        val targetReps = repsRange.split("-").lastOrNull()?.trim()?.toIntOrNull() ?: 10
        return if (last.lastReps >= targetReps) {
            val nextWeight = last.lastWeightKg + 2.5
            RecommendedTarget(
                coachingCue = "Let's see if you can beat ${last.lastReps} reps or push to $nextWeight kg.",
                targetLoadKg = nextWeight,
                targetReps = "${last.lastReps}"
            )
        } else {
            RecommendedTarget(
                coachingCue = "Last time was ${last.lastWeightKg} kg × ${last.lastReps}. Let's beat ${last.lastReps}.",
                targetLoadKg = last.lastWeightKg,
                targetReps = "${last.lastReps + 1}"
            )
        }
    }

    @Synchronized
    fun compareSet(
        exerciseName: String,
        setNumber: Int,
        weightKg: Double,
        reps: Int,
        equipment: String,
        currentTargetRepRange: String
    ): SetComparisonResult {
        val last = getLastPerformance(exerciseName)
        if (last == null) {
            return SetComparisonResult(
                isPersonalRecord = false,
                expression = "observing",
                headline = "BASELINE ESTABLISHED",
                message = "$weightKg kg × $reps reps",
                detail = "First logged entry for $exerciseName. Baseline recorded."
            )
        }

        val lastWeight = last.lastWeightKg
        val lastReps = last.lastReps

        // Excessive weight jump detection (e.g. doubling weight or jumping >40% with high load)
        if (lastWeight > 0 && weightKg >= lastWeight * 1.5 && (weightKg - lastWeight) >= 10.0) {
            return SetComparisonResult(
                isPersonalRecord = true,
                expression = "worried",
                headline = "EXCESSIVE WEIGHT JUMP",
                message = "That's a big jump (+${weightKg - lastWeight} kg).",
                detail = "Control the weight before chasing the number. Protect joints.",
                isWeightJumpExcessive = true,
                deltaWeightKg = weightKg - lastWeight
            )
        }

        // New genuine PR check
        val isPR = weightKg > last.bestWeightKg || (weightKg == last.bestWeightKg && reps > last.bestReps)
        if (isPR) {
            val deltaW = weightKg - last.bestWeightKg
            val deltaR = reps - last.bestReps
            val detailMsg = if (deltaW > 0) "+$deltaW kg over previous best!" else "+$deltaR reps over previous best!"
            return SetComparisonResult(
                isPersonalRecord = true,
                expression = "celebrating",
                headline = "NEW PERSONAL RECORD!",
                message = "NEW PR: $weightKg kg × $reps reps!",
                detail = detailMsg,
                deltaReps = deltaR,
                deltaWeightKg = deltaW
            )
        }

        // Improvement
        if (weightKg > lastWeight || (weightKg == lastWeight && reps > lastReps)) {
            val deltaR = reps - lastReps
            val deltaW = weightKg - lastWeight
            val msg = if (deltaR > 0 && deltaW == 0.0) "$deltaR more reps than last time." else "+$deltaW kg progression."
            return SetComparisonResult(
                isPersonalRecord = false,
                expression = "impressed",
                headline = "PERFORMANCE IMPROVED",
                message = msg,
                detail = "Solid progressive overload executed.",
                deltaReps = deltaR,
                deltaWeightKg = deltaW
            )
        }

        // Maintained
        if (weightKg == lastWeight && reps == lastReps) {
            return SetComparisonResult(
                isPersonalRecord = false,
                expression = "thinking",
                headline = "MAINTAINING VOLUME",
                message = "Same as last time: $weightKg kg × $reps reps.",
                detail = "Consistent workload preserved.",
                isRepeatedStagnant = true
            )
        }

        // Decline
        val repDrop = lastReps - reps
        val weightDrop = lastWeight - weightKg
        val declineDetail = if (repDrop > 0) "$repDrop reps lower than last session." else "-$weightDrop kg lower."
        return SetComparisonResult(
            isPersonalRecord = false,
            expression = "concerned",
            headline = "LOWER OUTPUT",
            message = "That's lower than last time.",
            detail = "$declineDetail One bad set is not a pattern. Keep form strict.",
            isPerformanceDecline = true,
            deltaReps = -repDrop,
            deltaWeightKg = -weightDrop
        )
    }

    @Synchronized
    fun getWorkoutStartMemorySummary(dayTitle: String, exercises: List<com.example.aura.data.model.BlueprintExercise>): String {
        val trackedCount = exercises.count {
            getLastPerformance(it.name) != null
        }
        return if (trackedCount > 0) {
            "$trackedCount of ${exercises.size} movements have historical memory."
        } else {
            "Ready to establish new performance records."
        }
    }

    data class ProgressionAdvice(
        val recommendedWeightKg: Double,
        val indicator: String, // "↑", "=", "↓"
        val explanation: String
    )

    @Synchronized
    fun getProgressionAdvice(exerciseName: String, baseWeightKg: Double, targetReps: String): ProgressionAdvice {
        val last = getLastPerformance(exerciseName) ?: return ProgressionAdvice(
            recommendedWeightKg = baseWeightKg,
            indicator = "",
            explanation = "Calibrated baseline based on your strength profile."
        )
        val maxRepTarget = targetReps.split("-").lastOrNull()?.trim()?.toIntOrNull() ?: 10
        val minRepTarget = targetReps.split("-").firstOrNull()?.trim()?.toIntOrNull() ?: 8

        return when {
            last.lastReps >= maxRepTarget -> {
                val nextWeight = (last.lastWeightKg + 2.5)
                ProgressionAdvice(
                    recommendedWeightKg = nextWeight,
                    indicator = "↑",
                    explanation = "You hit ${last.lastReps} reps at the top of your range last session."
                )
            }
            last.lastReps < minRepTarget -> {
                ProgressionAdvice(
                    recommendedWeightKg = last.lastWeightKg,
                    indicator = "=",
                    explanation = "Performance dropped slightly last session, so the load is being held."
                )
            }
            else -> {
                ProgressionAdvice(
                    recommendedWeightKg = last.lastWeightKg,
                    indicator = "=",
                    explanation = "Maintain load and aim for another clean rep before adding weight."
                )
            }
        }
    }
}

