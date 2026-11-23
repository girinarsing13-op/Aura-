package com.example.aura.data.repository

import com.example.aura.data.db.AuraDao
import com.example.aura.data.db.BlueprintCacheEntity
import com.example.aura.data.db.BodyCheckInEntity
import com.example.aura.data.db.DailyHabitEntity
import com.example.aura.data.db.ExerciseLogEntity
import com.example.aura.data.db.ExerciseSubstitutionEntity
import com.example.aura.data.db.FoodEntity
import com.example.aura.data.db.MealLogEntity
import com.example.aura.data.db.UnlockedAchievementEntity
import com.example.aura.data.db.UserProfileEntity
import com.example.aura.data.db.WorkoutSessionEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AuraBackupPayload(
    val schemaVersion: Int = 1,
    val exportedAt: String = "",
    val profile: UserProfileEntity? = null,
    val blueprintCache: BlueprintCacheEntity? = null,
    val workoutSessions: List<WorkoutSessionEntity> = emptyList(),
    val dailyHabits: List<DailyHabitEntity> = emptyList(),
    val exerciseLogs: List<ExerciseLogEntity> = emptyList(),
    val substitutions: List<ExerciseSubstitutionEntity> = emptyList(),
    val bodyCheckIns: List<BodyCheckInEntity> = emptyList(),
    val achievements: List<UnlockedAchievementEntity> = emptyList(),
    val customFoods: List<FoodEntity> = emptyList(),
    val mealLogs: List<MealLogEntity> = emptyList()
)

object AuraBackupManager {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(AuraBackupPayload::class.java).indent("  ")

    suspend fun exportToJson(dao: AuraDao): String {
        val payload = AuraBackupPayload(
            schemaVersion = 1,
            exportedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            profile = dao.getUserProfileOnce(),
            blueprintCache = dao.getBlueprintCacheOnce(),
            workoutSessions = dao.getAllWorkoutSessionsOnce(),
            dailyHabits = dao.getAllDailyHabitsOnce(),
            exerciseLogs = dao.getAllExerciseLogsOnce(),
            substitutions = dao.getAllSubstitutionsOnce(),
            bodyCheckIns = dao.getAllBodyCheckInsOnce(),
            achievements = dao.getAllAchievementsOnce(),
            customFoods = dao.getCustomFoodsOnce(),
            mealLogs = dao.getAllMealLogsOnce()
        )
        return adapter.toJson(payload)
    }

    suspend fun importFromJson(dao: AuraDao, rawJson: String): Result<AuraBackupPayload> {
        return try {
            val trimmed = rawJson.trim()
            if (trimmed.isEmpty()) {
                return Result.failure(IllegalArgumentException("Backup file is empty."))
            }
            val parsed = adapter.fromJson(trimmed)
                ?: return Result.failure(IllegalArgumentException("Could not read backup JSON."))

            if (parsed.profile == null &&
                parsed.workoutSessions.isEmpty() &&
                parsed.exerciseLogs.isEmpty() &&
                parsed.bodyCheckIns.isEmpty() &&
                parsed.mealLogs.isEmpty()
            ) {
                return Result.failure(IllegalArgumentException("Backup file has no Aura data."))
            }

            // Clear existing tables cleanly before restoring snapshot
            dao.deleteAllWorkoutSessions()
            dao.deleteAllDailyHabits()
            dao.deleteAllExerciseLogs()
            dao.deleteAllSubstitutions()
            dao.deleteAllBodyCheckIns()
            dao.deleteAllAchievements()
            dao.deleteCustomFoods()
            dao.deleteAllMealLogs()

            parsed.profile?.let { dao.insertUserProfile(it.copy(id = 1)) }
            parsed.blueprintCache?.let { dao.insertBlueprintCache(it.copy(id = 1)) }
            if (parsed.workoutSessions.isNotEmpty()) dao.insertWorkoutSessions(parsed.workoutSessions)
            if (parsed.dailyHabits.isNotEmpty()) dao.insertDailyHabits(parsed.dailyHabits)
            if (parsed.exerciseLogs.isNotEmpty()) dao.insertExerciseLogs(parsed.exerciseLogs)
            if (parsed.substitutions.isNotEmpty()) dao.insertSubstitutions(parsed.substitutions)
            if (parsed.bodyCheckIns.isNotEmpty()) dao.insertBodyCheckIns(parsed.bodyCheckIns)
            if (parsed.achievements.isNotEmpty()) dao.insertAchievements(parsed.achievements)
            parsed.customFoods.forEach { dao.insertFoodReplace(it.copy(isCustom = true)) }
            if (parsed.mealLogs.isNotEmpty()) dao.insertMealLogs(parsed.mealLogs)

            Result.success(parsed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
