package com.example.aura.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aura.data.model.ScaledMeal
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.ui.AuraViewModel
import com.example.aura.ui.theme.AuraTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DietScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val blueprint by viewModel.blueprint.collectAsState()
    val habits by viewModel.dailyHabits.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val user = userProfile ?: viewModel.draftProfile.collectAsState().value

    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayKey = remember { sdf.format(Date()) }
    val todayHabit = habits[todayKey]

    val isTodayWorkout = viewModel.isTodayWorkoutDay(user, blueprint)
    val targetWaterLiters = remember(user.weight, isTodayWorkout) {
        FitnessCalcEngine.calcWaterTargetLiters(user.weight, isTodayWorkout)
    }
    val targetWaterMl = remember(user.weight, isTodayWorkout) {
        FitnessCalcEngine.calcWaterTargetMl(user.weight, isTodayWorkout)
    }
    val waterIntakeMl = todayHabit?.waterMl ?: 0
    val eatenMealIndices = remember(todayHabit?.eatenMealsCsv) {
        todayHabit?.eatenMealsCsv.orEmpty()
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }
    val expandedMealMap = remember { mutableStateMapOf<String, Boolean>() }

    val nutritionPlan = blueprint?.nutrition?.week?.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraTheme.colors.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "diet_header") {
                Spacer(modifier = Modifier.height(4.dp))
                DietHeader(goal = user.goal, preference = user.dietPreference)
            }

            // Macro Targets Hero Card
            if (blueprint != null) {
                val metrics = blueprint!!.metrics
                item(key = "diet_macros") {
                    MacroTargetsHeroCard(
                        targetCals = metrics.goalTargetCals,
                        protein = metrics.macroTargets.protein,
                        carbs = metrics.macroTargets.carbs,
                        fat = metrics.macroTargets.fat,
                        completedDiet = todayHabit?.diet == true,
                        onToggleDietComplete = {
                            viewModel.toggleHabit(todayKey, "diet")
                        }
                    )
                }

                // "Why these numbers" explanation line
                item(key = "diet_why_numbers") {
                    val whyText = remember(user, metrics, targetWaterLiters, isTodayWorkout) {
                        FitnessCalcEngine.buildWhyTheseNumbersSummary(
                            profile = user,
                            bmr = metrics.bmr,
                            tdee = metrics.tdee,
                            targetCals = metrics.goalTargetCals,
                            proteinG = metrics.macroTargets.protein,
                            waterLiters = targetWaterLiters,
                            isWorkoutDay = isTodayWorkout
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AuraTheme.colors.surfaceSecondary)
                            .border(1.dp, AuraTheme.colors.border, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("why_these_numbers_text")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Why these numbers",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                            }
                            Text(
                                text = whyText,
                                fontSize = 12.sp,
                                color = AuraTheme.colors.secondaryText
                            )
                        }
                    }
                }
            }

            // Hydration Tracker Card
            item(key = "diet_hydration") {
                HydrationTrackerCard(
                    currentMl = waterIntakeMl,
                    targetMl = targetWaterMl,
                    isWorkoutDay = isTodayWorkout,
                    onAddWater = { delta ->
                        viewModel.addWaterMl(delta, targetWaterMl)
                    }
                )
            }

            // Meals Plan
            if (nutritionPlan != null) {
                item(key = "diet_meals_heading") {
                    Text(
                        text = "DAILY MEALS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        letterSpacing = 1.sp
                    )
                }

                nutritionPlan.meals.forEachIndexed { idx, meal ->
                    val mealKey = "meal_$idx"
                    item(key = mealKey) {
                        MealCard(
                            title = meal.label.ifBlank { "Meal ${idx + 1}" },
                            meal = meal,
                            isExpanded = expandedMealMap[mealKey] ?: false,
                            isEaten = eatenMealIndices.contains(idx),
                            onToggleExpand = { expandedMealMap[mealKey] = !(expandedMealMap[mealKey] ?: false) },
                            onToggleEaten = {
                                val cals = blueprint?.metrics?.goalTargetCals ?: 0
                                viewModel.toggleMealEaten(idx, nutritionPlan.meals, cals)
                            }
                        )
                    }
                }
            }

            item(key = "diet_bottom_spacer") {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun DietHeader(goal: String, preference: String) {
    val accent = AuraTheme.colors.accent

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NUTRITION FUEL ENGINE",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 1.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Macro Blueprint",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AuraTheme.colors.primaryText
        )
        Text(
            text = "$goal • $preference Protocol",
            fontSize = 12.sp,
            color = AuraTheme.colors.secondaryText
        )
    }
}

