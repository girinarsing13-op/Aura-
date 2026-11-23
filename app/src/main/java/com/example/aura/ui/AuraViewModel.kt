package com.example.aura.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aura.data.db.AppDatabase
import com.example.aura.data.db.ExerciseLogEntity
import com.example.aura.data.db.UnlockedAchievementEntity
import com.example.aura.data.model.Blueprint
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.data.model.BodyCheckIn
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.Exercise
import com.example.aura.data.model.ScaledMeal
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutSessionRecord
import com.example.aura.data.repository.AuraRepository
import com.example.aura.engine.AuraTrainingMemoryEngine
import com.example.aura.engine.DiagnosticsEngine
import com.example.aura.engine.DiagnosticItem
import com.example.aura.engine.ExerciseDemoRepository
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.engine.ProgressAnalyticsEngine
import com.example.aura.engine.ProgressionEngine
import com.example.aura.engine.WorkoutBlueprintEngine
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.engine.WorkoutSplitEngine
import com.example.aura.notifications.WorkoutReminderScheduler
import com.example.aura.ui.character.AuraEventBus
import com.example.aura.ui.character.FitnessEvent
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuraViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = AuraRepository(database.auraDao())

    val userProfile: StateFlow<UserProfile?> = repository.userProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _draftProfile = MutableStateFlow(
        UserProfile(
            name = "",
            age = 26,
            height = 175.0,
            weight = 74.0,
            targetWeight = 78.0,
            gender = "Male",
            goal = "Muscle Gain",
            experience = "Intermediate",
            environment = "Full Gym",
            daysPerWeek = 4,
            preferredWorkoutDays = WorkoutScheduleEngine.defaultWorkoutDays(4),
            trainingSplit = "Upper / Lower – 4 Days",
            onboardingCompleted = false
        )
    )
    val draftProfile: StateFlow<UserProfile> = _draftProfile.asStateFlow()

    private val _blueprint = MutableStateFlow<Blueprint?>(null)
    val blueprint: StateFlow<Blueprint?> = _blueprint.asStateFlow()

    val workoutSessions: StateFlow<List<WorkoutSessionRecord>> = repository.workoutSessionsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val dailyHabits: StateFlow<Map<String, DailyHabitStatus>> = repository.dailyHabitsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val exerciseLogs: StateFlow<List<ExerciseLogEntity>> = repository.exerciseLogsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val bodyCheckIns: StateFlow<List<BodyCheckIn>> = repository.bodyCheckInsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val unlockedAchievements: StateFlow<List<UnlockedAchievementEntity>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Real-time custom live overrides for Settings sliders
    val liveNavTransparency = MutableStateFlow<Float?>(null)
    val liveNavBlur = MutableStateFlow<Float?>(null)
    val liveThemeMode = MutableStateFlow<String?>(null)
    val liveThemeAccent = MutableStateFlow<String?>(null)

    // Current selected workout day index (0..6)
    private val _selectedWorkoutDay = MutableStateFlow(0)
    val selectedWorkoutDay: StateFlow<Int> = _selectedWorkoutDay.asStateFlow()

    // Flag indicating user just completed daily workout today (triggers Streaks celebration animation)
    private val _justCompletedWorkoutEvent = MutableStateFlow(false)
    val justCompletedWorkoutEvent: StateFlow<Boolean> = _justCompletedWorkoutEvent.asStateFlow()

    private val _recentPbEvent = MutableStateFlow<PersonalBestEvent?>(null)
    val recentPbEvent: StateFlow<PersonalBestEvent?> = _recentPbEvent.asStateFlow()

    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    // Day-only exercise swaps: key = (dayNumber to exerciseIndex), leaves saved weekly plan intact
    private val _dayExerciseSwaps = MutableStateFlow<Map<Pair<Int, Int>, BlueprintExercise>>(emptyMap())
    val dayExerciseSwaps: StateFlow<Map<Pair<Int, Int>, BlueprintExercise>> = _dayExerciseSwaps.asStateFlow()

    // Persistent Active Workout Session State
    private val activeSessionPrefs = application.getSharedPreferences("aura_active_workout_session", Context.MODE_PRIVATE)
    private val _activeWorkoutSession = MutableStateFlow<ActiveWorkoutSession?>(null)
    val activeWorkoutSession: StateFlow<ActiveWorkoutSession?> = _activeWorkoutSession.asStateFlow()

    // Persistent Onboarding Progress State
    private val onboardingDraftPrefs = application.getSharedPreferences("aura_onboarding_draft", Context.MODE_PRIVATE)
    private val _onboardingStep = MutableStateFlow(onboardingDraftPrefs.getInt("step", 0).coerceIn(0, 11))
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    // Persisted App Settings & Safety Preferences
    private val appSettingsPrefs = application.getSharedPreferences("aura_app_settings", Context.MODE_PRIVATE)
    private val _hapticsEnabled = MutableStateFlow(appSettingsPrefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _soundEnabled = MutableStateFlow(appSettingsPrefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _keepScreenOn = MutableStateFlow(appSettingsPrefs.getBoolean("keep_screen_on", true))
    val keepScreenOn: StateFlow<Boolean> = _keepScreenOn.asStateFlow()

    private val _disclaimerAcknowledged = MutableStateFlow(appSettingsPrefs.getBoolean("disclaimer_acknowledged", false))
    val disclaimerAcknowledged: StateFlow<Boolean> = _disclaimerAcknowledged.asStateFlow()

    private val _parqCompleted = MutableStateFlow(appSettingsPrefs.getBoolean("parq_completed", false))
    val parqCompleted: StateFlow<Boolean> = _parqCompleted.asStateFlow()

    private val _parqFlagged = MutableStateFlow(appSettingsPrefs.getBoolean("parq_flagged", false))
    val parqFlagged: StateFlow<Boolean> = _parqFlagged.asStateFlow()

    init {
        ExerciseDemoRepository.ensureInitialized(application)
        restorePersistedActiveWorkout()
        viewModelScope.launch {
            // Check Room database for real saved user profile first
            val persisted = repository.getUserProfileOnce()
            if (persisted != null) {
                _draftProfile.value = persisted
                val bp = repository.getBlueprintOnce(persisted)
                _blueprint.value = bp
                determineTodayWorkoutDayIndex(bp)
                WorkoutReminderScheduler.syncReminderSchedule(getApplication(), persisted)
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                repository.syncAchievements(persisted, todayStr)
            } else {
                restorePersistedDraft()
            }

            // Observe subsequent updates
            userProfile.collect { profile ->
                if (profile != null) {
                    _draftProfile.value = profile
                    val bp = repository.getBlueprintOnce(profile)
                    _blueprint.value = bp
                    determineTodayWorkoutDayIndex(bp)
                }
            }
        }
    }

    private fun restorePersistedDraft() {
        if (onboardingDraftPrefs.contains("name")) {
            val savedName = onboardingDraftPrefs.getString("name", "Alex") ?: "Alex"
            val savedAge = onboardingDraftPrefs.getInt("age", 26)
            val savedHeight = onboardingDraftPrefs.getFloat("height", 175.0f).toDouble()
            val savedWeight = onboardingDraftPrefs.getFloat("weight", 74.0f).toDouble()
            val savedTargetWeight = onboardingDraftPrefs.getFloat("targetWeight", 78.0f).toDouble()
            val savedGender = onboardingDraftPrefs.getString("gender", "Male") ?: "Male"
            val savedGoal = onboardingDraftPrefs.getString("goal", "Muscle Gain") ?: "Muscle Gain"
            val savedExp = onboardingDraftPrefs.getString("experience", "Intermediate") ?: "Intermediate"
            val savedEnv = onboardingDraftPrefs.getString("environment", "Full Gym") ?: "Full Gym"
            val savedDays = onboardingDraftPrefs.getInt("daysPerWeek", 4)
            val savedSplit = onboardingDraftPrefs.getString("trainingSplit", "Upper / Lower – 4 Days") ?: "Upper / Lower – 4 Days"
            val savedDiet = onboardingDraftPrefs.getString("dietPreference", "Eggetarian") ?: "Eggetarian"
            val savedDurationMins = onboardingDraftPrefs.getInt("sessionDurationMinutes", 60)
            val savedSleepHours = onboardingDraftPrefs.getFloat("sleepHours", 7.5f).toDouble()
            val savedSleepQuality = onboardingDraftPrefs.getString("sleepQuality", "Good") ?: "Good"
            val savedStress = onboardingDraftPrefs.getString("stressLevel", "Moderate") ?: "Moderate"
            val savedActivity = onboardingDraftPrefs.getString("lifestyleActivity", "Moderately Active") ?: "Moderately Active"
            val savedCardio = onboardingDraftPrefs.getString("cardioPreference", "LISS Walking") ?: "LISS Walking"
            val savedPosture = onboardingDraftPrefs.getString("postureFocus", "Neutral Spine") ?: "Neutral Spine"
            val savedWarmup = onboardingDraftPrefs.getString("warmupPreference", "Full Dynamic Mobility Primer") ?: "Full Dynamic Mobility Primer"
            val savedBudget = onboardingDraftPrefs.getString("budgetTier", "Moderate") ?: "Moderate"
            val savedMeals = onboardingDraftPrefs.getInt("mealsPerDay", 3)
            val savedWater = onboardingDraftPrefs.getFloat("waterLiters", 3.0f).toDouble()
            val savedInjuries = onboardingDraftPrefs.getString("injuries", "None")?.split(",") ?: listOf("None")
            val savedFocus = onboardingDraftPrefs.getString("focusBodyParts", "")?.let { str ->
                val list = str.split(",").filter { it.isNotBlank() }
                com.example.aura.data.model.PriorityMuscle.normalize(list)
            } ?: emptyList()
            val savedAllergies = onboardingDraftPrefs.getString("allergies", null)?.let { str ->
                val items = str.split(",").map { it.trim() }.filter { it.isNotBlank() }
                if (items.isEmpty()) listOf("None") else items
            } ?: listOf("None")

            fun decodeEquipDraft(key: String, fallback: List<String>): List<String> {
                val raw = onboardingDraftPrefs.getString(key, null) ?: return fallback
                return when {
                    raw == "__NONE__" -> emptyList()
                    raw.isBlank() -> fallback
                    else -> raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                }
            }

            val savedHomeEq = decodeEquipDraft("homeEquipment", listOf("Dumbbells"))
            val savedGymEq = decodeEquipDraft("gymEquipment", com.example.aura.data.model.EquipmentOptions.GYM)
            val savedCalEq = decodeEquipDraft("calisthenicsEquipment", com.example.aura.data.model.EquipmentOptions.DEFAULT_CALISTHENICS)
            val savedWake = onboardingDraftPrefs.getInt("wakeTimeMinutes", 7 * 60)
            val savedHasWork = onboardingDraftPrefs.getBoolean("hasWorkOrSchoolHours", true)
            val savedWorkStart = onboardingDraftPrefs.getInt("workStartMinutes", 9 * 60)
            val savedWorkEnd = onboardingDraftPrefs.getInt("workEndMinutes", 17 * 60)
            val savedSleepTime = onboardingDraftPrefs.getInt("sleepTimeMinutes", 23 * 60)
            val savedWorkoutTime = onboardingDraftPrefs.getInt("preferredWorkoutMinutes", 18 * 60)
            val savedWorkoutDays = onboardingDraftPrefs.getString("preferredWorkoutDays", null)
                ?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.filter { it in 1..7 }?.distinct()
                ?.takeIf { it.isNotEmpty() }
                ?: WorkoutScheduleEngine.defaultWorkoutDays(savedDays)
            val savedReminder = onboardingDraftPrefs.getBoolean("workoutReminderEnabled", true)

            _draftProfile.value = _draftProfile.value.copy(
                name = savedName,
                age = savedAge,
                height = savedHeight,
                weight = savedWeight,
                targetWeight = savedTargetWeight,
                gender = savedGender,
                goal = savedGoal,
                experience = savedExp,
                environment = savedEnv,
                homeEquipment = savedHomeEq,
                gymEquipment = savedGymEq,
                calisthenicsEquipment = savedCalEq,
                daysPerWeek = savedDays,
                trainingSplit = savedSplit,
                dietPreference = savedDiet,
                sessionDurationMinutes = savedDurationMins,
                wakeTimeMinutes = savedWake,
                hasWorkOrSchoolHours = savedHasWork,
                workStartMinutes = savedWorkStart,
                workEndMinutes = savedWorkEnd,
                sleepTimeMinutes = savedSleepTime,
                preferredWorkoutMinutes = savedWorkoutTime,
                preferredWorkoutDays = savedWorkoutDays,
                workoutReminderEnabled = savedReminder,
                sleepHours = savedSleepHours,
                sleepQuality = savedSleepQuality,
                stressLevel = savedStress,
                lifestyleActivity = savedActivity,
                cardioPreference = savedCardio,
                postureFocus = savedPosture,
                warmupPreference = savedWarmup,
                budgetTier = savedBudget,
                mealsPerDay = savedMeals,
                waterLiters = savedWater,
                injuries = savedInjuries,
                focusBodyParts = savedFocus,
                allergies = savedAllergies
            )
        }
    }

    private fun restorePersistedActiveWorkout() {
        val isActive = activeSessionPrefs.getBoolean("is_active", false)
        val activeDate = activeSessionPrefs.getString("active_date", "")
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (isActive && activeDate == todayStr) {
            val dIdx = activeSessionPrefs.getInt("day_index", 0)
            val dNum = activeSessionPrefs.getInt("day_number", 1)
            val dTitle = activeSessionPrefs.getString("day_title", "") ?: ""
            val sTime = activeSessionPrefs.getLong("start_time", System.currentTimeMillis())
            _activeWorkoutSession.value = ActiveWorkoutSession(
                dayIndex = dIdx,
                dayNumber = dNum,
                dayTitle = dTitle,
                startTimeMillis = sTime
            )
        } else if (isActive && activeDate != todayStr) {
            // Previous day's stale active session, reset it
            activeSessionPrefs.edit().clear().apply()
            _activeWorkoutSession.value = null
        }
    }

    fun startWorkoutSession(dayIndex: Int, dayNumber: Int, dayTitle: String) {
        val now = System.currentTimeMillis()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        activeSessionPrefs.edit()
            .putBoolean("is_active", true)
            .putInt("day_index", dayIndex)
            .putInt("day_number", dayNumber)
            .putString("day_title", dayTitle)
            .putString("active_date", todayStr)
            .putLong("start_time", now)
            .apply()

        _activeWorkoutSession.value = ActiveWorkoutSession(
            dayIndex = dayIndex,
            dayNumber = dayNumber,
            dayTitle = dayTitle,
            startTimeMillis = now
        )

        AuraEventBus.post(FitnessEvent.WorkoutStarted(title = dayTitle, dayNumber = dayNumber))
    }

    fun endActiveWorkoutSession() {
        activeSessionPrefs.edit().clear().apply()
        _activeWorkoutSession.value = null
    }

    private fun determineTodayWorkoutDayIndex(bp: Blueprint) {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val todayShort = SimpleDateFormat("EEE", Locale.US).format(Date())
        val matchedIdx = bp.workout.week.indexOfFirst { it.dateKey == todayKey }
            .takeIf { it >= 0 }
            ?: bp.workout.week.indexOfFirst { it.scheduledDate.startsWith(todayShort, ignoreCase = true) }
                .takeIf { it >= 0 }
        val fallbackIdx = (WorkoutScheduleEngine.calendarToWeekdayIndex() - 1).coerceIn(0, bp.workout.week.lastIndex.coerceAtLeast(0))
        _selectedWorkoutDay.value = (matchedIdx ?: fallbackIdx).coerceIn(0, bp.workout.week.lastIndex.coerceAtLeast(0))
    }

    fun isTodayWorkoutDay(user: UserProfile, bp: Blueprint?): Boolean {
        val week = bp?.workout?.week.orEmpty()
        if (week.isNotEmpty()) {
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val todayShort = SimpleDateFormat("EEE", Locale.US).format(Date())
            val todayPlan = week.find { it.dateKey == todayKey }
                ?: week.find { it.scheduledDate.startsWith(todayShort, ignoreCase = true) }
                ?: week.getOrNull(_selectedWorkoutDay.value.coerceIn(0, week.lastIndex))
            if (todayPlan != null) {
                return !todayPlan.isRest
            }
        }
        return WorkoutScheduleEngine.isWorkoutDayForCalendar(user)
    }

    fun setSelectedWorkoutDay(dayIndex: Int) {
        val maxIdx = (_blueprint.value?.workout?.week?.lastIndex ?: 6).coerceAtLeast(0)
        _selectedWorkoutDay.value = dayIndex.coerceIn(0, maxIdx)
    }

    fun updateDraft(updater: (UserProfile) -> UserProfile) {
        val current = _draftProfile.value
        val updated = updater(current)
        _draftProfile.value = updated
        persistDraftAsync(updated)
    }

    fun toggleAllergy(allergy: String) {
        val current = _draftProfile.value
        val updatedAllergies = if (allergy.equals("None", ignoreCase = true)) {
            listOf("None")
        } else {
            val withoutNone = current.allergies.filter { !it.equals("None", ignoreCase = true) }
            if (withoutNone.any { it.equals(allergy, ignoreCase = true) }) {
                val remaining = withoutNone.filter { !it.equals(allergy, ignoreCase = true) }
                if (remaining.isEmpty()) listOf("None") else remaining
            } else {
                withoutNone + allergy
            }
        }
        val updated = current.copy(allergies = updatedAllergies)
        _draftProfile.value = updated
        persistDraftAsync(updated)
    }

    fun setOnboardingStep(step: Int) {
        val clamped = step.coerceIn(0, 11)
        _onboardingStep.value = clamped
        onboardingDraftPrefs.edit().putInt("step", clamped).apply()
    }

    private fun persistDraftAsync(p: UserProfile) {
        fun encodeEquip(list: List<String>) = if (list.isEmpty()) "__NONE__" else list.joinToString(",")
        onboardingDraftPrefs.edit()
            .putString("name", p.name)
            .putInt("age", p.age)
            .putFloat("height", p.height.toFloat())
            .putFloat("weight", p.weight.toFloat())
            .putFloat("targetWeight", (p.targetWeight ?: p.weight).toFloat())
            .putString("gender", p.gender)
            .putString("goal", p.goal)
            .putString("experience", p.experience)
            .putString("environment", p.environment)
            .putString("homeEquipment", encodeEquip(p.homeEquipment))
            .putString("gymEquipment", encodeEquip(p.gymEquipment))
            .putString("calisthenicsEquipment", encodeEquip(p.calisthenicsEquipment))
            .putInt("daysPerWeek", p.daysPerWeek)
            .putString("trainingSplit", p.trainingSplit)
            .putString("dietPreference", p.dietPreference)
            .putInt("sessionDurationMinutes", p.sessionDurationMinutes)
            .putInt("wakeTimeMinutes", p.wakeTimeMinutes)
            .putBoolean("hasWorkOrSchoolHours", p.hasWorkOrSchoolHours)
            .putInt("workStartMinutes", p.workStartMinutes)
            .putInt("workEndMinutes", p.workEndMinutes)
            .putInt("sleepTimeMinutes", p.sleepTimeMinutes)
            .putInt("preferredWorkoutMinutes", p.preferredWorkoutMinutes)
            .putString("preferredWorkoutDays", p.preferredWorkoutDays.joinToString(","))
            .putBoolean("workoutReminderEnabled", p.workoutReminderEnabled)
            .putFloat("sleepHours", p.sleepHours.toFloat())
            .putString("sleepQuality", p.sleepQuality)
            .putString("stressLevel", p.stressLevel)
            .putString("lifestyleActivity", p.lifestyleActivity)
            .putString("cardioPreference", p.cardioPreference)
            .putString("postureFocus", p.postureFocus)
            .putString("warmupPreference", p.warmupPreference)
            .putString("budgetTier", p.budgetTier)
            .putInt("mealsPerDay", p.mealsPerDay)
            .putFloat("waterLiters", p.waterLiters.toFloat())
            .putString("injuries", p.injuries.joinToString(","))
            .putString("focusBodyParts", p.focusBodyParts.joinToString(","))
            .putString("allergies", p.allergies.joinToString(","))
            .apply()
    }

    fun setNavTransparency(value: Float) {
        liveNavTransparency.value = value.coerceIn(0f, 1f)
        viewModelScope.launch(Dispatchers.IO) {
            val curr = userProfile.value ?: _draftProfile.value
            repository.saveUserProfileOnly(curr.copy(navTransparency = value))
        }
    }

    fun setNavBlur(value: Float) {
        liveNavBlur.value = value.coerceIn(0f, 1f)
        viewModelScope.launch(Dispatchers.IO) {
            val curr = userProfile.value ?: _draftProfile.value
            repository.saveUserProfileOnly(curr.copy(navBlur = value))
        }
    }

    fun setThemeMode(mode: String) {
        liveThemeMode.value = mode
        viewModelScope.launch(Dispatchers.IO) {
            val curr = userProfile.value ?: _draftProfile.value
            repository.saveUserProfileOnly(curr.copy(themeMode = mode))
        }
    }

    fun updateThemeAccent(hex: String) {
        liveThemeAccent.value = hex
        viewModelScope.launch(Dispatchers.IO) {
            val curr = userProfile.value ?: _draftProfile.value
            repository.saveUserProfileOnly(curr.copy(themeAccent = hex))
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        appSettingsPrefs.edit().putBoolean("haptics_enabled", enabled).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        appSettingsPrefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun setKeepScreenOn(enabled: Boolean) {
        _keepScreenOn.value = enabled
        appSettingsPrefs.edit().putBoolean("keep_screen_on", enabled).apply()
    }

    fun acknowledgeDisclaimer() {
        _disclaimerAcknowledged.value = true
        appSettingsPrefs.edit().putBoolean("disclaimer_acknowledged", true).apply()
    }

    fun saveParqResult(hasRiskFlags: Boolean) {
        _parqCompleted.value = true
        _parqFlagged.value = hasRiskFlags
        appSettingsPrefs.edit()
            .putBoolean("parq_completed", true)
            .putBoolean("parq_flagged", hasRiskFlags)
            .apply()
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            val freshBp = repository.calculateFreshBlueprint(profile)
            repository.saveProfileAndBlueprint(profile, freshBp)
            _dayExerciseSwaps.value = emptyMap()
            _blueprint.value = freshBp
            _draftProfile.value = profile
            persistDraftAsync(profile)
            WorkoutReminderScheduler.syncReminderSchedule(getApplication(), profile)
        }
    }

    fun completeOnboarding(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val resolvedDays = WorkoutScheduleEngine.resolveWorkoutDays(profile)
            val computedWater = FitnessCalcEngine.calcWaterTargetLiters(profile.weight, isWorkoutDay = true)
            val finalized = profile.copy(
                name = profile.name.trim().ifBlank { "Athlete" },
                preferredWorkoutDays = resolvedDays,
                waterLiters = computedWater,
                startDate = todayStr,
                onboardingCompleted = true
            )
            val freshBp = repository.calculateFreshBlueprint(finalized)
            repository.saveProfileAndBlueprint(finalized, freshBp)
            _dayExerciseSwaps.value = emptyMap()
            _blueprint.value = freshBp
            _draftProfile.value = finalized
            onboardingDraftPrefs.edit().clear().apply()
            _onboardingStep.value = 0
            determineTodayWorkoutDayIndex(freshBp)
            WorkoutReminderScheduler.syncReminderSchedule(getApplication(), finalized)
        }
    }

    fun retakeOnboarding() {
        val curr = userProfile.value ?: _draftProfile.value
        _draftProfile.value = curr.copy(onboardingCompleted = false)
        _onboardingStep.value = 0
        onboardingDraftPrefs.edit().putInt("step", 0).apply()
    }

    fun recordWorkoutSession(
        dayNumber: Int,
        dayTitle: String,
        setsCompleted: Int,
        totalSets: Int,
        exerciseLogs: List<ExerciseLogEntity> = emptyList(),
        volumeKg: Double = 0.0,
        prCount: Int = 0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val dateLabel = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())
            val isFull = setsCompleted >= totalSets

            repository.recordWorkoutSession(
                dayNumber = dayNumber,
                dayTitle = dayTitle,
                status = if (isFull) "completed" else "partial",
                isFullyCompleted = isFull,
                setsCompleted = setsCompleted,
                totalSets = totalSets,
                dateStr = dateLabel,
                todayKey = todayStr,
                exerciseLogs = exerciseLogs
            )

            _justCompletedWorkoutEvent.value = true
            endActiveWorkoutSession()

            // Notify Event Bus
            AuraEventBus.post(
                FitnessEvent.WorkoutCompleted(
                    durationMinutes = 45,
                    totalVolumeKg = volumeKg,
                    prCount = prCount
                )
            )

            // Calculate updated streak & sync achievements
            val currProfile = userProfile.value ?: _draftProfile.value
            repository.syncAchievements(currProfile, todayStr)
            val newStreak = calculateCurrentStreak() + 1
            AuraEventBus.post(FitnessEvent.StreakUpdated(streakDays = newStreak, isImproving = true))
        }
    }

    fun logSetImmediate(
        exercise: BlueprintExercise,
        setNumber: Int,
        weightKg: Double,
        reps: Int,
        completed: Boolean
    ): Boolean {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val currentLogs = exerciseLogs.value
        val isNewPb = completed && ProgressAnalyticsEngine.isNewPersonalBestSet(
            exerciseName = exercise.name,
            weightKg = weightKg,
            reps = reps,
            existingLogs = currentLogs,
            todayKey = todayKey
        )
        if (isNewPb) {
            _recentPbEvent.value = PersonalBestEvent(
                exerciseName = exercise.name,
                weightKg = weightKg,
                reps = reps,
                estimated1RmKg = ProgressionEngine.calculateEstimated1Rm(weightKg, reps),
                timestampMillis = System.currentTimeMillis()
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSingleSetLog(
                dateKey = todayKey,
                exerciseId = exercise.id,
                exerciseName = exercise.name,
                equipment = exercise.equipment,
                variation = exercise.detail,
                setNumber = setNumber,
                weightKg = weightKg,
                reps = reps,
                completed = completed
            )
            if (completed) {
                val curr = userProfile.value ?: _draftProfile.value
                repository.syncAchievements(curr, todayKey)
            }
        }
        return isNewPb
    }

    fun clearPbEvent() {
        _recentPbEvent.value = null
    }

    fun saveBodyCheckIn(
        weightKg: Double,
        waistCm: Double?,
        chestCm: Double?,
        armCm: Double?,
        pickedPhotoUri: Uri?,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val savedPhotoPath = pickedPhotoUri?.let { copyPhotoToAppPrivateDir(it) }
            val checkIn = BodyCheckIn(
                dateKey = todayKey,
                timestampMillis = System.currentTimeMillis(),
                weightKg = weightKg,
                waistCm = waistCm,
                chestCm = chestCm,
                armCm = armCm,
                photoPath = savedPhotoPath,
                note = note.trim()
            )
            repository.saveBodyCheckIn(checkIn)
            val curr = userProfile.value ?: _draftProfile.value
            val updatedProfile = curr.copy(weight = weightKg)
            if (kotlin.math.abs(curr.weight - weightKg) >= 0.1) {
                val freshBp = repository.calculateFreshBlueprint(updatedProfile)
                repository.saveProfileAndBlueprint(updatedProfile, freshBp)
                _blueprint.value = freshBp
                _draftProfile.value = updatedProfile
            }
            repository.syncAchievements(updatedProfile, todayKey)
        }
    }

    private fun copyPhotoToAppPrivateDir(uri: Uri): String? {
        return try {
            val ctx = getApplication<Application>()
            val dir = File(ctx.filesDir, "progress_photos").apply { if (!exists()) mkdirs() }
            val dest = File(dir, "checkin_${System.currentTimeMillis()}.jpg")
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun deleteBodyCheckIn(id: Long, photoPath: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!photoPath.isNullOrBlank()) {
                try {
                    File(photoPath).takeIf { it.exists() }?.delete()
                } catch (_: Exception) {}
            }
            repository.deleteBodyCheckIn(id)
        }
    }

    fun acceptDeloadWeek() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val curr = userProfile.value ?: _draftProfile.value
            val updated = curr.copy(deloadActive = true, deloadStartDateKey = todayKey)
            repository.saveUserProfileOnly(updated)
            _draftProfile.value = updated
        }
    }

    fun endDeloadWeek() {
        viewModelScope.launch(Dispatchers.IO) {
            val curr = userProfile.value ?: _draftProfile.value
            val updated = curr.copy(deloadActive = false)
            repository.saveUserProfileOnly(updated)
            _draftProfile.value = updated
        }
    }

    fun dismissDeloadForThisWeek() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val weekKey = ProgressionEngine.getIsoWeekKey(todayKey)
            val curr = userProfile.value ?: _draftProfile.value
            val updated = curr.copy(deloadDismissedAtWeekKey = weekKey)
            repository.saveUserProfileOnly(updated)
            _draftProfile.value = updated
        }
    }

    fun toggleWeighInReminder(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val curr = userProfile.value ?: _draftProfile.value
            val updated = curr.copy(weighInReminderEnabled = enabled)
            repository.saveUserProfileOnly(updated)
            _draftProfile.value = updated
            WorkoutReminderScheduler.syncReminderSchedule(getApplication(), updated)
        }
    }

    fun moveTodayWorkoutToTomorrow() {
        viewModelScope.launch(Dispatchers.IO) {
            val bp = _blueprint.value ?: return@launch
            val week = bp.workout.week
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val todayShort = SimpleDateFormat("EEE", Locale.US).format(Date())
            val todayIdx = week.indexOfFirst { it.dateKey == todayKey }
                .takeIf { it >= 0 }
                ?: week.indexOfFirst { it.scheduledDate.startsWith(todayShort, ignoreCase = true) }
                    .takeIf { it >= 0 }
                ?: _selectedWorkoutDay.value.coerceIn(0, week.lastIndex)

            if (todayIdx !in week.indices || week[todayIdx].isRest) return@launch

            val shiftedRaw = WorkoutBlueprintEngine.shiftScheduleForRestDay(week, todayIdx)
            val shifted = if (shiftedRaw.size > 7 && shiftedRaw.last().isRest) {
                shiftedRaw.dropLast(1).mapIndexed { idx, plan -> plan.copy(dayNumber = idx + 1) }
            } else {
                shiftedRaw
            }
            val updatedBp = bp.copy(workout = bp.workout.copy(week = shifted))
            repository.saveBlueprint(updatedBp)
            _blueprint.value = updatedBp
            _dayExerciseSwaps.value = emptyMap()
            _selectedWorkoutDay.value = todayIdx.coerceIn(0, shifted.lastIndex)
        }
    }

    fun exportBackupToUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = repository.exportBackupJson()
                val ctx = getApplication<Application>()
                ctx.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("Could not open file for writing")
                _backupStatusMessage.value = "Backup exported successfully."
            } catch (e: Exception) {
                _backupStatusMessage.value = "Export failed: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun importBackupFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                val raw = ctx.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    ?: throw IllegalStateException("Could not open backup file")
                val result = repository.importBackupJson(raw)
                result.fold(
                    onSuccess = { payload ->
                        val restoredProfile = repository.getUserProfileOnce()
                        if (restoredProfile != null) {
                            _draftProfile.value = restoredProfile
                            val bp = repository.getBlueprintOnce(restoredProfile)
                            _blueprint.value = bp
                            determineTodayWorkoutDayIndex(bp)
                            WorkoutReminderScheduler.syncReminderSchedule(getApplication(), restoredProfile)
                        }
                        _dayExerciseSwaps.value = emptyMap()
                        _backupStatusMessage.value =
                            "Restored ${payload.workoutSessions.size} workouts, ${payload.exerciseLogs.size} sets, ${payload.bodyCheckIns.size} check-ins."
                    },
                    onFailure = { err ->
                        _backupStatusMessage.value = "Import failed: ${err.message ?: "Invalid backup"}"
                    }
                )
            } catch (e: Exception) {
                _backupStatusMessage.value = "Import failed: ${e.message ?: "Unknown error"}"
            }
        }
    }

    fun clearBackupStatus() {
        _backupStatusMessage.value = null
    }

    fun resetCompletedWorkoutEvent() {
        _justCompletedWorkoutEvent.value = false
    }

    fun toggleHabit(dateKey: String, habitType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleHabit(dateKey, habitType)
            val curr = userProfile.value ?: _draftProfile.value
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            repository.syncAchievements(curr, todayStr)
        }
    }

    fun addWaterMl(deltaMl: Int, targetWaterMl: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            repository.addWaterMl(todayKey, deltaMl, targetWaterMl)
            val curr = userProfile.value ?: _draftProfile.value
            repository.syncAchievements(curr, todayKey)
        }
    }

    fun toggleMealEaten(mealIndex: Int, meals: List<ScaledMeal>, targetCals: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            repository.toggleMealEaten(todayKey, mealIndex, meals, targetCals)
        }
    }

    fun swapExerciseForDayOnly(dayNumber: Int, exerciseIndex: Int, replacement: Exercise): BlueprintExercise {
        val currProfile = userProfile.value ?: _draftProfile.value
        val existingDayExercise = _blueprint.value?.workout?.week
            ?.find { it.dayNumber == dayNumber }
            ?.exercises?.getOrNull(exerciseIndex)
        val swapped = WorkoutBlueprintEngine.createBlueprintExercise(
            ex = replacement,
            profile = currProfile,
            setsOverride = existingDayExercise?.sets,
            isPriorityOverride = existingDayExercise?.isPriority
        )
        _dayExerciseSwaps.value = _dayExerciseSwaps.value.toMutableMap().apply {
            put(dayNumber to exerciseIndex, swapped)
        }
        return swapped
    }

    fun recordSubstitution(originalId: String, newId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordSubstitution(originalId, newId, newName)
        }
    }

    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAllData()
            WorkoutReminderScheduler.cancelReminders(getApplication())
            onboardingDraftPrefs.edit().clear().apply()
            _dayExerciseSwaps.value = emptyMap()
            _onboardingStep.value = 0
            _draftProfile.value = UserProfile(
                preferredWorkoutDays = WorkoutScheduleEngine.defaultWorkoutDays(4),
                onboardingCompleted = false
            )
        }
    }

    fun calculateCurrentStreak(): Int {
        val curr = userProfile.value ?: _draftProfile.value
        return ProgressAnalyticsEngine.calculateSmartStreak(
            profile = curr,
            sessions = workoutSessions.value,
            habits = dailyHabits.value
        )
    }

    fun runDiagnostics(): List<DiagnosticItem> {
        return DiagnosticsEngine.runAll()
    }
}

data class ActiveWorkoutSession(
    val dayIndex: Int,
    val dayNumber: Int,
    val dayTitle: String,
    val startTimeMillis: Long = System.currentTimeMillis()
)

data class PersonalBestEvent(
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val estimated1RmKg: Double,
    val timestampMillis: Long = System.currentTimeMillis()
)
