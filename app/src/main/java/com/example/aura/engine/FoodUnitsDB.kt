package com.example.aura.engine

data class FoodItem(
    val name: String,
    val basePortion: String,
    val cals: Int,
    val p: Int,
    val c: Int,
    val f: Int,
    val recipe: List<String>,
    val budgetTier: String = "Moderate" // "Low", "Moderate", "Premium"
)

data class DietMenuPool(
    val breakfast: List<FoodItem>,
    val lunch: List<FoodItem>,
    val snack: List<FoodItem>,
    val dinner: List<FoodItem>,
    val midMorning: List<FoodItem> = emptyList(),
    val bedtime: List<FoodItem> = emptyList()
)

object FoodUnitsDB {
    val pool: Map<String, DietMenuPool> = mapOf(
        "Vegetarian" to DietMenuPool(
            breakfast = listOf(
                FoodItem(
                    name = "Moong Dal Chilla & Mint Chutney",
                    basePortion = "2 chillas (150g)",
                    cals = 320,
                    p = 18,
                    c = 44,
                    f = 8,
                    recipe = listOf(
                        "Blend soaked yellow moong dal with ginger, green chilies, and salt into a smooth batter.",
                        "Heat a non-stick tawa with 1/2 tsp oil and spread batter thinly in circular motion.",
                        "Cook 2 mins each side until crisp and golden. Serve with homemade mint-coriander chutney."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Paneer Bhurji & Multigrain Roti",
                    basePortion = "120g paneer + 2 rotis",
                    cals = 420,
                    p = 26,
                    c = 38,
                    f = 18,
                    recipe = listOf(
                        "Sauté cumin, finely diced onions, green chilies, and tomatoes until soft.",
                        "Add turmeric, chili powder, and crumbled fresh paneer.",
                        "Stir cook for 3-4 mins. Pair with warm 100% whole grain phulkas."
                    ),
                    budgetTier = "Moderate"
                ),
                FoodItem(
                    name = "Greek Yogurt Berry & Chia Parfait",
                    basePortion = "200g Greek yogurt + berries + chia",
                    cals = 340,
                    p = 22,
                    c = 34,
                    f = 11,
                    recipe = listOf(
                        "Layer thick Greek yogurt with fresh blueberries, strawberries, and soaked chia seeds.",
                        "Top with a drizzle of raw honey and toasted pumpkin seeds."
                    ),
                    budgetTier = "Premium"
                )
            ),
            midMorning = listOf(
                FoodItem(
                    name = "Seasonal Fruit & Roasted Chana",
                    basePortion = "1 apple/banana + 35g roasted chana",
                    cals = 200,
                    p = 8,
                    c = 36,
                    f = 3,
                    recipe = listOf(
                        "Pair one seasonal whole fruit with a handful of crunchy roasted chana."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Spiced Buttermilk & Sprouted Moong",
                    basePortion = "1 glass chaas + 80g moong sprouts",
                    cals = 180,
                    p = 11,
                    c = 26,
                    f = 3,
                    recipe = listOf(
                        "Toss steamed moong sprouts with lemon, tomato, and black salt.",
                        "Serve alongside chilled roasted-cumin buttermilk."
                    ),
                    budgetTier = "Low"
                )
            ),
            lunch = listOf(
                FoodItem(
                    name = "Rajma Masala with Steamed Brown Rice",
                    basePortion = "1 bowl rajma + 1 bowl rice",
                    cals = 480,
                    p = 20,
                    c = 80,
                    f = 9,
                    recipe = listOf(
                        "Pressure cook soaked rajma beans until soft.",
                        "Prepare onion-tomato gravy tempered with jeera, ginger-garlic, and garam masala.",
                        "Simmer beans in gravy for 15 minutes. Serve over steamed fragrant brown rice."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Tadka Chana Dal with 2 Phulkas & Salad",
                    basePortion = "1 bowl dal + 2 rotis + cucumber",
                    cals = 440,
                    p = 22,
                    c = 68,
                    f = 9,
                    recipe = listOf(
                        "Boil chana dal with turmeric, ginger, and pink salt.",
                        "Temper with 1 tsp ghee, hing, cumin seeds, and fresh garlic cloves.",
                        "Stir hot tempering into dal and enjoy with fresh rotis and sliced cucumber."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Quinoa & Avocado Paneer Power Bowl",
                    basePortion = "1 bowl quinoa + 100g paneer + avocado",
                    cals = 510,
                    p = 25,
                    c = 52,
                    f = 22,
                    recipe = listOf(
                        "Simmer white quinoa until fluffy and toss with grilled paneer cubes.",
                        "Top with sliced Hass avocado, cherry tomatoes, and lemon olive-oil dressing."
                    ),
                    budgetTier = "Premium"
                )
            ),
            snack = listOf(
                FoodItem(
                    name = "Sattu Protein Drink (Savoury)",
                    basePortion = "40g sattu + 300ml water",
                    cals = 190,
                    p = 12,
                    c = 26,
                    f = 3,
                    recipe = listOf(
                        "Whisk roasted gram sattu powder in chilled water.",
                        "Add roasted jeera powder, black salt, fresh lemon juice, and chopped mint. Stir thoroughly."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Roasted Spiced Chana & Curd Dip",
                    basePortion = "45g chana + 80g curd",
                    cals = 210,
                    p = 12,
                    c = 28,
                    f = 5,
                    recipe = listOf(
                        "Dry roast whole Bengal gram in a skillet with chaat masala and black pepper.",
                        "Serve with a cool bowl of whisked low-fat curd."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Roasted Spiced Chana & Almonds",
                    basePortion = "45g chana + 10 almonds",
                    cals = 230,
                    p = 11,
                    c = 24,
                    f = 10,
                    recipe = listOf(
                        "Dry roast whole Bengal gram in a skillet with chaat masala and black pepper.",
                        "Toss in whole raw almonds for sustained satiety and micronutrient balance."
                    ),
                    budgetTier = "Moderate"
                )
            ),
            dinner = listOf(
                FoodItem(
                    name = "Soya Chunks Masala with 2 Rotis",
                    basePortion = "55g soya + 2 rotis",
                    cals = 420,
                    p = 34,
                    c = 48,
                    f = 9,
                    recipe = listOf(
                        "Boil soya chunks for 6 mins, drain and squeeze out all excess moisture.",
                        "Sauté with onion, tomato puree, coriander powder, and kasuri methi.",
                        "Simmer for 8 mins so chunks absorb the savory spices. Serve with phulkas."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Yellow Toor Dal Khichdi & Curd",
                    basePortion = "1 large bowl khichdi + 100g curd",
                    cals = 450,
                    p = 19,
                    c = 72,
                    f = 10,
                    recipe = listOf(
                        "Cook equal parts toor dal and rice with turmeric and salt in a pressure cooker.",
                        "Temper with jeera and hing in 1 tsp ghee.",
                        "Serve warm with fresh probiotics from unflavored low-fat curd."
                    ),
                    budgetTier = "Low"
                )
            ),
            bedtime = listOf(
                FoodItem(
                    name = "Low-Fat Curd Bowl with Roasted Cumin",
                    basePortion = "180g fresh low-fat curd",
                    cals = 135,
                    p = 10,
                    c = 11,
                    f = 5,
                    recipe = listOf(
                        "Spoon chilled fresh low-fat curd into a bowl.",
                        "Sprinkle lightly with roasted jeera powder for slow overnight casein digestion."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Warm Toned Milk with Turmeric",
                    basePortion = "250ml toned milk",
                    cals = 150,
                    p = 9,
                    c = 13,
                    f = 7,
                    recipe = listOf(
                        "Gently warm toned milk with a pinch of turmeric and cardamom.",
                        "Sip 45 minutes before bed for steady overnight recovery."
                    ),
                    budgetTier = "Low"
                )
            )
        ),
        "Eggetarian" to DietMenuPool(
            breakfast = listOf(
                FoodItem(
                    name = "Boiled Eggs (3) with Multigrain Toast",
                    basePortion = "3 whole eggs + 2 slices",
                    cals = 360,
                    p = 24,
                    c = 30,
                    f = 15,
                    recipe = listOf(
                        "Boil eggs in bubbling water for 8 minutes for firm yolks.",
                        "Toast whole wheat or sourdough slices.",
                        "Slice eggs and season with freshly cracked black pepper and rock salt."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Egg Bhurji (3 eggs) & 2 Warm Phulkas",
                    basePortion = "3 eggs + 2 rotis",
                    cals = 410,
                    p = 25,
                    c = 36,
                    f = 18,
                    recipe = listOf(
                        "Sauté finely chopped onions, ginger, green chilies, and tomatoes.",
                        "Crack eggs into the pan and scramble on medium flame until just cooked and moist.",
                        "Garnish with fresh coriander leaves and serve alongside warm rotis."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Avocado Sourdough Toast with Poached Eggs",
                    basePortion = "2 eggs + sourdough + 1/2 avocado",
                    cals = 430,
                    p = 20,
                    c = 36,
                    f = 22,
                    recipe = listOf(
                        "Toast artisan sourdough and spread mashed Hass avocado with chili flakes.",
                        "Top with two gently poached eggs."
                    ),
                    budgetTier = "Premium"
                )
            ),
            midMorning = listOf(
                FoodItem(
                    name = "Boiled Eggs (2) & Seasonal Fruit",
                    basePortion = "2 boiled eggs + 1 seasonal fruit",
                    cals = 220,
                    p = 13,
                    c = 20,
                    f = 10,
                    recipe = listOf(
                        "Pair 2 hard-boiled eggs seasoned with pepper alongside a fresh seasonal fruit."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Seasonal Fruit & Roasted Chana",
                    basePortion = "1 fruit + 35g roasted chana",
                    cals = 200,
                    p = 8,
                    c = 36,
                    f = 3,
                    recipe = listOf(
                        "Enjoy a fresh seasonal fruit with crisp roasted Bengal gram."
                    ),
                    budgetTier = "Low"
                )
            ),
            lunch = listOf(
                FoodItem(
                    name = "Homestyle Egg Curry (3 eggs) & Steamed Jeera Rice",
                    basePortion = "3 eggs + 1 bowl rice",
                    cals = 490,
                    p = 26,
                    c = 62,
                    f = 16,
                    recipe = listOf(
                        "Pan sear boiled eggs lightly in 1/2 tsp oil with a pinch of turmeric and degi mirch.",
                        "Simmer a rich onion, ginger, garlic, and tomato curry base.",
                        "Drop seared eggs into simmering curry for 6 mins so flavors meld. Pair with jeera rice."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Rajma Masala with 2 Boiled Eggs & Rice",
                    basePortion = "1 bowl rajma + 2 eggs + rice",
                    cals = 520,
                    p = 31,
                    c = 68,
                    f = 15,
                    recipe = listOf(
                        "Pressure cook rajma with aromatic whole spices.",
                        "Prepare a tomato-onion masala and simmer until thickened.",
                        "Serve hot over steamed rice alongside halved boiled eggs for elevated protein synthesis."
                    ),
                    budgetTier = "Low"
                )
            ),
            snack = listOf(
                FoodItem(
                    name = "Egg White Omelette (3 whites) & Black Coffee",
                    basePortion = "3 egg whites + veggies",
                    cals = 160,
                    p = 15,
                    c = 8,
                    f = 4,
                    recipe = listOf(
                        "Whisk egg whites with finely minced bell peppers, onions, and salt.",
                        "Pour on a lightly greased non-stick skillet and flip once firm.",
                        "Serve alongside freshly brewed unsweetened black roast coffee."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Roasted Spiced Chana & Almonds",
                    basePortion = "45g chana + 10 almonds",
                    cals = 230,
                    p = 11,
                    c = 24,
                    f = 10,
                    recipe = listOf(
                        "Dry roast Bengal gram with rock salt and roasted cumin powder.",
                        "Pair with whole raw almonds for healthy mono-unsaturated fats."
                    ),
                    budgetTier = "Moderate"
                )
            ),
            dinner = listOf(
                FoodItem(
                    name = "Yellow Tadka Dal, 2 Boiled Eggs & 2 Rotis",
                    basePortion = "1 bowl dal + 2 eggs + 2 rotis",
                    cals = 450,
                    p = 29,
                    c = 54,
                    f = 14,
                    recipe = listOf(
                        "Simmer yellow toor dal with garlic and cumin tadka.",
                        "Serve with 2 soft boiled eggs and two whole grain phulkas."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Egg & Paneer Scramble with 2 Phulkas",
                    basePortion = "2 eggs + 60g paneer + 2 rotis",
                    cals = 460,
                    p = 32,
                    c = 40,
                    f = 18,
                    recipe = listOf(
                        "Sauté onions and spices in a pan.",
                        "Add 2 beaten eggs and 60g crumbled paneer together.",
                        "Cook gently for 3-4 mins until fluffy. Serve with whole wheat phulkas."
                    ),
                    budgetTier = "Moderate"
                )
            ),
            bedtime = listOf(
                FoodItem(
                    name = "Low-Fat Curd Bowl with Roasted Cumin",
                    basePortion = "180g fresh low-fat curd",
                    cals = 135,
                    p = 10,
                    c = 11,
                    f = 5,
                    recipe = listOf(
                        "Enjoy chilled low-fat curd with roasted cumin for slow-release nighttime protein."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Warm Toned Milk with Turmeric",
                    basePortion = "250ml toned milk",
                    cals = 150,
                    p = 9,
                    c = 13,
                    f = 7,
                    recipe = listOf(
                        "Warm toned milk gently with a pinch of turmeric before bed."
                    ),
                    budgetTier = "Low"
                )
            )
        ),
        "Non-Vegetarian" to DietMenuPool(
            breakfast = listOf(
                FoodItem(
                    name = "Boiled Eggs (3) with Multigrain Toast",
                    basePortion = "3 whole eggs + 2 slices",
                    cals = 360,
                    p = 24,
                    c = 30,
                    f = 15,
                    recipe = listOf(
                        "Boil fresh eggs 8 minutes for firm, golden yolks.",
                        "Toast whole wheat slices.",
                        "Season with cracked black pepper and a touch of sea salt."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Chicken Breast Keema & 2 Warm Phulkas",
                    basePortion = "130g chicken keema + 2 rotis",
                    cals = 430,
                    p = 38,
                    c = 36,
                    f = 12,
                    recipe = listOf(
                        "Sauté minced chicken breast with onions, ginger-garlic paste, and crushed spices.",
                        "Cook covered for 10 mins until juices run clear.",
                        "Garnish with lemon juice and coriander; serve with whole wheat rotis."
                    ),
                    budgetTier = "Low"
                )
            ),
            midMorning = listOf(
                FoodItem(
                    name = "Boiled Eggs (2) & Seasonal Fruit",
                    basePortion = "2 boiled eggs + 1 seasonal fruit",
                    cals = 220,
                    p = 13,
                    c = 20,
                    f = 10,
                    recipe = listOf(
                        "Pair 2 boiled eggs with a fresh seasonal fruit for steady morning energy."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Curd Bowl & Seasonal Fruit",
                    basePortion = "150g curd + 1 fruit",
                    cals = 195,
                    p = 9,
                    c = 28,
                    f = 5,
                    recipe = listOf(
                        "Mix diced seasonal fruit into chilled low-fat curd."
                    ),
                    budgetTier = "Low"
                )
            ),
            lunch = listOf(
                FoodItem(
                    name = "Homestyle Chicken Curry & Steamed Brown Rice",
                    basePortion = "150g chicken breast + rice",
                    cals = 520,
                    p = 44,
                    c = 58,
                    f = 12,
                    recipe = listOf(
                        "Sear boneless chicken breast cubes with ginger-garlic, onions, and ground spices.",
                        "Add fresh tomato puree and simmer for 15 minutes until tender.",
                        "Serve with steamed brown rice and cucumber slices."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Tawa Pan-Grilled Chicken & 2 Rotis with Dal",
                    basePortion = "140g chicken + 1/2 bowl dal + 2 rotis",
                    cals = 510,
                    p = 46,
                    c = 48,
                    f = 11,
                    recipe = listOf(
                        "Marinate chicken breast in hung curd, lemon, turmeric, and tandoori spices for 20 mins.",
                        "Pan grill on medium heat for 6 mins per side.",
                        "Pair with a small katori of yellow dal and 2 whole wheat rotis."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Herb-Seared Atlantic Salmon & Quinoa",
                    basePortion = "150g salmon + 1 bowl quinoa",
                    cals = 540,
                    p = 39,
                    c = 44,
                    f = 22,
                    recipe = listOf(
                        "Pan-sear salmon fillet skin-side down with lemon, dill, and olive oil.",
                        "Serve over fluffy quinoa with steamed asparagus."
                    ),
                    budgetTier = "Premium"
                )
            ),
            snack = listOf(
                FoodItem(
                    name = "Boiled Egg Whites (4) with Chaat Masala",
                    basePortion = "4 egg whites",
                    cals = 75,
                    p = 16,
                    c = 2,
                    f = 0,
                    recipe = listOf(
                        "Boil eggs, remove yolks, and slice whites into bite-sized segments.",
                        "Sprinkle with tangy chaat masala and freshly ground black pepper."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Roasted Spiced Chana & Almonds",
                    basePortion = "45g chana + 10 almonds",
                    cals = 230,
                    p = 11,
                    c = 24,
                    f = 10,
                    recipe = listOf(
                        "Dry roast chana and raw almonds for an easy, portable protein and fiber snack."
                    ),
                    budgetTier = "Moderate"
                )
            ),
            dinner = listOf(
                FoodItem(
                    name = "Tawa Grilled Chicken Breast with Veggie Stir-Fry",
                    basePortion = "160g chicken breast + veggies",
                    cals = 430,
                    p = 48,
                    c = 16,
                    f = 11,
                    recipe = listOf(
                        "Marinate chicken breast in curd, garlic, and cracked pepper.",
                        "Pan grill on a hot non-stick skillet for 6 minutes each side.",
                        "Toss bell peppers and onions in the same skillet for a nutrient-packed side."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Chicken Curry with 2 Whole Wheat Phulkas",
                    basePortion = "140g chicken + 2 rotis",
                    cals = 450,
                    p = 42,
                    c = 42,
                    f = 12,
                    recipe = listOf(
                        "Cook lean chicken in an aromatic homestyle onion-tomato gravy.",
                        "Serve alongside 2 soft whole wheat phulkas and green salad."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Grilled Prawns & Avocado Quinoa Salad",
                    basePortion = "150g prawns + quinoa + avocado",
                    cals = 470,
                    p = 36,
                    c = 40,
                    f = 18,
                    recipe = listOf(
                        "Sauté fresh prawns with garlic and lemon.",
                        "Serve over quinoa with diced avocado and leafy greens."
                    ),
                    budgetTier = "Premium"
                )
            ),
            bedtime = listOf(
                FoodItem(
                    name = "Low-Fat Curd Bowl with Roasted Cumin",
                    basePortion = "180g fresh low-fat curd",
                    cals = 135,
                    p = 10,
                    c = 11,
                    f = 5,
                    recipe = listOf(
                        "Eat fresh chilled curd with roasted cumin before bed for overnight muscle recovery."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Warm Toned Milk with Turmeric",
                    basePortion = "250ml toned milk",
                    cals = 150,
                    p = 9,
                    c = 13,
                    f = 7,
                    recipe = listOf(
                        "Warm toned milk gently with a pinch of turmeric before sleep."
                    ),
                    budgetTier = "Low"
                )
            )
        ),
        "Vegan" to DietMenuPool(
            breakfast = listOf(
                FoodItem(
                    name = "Moong Dal Chilla & Mint Chutney",
                    basePortion = "2 chillas (150g)",
                    cals = 320,
                    p = 18,
                    c = 44,
                    f = 8,
                    recipe = listOf(
                        "Blend soaked yellow moong dal with ginger, green chilies, and salt into a smooth batter.",
                        "Heat a non-stick tawa with 1/2 tsp oil and spread batter thinly in circular motion.",
                        "Cook 2 mins each side until crisp and golden. Serve with homemade mint chutney."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Tofu Scramble with Multigrain Toast",
                    basePortion = "150g firm tofu + 2 toasts",
                    cals = 340,
                    p = 22,
                    c = 34,
                    f = 12,
                    recipe = listOf(
                        "Crumble firm pressed organic tofu into a warm skillet.",
                        "Season with nutritional yeast, turmeric, black salt (kala namak), and bell peppers.",
                        "Sauté 5 mins until fragrant. Serve alongside toasted wholegrain slices."
                    ),
                    budgetTier = "Moderate"
                ),
                FoodItem(
                    name = "Almond Butter Chia & Berry Bowl",
                    basePortion = "Chia pudding + berries + almond butter",
                    cals = 370,
                    p = 14,
                    c = 40,
                    f = 18,
                    recipe = listOf(
                        "Soak chia seeds overnight in unsweetened almond drink.",
                        "Top with mixed berries and natural almond butter."
                    ),
                    budgetTier = "Premium"
                )
            ),
            midMorning = listOf(
                FoodItem(
                    name = "Seasonal Fruit & Roasted Chana",
                    basePortion = "1 seasonal fruit + 35g chana",
                    cals = 200,
                    p = 8,
                    c = 36,
                    f = 3,
                    recipe = listOf(
                        "Pair one seasonal whole fruit with crunchy roasted chana."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Spiced Moong Sprouts Bowl",
                    basePortion = "120g steamed moong sprouts",
                    cals = 175,
                    p = 12,
                    c = 28,
                    f = 2,
                    recipe = listOf(
                        "Toss steamed green moong sprouts with lemon juice, chopped onion, and cucumber."
                    ),
                    budgetTier = "Low"
                )
            ),
            lunch = listOf(
                FoodItem(
                    name = "Rajma Masala with Steamed Brown Rice",
                    basePortion = "1 bowl rajma + 1 bowl rice",
                    cals = 480,
                    p = 20,
                    c = 80,
                    f = 9,
                    recipe = listOf(
                        "Pressure cook soaked kidney beans with aromatic whole spices.",
                        "Simmer in an onion, garlic, tomato, and garam masala gravy for 15 mins.",
                        "Serve hot over steamed brown rice."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Chana Masala with Jeera Rice & Salad",
                    basePortion = "1 bowl chickpeas + 1 bowl rice",
                    cals = 460,
                    p = 21,
                    c = 76,
                    f = 8,
                    recipe = listOf(
                        "Simmer tender white chickpeas in a robust tomato, ginger, and amchur gravy.",
                        "Pair with fragrant jeera brown rice and fresh cucumber slices."
                    ),
                    budgetTier = "Low"
                )
            ),
            snack = listOf(
                FoodItem(
                    name = "Savoury Sattu Protein Drink",
                    basePortion = "40g sattu + 300ml water",
                    cals = 190,
                    p = 12,
                    c = 26,
                    f = 3,
                    recipe = listOf(
                        "Whisk roasted Bengal gram flour in chilled water.",
                        "Add lemon juice, roasted cumin, and pink rock salt."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Roasted Spiced Chana & Almonds",
                    basePortion = "45g chana + 10 almonds",
                    cals = 230,
                    p = 11,
                    c = 24,
                    f = 10,
                    recipe = listOf(
                        "Dry roast whole Bengal gram in a skillet with chaat masala.",
                        "Toss with whole raw almonds for healthy plant-based fats."
                    ),
                    budgetTier = "Moderate"
                )
            ),
            dinner = listOf(
                FoodItem(
                    name = "Soya Chunks Masala with 2 Rotis",
                    basePortion = "55g soya + 2 rotis",
                    cals = 420,
                    p = 34,
                    c = 48,
                    f = 9,
                    recipe = listOf(
                        "Boil high-protein soya chunks for 6 mins, drain and squeeze excess moisture.",
                        "Sauté with onion, tomato puree, coriander, and kasuri methi.",
                        "Simmer for 8 mins so chunks absorb savory spices. Serve with warm phulkas."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Tofu & Mixed Veggie Stir-Fry with Rice",
                    basePortion = "150g tofu + 1 bowl rice",
                    cals = 440,
                    p = 24,
                    c = 58,
                    f = 12,
                    recipe = listOf(
                        "Pan sear cubed firm tofu until edges turn golden.",
                        "Toss in broccoli florets, bell peppers, soy sauce, and garlic.",
                        "Serve over a warm bowl of steamed brown rice."
                    ),
                    budgetTier = "Moderate"
                )
            ),
            bedtime = listOf(
                FoodItem(
                    name = "Warm Unsweetened Soy Drink with Cinnamon",
                    basePortion = "250ml unsweetened soy drink",
                    cals = 120,
                    p = 10,
                    c = 6,
                    f = 5,
                    recipe = listOf(
                        "Warm unsweetened soy drink gently with a pinch of cinnamon before sleep."
                    ),
                    budgetTier = "Low"
                ),
                FoodItem(
                    name = "Roasted Chana & Pumpkin Seeds Bowl",
                    basePortion = "35g roasted chana + 15g pumpkin seeds",
                    cals = 165,
                    p = 10,
                    c = 17,
                    f = 6,
                    recipe = listOf(
                        "Lightly toast Bengal gram and pumpkin seeds for a dairy-free, soy-free evening snack."
                    ),
                    budgetTier = "Low"
                )
            )
        )
    )
}
