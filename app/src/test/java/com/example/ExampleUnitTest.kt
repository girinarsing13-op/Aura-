package com.example

import com.example.aura.data.model.UserProfile
import com.example.aura.engine.DiagnosticsEngine
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.engine.NutritionBlueprintEngine
import com.example.aura.engine.WorkoutBlueprintEngine
import com.example.aura.engine.WorkoutSplitEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDiagnosticsEngineIntegrity() {
    val diagnostics = DiagnosticsEngine.runAll()
    assertTrue(diagnostics.isNotEmpty())
    val allPassed = diagnostics.all { it.passed }
    assertTrue("All diagnostics checks must pass", allPassed)
  }

  @Test
  fun testBmrAndTdeeCalculations() {
    val bmr = FitnessCalcEngine.calcBMR(75.0, 180.0, 25, "Male")
    val tdee = FitnessCalcEngine.calcTDEE(bmr, 4)
    assertTrue(bmr > 1500)
    assertTrue(tdee > bmr)
  }

  @Test
  fun testSixDaysFrequencyGeneratesExactlySixTrainingDays() {
    // 1. 6-day PPL
    val profilePpl = UserProfile(
      name = "Test Lifter",
      daysPerWeek = 6,
      trainingSplit = "Push / Pull / Legs (PPL)",
      experience = "Intermediate"
    )
    val bpPpl = WorkoutBlueprintEngine.generate(profilePpl)
    val pplTrainingDays = bpPpl.week.count { !it.isRest }
    val pplRestDays = bpPpl.week.count { it.isRest }
    assertEquals("6 days per week must produce exactly 6 training days", 6, pplTrainingDays)
    assertEquals("6 days per week must produce exactly 1 rest day", 1, pplRestDays)

    // 2. 6-day Upper/Lower
    val profileUl = UserProfile(
      name = "Test Lifter",
      daysPerWeek = 6,
      trainingSplit = "6-Day Upper / Lower",
      experience = "Intermediate"
    )
    val bpUl = WorkoutBlueprintEngine.generate(profileUl)
    val ulTrainingDays = bpUl.week.count { !it.isRest }
    assertEquals("6 days Upper/Lower must produce exactly 6 training days", 6, ulTrainingDays)

    // 3. 6 days with an older 'Upper / Lower – 4 Days' split must still respect 6 days
    val profileUlStale = UserProfile(
      name = "Test Lifter",
      daysPerWeek = 6,
      trainingSplit = "Upper / Lower – 4 Days",
      experience = "Intermediate"
    )
    val bpUlStale = WorkoutBlueprintEngine.generate(profileUlStale)
    val ulStaleTrainingDays = bpUlStale.week.count { !it.isRest }
    assertEquals("6 days selection must override 4-day split name to produce exactly 6 training days", 6, ulStaleTrainingDays)
  }

  @Test
  fun testFourDaysFrequencyGeneratesExactlyFourTrainingDays() {
    val profile = UserProfile(
      name = "Test Lifter",
      daysPerWeek = 4,
      trainingSplit = "4-Day Upper / Lower",
      experience = "Intermediate"
    )
    val bp = WorkoutBlueprintEngine.generate(profile)
    val trainingDays = bp.week.count { !it.isRest }
    val restDays = bp.week.count { it.isRest }
    assertEquals("4 days per week must produce exactly 4 training days", 4, trainingDays)
    assertEquals("4 days per week must produce exactly 3 rest days", 3, restDays)
  }

  @Test
  fun testSplitAndDaysReconciliation() {
    // 4 to 6
    val splitFor6 = WorkoutSplitEngine.reconcileSplitForDays("4-Day Upper / Lower", 6)
    assertEquals("6-Day Upper / Lower", splitFor6)

    val splitFor6FromUL = WorkoutSplitEngine.reconcileSplitForDays("Upper / Lower (UL)", 6)
    assertEquals("6-Day Upper / Lower", splitFor6FromUL)

    // 6 to 4
    val splitFor4 = WorkoutSplitEngine.reconcileSplitForDays("6-Day Upper / Lower", 4)
    assertEquals("4-Day Upper / Lower", splitFor4)

    val splitFor4FromPpl = WorkoutSplitEngine.reconcileSplitForDays("Push / Pull / Legs (PPL)", 4)
    assertEquals("4-Day Upper / Lower", splitFor4FromPpl)
  }

  @Test
  fun testVolumeAuditNoExerciseHasFiveOrMoreSets() {
    val profile = UserProfile(
      name = "Test Athlete",
      daysPerWeek = 6,
      trainingSplit = "6-Day Upper / Lower",
      experience = "Intermediate",
      focusBodyParts = listOf("Chest", "Triceps")
    )
    val bp = WorkoutBlueprintEngine.generate(profile)
    for (day in bp.week) {
      if (day.isRest) continue
      for (ex in day.exercises) {
        assertTrue("No exercise should have 5 or more working sets (${ex.name} has ${ex.sets})", ex.sets < 5)
        assertTrue("Working sets must be at least 2 (${ex.name} has ${ex.sets})", ex.sets >= 2)
      }
    }
  }

  @Test
  fun testExerciseSlotsDynamicQualityOverQuantity() {
    // 6-day split with 60 min sessions should have 4 exercises per session, not 6
    val profile6d = UserProfile(
      name = "High Freq Lifter",
      daysPerWeek = 6,
      sessionDurationMinutes = 60,
      trainingSplit = "Push / Pull / Legs (PPL)",
      experience = "Intermediate"
    )
    val bp6d = WorkoutBlueprintEngine.generate(profile6d)
    for (day in bp6d.week) {
      if (day.isRest) continue
      assertTrue("6-day high-frequency sessions should have 4 or 5 exercises (${day.exercises.size})", day.exercises.size in 4..5)
    }
  }

  @Test
  fun testAllergenSafeDietGeneration() {
    val profileAllergies = UserProfile(
      name = "Allergic Athlete",
      dietPreference = "Vegetarian",
      allergies = listOf("Dairy", "Gluten")
    )
    val macroTargets = FitnessCalcEngine.calcMacroTargets(2200, 75.0, "Muscle Gain")
    val nutrition = NutritionBlueprintEngine.generate(profileAllergies, 2200, macroTargets)

    val dairyForbidden = listOf("paneer", "milk", "curd", "yogurt", "cheese", "butter", "ghee", "dairy", "cream")
    val glutenForbidden = listOf("roti", "wheat", "multigrain", "bread", "atta", "pasta", "phulka")

    for (day in nutrition.week) {
      for (meal in day.meals) {
        val lowerText = (meal.name + " " + meal.portion + " " + meal.recipe.joinToString(" ")).lowercase()
        for (term in dairyForbidden) {
          assertFalse("Meal ${meal.name} should not contain dairy term '$term'", lowerText.contains(term))
        }
        for (term in glutenForbidden) {
          assertFalse("Meal ${meal.name} should not contain gluten term '$term'", lowerText.contains(term))
        }
      }
    }
  }
}
