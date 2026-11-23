package com.example.aura.engine

import com.example.aura.data.model.MacroTargets
import com.example.aura.data.model.PaceAssessment
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutDayPlan
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

enum class RecoveryLevel(val displayName: String) {
    GOOD("Good"),
    MODERATE("Moderate"),
    LOW("Low")
}

data class CardioSessionPlan(
    val cardioType: String,
    val durationMinutes: Int,
    val weeklySessions: Int,
    val placementLabel: String,
    val description: String
)

data class PlanPersonalizationRow(
    val topic: String,
    val answerSummary: String,
    val effectSentence: String
)

data class SplitConfig(
    val name: String,
    val days: List<String>
)

data class CalorieSafetyResult(
    val targetCals: Int,
    val hitBmrFloor: Boolean,
    val hitDeficitCap: Boolean,
    val note: String?
)

data class SafetyAdvisory(
    val requiresMinorSupervision: Boolean,
    val hasExtremeGoalWarning: Boolean,
    val messages: List<String>
)

object FitnessCalcEngine {

    fun normalizeGoal(goal: String): String {
        val g = goal.trim().lowercase()
        return when {
            g.contains("muscle") || g.contains("hypertrophy") || g.contains("strength") || g.contains("bulk") -> "Muscle Gain"
            g.contains("fat") || g.contains("loss") || g.contains("cut") || g.contains("lean") -> "Fat Loss"
            g.contains("recomp") -> "Recomposition"
            g.contains("general") || g.contains("athletic") || g.contains("fitness") || g.contains("maint") -> "General Fitness"
            else -> "General Fitness"
        }
    }

    fun calcBMI(weightKg: Double, heightCm: Double): Double {
        if (weightKg <= 0.0 || heightCm <= 0.0) return 0.0
        val hM = heightCm / 100.0
        val raw = weightKg / (hM * hM)
        return (raw * 10.0).roundToInt() / 10.0
    }

    fun calcBMR(weightKg: Double, heightCm: Double, age: Int, gender: String): Int {
        if (weightKg <= 0.0 || heightCm <= 0.0 || age <= 0) return 0
        return if (gender.equals("Female", ignoreCase = true)) {
            ((10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) - 161.0).roundToInt()
        } else {
            ((10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age) + 5.0).roundToInt()
        }
    }

    fun calcTDEE(bmr: Int, daysPerWeek: Int): Int {
        return calcTDEE(bmr, daysPerWeek, "Moderately Active")
    }

    fun calcTDEE(bmr: Int, daysPerWeek: Int, lifestyleActivity: String): Int {
        if (bmr <= 0) return 0
        val act = lifestyleActivity.trim().lowercase(Locale.US)
        val lifestyleBonus = when {
            act.contains("sedentary") || act.contains("sitting") -> 0.0
            act.contains("lightly") -> 0.05
            act.contains("very") || act.contains("athlete") || act.contains("physical") -> 0.18
            act.contains("moderate") || act.contains("on my feet") -> 0.10
            else -> 0.10
        }
        val trainingBonus = when {
            daysPerWeek <= 2 -> 0.05
            daysPerWeek == 3 -> 0.10
            daysPerWeek == 4 -> 0.15
            daysPerWeek == 5 -> 0.20
            else -> 0.25
        }
        val mult = (1.2 + lifestyleBonus + trainingBonus).coerceAtMost(1.8)
        return (bmr * mult).roundToInt()
    }

    fun computeRecoveryLevel(
        sleepHours: Double,
        sleepQuality: String,
        stressLevel: String
    ): RecoveryLevel {
        val q = sleepQuality.trim().lowercase(Locale.US)
        val s = stressLevel.trim().lowercase(Locale.US)
        if (sleepHours < 6.0 || q.contains("poor") || s.contains("high") || s.contains("severe")) {
            return RecoveryLevel.LOW
        }
        if (sleepHours >= 7.0 && (q.contains("good") || q.contains("optimal")) && s.contains("low")) {
            return RecoveryLevel.GOOD
        }
        return RecoveryLevel.MODERATE
    }

