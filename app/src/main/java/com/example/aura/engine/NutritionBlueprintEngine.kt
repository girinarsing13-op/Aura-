package com.example.aura.engine

import com.example.aura.data.model.MacroTargets
import com.example.aura.data.model.NutritionBlueprint
import com.example.aura.data.model.NutritionDayPlan
import com.example.aura.data.model.ScaledMeal
import com.example.aura.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

object NutritionBlueprintEngine {

    fun hasAllergenConflict(item: FoodItem, allergies: List<String>): Boolean {
        if (allergies.isEmpty() || allergies.all { it.equals("None", ignoreCase = true) }) return false
        val text = (item.name + " " + item.basePortion + " " + item.recipe.joinToString(" ")).lowercase(Locale.US)
        for (allergy in allergies) {
            val a = allergy.lowercase(Locale.US).trim()
            if (a == "none") continue
            when {
                a.contains("dairy") || a.contains("lactose") -> {
                    val dairyTerms = listOf("paneer", "milk", "curd", "yogurt", "whey", "cheese", "butter", "ghee", "dairy", "cream", "chaas")
                    if (dairyTerms.any { text.contains(it) }) return true
                }
                a.contains("gluten") -> {
                    val glutenTerms = listOf("roti", "wheat", "multigrain", "bread", "flour", "atta", "pasta", "oats", "phulka", "sourdough")
                    if (glutenTerms.any { text.contains(it) }) return true
                }
                a.contains("nut") || a.contains("peanut") -> {
                    val nutTerms = listOf("nut", "peanut", "almond", "cashew", "walnut", "pistachio")
                    if (nutTerms.any { text.contains(it) }) return true
                }
                a.contains("soy") -> {
                    val soyTerms = listOf("soya", "tofu", "soy", "edamame")
                    if (soyTerms.any { text.contains(it) }) return true
                }
                a.contains("egg") -> {
                    val eggTerms = listOf("egg", "omelette", "eggs", "scramble")
                    if (eggTerms.any { text.contains(it) }) return true
                }
                a.contains("seafood") || a.contains("fish") || a.contains("shellfish") -> {
                    val seafoodTerms = listOf("fish", "salmon", "prawn", "tuna", "seafood", "shrimp", "crab")
                    if (seafoodTerms.any { text.contains(it) }) return true
                }
            }
        }
        return false
    }

    private val HYPOALLERGENIC_BREAKFAST = FoodItem(
        name = "Spiced Poha with Steamed Vegetables & Seeds",
        basePortion = "1 bowl poha (180g)",
        cals = 310,
        p = 10,
        c = 54,
        f = 6,
        recipe = listOf(
            "Sauté mustard seeds, curry leaves, onions, turmeric in 1 tsp olive oil.",
            "Mix in rinsed flattened rice flakes (poha) and steam for 3 mins.",
            "Garnish with fresh lemon juice, roasted pumpkin seeds, and fresh coriander."
        ),
        budgetTier = "Low"
    )

    private val HYPOALLERGENIC_MID_MORNING = FoodItem(
        name = "Seasonal Fruit & Roasted Chickpeas",
        basePortion = "1 seasonal fruit + 35g roasted chana",
        cals = 195,
        p = 8,
        c = 36,
        f = 3,
        recipe = listOf(
            "Pair one fresh seasonal fruit with dry-roasted Bengal gram."
        ),
        budgetTier = "Low"
    )

    private val HYPOALLERGENIC_LUNCH = FoodItem(
        name = "Steamed Basmati Rice & Tadka Yellow Moong Dal",
        basePortion = "1 bowl dal + 1 bowl rice",
        cals = 440,
        p = 18,
        c = 76,
        f = 6,
        recipe = listOf(
            "Pressure cook yellow moong dal with turmeric, ginger, and pink rock salt.",
            "Temper with 1 tsp cold-pressed oil, cumin seeds, and fresh garlic.",
            "Serve hot over steamed fragrant long-grain basmati rice."
        ),
        budgetTier = "Low"
    )

