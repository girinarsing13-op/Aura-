package com.example.aura.engine

import android.content.Context
import com.example.aura.data.db.FoodEntity
import com.example.aura.data.db.MealLogEntity
import com.example.aura.data.model.Blueprint
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.ScaledMeal
import com.example.aura.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import org.json.JSONArray

enum class FoodFilterTab {
    ALL,
    RECENT,
    FAVORITES
}

data class ScaledNutritionValues(
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val gramsLogged: Double
)

data class DailyNutritionTotals(
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val count: Int = 0
)

data class WeeklyNutritionDayPoint(
    val dateKey: String,
    val dayShortLabel: String, // "M", "T", ...
    val calories: Int,
    val proteinG: Int,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class WeeklyNutritionSummary(
    val days: List<WeeklyNutritionDayPoint>,
    val avgCalories: Int,
    val avgProteinG: Int,
    val targetCalories: Int,
    val targetProteinG: Int,
    val loggedDaysCount: Int
)

object NutritionTrackingEngine {

    val MEAL_SLOTS = listOf("Breakfast", "Lunch", "Snack", "Dinner")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun loadFoodsFromAssets(context: Context): List<FoodEntity> {
        return try {
            val raw = context.assets.open("foods.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            parseFoodsJson(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseFoodsJson(rawJson: String): List<FoodEntity> {
        val arr = JSONArray(rawJson)
        val out = ArrayList<FoodEntity>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id").trim()
            val name = obj.optString("name").trim()
            if (id.isBlank() || name.isBlank()) continue
            out.add(
                FoodEntity(
                    id = id,
                    name = name,
                    servingDesc = obj.optString("servingDesc", "1 serving").ifBlank { "1 serving" },
                    gramsPerServing = obj.optDouble("gramsPerServing", 100.0).coerceAtLeast(1.0),
                    calories = obj.optInt("calories", 0).coerceAtLeast(0),
                    protein = obj.optDouble("protein", 0.0).coerceAtLeast(0.0),
                    carbs = obj.optDouble("carbs", 0.0).coerceAtLeast(0.0),
                    fat = obj.optDouble("fat", 0.0).coerceAtLeast(0.0),
                    dietType = obj.optString("dietType", "veg").lowercase(Locale.US),
                    isVegan = obj.optBoolean("isVegan", false),
                    allergensCsv = obj.optString("allergens", "").lowercase(Locale.US),
                    isCustom = false,
                    isFavorite = false,
                    lastUsedAt = 0L
                )
            )
        }
        return out
    }

    fun getDefaultMealSlotForHour(hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): String {
        return when {
            hourOfDay < 11 -> "Breakfast"
            hourOfDay < 15 -> "Lunch"
            hourOfDay < 19 -> "Snack"
            else -> "Dinner"
        }
    }

    fun mapPlanLabelToSlot(label: String, index: Int): String {
        val l = label.lowercase(Locale.US)
        return when {
            l.contains("breakfast") || l.contains("morning") -> "Breakfast"
            l.contains("lunch") || l.contains("midday") -> "Lunch"
            l.contains("dinner") || l.contains("night") -> "Dinner"
            l.contains("snack") || l.contains("evening") || l.contains("fuel") -> "Snack"
            else -> MEAL_SLOTS[index.coerceIn(0, MEAL_SLOTS.lastIndex)]
        }
    }

    fun computeScaledMacros(
        food: FoodEntity,
        quantity: Double,
        unitMode: String
    ): ScaledNutritionValues {
        val baseGrams = food.gramsPerServing.coerceAtLeast(1.0)
        val safeQty = quantity.coerceAtLeast(0.0)
        val multiplier = if (unitMode == "grams") {
            safeQty / baseGrams
        } else {
            safeQty
        }
        val gramsLogged = if (unitMode == "grams") safeQty else baseGrams * multiplier
        return ScaledNutritionValues(
            calories = (food.calories * multiplier).roundToInt().coerceAtLeast(0),
            protein = ((food.protein * multiplier) * 10.0).roundToInt() / 10.0,
            carbs = ((food.carbs * multiplier) * 10.0).roundToInt() / 10.0,
            fat = ((food.fat * multiplier) * 10.0).roundToInt() / 10.0,
            gramsLogged = (gramsLogged * 10.0).roundToInt() / 10.0
        )
    }

    fun computeScaledMacrosFromBase(
        baseGramsPerServing: Double,
        baseCaloriesAtServing: Int,
        baseProteinAtServing: Double,
        baseCarbsAtServing: Double,
        baseFatAtServing: Double,
        quantity: Double,
        unitMode: String
    ): ScaledNutritionValues {
        val dummy = FoodEntity(
            id = "",
            name = "",
            servingDesc = "",
            gramsPerServing = baseGramsPerServing.coerceAtLeast(1.0),
            calories = baseCaloriesAtServing,
            protein = baseProteinAtServing,
            carbs = baseCarbsAtServing,
            fat = baseFatAtServing,
            dietType = "veg"
        )
        return computeScaledMacros(dummy, quantity, unitMode)
    }

    fun matchesUserDietAndAllergies(food: FoodEntity, profile: UserProfile): Boolean {
        val pref = profile.dietPreference.trim().lowercase(Locale.US)
        val dietAllowed = when {
            pref.contains("vegan") -> food.isVegan && food.dietType == "veg"
            pref.contains("veg") && !pref.contains("non") && !pref.contains("egg") -> food.dietType == "veg"
            pref.contains("egg") -> food.dietType == "veg" || food.dietType == "egg"
            else -> true
        }
        if (!dietAllowed) return false

        val activeAllergies = profile.allergies
            .map { it.trim().lowercase(Locale.US) }
            .filter { it.isNotBlank() && it != "none" }
        if (activeAllergies.isEmpty()) return true

        val allergenTags = food.allergensCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
        val foodText = food.name.lowercase(Locale.US)
        for (allergy in activeAllergies) {
            when {
                allergy.contains("dairy") || allergy.contains("lactose") -> {
                    if ("dairy" in allergenTags || listOf("milk", "cheese", "paneer", "curd", "yogurt", "whey", "casein", "ghee", "butter").any { it in foodText }) {
                        return false
                    }
                }
                allergy.contains("gluten") -> {
                    if ("gluten" in allergenTags || listOf("wheat", "roti", "naan", "paratha", "bread", "pasta", "oats", "barley").any { it in foodText }) {
                        return false
                    }
                }
                allergy.contains("nut") || allergy.contains("peanut") -> {
                    if ("nuts" in allergenTags || "peanut" in allergenTags || listOf("almond", "peanut", "walnut", "cashew", "pistachio", "pecan", "macadamia").any { it in foodText }) {
                        return false
                    }
                }
                allergy.contains("soy") -> {
                    if ("soy" in allergenTags || listOf("soy", "soya", "tofu", "tempeh", "edamame").any { it in foodText }) {
                        return false
                    }
                }
                allergy.contains("egg") -> {
                    if ("egg" in allergenTags || food.dietType == "egg" || "egg" in foodText) {
                        return false
                    }
                }
                allergy.contains("seafood") || allergy.contains("fish") || allergy.contains("shellfish") -> {
                    if ("seafood" in allergenTags || listOf("fish", "salmon", "tuna", "shrimp", "prawn", "cod", "tilapia", "mackerel", "trout", "sardine", "scallop").any { it in foodText }) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun searchFoods(
        foods: List<FoodEntity>,
        query: String,
        filterTab: FoodFilterTab,
        respectUserDiet: Boolean,
        profile: UserProfile
    ): List<FoodEntity> {
        val base = foods.asSequence()
            .filter { !respectUserDiet || matchesUserDietAndAllergies(it, profile) }
            .filter {
                when (filterTab) {
                    FoodFilterTab.ALL -> true
                    FoodFilterTab.RECENT -> it.lastUsedAt > 0L
                    FoodFilterTab.FAVORITES -> it.isFavorite
                }
            }

        val cleanQuery = query.trim().lowercase(Locale.US)
        if (cleanQuery.isEmpty()) {
            return when (filterTab) {
                FoodFilterTab.RECENT -> base.sortedByDescending { it.lastUsedAt }.toList()
                FoodFilterTab.FAVORITES -> base.sortedBy { it.name }.toList()
                FoodFilterTab.ALL -> base.sortedWith(
                    compareByDescending<FoodEntity> { it.isFavorite }
                        .thenByDescending { it.lastUsedAt }
                        .thenBy { it.name }
                ).toList()
            }
        }

        val queryTokens = cleanQuery.split(Regex("[\\s/(),-]+")).filter { it.isNotBlank() }

        return base.mapNotNull { food ->
            val score = scoreFoodMatch(food, cleanQuery, queryTokens)
            if (score > 0) food to score else null
        }.sortedWith(
            compareByDescending<Pair<FoodEntity, Int>> { it.second }
                .thenByDescending { it.first.isFavorite }
                .thenByDescending { it.first.lastUsedAt }
                .thenBy { it.first.name }
        ).map { it.first }.toList()
    }

    private fun scoreFoodMatch(
        food: FoodEntity,
        cleanQuery: String,
        queryTokens: List<String>
    ): Int {
        val nameLower = food.name.lowercase(Locale.US)
        if (nameLower == cleanQuery) return 1000
        if (nameLower.startsWith(cleanQuery)) return 850
        if (nameLower.contains(cleanQuery)) return 720

        val nameWords = nameLower.split(Regex("[\\s/(),-]+")).filter { it.isNotBlank() }
        var totalScore = 0
        var matchedTokens = 0

        for (qt in queryTokens) {
            var bestTokenScore = 0
            for (nw in nameWords) {
                if (nw == qt) {
                    bestTokenScore = max(bestTokenScore, 240)
                } else if (nw.startsWith(qt)) {
                    bestTokenScore = max(bestTokenScore, 190)
                } else if (qt.length >= 3 && nw.contains(qt)) {
                    bestTokenScore = max(bestTokenScore, 145)
                } else {
                    val maxDist = when {
                        qt.length >= 6 -> 2
                        qt.length >= 3 -> 1
                        else -> 0
                    }
                    if (maxDist > 0 && abs(nw.length - qt.length) <= maxDist + 2) {
                        val prefixTarget = if (nw.length > qt.length + maxDist) nw.substring(0, qt.length) else nw
                        val d = min(levenshtein(qt, nw), levenshtein(qt, prefixTarget))
                        if (d <= maxDist) {
                            bestTokenScore = max(bestTokenScore, 125 - d * 25)
                        }
                    }
                }
            }
            if (bestTokenScore == 0 && qt.length >= 3 && isSubsequence(qt, nameLower)) {
                bestTokenScore = 70
            }
            if (bestTokenScore > 0) {
                matchedTokens++
                totalScore += bestTokenScore
            }
        }

        if (matchedTokens == 0) return 0
        if (matchedTokens == queryTokens.size) {
            totalScore += 180
        }
        if (food.lastUsedAt > 0L) totalScore += 10
        if (food.isFavorite) totalScore += 15
        return totalScore
    }

    private fun isSubsequence(needle: String, haystack: String): Boolean {
        var i = 0
        var j = 0
        while (i < needle.length && j < haystack.length) {
            if (needle[i] == haystack[j]) i++
            j++
        }
        return i == needle.length
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in a.indices) {
            curr[0] = i + 1
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                curr[j + 1] = min(min(curr[j] + 1, prev[j + 1] + 1), prev[j] + cost)
            }
            for (j in prev.indices) prev[j] = curr[j]
        }
        return prev[b.length]
    }

    fun summarizeDayLogs(logsForDate: List<MealLogEntity>): DailyNutritionTotals {
        if (logsForDate.isEmpty()) return DailyNutritionTotals()
        var cals = 0
        var p = 0.0
        var c = 0.0
        var f = 0.0
        for (item in logsForDate) {
            cals += item.calories
            p += item.protein
            c += item.carbs
            f += item.fat
        }
        return DailyNutritionTotals(
            calories = cals,
            protein = (p * 10.0).roundToInt() / 10.0,
            carbs = (c * 10.0).roundToInt() / 10.0,
            fat = (f * 10.0).roundToInt() / 10.0,
            count = logsForDate.size
        )
    }

    /**
     * If protein is still low later in the day (>= 16:00 and >= 20g protein remaining),
     * suggest 2-3 catalog foods fitting remaining calories while respecting diet type & allergies.
     */
    fun suggestLateDayProteinFoods(
        foods: List<FoodEntity>,
        profile: UserProfile,
        remainingCalories: Int,
        remainingProteinG: Int,
        currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): List<FoodEntity> {
        if (currentHour < 16 || remainingProteinG < 20) return emptyList()
        val calBudget = if (remainingCalories > 100) remainingCalories else 250

        val eligible = foods.filter { food ->
            !food.isCustom &&
                food.protein >= 10.0 &&
                food.calories in 50..(calBudget + 40) &&
                matchesUserDietAndAllergies(food, profile)
        }

        val ranked = eligible.sortedWith(
            compareByDescending<FoodEntity> { it.protein / max(it.calories, 1).toDouble() }
                .thenByDescending { it.protein }
        )
        if (ranked.size >= 2) return ranked.take(3)

        // Fallback if calories are very tight: include compact lean protein options <= 280 kcal
        return foods.filter { food ->
            !food.isCustom &&
                food.protein >= 8.0 &&
                food.calories in 30..280 &&
                matchesUserDietAndAllergies(food, profile)
        }.sortedByDescending { it.protein / max(it.calories, 1).toDouble() }.take(3)
    }

    fun buildWeeklyNutritionSummary(
        profile: UserProfile,
        blueprint: Blueprint?,
        mealLogs: List<MealLogEntity>,
        habits: Map<String, DailyHabitStatus>,
        nowCal: Calendar = Calendar.getInstance()
    ): WeeklyNutritionSummary {
        val targetCals = blueprint?.metrics?.goalTargetCals
            ?: FitnessCalcEngine.calcGoalTargetCals(
                tdee = FitnessCalcEngine.calcTDEE(
                    FitnessCalcEngine.calcBMR(profile.weight, profile.height, profile.age, profile.gender),
                    profile.daysPerWeek
                ),
                goal = profile.goal,
                bmr = FitnessCalcEngine.calcBMR(profile.weight, profile.height, profile.age, profile.gender),
                weightKg = profile.weight,
                targetWeightKg = profile.targetWeight,
                durationMonths = profile.durationMonths
            )
        val targetProteinG = blueprint?.metrics?.macroTargets?.protein
            ?: FitnessCalcEngine.calcMacroTargets(targetCals, profile.weight, profile.goal).protein

        val todayKey = dateFormat.format(nowCal.time)
        val daysToMonday = (WorkoutScheduleEngine.calendarToWeekdayIndex(nowCal) - 1).coerceIn(0, 6)
        val mondayCal = (nowCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -daysToMonday)
        }
        val logsByDate = mealLogs.groupBy { it.dateKey }
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

        val points = (0 until 7).map { offset ->
            val c = (mondayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
            val key = dateFormat.format(c.time)
            val dayLogs = logsByDate[key].orEmpty()
            val habit = habits[key]
            val cals = if (dayLogs.isNotEmpty()) {
                dayLogs.sumOf { it.calories }
            } else {
                habit?.loggedCalories ?: 0
            }
            val prot = if (dayLogs.isNotEmpty()) {
                dayLogs.sumOf { it.protein }.roundToInt()
            } else {
                habit?.loggedProteinG ?: 0
            }
            WeeklyNutritionDayPoint(
                dateKey = key,
                dayShortLabel = dayLabels[offset],
                calories = cals,
                proteinG = prot,
                isToday = key == todayKey,
                isFuture = key > todayKey
            )
        }

        val activeLoggedDays = points.filter { !it.isFuture && (it.calories > 0 || it.proteinG > 0) }
        val avgCals = if (activeLoggedDays.isNotEmpty()) {
            (activeLoggedDays.sumOf { it.calories }.toDouble() / activeLoggedDays.size).roundToInt()
        } else 0
        val avgProt = if (activeLoggedDays.isNotEmpty()) {
            (activeLoggedDays.sumOf { it.proteinG }.toDouble() / activeLoggedDays.size).roundToInt()
        } else 0

        return WeeklyNutritionSummary(
            days = points,
            avgCalories = avgCals,
            avgProteinG = avgProt,
            targetCalories = targetCals,
            targetProteinG = targetProteinG,
            loggedDaysCount = activeLoggedDays.size
        )
    }

    fun getYesterdayDateKey(todayCal: Calendar = Calendar.getInstance()): String {
        val c = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        return dateFormat.format(c.time)
    }

    fun formatMacroNumber(value: Double): String {
        val rounded = (value * 10.0).roundToInt() / 10.0
        return if (abs(rounded - rounded.roundToInt()) < 0.05) {
            "${rounded.roundToInt()}"
        } else {
            String.format(Locale.US, "%.1f", rounded)
        }
    }

    fun suggestedMealToLogEntity(
        meal: ScaledMeal,
        dateKey: String,
        mealSlot: String
    ): MealLogEntity {
        val cleanName = if (meal.portion.isNotBlank()) "${meal.name} (${meal.portion})" else meal.name
        return MealLogEntity(
            dateKey = dateKey,
            timestampMillis = System.currentTimeMillis(),
            mealSlot = mealSlot,
            foodId = "plan_${meal.name.lowercase(Locale.US).replace(Regex("[^a-z0-9]+"), "_").take(24)}",
            foodName = cleanName,
            servingDesc = meal.portion.ifBlank { "1 meal" },
            gramsPerServing = 100.0,
            quantity = 1.0,
            unitMode = "serving",
            gramsLogged = 100.0,
            calories = meal.cals.coerceAtLeast(0),
            protein = meal.p.toDouble().coerceAtLeast(0.0),
            carbs = meal.c.toDouble().coerceAtLeast(0.0),
            fat = meal.f.toDouble().coerceAtLeast(0.0),
            isQuickAdd = false
        )
    }
}
