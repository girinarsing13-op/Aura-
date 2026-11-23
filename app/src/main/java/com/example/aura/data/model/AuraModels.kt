package com.example.aura.data.model

object EquipmentOptions {
    val GYM = listOf(
        "Barbell",
        "Dumbbells",
        "Cable Machine",
        "Smith Machine",
        "Leg Press",
        "Hack Squat",
        "Lat Pulldown",
        "Chest Press Machine",
        "Pull-up Bar",
        "Pec Deck / Fly",
        "Row Machine",
        "Leg Extension / Curl",
        "Shoulder Press Machine",
        "Dip Station",
        "Calf Raise Machine",
        "Preacher Curl"
    )

    val HOME = listOf(
        "Dumbbells",
        "Resistance Bands",
        "Bench",
        "Pull-up Bar",
        "Barbell",
        "Dip Station"
    )

    val CALISTHENICS = listOf(
        "Pull-up Bar",
        "Dip Bars",
        "Rings",
        "Parallettes"
    )

    val DEFAULT_CALISTHENICS = listOf("Pull-up Bar", "Dip Bars")
}

data class UserProfile(
    val id: Int = 1,
    val name: String = "",
    val age: Int = 26,
    val height: Double = 175.0,
    val weight: Double = 74.0,
    val gender: String = "Male", // "Male" or "Female"
    val goal: String = "Muscle Gain", // "Muscle Gain", "Fat Loss", "Recomposition", "General Fitness"
    val targetWeight: Double? = 78.0,
    val durationMonths: Int = 3,
    val experience: String = "Beginner", // "Beginner", "Intermediate", "Advanced"
    val environment: String = "Full Gym", // "Full Gym", "Home Workout", "Calisthenics"
    val homeEquipment: List<String> = listOf("Dumbbells"),
    val gymEquipment: List<String> = EquipmentOptions.GYM,
    val calisthenicsEquipment: List<String> = EquipmentOptions.DEFAULT_CALISTHENICS,
    val daysPerWeek: Int = 4,
    val trainingSplit: String = "Upper / Lower – 4 Days",
    val customSplitDays: List<String> = emptyList(),
    val injuries: List<String> = listOf("None"),
    val focusBodyParts: List<String> = emptyList(),
    val dietPreference: String = "Eggetarian", // "Vegetarian", "Eggetarian", "Non-Vegetarian", "Vegan"
    val trackCycle: Boolean = false,
    val periodWeekActive: Boolean = false,
    val themeAccent: String = "#E8B64C",
    val themeMode: String = "SYSTEM", // "DARK", "LIGHT", "SYSTEM"
    val navTransparency: Float = 0.05f,
    val navBlur: Float = 0.80f,
    val sessionDurationMinutes: Int = 60,
    val wakeTimeMinutes: Int = 7 * 60, // 07:00
    val hasWorkOrSchoolHours: Boolean = true,
    val workStartMinutes: Int = 9 * 60, // 09:00
    val workEndMinutes: Int = 17 * 60, // 17:00
    val sleepTimeMinutes: Int = 23 * 60, // 23:00
    val preferredWorkoutMinutes: Int = 18 * 60, // 18:00
    val preferredWorkoutDays: List<Int> = emptyList(), // 1=Mon .. 7=Sun
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
    val allergies: List<String> = listOf("None"),
    val proteinSupplement: Boolean = true,
    val startDate: String = "2026-09-25",
    val createdAt: String = "2026-09-25T00:00:00Z",
    val onboardingCompleted: Boolean = true,
    val weighInReminderEnabled: Boolean = true,
    val deloadActive: Boolean = false,
    val deloadStartDateKey: String = "",
    val deloadDismissedAtWeekKey: String = ""
) {
    fun isOnboardingComplete(): Boolean {
        return onboardingCompleted &&
            name.isNotBlank() &&
            goal.isNotBlank() &&
            experience.isNotBlank() &&
            environment.isNotBlank() &&
            daysPerWeek > 0 &&
            trainingSplit.isNotBlank()
    }
}

data class BodyCheckIn(
    val id: Long = 0,
    val dateKey: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val weightKg: Double,
    val waistCm: Double? = null,
    val chestCm: Double? = null,
    val armCm: Double? = null,
    val photoPath: String? = null,
    val note: String = ""
)

data class MacroTargets(
    val protein: Int,
    val fat: Int,
    val carbs: Int
)

data class PaceAssessment(
    val status: String,
    val rate: String,
    val badge: String,
    val isWarning: Boolean,
    val message: String
)