    private val HYPOALLERGENIC_SNACK = FoodItem(
        name = "Roasted Spiced Chickpeas & Pumpkin Seeds",
        basePortion = "50g roasted chana + 15g seeds",
        cals = 220,
        p = 12,
        c = 28,
        f = 7,
        recipe = listOf(
            "Dry roast whole Bengal gram and pepitas with cumin and rock salt.",
            "Rich in zinc, magnesium, and slow-burning complex carbs."
        ),
        budgetTier = "Low"
    )

    private val HYPOALLERGENIC_DINNER = FoodItem(
        name = "Yellow Moong Dal Khichdi with Mixed Vegetables",
        basePortion = "1 large bowl (250g)",
        cals = 410,
        p = 16,
        c = 66,
        f = 8,
        recipe = listOf(
            "Sauté cumin, chopped carrots, green peas, and tomatoes in 1 tsp olive oil.",
            "Add washed rice and yellow moong dal.",
            "Simmer with water until tender and comforting."
        ),
        budgetTier = "Low"
    )

    private val HYPOALLERGENIC_BEDTIME = FoodItem(
        name = "Light Roasted Chana & Pumpkin Seeds",
        basePortion = "30g roasted chana + 15g pumpkin seeds",
        cals = 155,
        p = 10,
        c = 16,
        f = 6,
        recipe = listOf(
            "Enjoy a light allergen-free handful of roasted chana and pumpkin seeds before bed."
        ),
        budgetTier = "Low"
    )

    private fun buildProteinShakeItem(profile: UserProfile): FoodItem {
        val isDairyRestricted = profile.dietPreference.equals("Vegan", ignoreCase = true) ||
            profile.allergies.any {
                it.contains("dairy", ignoreCase = true) || it.contains("lactose", ignoreCase = true)
            }

        return if (isDairyRestricted) {
            FoodItem(
                name = "Plant Protein Shake (Pea & Rice)",
                basePortion = "1 scoop (32g) + 300ml cold water",
                cals = 130,
                p = 25,
                c = 4,
                f = 2,
                recipe = listOf(
                    "Blend or shake 1 scoop (32g) unflavored or vanilla pea-and-rice isolate with 300ml chilled water.",
                    "Drink within 60 minutes after training to hit your daily protein goal easily."
                ),
                budgetTier = "Moderate"
            )
        } else {
            FoodItem(
                name = "Whey Protein Shake",
                basePortion = "1 scoop (32g) + 300ml cold water",
                cals = 130,
                p = 25,
                c = 4,
                f = 2,
                recipe = listOf(
                    "Shake 1 scoop (32g) whey protein concentrate or isolate in 300ml chilled water.",
                    "Take post-workout or in the evening to support muscle recovery."
                ),
                budgetTier = "Moderate"
            )
        }
    }

    private fun filterForBudget(items: List<FoodItem>, budgetTier: String): List<FoodItem> {
        if (!budgetTier.equals("Low", ignoreCase = true)) return items
        val nonPremium = items.filterNot { it.budgetTier.equals("Premium", ignoreCase = true) }
        val lowOnly = nonPremium.filter { it.budgetTier.equals("Low", ignoreCase = true) }
        return when {
            lowOnly.isNotEmpty() -> lowOnly
            nonPremium.isNotEmpty() -> nonPremium
            else -> emptyList()
        }
    }