    fun computeRecoveryLevel(profile: UserProfile): RecoveryLevel {
        return computeRecoveryLevel(profile.sleepHours, profile.sleepQuality, profile.stressLevel)
    }

    fun isCardioDisabled(cardioPreference: String): Boolean {
        val c = cardioPreference.trim().lowercase(Locale.US)
        return c.isBlank() || c == "none" || c.contains("no cardio")
    }

    fun buildWeeklyCardioSchedule(
        profile: UserProfile,
        weekPlans: List<WorkoutDayPlan>
    ): Map<Int, CardioSessionPlan> {
        if (isCardioDisabled(profile.cardioPreference)) return emptyMap()
        val cardioType = profile.cardioPreference.trim()
        val normGoal = normalizeGoal(profile.goal)
        val isHiit = cardioType.contains("hiit", ignoreCase = true) || cardioType.contains("sprint", ignoreCase = true)

        val (sessionsCount, durationMinutes) = when (normGoal) {
            "Fat Loss" -> 3 to (if (isHiit) 20 else 25)
            "Muscle Gain" -> (if (profile.daysPerWeek <= 4) 2 else 1) to (if (isHiit) 15 else 20)
            "Recomposition" -> 2 to (if (isHiit) 20 else 25)
            else -> (if (profile.daysPerWeek <= 4) 3 else 2) to (if (isHiit) 20 else 25)
        }

        val description = when {
            cardioType.contains("hiit", ignoreCase = true) ->
                "Short hard intervals (30s fast, 90s easy) to boost conditioning without long sessions."
            cardioType.contains("zone 2", ignoreCase = true) || cardioType.contains("jog", ignoreCase = true) ->
                "Steady conversational pace (Zone 2) to build aerobic base and recovery."
            cardioType.contains("cycling", ignoreCase = true) ->
                "Low-impact steady cycling at moderate cadence to protect leg recovery."
            cardioType.contains("rowing", ignoreCase = true) ->
                "Full-body moderate rowing strokes at steady rhythm."
            else ->
                "Brisk incline or outdoor walk to increase daily burn without hurting lift recovery."
        }

        val dayPool = if (weekPlans.isNotEmpty()) {
            val restDays = weekPlans.filter { it.isRest }.map { it.dayNumber to "Rest-day cardio" }
            val trainingDays = weekPlans.filter { !it.isRest }.map { it.dayNumber to "Post-workout finisher" }
            // Prefer rest days first for non-HIIT, or mix evenly across training & rest days
            if (isHiit) {
                trainingDays + restDays
            } else {
                restDays + trainingDays
            }
        } else {
            val workoutDays = WorkoutScheduleEngine.resolveWorkoutDays(profile).toSet()
            val restNums = (1..7).filter { it !in workoutDays }.map { it to "Rest-day cardio" }
            val trainNums = (1..7).filter { it in workoutDays }.map { it to "Post-workout finisher" }
            if (isHiit) trainNums + restNums else restNums + trainNums
        }

        val chosen = dayPool.take(sessionsCount.coerceIn(0, dayPool.size))
        return chosen.associate { (dayNumber, placement) ->
            dayNumber to CardioSessionPlan(
                cardioType = cardioType,
                durationMinutes = durationMinutes,
                weeklySessions = chosen.size,
                placementLabel = placement,
                description = description
            )
        }
    }

    fun getCardioForDay(
        profile: UserProfile,
        weekPlans: List<WorkoutDayPlan>,
        dayNumber: Int
    ): CardioSessionPlan? {
        return buildWeeklyCardioSchedule(profile, weekPlans)[dayNumber]
    }

