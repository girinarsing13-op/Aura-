package com.example.aura.engine

import com.example.aura.data.db.ExerciseLogEntity
import com.example.aura.data.db.UnlockedAchievementEntity
import com.example.aura.data.model.BodyCheckIn
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutSessionRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Int
)

data class AchievementProgress(
    val definition: AchievementDefinition,
    val currentValue: Int,
    val isUnlocked: Boolean,
    val unlockedDateKey: String? = null
) {
    val progressFraction: Float
        get() = if (definition.targetValue <= 0) 1f else (currentValue.toFloat() / definition.targetValue.toFloat()).coerceIn(0f, 1f)
}

object AchievementEngine {

    val CATALOG: List<AchievementDefinition> = listOf(
        AchievementDefinition("first_workout", "First Workout", "Complete your very first session", 1),
        AchievementDefinition("workouts_5", "Finding Rhythm", "Complete 5 workouts", 5),
        AchievementDefinition("workouts_10", "Double Digits", "Complete 10 workouts", 10),
        AchievementDefinition("workouts_25", "Quarter Century", "Complete 25 workouts", 25),
        AchievementDefinition("streak_3", "3-Day Streak", "Keep a 3-day training streak", 3),
        AchievementDefinition("streak_7", "7-Day Streak", "Hold a 7-day training streak", 7),
        AchievementDefinition("streak_14", "Two Weeks Strong", "Hold a 14-day training streak", 14),
        AchievementDefinition("streak_30", "30-Day Consistency", "Reach a 30-day training streak", 30),
        AchievementDefinition("first_pb", "First Personal Best", "Log your first strength PB", 1),
        AchievementDefinition("pbs_5", "PB Collector", "Set personal bests on 5 lifts", 5),
        AchievementDefinition("sets_50", "50 Sets Logged", "Complete 50 working sets", 50),
        AchievementDefinition("sets_100", "Century Sets", "Complete 100 working sets", 100),
        AchievementDefinition("sets_500", "500 Club", "Complete 500 working sets", 500),
        AchievementDefinition("first_checkin", "First Check-In", "Log a body weight check-in", 1),
        AchievementDefinition("hydration_7", "Hydration Week", "Hit your water target on 7 days", 7)
    )

    fun evaluate(
        profile: UserProfile,
        sessions: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>,
        logs: List<ExerciseLogEntity>,
        checkIns: List<BodyCheckIn>,
        persistedUnlocked: List<UnlockedAchievementEntity>
    ): List<AchievementProgress> {
        val totalWorkouts = ProgressionEngine.extractCompletedWorkoutDateKeys(sessions, habits).size
            .coerceAtLeast(sessions.count { it.isFullyCompleted || it.setsCompleted > 0 })
        val streak = ProgressAnalyticsEngine.calculateSmartStreak(profile, sessions, habits)
        val pbCount = ProgressAnalyticsEngine.buildPersonalBests(logs).size
        val completedSets = logs.count { it.completed }
        val checkInCount = checkIns.size
        val waterDays = habits.values.count { it.water }
        val persistedMap = persistedUnlocked.associateBy { it.id }

        return CATALOG.map { def ->
            val current = when (def.id) {
                "first_workout", "workouts_5", "workouts_10", "workouts_25" -> totalWorkouts
                "streak_3", "streak_7", "streak_14", "streak_30" -> streak
                "first_pb", "pbs_5" -> pbCount
                "sets_50", "sets_100", "sets_500" -> completedSets
                "first_checkin" -> checkInCount
                "hydration_7" -> waterDays
                else -> 0
            }
            val persisted = persistedMap[def.id]
            val unlocked = persisted != null || current >= def.targetValue
            AchievementProgress(
                definition = def,
                currentValue = if (unlocked) maxOf(current, def.targetValue) else current,
                isUnlocked = unlocked,
                unlockedDateKey = persisted?.unlockedDateKey
            )
        }
    }

    fun findNewlyUnlockedEntities(
        profile: UserProfile,
        sessions: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>,
        logs: List<ExerciseLogEntity>,
        checkIns: List<BodyCheckIn>,
        existingPersisted: List<UnlockedAchievementEntity>,
        todayKey: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    ): List<UnlockedAchievementEntity> {
        val existingIds = existingPersisted.map { it.id }.toSet()
        val evaluated = evaluate(profile, sessions, habits, logs, checkIns, existingPersisted)
        return evaluated
            .filter { it.isUnlocked && it.definition.id !in existingIds }
            .map { progress ->
                UnlockedAchievementEntity(
                    id = progress.definition.id,
                    title = progress.definition.title,
                    description = progress.definition.description,
                    unlockedDateKey = todayKey,
                    unlockedAtMillis = System.currentTimeMillis()
                )
            }
    }
}