    private fun getSafeFoodItem(
        mealType: String,
        preferredPool: List<FoodItem>,
        fallbackPools: List<List<FoodItem>>,
        dayOffset: Int,
        allergies: List<String>,
        budgetTier: String,
        allowShake: Boolean
    ): FoodItem {
        fun cleanCandidates(pool: List<FoodItem>): List<FoodItem> {
            return pool.filter { item ->
                !hasAllergenConflict(item, allergies) &&
                    (allowShake || !item.name.contains("shake", ignoreCase = true))
            }
        }

        // 1. Allergen-safe items in preferred pool, honoring Low budget first
        val safePreferred = cleanCandidates(preferredPool)
        val budgetPreferred = filterForBudget(safePreferred, budgetTier)
        if (budgetPreferred.isNotEmpty()) {
            return budgetPreferred[dayOffset % budgetPreferred.size]
        }

        // 2. Allergen-safe items in fallback pools honoring Low budget
        for (pool in fallbackPools) {
            val safe = cleanCandidates(pool)
            val budgetSafe = filterForBudget(safe, budgetTier)
            if (budgetSafe.isNotEmpty()) {
                val candidate = budgetSafe[dayOffset % budgetSafe.size]
                return candidate.copy(
                    recipe = listOf("Tailored safe substitution matching your diet & preferences.") + candidate.recipe
                )
            }
        }

        // 3. If Low budget excluded every non-premium option, use certified hypoallergenic low-budget meal
        //    (Strictly NEVER bypasses allergen filter!)
        val base = when (mealType) {
            "breakfast" -> HYPOALLERGENIC_BREAKFAST
            "mid_morning" -> HYPOALLERGENIC_MID_MORNING
            "lunch" -> HYPOALLERGENIC_LUNCH
            "snack" -> HYPOALLERGENIC_SNACK
            "bedtime" -> HYPOALLERGENIC_BEDTIME
            else -> HYPOALLERGENIC_DINNER
        }
        return base.copy(
            recipe = listOf("Allergen-safe budget recipe.") + base.recipe
        )
    }