data class Metrics(
    val bmi: Double,
    val bmr: Int,
    val tdee: Int,
    val goalTargetCals: Int,
    val macroTargets: MacroTargets,
    val pace: PaceAssessment
)

data class Exercise(
    val id: String,
    val name: String,
    val muscle: String,
    val detail: String,
    val type: String, // "compound", "isolation"
    val equipment: List<String>,
    val loadPctBW: Double,
    val isBodyweight: Boolean,
    val injuries: List<String>,
    val baseSets: Int,
    val baseReps: String,
    val restSec: Int,
    val primaryMuscle: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val level: String = "intermediate", // "beginner", "intermediate", "advanced"
    val movementPattern: MovementPattern = MovementPattern.CARDIO_JUMP
) {
    fun getEffectivePrimary(): String = if (primaryMuscle.isNotBlank()) primaryMuscle else muscle.replaceFirstChar { it.uppercase() }
    fun getEffectiveSecondary(): List<String> = secondaryMuscles
    fun isTimedHold(): Boolean = baseReps.contains("sec", ignoreCase = true)
}

data class SetLog(
    val setNumber: Int,
    val prescribedWeightKg: Double,
    val prescribedReps: String,
    val completed: Boolean = false,
    val loggedWeightKg: Double? = null,
    val loggedReps: Int? = null
)

data class BlueprintExercise(
    val id: String,
    val name: String,
    val muscle: String,
    val detail: String,
    val type: String,
    val equipment: String,
    val isBodyweight: Boolean,
    val sets: Int,
    val reps: String,
    val restSeconds: Int,
    val recommendedLoadKg: Double,
    val prescribedSets: List<SetLog>,
    val isPriority: Boolean,
    val primaryMuscle: String = "",
    val secondaryMuscles: List<String> = emptyList(),
    val level: String = "intermediate",
    val movementPattern: MovementPattern? = null
) {
    fun getEffectivePrimary(): String = if (primaryMuscle.isNotBlank()) primaryMuscle else muscle.replaceFirstChar { it.uppercase() }
    fun getEffectiveSecondary(): List<String> = secondaryMuscles
    fun getEffectivePattern(): MovementPattern = movementPattern ?: MovementPattern.inferFrom(name, muscle)
    fun isTimedHold(): Boolean = reps.contains("sec", ignoreCase = true) ||
        name.contains("plank", ignoreCase = true) ||
        name.contains("l-sit", ignoreCase = true) ||
        name.contains("wall sit", ignoreCase = true) ||
        name.contains("lever", ignoreCase = true) ||
        name.contains("hollow body", ignoreCase = true) ||
        name.contains("handstand wall", ignoreCase = true)

    fun targetHoldSeconds(): Int {
        val nums = Regex("\\d+").findAll(reps).mapNotNull { it.value.toIntOrNull() }.toList()
        return nums.firstOrNull()?.coerceIn(10, 180) ?: 45
    }
}

data class WorkoutDayPlan(
    val dayNumber: Int,
    val dayTitle: String,
    val isRest: Boolean,
    val focus: String,
    val exercises: List<BlueprintExercise>,
    val targetMusclesText: String = "",
    val scheduledDate: String = "", // e.g. "Fri, Sep 25"
    val dateKey: String = "", // e.g. "2026-09-25"
    val isMissedOrRest: Boolean = false,
    val isCompleted: Boolean = false
)

data class WorkoutBlueprint(
    val splitName: String,
    val generatedAt: String,
    val week: List<WorkoutDayPlan>
)

data class ScaledMeal(
    val label: String, // "Breakfast", "Lunch", "Evening Fuel", "Dinner"
    val name: String,
    val portion: String,
    val cals: Int,
    val p: Int,
    val c: Int,
    val f: Int,
    val recipe: List<String>
)

data class NutritionDayPlan(
    val dayNumber: Int,
    val dayTitle: String,
    val totalCals: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFats: Int,
    val meals: List<ScaledMeal>
)

data class NutritionBlueprint(
    val targetCals: Int,
    val targetMacros: MacroTargets,
    val generatedAt: String,
    val week: List<NutritionDayPlan>
)

data class Blueprint(
    val derivedAt: String,
    val metrics: Metrics,
    val workout: WorkoutBlueprint,
    val nutrition: NutritionBlueprint
)

data class WorkoutSessionRecord(
    val id: Long = 0,
    val date: String,
    val dayNumber: Int,
    val dayTitle: String,
    val status: String,
    val isFullyCompleted: Boolean,
    val setsCompleted: Int,
    val totalSets: Int,
    val exerciseNames: List<String> = emptyList()
)

data class DailyHabitStatus(
    val dateKey: String, // "YYYY-MM-DD"
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
