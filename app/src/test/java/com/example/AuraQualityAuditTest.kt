package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.aura.data.db.ExerciseLogEntity
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.data.model.UserProfile
import com.example.aura.engine.DiagnosticsEngine
import com.example.aura.engine.ExerciseDB
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.engine.ProgressionAction
import com.example.aura.engine.ProgressionEngine
import com.example.aura.engine.WorkoutBlueprintEngine
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.notifications.BootCompletedReceiver
import com.example.aura.ui.AuraViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuraQualityAuditTest {

    @Test
    fun `plan generator passes 24+ profile combinations across goals environments days levels and injuries`() {
        val goals = listOf("Muscle Gain", "Fat Loss", "Recomposition", "General Fitness")
        val environments = listOf(
            "Full Gym" to listOf("Barbell", "Dumbbells", "Cable Machine", "Leg Press", "Lat Pulldown"),
            "Home Workout" to listOf("Dumbbells", "Resistance Bands", "Bench"),
            "Calisthenics" to listOf("Pull-up Bar", "Dip Bars")
        )
        val dayCounts = listOf(2, 3, 4, 5, 6)
        val levels = listOf("Beginner", "Advanced")
        val injurySets = listOf(listOf("None"), listOf("Lower Back"), listOf("Shoulder"), listOf("Knee"))

        val dbById = ExerciseDB.list.associateBy { it.id }
        val dbByName = ExerciseDB.list.associateBy { it.name.lowercase() }

        var combinationsTested = 0

        for ((goalIdx, goal) in goals.withIndex()) {
            for ((envIdx, envPair) in environments.withIndex()) {
                val (env, equip) = envPair
                for (days in dayCounts) {
                    val level = levels[(goalIdx + envIdx + days) % levels.size]
                    val injuries = injurySets[(goalIdx + envIdx * 2 + days) % injurySets.size]
                    val split = when (days) {
                        2 -> "Upper / Lower – 2 Days"
                        3 -> if (envIdx % 2 == 0) "Push / Pull / Legs (PPL) – 3 Days" else "Full Body – 3 Days"
                        4 -> "Upper / Lower – 4 Days"
                        5 -> "Upper / Lower / Push / Pull / Legs (ULPPL)"
                        else -> "Push / Pull / Legs (PPL)"
                    }
                    val profile = UserProfile(
                        name = "Audit_${goalIdx}_${envIdx}_$days",
                        age = 24 + (days * 3),
                        height = 172.0,
                        weight = 74.0,
                        gender = if (goalIdx % 2 == 0) "Male" else "Female",
                        goal = goal,
                        experience = level,
                        environment = env,
                        gymEquipment = if (env == "Full Gym") equip else emptyList(),
                        homeEquipment = if (env == "Home Workout") equip else emptyList(),
                        calisthenicsEquipment = if (env == "Calisthenics") equip else emptyList(),
                        daysPerWeek = days,
                        preferredWorkoutDays = WorkoutScheduleEngine.defaultWorkoutDays(days),
                        trainingSplit = split,
                        injuries = injuries,
                        startDate = "2026-09-28"
                    )

                    val bp = WorkoutBlueprintEngine.generate(profile)
                    val equipPool = WorkoutBlueprintEngine.buildEquipmentPool(profile)
                    val activeDays = bp.week.filter { !it.isRest }
                    val restDays = bp.week.filter { it.isRest }

                    assertEquals("Total week days should be 7 for $goal/$env/$days", 7, bp.week.size)
                    assertEquals("Active days mismatch for $goal/$env/$days", days, activeDays.size)
                    assertEquals("Rest days mismatch for $goal/$env/$days", 7 - days, restDays.size)

                    // For 2-3 days/week, rest days should be spaced so back-to-back training is avoided
                    if (days <= 3) {
                        val activeIndices = bp.week.mapIndexedNotNull { idx, d -> if (!d.isRest) idx else null }
                        for (i in 0 until activeIndices.lastIndex) {
                            assertTrue(
                                "Rest day spacing failed for $days days ($goal/$env): $activeIndices",
                                activeIndices[i + 1] - activeIndices[i] >= 2
                            )
                        }
                    }

                    for (day in activeDays) {
                        assertTrue("Non-empty exercises required on ${day.dayTitle} ($goal/$env/$days)", day.exercises.isNotEmpty())
                        for (ex in day.exercises) {
                            val dbEx = dbById[ex.id] ?: dbByName[ex.name.lowercase()]
                            if (dbEx != null) {
                                assertFalse(
                                    "Injury conflict (${injuries}) for ${dbEx.name} in $goal/$env/$days",
                                    WorkoutBlueprintEngine.hasInjuryConflict(dbEx.injuries, injuries, dbEx.name)
                                )
                                assertTrue(
                                    "Missing equipment violation for ${dbEx.name} (${dbEx.equipment}) in $env",
                                    WorkoutBlueprintEngine.isExerciseAllowedForEnvironment(dbEx, env, equipPool, injuries)
                                )
                            }
                            if (!ex.isBodyweight) {
                                assertTrue("Weight target must be >= 0 for ${ex.name}", ex.recommendedLoadKg >= 0.0)
                            }
                        }
                    }

                    val weeklyVolume = WorkoutBlueprintEngine.calculateWeeklyMuscleVolume(bp)
                    assertTrue("Weekly muscle volume must be non-empty for $goal/$env/$days", weeklyVolume.isNotEmpty())
                    val totalWeeklySets = weeklyVolume.values.sum()
                    assertTrue(
                        "Weekly volume ($totalWeeklySets sets) out of healthy range for $days days",
                        totalWeeklySets in (days * 6)..(days * 45)
                    )
                    weeklyVolume.forEach { (muscle, sets) ->
                        assertTrue("Excessive per-muscle sets ($muscle=$sets) for $goal/$env/$days", sets <= 30)
                    }

                    combinationsTested++
                }
            }
        }

        assertTrue("Expected at least 24 combinations, tested $combinationsTested", combinationsTested >= 24)
    }

    @Test
    fun `boundary schedules for 1 day and 7 days and zero equipment generate valid non-empty days`() {
        val envs = listOf("Full Gym", "Home Workout", "Calisthenics")
        for (env in envs) {
            for (days in listOf(1, 7)) {
                val profile = UserProfile(
                    name = "Edge_${env}_$days",
                    weight = 0.0,
                    height = 0.0,
                    age = 30,
                    environment = env,
                    gymEquipment = emptyList(),
                    homeEquipment = emptyList(),
                    calisthenicsEquipment = emptyList(),
                    daysPerWeek = days,
                    preferredWorkoutDays = WorkoutScheduleEngine.defaultWorkoutDays(days),
                    trainingSplit = "Full Body",
                    goal = "General Fitness",
                    injuries = listOf("Lower Back")
                )
                val bp = WorkoutBlueprintEngine.generate(profile)
                val active = bp.week.filter { !it.isRest }
                assertEquals("Active days should equal $days in $env with zero equipment", days, active.size)
                assertTrue(
                    "All active days must contain fallback exercises in $env ($days d/wk)",
                    active.all { it.exercises.isNotEmpty() }
                )
                assertTrue(
                    "Recommended loads must never be negative when weight is 0",
                    active.flatMap { it.exercises }.all { it.recommendedLoadKg >= 0.0 }
                )
            }
        }
    }

    @Test
    fun `fitness calc engine enforces safe calorie protein water clamps and safety advisories`() {
        // Zero / null safety
        assertEquals(0.0, FitnessCalcEngine.calcBMI(0.0, 175.0), 0.001)
        assertEquals(0.0, FitnessCalcEngine.calcBMI(70.0, 0.0), 0.001)
        assertEquals(0, FitnessCalcEngine.calcBMR(0.0, 175.0, 25, "Male"))
        assertEquals(0, FitnessCalcEngine.calcTDEE(0, 4))
        val zeroMacros = FitnessCalcEngine.calcMacroTargets(0, 0.0, "Fat Loss")
        assertEquals(0, zeroMacros.protein)
        assertEquals(0, zeroMacros.fat)
        assertEquals(0, zeroMacros.carbs)

        // Water clamps: very light (32kg -> min 1.5L) and very heavy (240kg -> max 5.0L)
        assertEquals(1.5, FitnessCalcEngine.calcWaterTargetLiters(32.0, isWorkoutDay = false), 0.001)
        assertEquals(5.0, FitnessCalcEngine.calcWaterTargetLiters(240.0, isWorkoutDay = true), 0.001)

        // Older user (age 78) + very light (40kg) fat loss safety floor
        val lightBmr = FitnessCalcEngine.calcBMR(40.0, 152.0, 78, "Female")
        val lightTdee = FitnessCalcEngine.calcTDEE(lightBmr, 3)
        val lightSafety = FitnessCalcEngine.calcSafeCalories(lightTdee, "Fat Loss", lightBmr, 40.0, 38.0, 2)
        assertTrue("Light/older user calories must stay >= BMR and >= 1000", lightSafety.targetCals >= maxOf(lightBmr, 1000))

        // Very heavy user (240kg) upper ceiling
        val heavyBmr = FitnessCalcEngine.calcBMR(240.0, 190.0, 35, "Male")
        val heavyTdee = FitnessCalcEngine.calcTDEE(heavyBmr, 5)
        val heavyBulk = FitnessCalcEngine.calcSafeCalories(heavyTdee, "Muscle Gain", heavyBmr, 240.0, 250.0, 6)
        assertTrue("Heavy user bulk calories should be clamped <= 5000", heavyBulk.targetCals in 1200..5000)

        // Macro sanity (no negative values)
        val extremeMacros = FitnessCalcEngine.calcMacroTargets(lightSafety.targetCals, 40.0, "Fat Loss")
        assertTrue(extremeMacros.protein >= 40)
        assertTrue(extremeMacros.fat >= 30)
        assertTrue(extremeMacros.carbs >= 0)

        // Safety advisories: Minor (<18)
        val minorProfile = UserProfile(name = "Teen", age = 16, height = 168.0, weight = 60.0, goal = "General Fitness")
        val minorAdvisory = FitnessCalcEngine.evaluateSafetyAdvisory(minorProfile)
        assertTrue(minorAdvisory.requiresMinorSupervision)
        assertTrue(minorAdvisory.messages.any { it.contains("Under-18") })

        // Safety advisories: Underweight target BMI < 18.5 in Fat Loss
        val underweightProfile = UserProfile(
            name = "LowBmi",
            age = 24,
            height = 175.0,
            weight = 62.0,
            targetWeight = 52.0, // BMI = 17.0
            durationMonths = 3,
            goal = "Fat Loss"
        )
        val underweightAdvisory = FitnessCalcEngine.evaluateSafetyAdvisory(underweightProfile)
        assertTrue(underweightAdvisory.hasExtremeGoalWarning)
        assertTrue(underweightAdvisory.messages.any { it.contains("18.5") })

        // Safety advisories: High target BMI >= 38.0 in Muscle Gain
        val highBmiBulkProfile = UserProfile(
            name = "HighBulk",
            age = 28,
            height = 170.0,
            weight = 105.0,
            targetWeight = 115.0, // BMI = 39.8
            durationMonths = 3,
            goal = "Muscle Gain"
        )
        val highBulkAdvisory = FitnessCalcEngine.evaluateSafetyAdvisory(highBmiBulkProfile)
        assertTrue(highBulkAdvisory.hasExtremeGoalWarning)
    }

    @Test
    fun `all 26 diagnostics checks pass cleanly`() {
        val items = DiagnosticsEngine.runAll()
        assertEquals(26, items.size)
        val failed = items.filter { !it.passed }
        assertTrue("Failed diagnostics: ${failed.joinToString { "${it.name}: ${it.detail}" }}", failed.isEmpty())
    }

    @Test
    fun `progression engine handles zero load safety and double progression variants`() {
        val bench = BlueprintExercise(
            id = "c1",
            name = "Barbell Bench Press",
            muscle = "chest",
            detail = "Compound",
            type = "Compound",
            equipment = "Barbell",
            isBodyweight = false,
            sets = 4,
            reps = "8-12",
            restSeconds = 90,
            recommendedLoadKg = 0.0,
            prescribedSets = emptyList(),
            isPriority = false
        )
        // Baseline with 0 kg
        val baseline = ProgressionEngine.buildExerciseCoaching(bench, emptyList(), todayDateKey = "2026-10-10")
        assertEquals(ProgressionAction.BASELINE, baseline.action)
        assertEquals(4, baseline.prefilledSets.size)
        assertEquals(0.0, baseline.prefilledSets.first().prescribedWeightKg, 0.001)

        // Hit top reps at 0 kg weighted -> gracefully increases reps instead of crashing
        val zeroLogs = (1..4).map { s ->
            ExerciseLogEntity(1, 1, "2026-10-05", "c1", "Barbell Bench Press", "Barbell", "Flat", s, 0.0, 12, true)
        }
        val zeroPlan = ProgressionEngine.buildExerciseCoaching(bench, zeroLogs, todayDateKey = "2026-10-10")
        assertTrue(zeroPlan.prefilledSets.all { (it.loggedReps ?: 0) >= 12 })

        // Deload reduces 4 sets by 40% -> max(1, round(2.4)) = 2
        val deloadPlan = ProgressionEngine.buildExerciseCoaching(
            exercise = bench.copy(recommendedLoadKg = 80.0),
            allLogs = emptyList(),
            isDeloadActive = true,
            todayDateKey = "2026-10-10"
        )
        assertEquals(ProgressionAction.DELOAD, deloadPlan.action)
        assertEquals(2, deloadPlan.targetSets)
    }

    @Test
    fun `boot completed receiver and viewmodel persisted settings behave safely`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)

        val receiver = BootCompletedReceiver()
        // Unrelated action is ignored cleanly
        receiver.onReceive(context, Intent("com.example.UNRELATED"))
        // Boot completed and package replaced do not throw
        receiver.onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))
        receiver.onReceive(context, Intent(Intent.ACTION_MY_PACKAGE_REPLACED))

        // AuraViewModel persisted settings round-trip via SharedPreferences
        context.getSharedPreferences("aura_app_settings", Context.MODE_PRIVATE).edit().clear().commit()
        val vm1 = AuraViewModel(context)
        assertTrue(vm1.hapticsEnabled.value)
        assertTrue(vm1.soundEnabled.value)
        assertTrue(vm1.keepScreenOn.value)
        assertFalse(vm1.disclaimerAcknowledged.value)
        assertFalse(vm1.parqCompleted.value)
        assertFalse(vm1.parqFlagged.value)

        vm1.setHapticsEnabled(false)
        vm1.setSoundEnabled(false)
        vm1.setKeepScreenOn(false)
        vm1.acknowledgeDisclaimer()
        vm1.saveParqResult(hasRiskFlags = true)

        val vm2 = AuraViewModel(context)
        assertFalse(vm2.hapticsEnabled.value)
        assertFalse(vm2.soundEnabled.value)
        assertFalse(vm2.keepScreenOn.value)
        assertTrue(vm2.disclaimerAcknowledged.value)
        assertTrue(vm2.parqCompleted.value)
        assertTrue(vm2.parqFlagged.value)

        // Not null assertion sanity on disclaimer text constant
        assertNotNull(context.getString(R.string.app_name))
    }
}