    fun buildPlanPersonalizationSummary(
        profile: UserProfile,
        bmr: Int,
        tdee: Int,
        targetCals: Int,
        proteinG: Int,
        weekPlans: List<WorkoutDayPlan>
    ): List<PlanPersonalizationRow> {
        val normGoal = normalizeGoal(profile.goal)
        val recovery = computeRecoveryLevel(profile)
        val cardioMap = buildWeeklyCardioSchedule(profile, weekPlans)
        val activeInjuries = profile.injuries.filter { !it.equals("None", ignoreCase = true) }
        val normEnv = WorkoutBlueprintEngine.normalizeEnvironment(profile.environment)
        val activeEquip = when (normEnv) {
            "Full Gym" -> profile.gymEquipment
            "Home Workout" -> profile.homeEquipment
            else -> profile.calisthenicsEquipment
        }
        val workoutDaysLabel = WorkoutScheduleEngine.resolveWorkoutDays(profile)
            .mapNotNull { id -> WorkoutScheduleEngine.WEEKDAYS.find { it.first == id }?.second }
            .joinToString(", ")
        val timeLabel = WorkoutScheduleEngine.formatTimeMinutes(profile.preferredWorkoutMinutes)
        val mealCount = profile.mealsPerDay.coerceIn(3, 6)

        val goalSentence = when (normGoal) {
            "Muscle Gain" -> "Muscle Gain adds +${max(0, targetCals - tdee)} kcal above maintenance with 8–12 rep strength and hypertrophy work."
            "Fat Loss" -> "Fat Loss sets a safe -${max(0, tdee - targetCals)} kcal deficit with high protein (${proteinG}g) and 10–15 rep sets."
            "Recomposition" -> "Recomposition trims ${max(0, tdee - targetCals)} kcal below maintenance with ${proteinG}g protein to build lean muscle while leaning out."
            else -> "General Fitness keeps calories at maintenance ($targetCals kcal) with balanced 8–12 rep training."
        }

        val recoverySentence = when (recovery) {
            RecoveryLevel.LOW -> "${profile.sleepHours} hrs sleep (${profile.sleepQuality}) and ${profile.stressLevel} stress put recovery at Low, trimming 1 set from accessories and offering deloads sooner."
            RecoveryLevel.GOOD -> "${profile.sleepHours} hrs sleep (${profile.sleepQuality}) and ${profile.stressLevel} stress give Good recovery, keeping full prescribed volume."
            RecoveryLevel.MODERATE -> "${profile.sleepHours} hrs sleep (${profile.sleepQuality}) and ${profile.stressLevel} stress give Moderate recovery, keeping your standard training volume."
        }

        val supplementSentence = if (profile.proteinSupplement) {
            val shakeType = if (profile.dietPreference.equals("Vegan", true) ||
                profile.allergies.any { it.contains("dairy", true) || it.contains("lactose", true) }
            ) "plant protein shake" else "whey protein shake"
            "Adds a 25g protein (130 kcal) $shakeType on workout days and trims other meals so totals stay at $targetCals kcal."
        } else {
            "No protein shakes anywhere — all ${proteinG}g of daily protein come from whole foods."
        }

        val cardioSentence = if (cardioMap.isEmpty()) {
            "No cardio finishers added since you chose None."
        } else {
            val sample = cardioMap.values.first()
            "Schedules ${sample.weeklySessions}× ${sample.durationMinutes}-min ${sample.cardioType} sessions per week for your $normGoal goal."
        }

        val warmupLower = profile.warmupPreference.lowercase(Locale.US)
        val warmupSentence = when {
            warmupLower.contains("skip") || warmupLower.contains("none") ->
                "Skips the pre-workout warm-up block so you go straight to your first lift."
            warmupLower.contains("quick") || warmupLower.contains("short") || warmupLower.contains("brief") ->
                "Uses a shortened 2-minute primer with only the 2 most essential mobility drills."
            else ->
                "Includes the full 5-minute dynamic mobility warm-up tailored to each session's muscles."
        }

        val postureSentence = if (profile.postureFocus.equals("Neutral Spine", ignoreCase = true)) {
            "Neutral Spine selected — no extra corrective mobility drills added."
        } else {
            "Adds 3 corrective drills targeting ${profile.postureFocus} to your warm-up protocol."
        }

        val budgetSentence = if (profile.budgetTier.equals("Low", ignoreCase = true)) {
            "Prioritizes budget staples (dal, eggs, soya, chicken, curd, seasonal produce) and avoids premium ingredients while strictly respecting allergies."
        } else {
            "${profile.budgetTier} budget allows the full variety of foods matched to your ${profile.dietPreference} plan."
        }

        val injurySentence = if (activeInjuries.isEmpty()) {
            "No joint restrictions active — all equipment-matched exercises are available."
        } else {
            "Protects ${activeInjuries.joinToString(", ")} by excluding high-stress movements for those joints across every workout and swap."
        }

        val equipSentence = if (activeEquip.isEmpty()) {
            "$normEnv with bodyweight only — every exercise needs zero extra gear."
        } else {
            "$normEnv with ${activeEquip.size} selected items — every exercise uses only the gear you picked."
        }

        return listOf(
            PlanPersonalizationRow(
                topic = "Age & weight",
                answerSummary = "${profile.age} yrs • ${profile.weight.toInt()} kg",
                effectSentence = "Sets your resting burn at $bmr kcal/day and anchors daily protein at ${proteinG}g."
            ),
            PlanPersonalizationRow(
                topic = "Goal",
                answerSummary = profile.goal,
                effectSentence = goalSentence
            ),
            PlanPersonalizationRow(
                topic = "Lifestyle",
                answerSummary = profile.lifestyleActivity,
                effectSentence = "${profile.lifestyleActivity} daily movement plus ${profile.daysPerWeek} training days sets your maintenance burn to $tdee kcal/day."
            ),
            PlanPersonalizationRow(
                topic = "Sleep & stress",
                answerSummary = "${recovery.displayName} recovery",
                effectSentence = recoverySentence
            ),
            PlanPersonalizationRow(
                topic = "Meals per day",
                answerSummary = "$mealCount meals",
                effectSentence = "Splits your $targetCals kcal and ${proteinG}g protein across $mealCount daily meals and matching Add Food slots."
            ),
            PlanPersonalizationRow(
                topic = "Protein supplement",
                answerSummary = if (profile.proteinSupplement) "Yes (workout days)" else "No",
                effectSentence = supplementSentence
            ),
            PlanPersonalizationRow(
                topic = "Cardio",
                answerSummary = profile.cardioPreference,
                effectSentence = cardioSentence
            ),
            PlanPersonalizationRow(
                topic = "Warm-up",
                answerSummary = profile.warmupPreference,
                effectSentence = warmupSentence
            ),
            PlanPersonalizationRow(
                topic = "Posture",
                answerSummary = profile.postureFocus,
                effectSentence = postureSentence
            ),
            PlanPersonalizationRow(
                topic = "Budget",
                answerSummary = profile.budgetTier,
                effectSentence = budgetSentence
            ),
            PlanPersonalizationRow(
                topic = "Injuries",
                answerSummary = if (activeInjuries.isEmpty()) "None" else activeInjuries.joinToString(", "),
                effectSentence = injurySentence
            ),
            PlanPersonalizationRow(
                topic = "Equipment",
                answerSummary = "$normEnv (${activeEquip.size})",
                effectSentence = equipSentence
            ),
            PlanPersonalizationRow(
                topic = "Schedule",
                answerSummary = "${profile.daysPerWeek} days • ${profile.sessionDurationMinutes} min",
                effectSentence = "Builds your ${profile.trainingSplit} on $workoutDaysLabel around $timeLabel (${profile.sessionDurationMinutes} min sessions)."
            )
        )
    }