    fun generate(profile: UserProfile, targetCals: Int, targetMacros: MacroTargets): NutritionBlueprint {
        val pref = profile.dietPreference
        val foodPool = FoodUnitsDB.pool[pref] ?: FoodUnitsDB.pool["Eggetarian"]!!
        val veganPool = FoodUnitsDB.pool["Vegan"] ?: foodPool
        val vegPool = FoodUnitsDB.pool["Vegetarian"] ?: foodPool
        val allergies = profile.allergies
        val budgetTier = profile.budgetTier
        val mealCount = profile.mealsPerDay.coerceIn(3, 6)
        val workoutDayIndices = WorkoutScheduleEngine.resolveWorkoutDays(profile)
            .toSet()
            .ifEmpty { (1..profile.daysPerWeek.coerceIn(1, 7)).toSet() }

        val week = mutableListOf<NutritionDayPlan>()

        for (day in 1..7) {
            val isWorkoutDay = day in workoutDayIndices
            val includeShakeToday = profile.proteinSupplement && isWorkoutDay
            val shakeItem = if (includeShakeToday) buildProteinShakeItem(profile) else null

            val bRaw = getSafeFoodItem(
                mealType = "breakfast",
                preferredPool = foodPool.breakfast,
                fallbackPools = listOf(veganPool.breakfast, vegPool.breakfast),
                dayOffset = day - 1,
                allergies = allergies,
                budgetTier = budgetTier,
                allowShake = false
            )
            val midMorningRaw = getSafeFoodItem(
                mealType = "mid_morning",
                preferredPool = foodPool.midMorning,
                fallbackPools = listOf(veganPool.midMorning, vegPool.midMorning),
                dayOffset = day,
                allergies = allergies,
                budgetTier = budgetTier,
                allowShake = false
            )
            val lRaw = getSafeFoodItem(
                mealType = "lunch",
                preferredPool = foodPool.lunch,
                fallbackPools = listOf(veganPool.lunch, vegPool.lunch),
                dayOffset = day,
                allergies = allergies,
                budgetTier = budgetTier,
                allowShake = false
            )
            val sRaw = getSafeFoodItem(
                mealType = "snack",
                preferredPool = foodPool.snack,
                fallbackPools = listOf(veganPool.snack, vegPool.snack),
                dayOffset = day + 1,
                allergies = allergies,
                budgetTier = budgetTier,
                allowShake = false
            )
            val dRaw = getSafeFoodItem(
                mealType = "dinner",
                preferredPool = foodPool.dinner,
                fallbackPools = listOf(veganPool.dinner, vegPool.dinner),
                dayOffset = day + 2,
                allergies = allergies,
                budgetTier = budgetTier,
                allowShake = false
            )
            val bedtimeRaw = getSafeFoodItem(
                mealType = "bedtime",
                preferredPool = foodPool.bedtime,
                fallbackPools = listOf(vegPool.bedtime, veganPool.bedtime),
                dayOffset = day + 3,
                allergies = allergies,
                budgetTier = budgetTier,
                allowShake = false
            )

            // Build the exact meal list matching mealsPerDay (3..6)
            data class MealSlotSpec(
                val label: String,
                val item: FoodItem,
                val isFixedShake: Boolean = false,
                val attachShake: Boolean = false
            )

            val slotSpecs: List<MealSlotSpec> = when (mealCount) {
                3 -> listOf(
                    MealSlotSpec("Breakfast", bRaw),
                    MealSlotSpec("Lunch", lRaw),
                    MealSlotSpec(
                        label = if (includeShakeToday) "Dinner & Post-Workout Shake" else "Dinner",
                        item = dRaw,
                        attachShake = includeShakeToday
                    )
                )
                4 -> listOf(
                    MealSlotSpec("Breakfast", bRaw),
                    MealSlotSpec("Lunch", lRaw),
                    if (includeShakeToday && shakeItem != null) {
                        MealSlotSpec("Evening Fuel (Post-Workout)", shakeItem, isFixedShake = true)
                    } else {
                        MealSlotSpec("Evening Fuel", sRaw)
                    },
                    MealSlotSpec("Dinner", dRaw)
                )
                5 -> listOf(
                    MealSlotSpec("Breakfast", bRaw),
                    MealSlotSpec("Mid-Morning Snack", midMorningRaw),
                    MealSlotSpec("Lunch", lRaw),
                    if (includeShakeToday && shakeItem != null) {
                        MealSlotSpec("Evening Fuel (Post-Workout)", shakeItem, isFixedShake = true)
                    } else {
                        MealSlotSpec("Evening Fuel", sRaw)
                    },
                    MealSlotSpec("Dinner", dRaw)
                )
                else -> listOf(
                    MealSlotSpec("Breakfast", bRaw),
                    MealSlotSpec("Mid-Morning Snack", midMorningRaw),
                    MealSlotSpec("Lunch", lRaw),
                    if (includeShakeToday && shakeItem != null) {
                        MealSlotSpec("Evening Fuel (Post-Workout)", shakeItem, isFixedShake = true)
                    } else {
                        MealSlotSpec("Evening Fuel", sRaw)
                    },
                    MealSlotSpec("Dinner", dRaw),
                    MealSlotSpec("Bedtime Snack", bedtimeRaw)
                )
            }

            // Reserve shake macros on workout days when supplement is enabled,
            // and scale all remaining food meals so daily totals stay consistent across 3..6 meals & supplement on/off.
            val shakeCals = shakeItem?.cals ?: 0
            val shakeP = shakeItem?.p ?: 0
            val shakeC = shakeItem?.c ?: 0
            val shakeF = shakeItem?.f ?: 0

            val foodTargetCals = max(0, targetCals - shakeCals)
            val foodTargetP = max(0, targetMacros.protein - shakeP)
            val foodTargetC = max(0, targetMacros.carbs - shakeC)
            val foodTargetF = max(0, targetMacros.fat - shakeF)

            val scalableSpecs = slotSpecs.filter { !it.isFixedShake }
            val sumBaseCals = max(1, scalableSpecs.sumOf { it.item.cals })
            val sumBaseP = max(1, scalableSpecs.sumOf { it.item.p })
            val sumBaseC = max(1, scalableSpecs.sumOf { it.item.c })
            val sumBaseF = max(1, scalableSpecs.sumOf { it.item.f })
            val calScaleFactor = foodTargetCals.toDouble() / sumBaseCals.toDouble()

            val scaledMutable = slotSpecs.map { spec ->
                if (spec.isFixedShake && shakeItem != null) {
                    ScaledMeal(
                        label = spec.label,
                        name = shakeItem.name,
                        portion = shakeItem.basePortion,
                        cals = shakeItem.cals,
                        p = shakeItem.p,
                        c = shakeItem.c,
                        f = shakeItem.f,
                        recipe = shakeItem.recipe
                    )
                } else {
                    val m = spec.item
                    val calShare = m.cals.toDouble() / sumBaseCals.toDouble()
                    val pShare = m.p.toDouble() / sumBaseP.toDouble()
                    val cShare = m.c.toDouble() / sumBaseC.toDouble()
                    val fShare = m.f.toDouble() / sumBaseF.toDouble()

                    var mealCals = (foodTargetCals * calShare).roundToInt()
                    var mealP = (foodTargetP * pShare).roundToInt()
                    var mealC = (foodTargetC * cShare).roundToInt()
                    var mealF = (foodTargetF * fShare).roundToInt()

                    val pct = (calScaleFactor * 100.0).roundToInt().coerceIn(35, 250)
                    var mealName = m.name
                    var mealPortion = "${pct}% standard serving (${m.basePortion})"
                    var mealRecipe = m.recipe

                    if (spec.attachShake && shakeItem != null) {
                        mealCals += shakeItem.cals
                        mealP += shakeItem.p
                        mealC += shakeItem.c
                        mealF += shakeItem.f
                        mealName = "${m.name} + ${shakeItem.name}"
                        mealPortion = "$mealPortion + ${shakeItem.basePortion}"
                        mealRecipe = m.recipe + listOf("Post-workout shake: ${shakeItem.recipe.first()}")
                    }

                    ScaledMeal(
                        label = spec.label,
                        name = mealName,
                        portion = mealPortion,
                        cals = mealCals,
                        p = mealP,
                        c = mealC,
                        f = mealF,
                        recipe = mealRecipe
                    )
                }
            }.toMutableList()

            // Reconcile any rounding remainder onto the largest non-shake meal so daily totals match exactly
            val anchorIdx = slotSpecs.indices
                .filter { !slotSpecs[it].isFixedShake }
                .maxByOrNull { scaledMutable[it].cals }
                ?: 0

            if (anchorIdx in scaledMutable.indices && targetCals > 0) {
                val calsDiff = targetCals - scaledMutable.sumOf { it.cals }
                val pDiff = targetMacros.protein - scaledMutable.sumOf { it.p }
                val cDiff = targetMacros.carbs - scaledMutable.sumOf { it.c }
                val fDiff = targetMacros.fat - scaledMutable.sumOf { it.f }
                val anchor = scaledMutable[anchorIdx]
                scaledMutable[anchorIdx] = anchor.copy(
                    cals = max(0, anchor.cals + calsDiff),
                    p = max(0, anchor.p + pDiff),
                    c = max(0, anchor.c + cDiff),
                    f = max(0, anchor.f + fDiff)
                )
            }

            val totalCals = scaledMutable.sumOf { it.cals }
            val totalProtein = scaledMutable.sumOf { it.p }
            val totalCarbs = scaledMutable.sumOf { it.c }
            val totalFats = scaledMutable.sumOf { it.f }

            week.add(
                NutritionDayPlan(
                    dayNumber = day,
                    dayTitle = "Day $day",
                    totalCals = totalCals,
                    totalProtein = totalProtein,
                    totalCarbs = totalCarbs,
                    totalFats = totalFats,
                    meals = scaledMutable
                )
            )
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        return NutritionBlueprint(
            targetCals = targetCals,
            targetMacros = targetMacros,
            generatedAt = dateStr,
            week = week
        )
    }
}