@Composable
private fun MacroTargetsHeroCard(
    targetCals: Int,
    protein: Int,
    carbs: Int,
    fat: Int,
    completedDiet: Boolean,
    onToggleDietComplete: () -> Unit
) {
    val accent = AuraTheme.colors.accent
    val accentContrast = AuraTheme.colors.accentContrast

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(26.dp))
            .padding(20.dp)
            .testTag("macro_targets_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAILY ENERGY TARGET",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$targetCals",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = AuraTheme.colors.primaryText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kcal",
                            fontSize = 14.sp,
                            color = AuraTheme.colors.secondaryText,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (completedDiet) accent else AuraTheme.colors.surfaceSecondary)
                        .clickable { onToggleDietComplete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark daily nutrition complete",
                        tint = if (completedDiet) accentContrast else AuraTheme.colors.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Macros Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroPill("PROTEIN", "${protein}g", "4 kcal/g", accent)
                MacroPill("CARBS", "${carbs}g", "4 kcal/g", Color(0xFF4A90E2))
                MacroPill("FATS", "${fat}g", "9 kcal/g", Color(0xFFF5A623))
            }
        }
    }
}

@Composable
private fun MacroPill(
    label: String,
    grams: String,
    density: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AuraTheme.colors.surfaceSecondary)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = grams,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTheme.colors.primaryText
            )
            Text(
                text = density,
                fontSize = 9.sp,
                color = AuraTheme.colors.secondaryText
            )
        }
    }
}

@Composable
private fun HydrationTrackerCard(
    currentMl: Int,
    targetMl: Int,
    isWorkoutDay: Boolean,
    onAddWater: (Int) -> Unit
) {
    val progress = if (targetMl > 0) (currentMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f) else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(AuraTheme.colors.card)
            .border(1.dp, AuraTheme.colors.cardBorder, RoundedCornerShape(22.dp))
            .padding(18.dp)
            .testTag("hydration_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF007AFF).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalDrink,
                            contentDescription = null,
                            tint = Color(0xFF007AFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isWorkoutDay) "WATER TARGET (+0.5 L WORKOUT)" else "WATER TARGET",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF007AFF)
                        )
                        Text(
                            text = "${currentMl}ml / ${targetMl}ml",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTheme.colors.primaryText
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { onAddWater(250) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraTheme.colors.surfaceSecondary,
                            contentColor = AuraTheme.colors.primaryText
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .heightIn(min = 40.dp)
                            .testTag("diet_add_water_250_button")
                    ) {
                        Text("+250ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onAddWater(500) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraTheme.colors.surfaceSecondary,
                            contentColor = AuraTheme.colors.primaryText
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .heightIn(min = 40.dp)
                            .testTag("diet_add_water_500_button")
                    ) {
                        Text("+500ml", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(0xFF007AFF),
                trackColor = AuraTheme.colors.surfaceSecondary
            )
        }
    }
}

@Composable
private fun MealCard(
    title: String,
    meal: ScaledMeal,
    isExpanded: Boolean,
    isEaten: Boolean,
    onToggleExpand: () -> Unit,
    onToggleEaten: () -> Unit
) {
    val accent = AuraTheme.colors.accent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AuraTheme.colors.card)
            .border(
                1.dp,
                if (isEaten) accent.copy(alpha = 0.4f) else AuraTheme.colors.cardBorder,
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isEaten) accent else AuraTheme.colors.surfaceSecondary)
                            .clickable { onToggleEaten() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isEaten) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Meal eaten",
                                tint = AuraTheme.colors.accentContrast,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                        Text(
                            text = meal.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEaten) AuraTheme.colors.secondaryText else AuraTheme.colors.primaryText
                        )
                        Text(
                            text = "${meal.cals} kcal • P: ${meal.p}g | C: ${meal.c}g | F: ${meal.f}g",
                            fontSize = 11.5.sp,
                            color = AuraTheme.colors.secondaryText
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = AuraTheme.colors.secondaryText
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "RECIPE & PREPARATION",
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = AuraTheme.colors.secondaryText
                    )
                    meal.recipe.forEachIndexed { i, step ->
                        Text(
                            text = "${i + 1}. $step",
                            fontSize = 12.sp,
                            color = AuraTheme.colors.primaryText,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
