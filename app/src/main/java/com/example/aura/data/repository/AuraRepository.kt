package com.example.aura.data.repository

import android.content.Context
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
import com.example.aura.data.model.Blueprint
import com.example.aura.data.model.BodyCheckIn
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.MacroTargets
import com.example.aura.data.model.Metrics
import com.example.aura.data.model.NutritionBlueprint
import com.example.aura.data.model.PaceAssessment
import com.example.aura.data.model.ScaledMeal
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutBlueprint
import com.example.aura.data.model.WorkoutSessionRecord
import com.example.aura.engine.AchievementEngine
import com.example.aura.engine.AuraTrainingMemoryEngine
import com.example.aura.engine.ExerciseDB
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.engine.LoggedSetData
import com.example.aura.engine.NutritionBlueprintEngine
import com.example.aura.engine.NutritionTrackingEngine
import com.example.aura.engine.NormalizedSplit
import com.example.aura.engine.WorkoutBlueprintEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuraRepository(private val dao: AuraDao) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val workoutBlueprintAdapter = moshi.adapter(WorkoutBlueprint::class.java)
    private val nutritionBlueprintAdapter = moshi.adapter(NutritionBlueprint::class.java)
    private val metricsAdapter = moshi.adapter(Metrics::class.java)

    val exerciseLogsFlow: Flow<List<ExerciseLogEntity>> = dao.getAllExerciseLogs()

    val bodyCheckInsFlow: Flow<List<BodyCheckIn>> = dao.getAllBodyCheckIns().map { list ->
        list.map { e ->
            BodyCheckIn(
                id = e.id,
                dateKey = e.dateKey,
                timestampMillis = e.timestampMillis,
                weightKg = e.weightKg,
                waistCm = e.waistCm,
                chestCm = e.chestCm,
                armCm = e.armCm,
                photoPath = e.photoPath,
                note = e.note
            )
        }
    }

    val achievementsFlow: Flow<List<UnlockedAchievementEntity>> = dao.getAllAchievements()

    val foodsFlow: Flow<List<FoodEntity>> = dao.getAllFoodsFlow()
    val mealLogsFlow: Flow<List<MealLogEntity>> = dao.getAllMealLogsFlow()
    private val seedMutex = Mutex()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            dao.getAllExerciseLogs().collect { list ->
                val grouped = list.groupBy { it.exerciseName to it.date }
                grouped.forEach { (key, setEntities) ->
                    val first = setEntities.first()
                    AuraTrainingMemoryEngine.recordExerciseSets(
                        sessionId = first.sessionId,
                        date = first.date,
                        exerciseId = first.exerciseId,
                        exerciseName = first.exerciseName,
                        equipment = first.equipment,
                        variation = first.variation,
                        sets = setEntities.map {
                            LoggedSetData(
                                setNumber = it.setNumber,
                                weightKg = it.weightKg,
                                reps = it.reps,
                                completed = it.completed
                            )
                        },
                        notes = first.notes
                    )
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            dao.getAllSubstitutions().collect { list ->
                list.forEach { sub ->
                    AuraTrainingMemoryEngine.recordSubstitution(
                        sub.originalExerciseId,
                        sub.substitutedExerciseName
                    )
                }
            }
        }
    }

    val userProfileFlow: Flow<UserProfile?> = dao.getUserProfile().map { entity ->
        entity?.toModel()
    }

    suspend fun getUserProfileOnce(): UserProfile? {
        return dao.getUserProfileOnce()?.toModel()
    }

    val workoutSessionsFlow: Flow<List<WorkoutSessionRecord>> = dao.getAllWorkoutSessions().map { list ->
        list.map { entity ->
            WorkoutSessionRecord(
                id = entity.id,
                date = entity.date,
                dayNumber = entity.dayNumber,
                dayTitle = entity.dayTitle,
                status = entity.status,
                isFullyCompleted = entity.isFullyCompleted,
                setsCompleted = entity.setsCompleted,
                totalSets = entity.totalSets
            )
        }
    }

    val dailyHabitsFlow: Flow<Map<String, DailyHabitStatus>> = dao.getAllDailyHabits().map { list ->
        list.associate { entity ->
            entity.dateKey to DailyHabitStatus(
                dateKey = entity.dateKey,
                workout = entity.workout,
                diet = entity.diet,
                water = entity.water,
                steps = entity.steps,
                sleep = entity.sleep,
                waterMl = entity.waterMl,
                loggedCalories = entity.loggedCalories,
                loggedProteinG = entity.loggedProteinG,
                eatenMealsCsv = entity.eatenMealsCsv
            )
        }
    }

    fun isValidBlueprintForProfile(blueprint: Blueprint, profile: UserProfile): Boolean {
        val workout = blueprint.workout
        val hasManualShift = workout.week.any { it.isMissedOrRest }
        // 1. workout.week.size == 7 (or 7..10 if a workout was manually shifted forward)
        if (!hasManualShift && workout.week.size != 7) return false
        if (hasManualShift && workout.week.size !in 7..10) return false
        // 2. actual training-day count == profile.daysPerWeek
        val requestedDays = profile.daysPerWeek.coerceIn(1, 7)
        val actualTrainingDays = workout.week.count { !it.isRest }
        if (actualTrainingDays != requestedDays) return false
        // 3. cached split corresponds to profile.trainingSplit using normalized identifier
        val profileNormalizedSplit = NormalizedSplit.from(profile.trainingSplit)
        val workoutNormalizedSplit = NormalizedSplit.from(workout.splitName)
        if (profileNormalizedSplit != workoutNormalizedSplit) return false
        // 4. every training day contains exercises
        val allTrainingDaysHaveExercises = workout.week.all { it.isRest || it.exercises.isNotEmpty() }
        if (!allTrainingDaysHaveExercises) return false
        // 5. exercises are compatible with profile.environment
        val equipPool = WorkoutBlueprintEngine.buildEquipmentPool(profile)
        val userInjuries = if (profile.injuries.isEmpty()) listOf("None") else profile.injuries
        val exercises = workout.week.filter { !it.isRest }.flatMap { it.exercises }
        val allCompatible = exercises.all { ex ->
            val dbEx = ExerciseDB.list.find { it.id == ex.id || it.name.equals(ex.name, ignoreCase = true) }
            if (dbEx != null) {
                WorkoutBlueprintEngine.isExerciseAllowedForEnvironment(dbEx, profile.environment, equipPool, userInjuries)
            } else true
        }
        if (!allCompatible) return false
        // 6. exercises do not conflict with profile.injuries
        if (!userInjuries.contains("None")) {
            val hasConflict = exercises.any { ex ->
                val dbEx = ExerciseDB.list.find { it.id == ex.id || it.name.equals(ex.name, ignoreCase = true) }
                dbEx != null && WorkoutBlueprintEngine.hasInjuryConflict(dbEx.injuries, userInjuries, ex.name)
            }
            if (hasConflict) return false
        }
        // 7. day structure corresponds to selected split (skip positional check when user manually shifted a workout)
        if (!hasManualShift) {
            val expectedSpec = WorkoutBlueprintEngine.resolveSplitDays(profile).second
            if (expectedSpec.size != 7) return false
            val structureMatches = workout.week.indices.all { i ->
                workout.week[i].isRest == expectedSpec[i].isRest &&
                    (expectedSpec[i].isRest || workout.week[i].focus.contains(expectedSpec[i].title, ignoreCase = true) || expectedSpec[i].title.contains(workout.week[i].focus, ignoreCase = true))
            }
            if (!structureMatches) return false
        }
        // 8. cached blueprint was generated from the CURRENT profile configuration
        val expectedBMI = FitnessCalcEngine.calcBMI(profile.weight, profile.height)
        if (kotlin.math.abs(blueprint.metrics.bmi - expectedBMI) > 0.5) return false

        val expectedBmr = FitnessCalcEngine.calcBMR(profile.weight, profile.height, profile.age, profile.gender)
        val expectedTdee = FitnessCalcEngine.calcTDEE(expectedBmr, profile.daysPerWeek)
        val expectedGoalCals = FitnessCalcEngine.calcGoalTargetCals(
            tdee = expectedTdee,
            goal = profile.goal,
            bmr = expectedBmr,
            weightKg = profile.weight,
            targetWeightKg = profile.targetWeight,
            durationMonths = profile.durationMonths
        )
        if (kotlin.math.abs(blueprint.metrics.goalTargetCals - expectedGoalCals) > 5) return false

        return true
    }

    fun getBlueprintFlow(profile: UserProfile): Flow<Blueprint?> = dao.getBlueprintCache().map { cache ->
        if (cache != null) {
            try {
                val workout = workoutBlueprintAdapter.fromJson(cache.workoutJson)
                val nutrition = nutritionBlueprintAdapter.fromJson(cache.nutritionJson)
                val metrics = metricsAdapter.fromJson(cache.metricsJson)
                if (workout != null && nutrition != null && metrics != null) {
                    val candidate = Blueprint(
                        derivedAt = cache.derivedAt,
                        metrics = metrics,
                        workout = workout,
                        nutrition = nutrition
                    )
                    if (isValidBlueprintForProfile(candidate, profile)) {
                        return@map candidate
                    }
                }
            } catch (e: Exception) {
                // fallback to calculation
            }
        }
        // Fallback: calculate fresh blueprint and save
        val fresh = calculateFreshBlueprint(profile)
        CoroutineScope(Dispatchers.IO).launch {
            saveBlueprint(fresh)
        }
        fresh
    }

    suspend fun getBlueprintOnce(profile: UserProfile): Blueprint {
        val cache = dao.getBlueprintCacheOnce()
        if (cache != null) {
            try {
                val workout = workoutBlueprintAdapter.fromJson(cache.workoutJson)
                val nutrition = nutritionBlueprintAdapter.fromJson(cache.nutritionJson)
                val metrics = metricsAdapter.fromJson(cache.metricsJson)
                if (workout != null && nutrition != null && metrics != null) {
                    val candidate = Blueprint(
                        derivedAt = cache.derivedAt,
                        metrics = metrics,
                        workout = workout,
                        nutrition = nutrition
                    )
                    if (isValidBlueprintForProfile(candidate, profile)) {
                        return candidate
                    }
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        val fresh = calculateFreshBlueprint(profile)
        saveBlueprint(fresh)
        return fresh
    }

    fun calculateFreshBlueprint(profile: UserProfile): Blueprint {
        val bmi = FitnessCalcEngine.calcBMI(profile.weight, profile.height)
        val bmr = FitnessCalcEngine.calcBMR(profile.weight, profile.height, profile.age, profile.gender)
        val tdee = FitnessCalcEngine.calcTDEE(bmr, profile.daysPerWeek)
        val goalCals = FitnessCalcEngine.calcGoalTargetCals(
            tdee = tdee,
            goal = profile.goal,
            bmr = bmr,
            weightKg = profile.weight,
            targetWeightKg = profile.targetWeight,
            durationMonths = profile.durationMonths
        )
        val macros = FitnessCalcEngine.calcMacroTargets(goalCals, profile.weight, profile.goal)
        val pace = FitnessCalcEngine.calcPaceAssessment(profile.weight, profile.targetWeight, profile.durationMonths, profile.goal)

        val metrics = Metrics(
            bmi = bmi,
            bmr = bmr,
            tdee = tdee,
            goalTargetCals = goalCals,
            macroTargets = macros,
            pace = pace
        )

        val workout = WorkoutBlueprintEngine.generate(profile)
        val nutrition = NutritionBlueprintEngine.generate(profile, goalCals, macros)

        return Blueprint(
            derivedAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date()),
            metrics = metrics,
            workout = workout,
            nutrition = nutrition
        )
    }

    suspend fun saveProfileAndBlueprint(profile: UserProfile, blueprint: Blueprint) {
        dao.insertUserProfile(profile.toEntity())
        val cache = BlueprintCacheEntity(
            id = 1,
            splitName = blueprint.workout.splitName,
            derivedAt = blueprint.derivedAt,
            workoutJson = workoutBlueprintAdapter.toJson(blueprint.workout),
            nutritionJson = nutritionBlueprintAdapter.toJson(blueprint.nutrition),
            metricsJson = metricsAdapter.toJson(blueprint.metrics)
        )
        dao.insertBlueprintCache(cache)
    }

    suspend fun saveUserProfileOnly(profile: UserProfile) {
        dao.insertUserProfile(profile.toEntity())
    }

    suspend fun saveBlueprint(blueprint: Blueprint) {
        val cache = BlueprintCacheEntity(
            id = 1,
            splitName = blueprint.workout.splitName,
            derivedAt = blueprint.derivedAt,
            workoutJson = workoutBlueprintAdapter.toJson(blueprint.workout),
            nutritionJson = nutritionBlueprintAdapter.toJson(blueprint.nutrition),
            metricsJson = metricsAdapter.toJson(blueprint.metrics)
        )
        dao.insertBlueprintCache(cache)
    }

    suspend fun saveSingleSetLog(
        dateKey: String,
        exerciseId: String,
        exerciseName: String,
        equipment: String,
        variation: String,
        setNumber: Int,
        weightKg: Double,
        reps: Int,
        completed: Boolean
    ) {
        if (!completed) {
            dao.deleteExerciseLogForSet(dateKey, exerciseId, setNumber)
            return
        }
        val existing = dao.getExerciseLogForSet(dateKey, exerciseId, setNumber)
        val entity = if (existing != null) {
            existing.copy(
                weightKg = weightKg,
                reps = reps,
                completed = true
            )
        } else {
            ExerciseLogEntity(
                sessionId = 0,
                date = dateKey,
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                equipment = equipment,
                variation = variation,
                setNumber = setNumber,
                weightKg = weightKg,
                reps = reps,
                completed = true
            )
        }
        dao.insertExerciseLog(entity)
    }

    suspend fun recordWorkoutSession(
        dayNumber: Int,
        dayTitle: String,
        status: String,
        isFullyCompleted: Boolean,
        setsCompleted: Int,
        totalSets: Int,
        dateStr: String,
        todayKey: String,
        exerciseLogs: List<ExerciseLogEntity> = emptyList()
    ) {
        val entity = WorkoutSessionEntity(
            date = todayKey,
            dayNumber = dayNumber,
            dayTitle = dayTitle,
            status = status,
            isFullyCompleted = isFullyCompleted,
            setsCompleted = setsCompleted,
            totalSets = totalSets,
            logsJson = "{}"
        )
        val sessionId = dao.insertWorkoutSession(entity)
        dao.attachSessionIdToDateLogs(todayKey, sessionId)
        if (exerciseLogs.isNotEmpty()) {
            val toInsert = mutableListOf<ExerciseLogEntity>()
            for (log in exerciseLogs) {
                val existing = dao.getExerciseLogForSet(log.date, log.exerciseId, log.setNumber)
                if (existing != null) {
                    toInsert.add(
                        existing.copy(
                            sessionId = sessionId,
                            weightKg = log.weightKg,
                            reps = log.reps,
                            completed = log.completed
                        )
                    )
                } else if (log.completed) {
                    toInsert.add(log.copy(sessionId = sessionId))
                }
            }
            if (toInsert.isNotEmpty()) {
                dao.insertExerciseLogs(toInsert)
            }
        }
        if (setsCompleted > 0 || isFullyCompleted) {
            val existing = dao.getDailyHabitByDate(todayKey)
            val updated = existing?.copy(workout = true) ?: DailyHabitEntity(dateKey = todayKey, workout = true)
            dao.insertDailyHabit(updated)
        }
    }

    suspend fun saveBodyCheckIn(checkIn: BodyCheckIn) {
        dao.insertBodyCheckIn(
            BodyCheckInEntity(
                id = checkIn.id,
                dateKey = checkIn.dateKey,
                timestampMillis = checkIn.timestampMillis,
                weightKg = checkIn.weightKg,
                waistCm = checkIn.waistCm,
                chestCm = checkIn.chestCm,
                armCm = checkIn.armCm,
                photoPath = checkIn.photoPath,
                note = checkIn.note
            )
        )
        val currentProfile = dao.getUserProfileOnce()?.toModel()
        if (currentProfile != null && kotlin.math.abs(currentProfile.weight - checkIn.weightKg) >= 0.1) {
            val updated = currentProfile.copy(weight = checkIn.weightKg)
            dao.insertUserProfile(updated.toEntity())
        }
    }

    suspend fun deleteBodyCheckIn(id: Long) {
        dao.deleteBodyCheckInById(id)
    }

    suspend fun syncAchievements(profile: UserProfile, todayKey: String) {
        val sessions = dao.getAllWorkoutSessionsOnce().map { entity ->
            WorkoutSessionRecord(
                id = entity.id,
                date = entity.date,
                dayNumber = entity.dayNumber,
                dayTitle = entity.dayTitle,
                status = entity.status,
                isFullyCompleted = entity.isFullyCompleted,
                setsCompleted = entity.setsCompleted,
                totalSets = entity.totalSets
            )
        }
        val habits = dao.getAllDailyHabitsOnce().associate { e ->
            e.dateKey to DailyHabitStatus(
                dateKey = e.dateKey,
                workout = e.workout,
                diet = e.diet,
                water = e.water,
                steps = e.steps,
                sleep = e.sleep,
                waterMl = e.waterMl,
                loggedCalories = e.loggedCalories,
                loggedProteinG = e.loggedProteinG,
                eatenMealsCsv = e.eatenMealsCsv
            )
        }
        val logs = dao.getAllExerciseLogsOnce()
        val checkIns = dao.getAllBodyCheckInsOnce().map { e ->
            BodyCheckIn(
                id = e.id,
                dateKey = e.dateKey,
                timestampMillis = e.timestampMillis,
                weightKg = e.weightKg,
                waistCm = e.waistCm,
                chestCm = e.chestCm,
                armCm = e.armCm,
                photoPath = e.photoPath,
                note = e.note
            )
        }
        val persisted = dao.getAllAchievementsOnce()
        val newUnlocks = AchievementEngine.findNewlyUnlockedEntities(
            profile = profile,
            sessions = sessions,
            habits = habits,
            logs = logs,
            checkIns = checkIns,
            existingPersisted = persisted,
            todayKey = todayKey
        )
        if (newUnlocks.isNotEmpty()) {
            dao.insertAchievements(newUnlocks)
        }
    }

    suspend fun exportBackupJson(): String = AuraBackupManager.exportToJson(dao)

    suspend fun importBackupJson(rawJson: String): Result<AuraBackupPayload> = AuraBackupManager.importFromJson(dao, rawJson)

    suspend fun recordSubstitution(originalId: String, substitutedId: String, substitutedName: String) {
        val dateNow = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        dao.insertSubstitution(
            ExerciseSubstitutionEntity(
                originalExerciseId = originalId,
                substitutedExerciseId = substitutedId,
                substitutedExerciseName = substitutedName,
                substitutedDate = dateNow
            )
        )
        AuraTrainingMemoryEngine.recordSubstitution(originalId, substitutedName)
    }

    suspend fun toggleHabit(dateKey: String, habitType: String) {
        val existing = dao.getDailyHabitByDate(dateKey) ?: DailyHabitEntity(dateKey = dateKey)
        val updated = when (habitType.lowercase()) {
            "workout" -> existing.copy(workout = !existing.workout)
            "diet" -> existing.copy(diet = !existing.diet)
            "water" -> existing.copy(water = !existing.water)
            "steps" -> existing.copy(steps = !existing.steps)
            "sleep" -> existing.copy(sleep = !existing.sleep)
            else -> existing
        }
        dao.insertDailyHabit(updated)
    }

    suspend fun addWaterMl(dateKey: String, deltaMl: Int, targetWaterMl: Int) {
        val existing = dao.getDailyHabitByDate(dateKey) ?: DailyHabitEntity(dateKey = dateKey)
        val nextMl = (existing.waterMl + deltaMl).coerceIn(0, 8000)
        val reachedTarget = nextMl >= targetWaterMl.coerceAtLeast(500)
        dao.insertDailyHabit(
            existing.copy(
                waterMl = nextMl,
                water = existing.water || reachedTarget
            )
        )
    }

    suspend fun toggleMealEaten(
        dateKey: String,
        mealIndex: Int,
        allMeals: List<com.example.aura.data.model.ScaledMeal>,
        targetCals: Int
    ) {
        val existing = dao.getDailyHabitByDate(dateKey) ?: DailyHabitEntity(dateKey = dateKey)
        val currentIndices = existing.eatenMealsCsv
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toMutableSet()
        if (currentIndices.contains(mealIndex)) {
            currentIndices.remove(mealIndex)
        } else {
            currentIndices.add(mealIndex)
        }
        val sorted = currentIndices.filter { it in allMeals.indices }.sorted()
        val sumCals = sorted.sumOf { allMeals[it].cals }
        val sumProtein = sorted.sumOf { allMeals[it].p }
        val dietDone = allMeals.isNotEmpty() && (sorted.size == allMeals.size || (targetCals > 0 && sumCals >= (targetCals * 0.85).toInt()))
        dao.insertDailyHabit(
            existing.copy(
                eatenMealsCsv = sorted.joinToString(","),
                loggedCalories = sumCals,
                loggedProteinG = sumProtein,
                diet = dietDone
            )
        )
    }

    suspend fun ensureFoodCatalogSeeded(context: Context) {
        seedMutex.withLock {
            if (dao.getFoodCount() == 0) {
                val seeded = NutritionTrackingEngine.loadFoodsFromAssets(context)
                if (seeded.isNotEmpty()) {
                    dao.insertFoodsIgnore(seeded)
                }
            }
        }
    }

    suspend fun seedFoodsList(foods: List<FoodEntity>) {
        seedMutex.withLock {
            if (foods.isNotEmpty()) {
                dao.insertFoodsIgnore(foods)
            }
        }
    }

    private suspend fun syncDailyHabitNutritionFromLogs(
        dateKey: String,
        targetCals: Int,
        targetProteinG: Int
    ) {
        val logs = dao.getMealLogsForDateOnce(dateKey)
        val totalCals = logs.sumOf { it.calories }
        val totalProtein = logs.sumOf { it.protein }.roundToInt()
        val existing = dao.getDailyHabitByDate(dateKey) ?: DailyHabitEntity(dateKey = dateKey)
        val dietDone = logs.isNotEmpty() && (
            (targetProteinG > 0 && totalProtein >= (targetProteinG * 0.8).toInt()) ||
                (targetCals > 0 && totalCals >= (targetCals * 0.85).toInt())
            )
        dao.insertDailyHabit(
            existing.copy(
                loggedCalories = totalCals,
                loggedProteinG = totalProtein,
                diet = dietDone
            )
        )
    }

    suspend fun logFood(
        food: FoodEntity,
        quantity: Double,
        unitMode: String,
        mealSlot: String,
        dateKey: String,
        targetCals: Int,
        targetProteinG: Int
    ) {
        val scaled = NutritionTrackingEngine.computeScaledMacros(food, quantity, unitMode)
        val now = System.currentTimeMillis()
        dao.insertMealLog(
            MealLogEntity(
                dateKey = dateKey,
                timestampMillis = now,
                mealSlot = mealSlot,
                foodId = food.id,
                foodName = food.name,
                servingDesc = food.servingDesc,
                gramsPerServing = food.gramsPerServing,
                quantity = quantity,
                unitMode = unitMode,
                gramsLogged = scaled.gramsLogged,
                calories = scaled.calories,
                protein = scaled.protein,
                carbs = scaled.carbs,
                fat = scaled.fat,
                isQuickAdd = false
            )
        )
        dao.markFoodUsed(food.id, now)
        syncDailyHabitNutritionFromLogs(dateKey, targetCals, targetProteinG)
    }

    suspend fun logCustomFood(
        name: String,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        dietType: String,
        saveToCatalog: Boolean,
        mealSlot: String,
        dateKey: String,
        targetCals: Int,
        targetProteinG: Int
    ) {
        val now = System.currentTimeMillis()
        val cleanName = name.trim().ifBlank { "Custom Meal" }
        val foodId = "custom_${now}_${cleanName.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_").take(16)}"
        if (saveToCatalog) {
            dao.insertFoodReplace(
                FoodEntity(
                    id = foodId,
                    name = cleanName,
                    servingDesc = "1 serving",
                    gramsPerServing = 100.0,
                    calories = calories.coerceAtLeast(0),
                    protein = protein.coerceAtLeast(0.0),
                    carbs = carbs.coerceAtLeast(0.0),
                    fat = fat.coerceAtLeast(0.0),
                    dietType = dietType,
                    isVegan = dietType == "veg",
                    allergensCsv = "",
                    isCustom = true,
                    isFavorite = false,
                    lastUsedAt = now
                )
            )
        }
        dao.insertMealLog(
            MealLogEntity(
                dateKey = dateKey,
                timestampMillis = now,
                mealSlot = mealSlot,
                foodId = foodId,
                foodName = cleanName,
                servingDesc = "1 serving",
                gramsPerServing = 100.0,
                quantity = 1.0,
                unitMode = "serving",
                gramsLogged = 100.0,
                calories = calories.coerceAtLeast(0),
                protein = protein.coerceAtLeast(0.0),
                carbs = carbs.coerceAtLeast(0.0),
                fat = fat.coerceAtLeast(0.0),
                isQuickAdd = false
            )
        )
        syncDailyHabitNutritionFromLogs(dateKey, targetCals, targetProteinG)
    }

    suspend fun quickAddNutrition(
        calories: Int,
        protein: Double,
        label: String,
        mealSlot: String,
        dateKey: String,
        targetCals: Int,
        targetProteinG: Int
    ) {
        val now = System.currentTimeMillis()
        val displayName = label.trim().ifBlank { "Quick Add (${calories} kcal, ${protein.roundToInt()}g P)" }
        dao.insertMealLog(
            MealLogEntity(
                dateKey = dateKey,
                timestampMillis = now,
                mealSlot = mealSlot,
                foodId = "quick_$now",
                foodName = displayName,
                servingDesc = "Quick add",
                gramsPerServing = 100.0,
                quantity = 1.0,
                unitMode = "serving",
                gramsLogged = 100.0,
                calories = calories.coerceAtLeast(0),
                protein = protein.coerceAtLeast(0.0),
                carbs = 0.0,
                fat = 0.0,
                isQuickAdd = true
            )
        )
        syncDailyHabitNutritionFromLogs(dateKey, targetCals, targetProteinG)
    }

    suspend fun updateMealLog(
        logId: Long,
        newQuantity: Double,
        newUnitMode: String,
        newMealSlot: String,
        targetCals: Int,
        targetProteinG: Int
    ) {
        val existing = dao.getMealLogByIdOnce(logId) ?: return
        val linkedFood = dao.getFoodById(existing.foodId)
        val updated = if (linkedFood != null && !existing.isQuickAdd) {
            val scaled = NutritionTrackingEngine.computeScaledMacros(linkedFood, newQuantity, newUnitMode)
            existing.copy(
                mealSlot = newMealSlot,
                quantity = newQuantity,
                unitMode = newUnitMode,
                gramsLogged = scaled.gramsLogged,
                calories = scaled.calories,
                protein = scaled.protein,
                carbs = scaled.carbs,
                fat = scaled.fat
            )
        } else {
            val oldMult = if (existing.unitMode == "grams") {
                (existing.quantity / existing.gramsPerServing.coerceAtLeast(1.0)).coerceAtLeast(0.01)
            } else {
                existing.quantity.coerceAtLeast(0.01)
            }
            val baseCals = (existing.calories / oldMult).roundToInt()
            val baseP = existing.protein / oldMult
            val baseC = existing.carbs / oldMult
            val baseF = existing.fat / oldMult
            val scaled = NutritionTrackingEngine.computeScaledMacrosFromBase(
                baseGramsPerServing = existing.gramsPerServing,
                baseCaloriesAtServing = baseCals,
                baseProteinAtServing = baseP,
                baseCarbsAtServing = baseC,
                baseFatAtServing = baseF,
                quantity = newQuantity,
                unitMode = newUnitMode
            )
            existing.copy(
                mealSlot = newMealSlot,
                quantity = newQuantity,
                unitMode = newUnitMode,
                gramsLogged = scaled.gramsLogged,
                calories = scaled.calories,
                protein = scaled.protein,
                carbs = scaled.carbs,
                fat = scaled.fat
            )
        }
        dao.insertMealLog(updated)
        syncDailyHabitNutritionFromLogs(existing.dateKey, targetCals, targetProteinG)
    }

    suspend fun deleteMealLog(
        logId: Long,
        targetCals: Int,
        targetProteinG: Int
    ) {
        val existing = dao.getMealLogByIdOnce(logId) ?: return
        dao.deleteMealLogById(logId)
        syncDailyHabitNutritionFromLogs(existing.dateKey, targetCals, targetProteinG)
    }

    suspend fun toggleFoodFavorite(foodId: String, isFavorite: Boolean) {
        dao.setFoodFavorite(foodId, isFavorite)
    }

    suspend fun logSuggestedPlanMeal(
        meal: ScaledMeal,
        mealSlot: String,
        dateKey: String,
        targetCals: Int,
        targetProteinG: Int
    ) {
        val entity = NutritionTrackingEngine.suggestedMealToLogEntity(meal, dateKey, mealSlot)
        dao.insertMealLog(entity)
        syncDailyHabitNutritionFromLogs(dateKey, targetCals, targetProteinG)
    }

    suspend fun copyYesterdaysMeals(
        todayKey: String,
        yesterdayKey: String,
        targetCals: Int,
        targetProteinG: Int
    ): Int {
        val yesterdayLogs = dao.getMealLogsForDateOnce(yesterdayKey)
        if (yesterdayLogs.isEmpty()) return 0
        val now = System.currentTimeMillis()
        val copies = yesterdayLogs.mapIndexed { idx, old ->
            old.copy(
                id = 0,
                dateKey = todayKey,
                timestampMillis = now + idx
            )
        }
        dao.insertMealLogs(copies)
        syncDailyHabitNutritionFromLogs(todayKey, targetCals, targetProteinG)
        return copies.size
    }

    suspend fun resetAllData() {
        dao.deleteUserProfile()
        dao.deleteBlueprintCache()
        dao.deleteAllWorkoutSessions()
        dao.deleteAllDailyHabits()
        dao.deleteAllExerciseLogs()
        dao.deleteAllSubstitutions()
        dao.deleteAllBodyCheckIns()
        dao.deleteAllAchievements()
        dao.deleteCustomFoods()
        dao.deleteAllMealLogs()
    }

    private fun encodeEquipCsv(items: List<String>): String {
        return if (items.isEmpty()) EQUIP_EMPTY_SENTINEL else items.joinToString(",")
    }

    private fun decodeEquipCsv(csv: String, legacyDefault: List<String>): List<String> {
        return when {
            csv == EQUIP_EMPTY_SENTINEL -> emptyList()
            csv.isBlank() -> legacyDefault
            else -> csv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
    }

    private fun UserProfile.toEntity(): UserProfileEntity {
        return UserProfileEntity(
            id = 1,
            name = name,
            age = age,
            height = height,
            weight = weight,
            gender = gender,
            goal = goal,
            targetWeight = targetWeight,
            durationMonths = durationMonths,
            experience = experience,
            environment = environment,
            homeEquipmentCsv = encodeEquipCsv(homeEquipment),
            gymEquipmentCsv = encodeEquipCsv(gymEquipment),
            calisthenicsEquipmentCsv = encodeEquipCsv(calisthenicsEquipment),
            daysPerWeek = daysPerWeek,
            trainingSplit = trainingSplit,
            customSplitCsv = customSplitDays.joinToString("||"),
            injuriesCsv = injuries.joinToString(","),
            focusBodyPartsCsv = focusBodyParts.joinToString(","),
            dietPreference = dietPreference,
            trackCycle = trackCycle,
            periodWeekActive = periodWeekActive,
            themeAccent = themeAccent,
            themeMode = themeMode,
            navTransparency = navTransparency,
            navBlur = navBlur,
            sessionDurationMinutes = sessionDurationMinutes,
            wakeTimeMinutes = wakeTimeMinutes,
            hasWorkOrSchoolHours = hasWorkOrSchoolHours,
            workStartMinutes = workStartMinutes,
            workEndMinutes = workEndMinutes,
            sleepTimeMinutes = sleepTimeMinutes,
            preferredWorkoutMinutes = preferredWorkoutMinutes,
            preferredWorkoutDaysCsv = preferredWorkoutDays.joinToString(","),
            workoutReminderEnabled = workoutReminderEnabled,
            sleepHours = sleepHours,
            sleepQuality = sleepQuality,
            stressLevel = stressLevel,
            lifestyleActivity = lifestyleActivity,
            cardioPreference = cardioPreference,
            postureFocus = postureFocus,
            warmupPreference = warmupPreference,
            budgetTier = budgetTier,
            mealsPerDay = mealsPerDay,
            waterLiters = waterLiters,
            allergiesCsv = if (allergies.isEmpty()) "None" else allergies.joinToString(","),
            proteinSupplement = proteinSupplement,
            startDate = startDate,
            createdAt = createdAt,
            onboardingCompleted = onboardingCompleted,
            weighInReminderEnabled = weighInReminderEnabled,
            deloadActive = deloadActive,
            deloadStartDateKey = deloadStartDateKey,
            deloadDismissedAtWeekKey = deloadDismissedAtWeekKey
        )
    }

    private fun UserProfileEntity.toModel(): UserProfile {
        return UserProfile(
            id = 1,
            name = name,
            age = age,
            height = height,
            weight = weight,
            gender = gender,
            goal = goal,
            targetWeight = targetWeight,
            durationMonths = durationMonths,
            experience = experience,
            environment = environment,
            homeEquipment = decodeEquipCsv(homeEquipmentCsv, listOf("Dumbbells")),
            gymEquipment = decodeEquipCsv(gymEquipmentCsv, com.example.aura.data.model.EquipmentOptions.GYM),
            calisthenicsEquipment = decodeEquipCsv(calisthenicsEquipmentCsv, com.example.aura.data.model.EquipmentOptions.DEFAULT_CALISTHENICS),
            daysPerWeek = daysPerWeek,
            trainingSplit = trainingSplit.ifBlank { "Upper / Lower – 4 Days" },
            customSplitDays = if (customSplitCsv.isBlank()) emptyList() else customSplitCsv.split("||"),
            injuries = if (injuriesCsv.isBlank()) listOf("None") else injuriesCsv.split(","),
            focusBodyParts = if (focusBodyPartsCsv.isBlank()) emptyList() else com.example.aura.data.model.PriorityMuscle.normalize(focusBodyPartsCsv.split(",")),
            dietPreference = dietPreference,
            trackCycle = trackCycle,
            periodWeekActive = periodWeekActive,
            themeAccent = themeAccent,
            themeMode = themeMode,
            navTransparency = navTransparency,
            navBlur = navBlur,
            sessionDurationMinutes = sessionDurationMinutes,
            wakeTimeMinutes = wakeTimeMinutes,
            hasWorkOrSchoolHours = hasWorkOrSchoolHours,
            workStartMinutes = workStartMinutes,
            workEndMinutes = workEndMinutes,
            sleepTimeMinutes = sleepTimeMinutes,
            preferredWorkoutMinutes = preferredWorkoutMinutes,
            preferredWorkoutDays = preferredWorkoutDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it in 1..7 }.distinct(),
            workoutReminderEnabled = workoutReminderEnabled,
            sleepHours = sleepHours,
            sleepQuality = sleepQuality,
            stressLevel = stressLevel,
            lifestyleActivity = lifestyleActivity,
            cardioPreference = cardioPreference,
            postureFocus = postureFocus,
            warmupPreference = warmupPreference,
            budgetTier = budgetTier,
            mealsPerDay = mealsPerDay,
            waterLiters = waterLiters,
            allergies = if (allergiesCsv.isBlank() || allergiesCsv.equals("None", ignoreCase = true)) listOf("None") else allergiesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
            proteinSupplement = proteinSupplement,
            startDate = startDate,
            createdAt = createdAt,
            onboardingCompleted = onboardingCompleted,
            weighInReminderEnabled = weighInReminderEnabled,
            deloadActive = deloadActive,
            deloadStartDateKey = deloadStartDateKey,
            deloadDismissedAtWeekKey = deloadDismissedAtWeekKey
        )
    }

    companion object {
        private const val EQUIP_EMPTY_SENTINEL = "__NONE__"
    }
}