    /**
     * Computes daily water target in Liters:
     * 35 ml per kg body weight + 0.5 L on workout days, rounded to nearest 0.25 L.
     */
    fun calcWaterTargetLiters(weightKg: Double, isWorkoutDay: Boolean): Double {
        val w = if (weightKg > 0.0) weightKg else 70.0
        val baseLiters = (w * 35.0) / 1000.0
        val rawLiters = baseLiters + if (isWorkoutDay) 0.5 else 0.0
        val rounded = (rawLiters / 0.25).roundToInt() * 0.25
        return rounded.coerceIn(1.5, 5.0)
    }

    fun calcWaterTargetMl(weightKg: Double, isWorkoutDay: Boolean): Int {
        return (calcWaterTargetLiters(weightKg, isWorkoutDay) * 1000.0).roundToInt()
    }

    /**
     * Calculates safe calorie target:
     * - Never below BMR.
     * - Fat-loss deficit capped at ~20% of TDEE and ~0.75% of body weight/week pace.
     */
    fun calcGoalTargetCals(
        tdee: Int,
        goal: String,
        bmr: Int = 0,
        weightKg: Double = 0.0,
        targetWeightKg: Double? = null,
        durationMonths: Int = 3
    ): Int {
        return calcSafeCalories(
            tdee = tdee,
            goal = goal,
            bmr = bmr,
            weightKg = weightKg,
            targetWeightKg = targetWeightKg,
            durationMonths = durationMonths
        ).targetCals
    }

