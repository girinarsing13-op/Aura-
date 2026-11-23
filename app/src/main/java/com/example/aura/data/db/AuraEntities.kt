package com.example.aura.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val age: Int,
    val height: Double,
    val weight: Double,
    val gender: String,
    val goal: String,
    val targetWeight: Double?,
    val durationMonths: Int,
    val experience: String,
    val environment: String,
    val homeEquipmentCsv: String,
    val gymEquipmentCsv: String = "",
    val calisthenicsEquipmentCsv: String = "Pull-up Bar,Dip Bars",
    val daysPerWeek: Int,
    val trainingSplit: String = "Upper / Lower – 4 Days",
    val customSplitCsv: String = "",
    val injuriesCsv: String,
    val focusBodyPartsCsv: String,
    val dietPreference: String,
    val trackCycle: Boolean,
    val periodWeekActive: Boolean,
    val themeAccent: String,
    val themeMode: String = "SYSTEM",
    val navTransparency: Float = 0.05f,
    val navBlur: Float = 0.80f,
    val sessionDurationMinutes: Int = 60,
    val wakeTimeMinutes: Int = 420,
    val hasWorkOrSchoolHours: Boolean = true,
    val workStartMinutes: Int = 540,
    val workEndMinutes: Int = 1020,
    val sleepTimeMinutes: Int = 1380,
    val preferredWorkoutMinutes: Int = 1080,
    val preferredWorkoutDaysCsv: String = "",
    val workoutReminderEnabled: Boolean = true,
    val sleepHours: Double = 7.5,
    val sleepQuality: String = "Good",
    val stressLevel: String = "Moderate",
    val lifestyleActivity: String = "Moderately Active",
    val cardioPreference: String = "LISS Walking",
    val postureFocus: String = "Neutral Spine",
    val warmupPreference: String = "Full Dynamic Mobility Primer",
    val budgetTier: String = "Moderate",
    val mealsPerDay: Int = 3,
    val waterLiters: Double = 3.0,
    val allergiesCsv: String = "",
    val proteinSupplement: Boolean = true,
    val startDate: String,
    val createdAt: String,
    val onboardingCompleted: Boolean = true,
    val weighInReminderEnabled: Boolean = true,
    val deloadActive: Boolean = false,
    val deloadStartDateKey: String = "",
    val deloadDismissedAtWeekKey: String = ""
)

@Entity(tableName = "blueprint_cache")
data class BlueprintCacheEntity(
    @PrimaryKey val id: Int = 1,
    val splitName: String,
    val derivedAt: String,
    val workoutJson: String,
    val nutritionJson: String,
    val metricsJson: String
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val dayNumber: Int,
    val dayTitle: String,
    val status: String,
    val isFullyCompleted: Boolean,
    val setsCompleted: Int,
    val totalSets: Int,
    val logsJson: String
)

@Entity(tableName = "daily_habits")
data class DailyHabitEntity(
    @PrimaryKey val dateKey: String,
    val workout: Boolean = false,
    val diet: Boolean = false,
    val water: Boolean = false,
    val steps: Boolean = false,
    val sleep: Boolean = false,
    val waterMl: Int = 0,
    val loggedCalories: Int = 0,
    val loggedProteinG: Int = 0,
    val eatenMealsCsv: String = ""
)

@Entity(tableName = "exercise_logs")
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long = 0,
    val date: String, // e.g. "2026-09-25"
    val exerciseId: String,
    val exerciseName: String,
    val equipment: String = "",
    val variation: String = "",
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val completed: Boolean,
    val notes: String = ""
)

@Entity(tableName = "exercise_substitutions")
data class ExerciseSubstitutionEntity(
    @PrimaryKey val originalExerciseId: String,
    val substitutedExerciseId: String,
    val substitutedExerciseName: String,
    val substitutedDate: String
)

@Entity(tableName = "body_check_ins")
data class BodyCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val weightKg: Double,
    val waistCm: Double? = null,
    val chestCm: Double? = null,
    val armCm: Double? = null,
    val photoPath: String? = null,
    val note: String = ""
)

@Entity(tableName = "achievements")
data class UnlockedAchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val unlockedDateKey: String,
    val unlockedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "foods",
    indices = [Index(value = ["name"])]
)
data class FoodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val servingDesc: String,
    val gramsPerServing: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val dietType: String, // "veg", "egg", "non-veg"
    val isVegan: Boolean = false,
    val allergensCsv: String = "",
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false,
    val lastUsedAt: Long = 0L
)

@Entity(
    tableName = "meal_logs",
    indices = [Index(value = ["dateKey"])]
)
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateKey: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val mealSlot: String, // "Breakfast", "Lunch", "Snack", "Dinner"
    val foodId: String,
    val foodName: String,
    val servingDesc: String,
    val gramsPerServing: Double,
    val quantity: Double,
    val unitMode: String, // "serving" or "grams"
    val gramsLogged: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val isQuickAdd: Boolean = false
)
