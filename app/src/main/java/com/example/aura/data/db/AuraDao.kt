package com.example.aura.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun deleteUserProfile()

    @Query("SELECT * FROM blueprint_cache WHERE id = 1 LIMIT 1")
    fun getBlueprintCache(): Flow<BlueprintCacheEntity?>

    @Query("SELECT * FROM blueprint_cache WHERE id = 1 LIMIT 1")
    suspend fun getBlueprintCacheOnce(): BlueprintCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlueprintCache(blueprint: BlueprintCacheEntity)

    @Query("DELETE FROM blueprint_cache")
    suspend fun deleteBlueprintCache()

    @Query("SELECT * FROM workout_sessions ORDER BY id DESC")
    fun getAllWorkoutSessions(): Flow<List<WorkoutSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Query("SELECT * FROM daily_habits ORDER BY dateKey DESC")
    fun getAllDailyHabits(): Flow<List<DailyHabitEntity>>

    @Query("SELECT * FROM daily_habits WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyHabitByDate(dateKey: String): DailyHabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyHabit(habit: DailyHabitEntity)

    // --- AURA TRAINING MEMORY QUERIES ---
    @Query("SELECT * FROM exercise_logs ORDER BY id DESC")
    fun getAllExerciseLogs(): Flow<List<ExerciseLogEntity>>

    @Query("SELECT * FROM exercise_logs WHERE exerciseId = :exId OR exerciseName = :name ORDER BY id DESC")
    suspend fun getLogsForExercise(exId: String, name: String): List<ExerciseLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLogs(logs: List<ExerciseLogEntity>)

    @Query("SELECT * FROM exercise_substitutions WHERE originalExerciseId = :exId LIMIT 1")
    suspend fun getSubstitution(exId: String): ExerciseSubstitutionEntity?

    @Query("SELECT * FROM exercise_substitutions")
    fun getAllSubstitutions(): Flow<List<ExerciseSubstitutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstitution(sub: ExerciseSubstitutionEntity)

    @Query("SELECT * FROM exercise_logs WHERE date = :date AND exerciseId = :exerciseId AND setNumber = :setNumber LIMIT 1")
    suspend fun getExerciseLogForSet(date: String, exerciseId: String, setNumber: Int): ExerciseLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseLog(log: ExerciseLogEntity): Long

    @Query("DELETE FROM exercise_logs WHERE date = :date AND exerciseId = :exerciseId AND setNumber = :setNumber")
    suspend fun deleteExerciseLogForSet(date: String, exerciseId: String, setNumber: Int)

    @Query("UPDATE exercise_logs SET sessionId = :sessionId WHERE date = :date AND sessionId = 0")
    suspend fun attachSessionIdToDateLogs(date: String, sessionId: Long)

    // --- BODY CHECK-INS ---
    @Query("SELECT * FROM body_check_ins ORDER BY dateKey DESC, timestampMillis DESC")
    fun getAllBodyCheckIns(): Flow<List<BodyCheckInEntity>>

    @Query("SELECT * FROM body_check_ins ORDER BY dateKey DESC, timestampMillis DESC")
    suspend fun getAllBodyCheckInsOnce(): List<BodyCheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyCheckIn(checkIn: BodyCheckInEntity): Long

    @Query("DELETE FROM body_check_ins WHERE id = :id")
    suspend fun deleteBodyCheckInById(id: Long)

    // --- ACHIEVEMENTS ---
    @Query("SELECT * FROM achievements ORDER BY unlockedAtMillis DESC")
    fun getAllAchievements(): Flow<List<UnlockedAchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievementsOnce(): List<UnlockedAchievementEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(items: List<UnlockedAchievementEntity>)

    // --- BACKUP & RESTORE HELPERS ---
    @Query("SELECT * FROM workout_sessions ORDER BY id ASC")
    suspend fun getAllWorkoutSessionsOnce(): List<WorkoutSessionEntity>

    @Query("SELECT * FROM daily_habits ORDER BY dateKey ASC")
    suspend fun getAllDailyHabitsOnce(): List<DailyHabitEntity>

    @Query("SELECT * FROM exercise_logs ORDER BY id ASC")
    suspend fun getAllExerciseLogsOnce(): List<ExerciseLogEntity>

    @Query("SELECT * FROM exercise_substitutions")
    suspend fun getAllSubstitutionsOnce(): List<ExerciseSubstitutionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSessions(sessions: List<WorkoutSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyHabits(habits: List<DailyHabitEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstitutions(subs: List<ExerciseSubstitutionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyCheckIns(checkIns: List<BodyCheckInEntity>)

    @Query("DELETE FROM workout_sessions")
    suspend fun deleteAllWorkoutSessions()

    @Query("DELETE FROM daily_habits")
    suspend fun deleteAllDailyHabits()

    @Query("DELETE FROM exercise_logs")
    suspend fun deleteAllExerciseLogs()

    @Query("DELETE FROM exercise_substitutions")
    suspend fun deleteAllSubstitutions()

    @Query("DELETE FROM body_check_ins")
    suspend fun deleteAllBodyCheckIns()

    @Query("DELETE FROM achievements")
    suspend fun deleteAllAchievements()

    // --- FOOD CATALOG & MEAL LOGS ---
    @Query("SELECT * FROM foods ORDER BY isFavorite DESC, lastUsedAt DESC, name ASC")
    fun getAllFoodsFlow(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods ORDER BY name ASC")
    suspend fun getAllFoodsOnce(): List<FoodEntity>

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodCount(): Int

    @Query("SELECT * FROM foods WHERE isCustom = 1")
    suspend fun getCustomFoodsOnce(): List<FoodEntity>

    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1")
    suspend fun getFoodById(id: String): FoodEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFoodsIgnore(foods: List<FoodEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodReplace(food: FoodEntity)

    @Query("UPDATE foods SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFoodFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE foods SET lastUsedAt = :usedAt WHERE id = :id")
    suspend fun markFoodUsed(id: String, usedAt: Long)

    @Query("DELETE FROM foods WHERE isCustom = 1")
    suspend fun deleteCustomFoods()

    @Query("SELECT * FROM meal_logs ORDER BY dateKey DESC, timestampMillis DESC")
    fun getAllMealLogsFlow(): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs ORDER BY dateKey ASC, timestampMillis ASC")
    suspend fun getAllMealLogsOnce(): List<MealLogEntity>

    @Query("SELECT * FROM meal_logs WHERE dateKey = :dateKey ORDER BY timestampMillis ASC")
    suspend fun getMealLogsForDateOnce(dateKey: String): List<MealLogEntity>

    @Query("SELECT * FROM meal_logs WHERE id = :id LIMIT 1")
    suspend fun getMealLogByIdOnce(id: Long): MealLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(log: MealLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLogs(logs: List<MealLogEntity>)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLogById(id: Long)

    @Query("DELETE FROM meal_logs")
    suspend fun deleteAllMealLogs()
}