    fun calcSafeCalories(
        tdee: Int,
        goal: String,
        bmr: Int = 0,
        weightKg: Double = 0.0,
        targetWeightKg: Double? = null,
        durationMonths: Int = 3
    ): CalorieSafetyResult {
        if (tdee <= 0) return CalorieSafetyResult(0, hitBmrFloor = false, hitDeficitCap = false, note = null)
        val normGoal = normalizeGoal(goal)
        val bmrFloor = if (bmr > 0) bmr else 0

        val minSafeCals = if (tdee >= 1500) max(bmrFloor, 1200) else max(bmrFloor, 1000)

        return when (normGoal) {
            "Muscle Gain" -> {
                val target = (tdee + 350).coerceIn(minSafeCals, 5000)
                CalorieSafetyResult(
                    targetCals = target,
                    hitBmrFloor = false,
                    hitDeficitCap = false,
                    note = null
                )
            }
            "Fat Loss" -> {
                val maxSafeDeficit20 = (tdee * 0.20).roundToInt()
                var hitPaceCap = false
                val paceCappedDeficit = if (weightKg > 0.0 && targetWeightKg != null && targetWeightKg < weightKg) {
                    val totalWeeks = max(1.0, max(1, durationMonths) * 4.33)
                    val desiredWeeklyKg = (weightKg - targetWeightKg) / totalWeeks
                    val maxWeeklyKgCap = weightKg * 0.0075
                    hitPaceCap = desiredWeeklyKg > maxWeeklyKgCap
                    val cappedWeeklyKg = minOf(desiredWeeklyKg, maxWeeklyKgCap).coerceAtLeast(0.25)
                    ((cappedWeeklyKg * 7700.0) / 7.0).roundToInt()
                } else {
                    450
                }
                val safeDeficit = paceCappedDeficit.coerceIn(300, max(300, maxSafeDeficit20))
                val after20PctCap = max(tdee - safeDeficit, (tdee * 0.80).roundToInt())
                val hitDeficitCap = paceCappedDeficit > maxSafeDeficit20 || hitPaceCap
                val rawFinal = max(after20PctCap, minSafeCals)
                val finalCals = rawFinal.coerceAtMost(4800)
                val hitBmrFloor = after20PctCap < max(bmrFloor, 1)

                val note = when {
                    hitBmrFloor -> "Kept at your safe energy floor ($finalCals kcal) so you don't under-eat."
                    hitDeficitCap -> "Deficit capped at 20% below maintenance to protect muscle and energy."
                    else -> null
                }

                CalorieSafetyResult(
                    targetCals = finalCals,
                    hitBmrFloor = hitBmrFloor,
                    hitDeficitCap = hitDeficitCap,
                    note = note
                )
            }
            "Recomposition" -> {
                val raw = tdee - 100
                val finalCals = max(raw, minSafeCals).coerceAtMost(4800)
                CalorieSafetyResult(
                    targetCals = finalCals,
                    hitBmrFloor = raw < minSafeCals,
                    hitDeficitCap = false,
                    note = if (raw < minSafeCals) "Kept at your safe floor ($finalCals kcal) for recovery." else null
                )
            }
            else -> {
                val finalCals = max(tdee, minSafeCals).coerceAtMost(5000)
                CalorieSafetyResult(
                    targetCals = finalCals,
                    hitBmrFloor = false,
                    hitDeficitCap = false,
                    note = null
                )
            }
        }
    }

    fun proteinPerKgForGoal(goal: String): Double {
        return when (normalizeGoal(goal)) {
            "Muscle Gain" -> 2.0 // 1.8 - 2.2 g/kg
            "Fat Loss" -> 2.2 // 2.0 - 2.4 g/kg
            "Recomposition" -> 2.1 // 2.0 - 2.2 g/kg
            else -> 1.6 // 1.4 - 1.8 g/kg
        }
    }

    fun proteinRangeLabel(goal: String): String {
        return when (normalizeGoal(goal)) {
            "Muscle Gain" -> "1.8–2.2 g/kg"
            "Fat Loss" -> "2.0–2.4 g/kg"
            "Recomposition" -> "2.0–2.2 g/kg"
            else -> "1.4–1.8 g/kg"
        }
    }

    fun calcMacroTargets(targetCals: Int, weightKg: Double, goal: String): MacroTargets {
        if (targetCals <= 0) return MacroTargets(protein = 0, fat = 0, carbs = 0)
        val w = if (weightKg > 0) weightKg else 70.0
        val normGoal = normalizeGoal(goal)
        val pPerKg = proteinPerKgForGoal(normGoal)
        val fatRatio = if (normGoal == "General Fitness") 0.28 else 0.25
        val rawProteinG = (w * pPerKg).roundToInt().coerceIn(40, 280)
        val maxProteinCappedByCals = max(40, ((targetCals * 0.45) / 4.0).roundToInt())
        val proteinG = minOf(rawProteinG, maxProteinCappedByCals)
        val fatG = ((targetCals * fatRatio) / 9.0).roundToInt().coerceAtLeast(30)
        val remainingCals = max(0, targetCals - ((proteinG * 4) + (fatG * 9)))
        val carbsG = (remainingCals / 4.0).roundToInt()
        return MacroTargets(protein = proteinG, fat = fatG, carbs = carbsG)
    }

    fun evaluateSafetyAdvisory(profile: UserProfile): SafetyAdvisory {
        val messages = mutableListOf<String>()
        val isMinor = profile.age in 1..17
        if (isMinor) {
            messages.add("Under-18 training note: Please train with adult or qualified coach supervision and avoid restrictive dieting.")
        }

        val normGoal = normalizeGoal(profile.goal)
        val targetW = profile.targetWeight
        var extremeGoal = false
        if (profile.height > 0.0 && targetW != null && targetW > 0.0) {
            val targetBmi = calcBMI(targetW, profile.height)
            if (targetBmi in 1.0..18.4 && normGoal == "Fat Loss") {
                extremeGoal = true
                messages.add("Your target weight corresponds to a BMI of $targetBmi (< 18.5). Consider setting a healthier sustainable target.")
            } else if (targetBmi >= 38.0 && normGoal == "Muscle Gain") {
                extremeGoal = true
                messages.add("Your target weight corresponds to a BMI of $targetBmi. Consider aiming for gradual lean muscle gain or body recomposition.")
            }
        }
        val pace = calcPaceAssessment(profile.weight, profile.targetWeight, profile.durationMonths, profile.goal)
        if (pace.isWarning) {
            extremeGoal = true
            messages.add(pace.message)
        }

        return SafetyAdvisory(
            requiresMinorSupervision = isMinor,
            hasExtremeGoalWarning = extremeGoal,
            messages = messages
        )
    }

    fun calcPaceAssessment(
        currentW: Double,
        targetW: Double?,
        durationMonths: Int,
        goal: String
    ): PaceAssessment {
        val cur = if (currentW > 0) currentW else 70.0
        val tgt = targetW ?: cur
        val dur = max(1, durationMonths)
        val totalWeeks = max(1.0, dur * 4.33)
        val normGoal = normalizeGoal(goal)

        if (normGoal == "Muscle Gain") {
            val delta = tgt - cur
            val weeklyRate = delta / totalWeeks
            val pctBW = (weeklyRate / cur) * 100.0
            if (delta <= 0.0) {
                return PaceAssessment(
                    status = "Maintenance",
                    rate = "0.00 kg/wk",
                    badge = "Steady Pace",
                    isWarning = false,
                    message = "Target weight matches your current weight. Focus is building lean strength around maintenance calories."
                )
            }
            if (pctBW > 0.75 || weeklyRate > 0.55) {
                val safeMonthly = (cur * 0.005 * 4.33).coerceAtLeast(0.8)
                val minMonths = ceil(delta / safeMonthly).toInt().coerceAtLeast(2)
                return PaceAssessment(
                    status = "Too Fast",
                    rate = String.format(Locale.US, "+%.2f kg/wk (%.2f%% BW)", weeklyRate, pctBW),
                    badge = "Above 0.75%/wk Cap",
                    isWarning = true,
                    message = "Gaining +${String.format(Locale.US, "%.2f", weeklyRate)} kg/week (${String.format(Locale.US, "%.1f", pctBW)}% of body weight) is faster than muscle grows and adds extra body fat. Try ${minMonths}+ months."
                )
            }
            return PaceAssessment(
                status = "Steady Muscle Gain",
                rate = String.format(Locale.US, "+%.2f kg/wk", weeklyRate),
                badge = "Healthy Pace",
                isWarning = false,
                message = "About +${String.format(Locale.US, "%.2f", weeklyRate)} kg/week supports steady muscle growth without unnecessary fat gain."
            )
        }

        if (normGoal == "Fat Loss") {
            val delta = cur - tgt
            val weeklyRate = delta / totalWeeks
            val pctBW = (weeklyRate / cur) * 100.0
            if (delta <= 0.0) {
                return PaceAssessment(
                    status = "Maintenance",
                    rate = "0.00 kg/wk",
                    badge = "Maintenance Pace",
                    isWarning = false,
                    message = "Your target weight matches your current weight, so calories stay comfortable and sustainable."
                )
            }
            if (pctBW > 0.75) {
                val minWeeks = ceil(delta / (cur * 0.0075))
                val minMonths = ceil(minWeeks / 4.33).toInt().coerceAtLeast(2)
                return PaceAssessment(
                    status = "Aggressive Timeline",
                    rate = String.format(Locale.US, "-%.2f kg/wk (%.2f%% BW)", weeklyRate, pctBW),
                    badge = "Above 0.75%/wk Safe Limit",
                    isWarning = true,
                    message = "Losing -${String.format(Locale.US, "%.2f", weeklyRate)} kg/week (${String.format(Locale.US, "%.2f", pctBW)}% body weight/week) goes past the ~0.75%/week limit. We capped your calorie deficit safely—consider ${minMonths}+ months."
                )
            }
            return PaceAssessment(
                status = "Safe Fat Loss Pace",
                rate = String.format(Locale.US, "-%.2f kg/wk (%.2f%% BW)", weeklyRate, pctBW),
                badge = "Sustainable Deficit",
                isWarning = false,
                message = "Losing -${String.format(Locale.US, "%.2f", weeklyRate)} kg/week stays within 0.75% of your body weight per week to protect muscle and energy."
            )
        }

        return PaceAssessment(
            status = if (normGoal == "Recomposition") "Body Recomposition" else "Balanced Fitness",
            rate = "Steady",
            badge = if (normGoal == "Recomposition") "Recomp Balance" else "Maintenance",
            isWarning = false,
            message = "Calories sit close to your daily burn so you can build strength, recover well, and stay consistent."
        )
    }

    fun buildWhyTheseNumbersSummary(
        profile: UserProfile,
        bmr: Int,
        tdee: Int,
        targetCals: Int,
        proteinG: Int,
        waterLiters: Double,
        isWorkoutDay: Boolean
    ): String {
        val normGoal = normalizeGoal(profile.goal)
        val goalPhrase = when (normGoal) {
            "Muscle Gain" -> "+${max(0, targetCals - tdee)} kcal lean surplus"
            "Fat Loss" -> {
                val diff = max(0, tdee - targetCals)
                "-$diff kcal safe deficit (capped at 20% and never below your $bmr kcal BMR)"
            }
            "Recomposition" -> "near-maintenance balance (~$targetCals kcal, >= $bmr kcal BMR)"
            else -> "daily maintenance equilibrium ($tdee kcal)"
        }
        val waterNote = if (isWorkoutDay) {
            "${String.format(Locale.US, "%.2f", waterLiters).trimEnd('0').trimEnd('.')} L water (35 ml/kg + 0.5 L training day)"
        } else {
            "${String.format(Locale.US, "%.2f", waterLiters).trimEnd('0').trimEnd('.')} L water (35 ml/kg)"
        }
        return "Calories use your $bmr kcal resting burn and ${profile.daysPerWeek} workouts/week (~$tdee kcal burn) with a $goalPhrase. Protein uses ${proteinRangeLabel(normGoal)} (${proteinG}g), paired with $waterNote."
    }

    fun selectSplit(days: Int): SplitConfig {
        return when (days) {
            1 -> SplitConfig(
                name = "1-Day Full Body Anchor",
                days = listOf("Full Body Strength & Core")
            )
            2 -> SplitConfig(
                name = "2-Day Upper / Lower Split",
                days = listOf("Upper Body & Core", "Lower Body & Core")
            )
            3 -> SplitConfig(
                name = "3-Day Hypertrophy Rotation",
                days = listOf(
                    "Chest, Shoulders & Triceps",
                    "Back, Biceps & Abs",
                    "Legs (Quads, Hamstrings, Calves)"
                )
            )
            5 -> SplitConfig(
                name = "5-Day Targeted Muscle Split",
                days = listOf(
                    "Chest & Triceps",
                    "Back & Biceps",
                    "Legs (Quads, Hamstrings, Calves)",
                    "Shoulders & Traps",
                    "Arms & Abs"
                )
            )
            6 -> SplitConfig(
                name = "6-Day High-Frequency Split",
                days = listOf(
                    "Chest & Triceps",
                    "Back & Lats",
                    "Legs (Quads & Calves)",
                    "Shoulders & Traps",
                    "Hamstrings, Glutes & Abs",
                    "Arms & Core"
                )
            )
            7 -> SplitConfig(
                name = "7-Day Continuous Athletic Split",
                days = listOf(
                    "Chest & Triceps",
                    "Back & Lats",
                    "Legs (Quads, Hamstrings, Calves)",
                    "Shoulders & Abs",
                    "Arms & Upper Body",
                    "Posterior Chain & Core",
                    "Active Mobility & Restoration"
                )
            )
            else -> SplitConfig(
                name = "4-Day Split Protocol",
                days = listOf(
                    "Chest & Triceps",
                    "Back & Biceps",
                    "Legs (Quads, Hamstrings, Calves)",
                    "Shoulders & Abs"
                )
            )
        }
    }
}
