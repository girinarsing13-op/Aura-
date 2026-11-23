package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.aura.data.db.AppDatabase
import com.example.aura.data.db.DailyHabitEntity
import com.example.aura.data.db.ExerciseLogEntity
import com.example.aura.data.model.BlueprintExercise
import com.example.aura.data.model.BodyCheckIn
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.MovementPattern
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutSessionRecord
import com.example.aura.data.repository.AuraBackupManager
import com.example.aura.data.repository.AuraRepository
import com.example.aura.engine.AchievementEngine
import com.example.aura.engine.DiagnosticsEngine
import com.example.aura.engine.ExerciseDB
import com.example.aura.engine.ExerciseDemoMatcher
import com.example.aura.engine.ExerciseDemoRepository
import com.example.aura.engine.ExerciseGuideEngine
import com.example.aura.engine.FitnessCalcEngine
import com.example.aura.engine.ProgressAnalyticsEngine
import com.example.aura.engine.ProgressionAction
import com.example.aura.engine.ProgressionEngine
import com.example.aura.engine.RemoteExerciseEntry
import com.example.aura.engine.WarmupMobilityEngine
import com.example.aura.engine.WorkoutBlueprintEngine
import com.example.aura.engine.WorkoutScheduleEngine
import com.example.aura.notifications.WorkoutReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuraFeaturesTest {

    @Test
    fun `every exercise in ExerciseDB has a specific guide with breathing and 3 to 5 steps`() {
        assertTrue("ExerciseDB should have ~103 exercises", ExerciseDB.list.size >= 100)
        for (ex in ExerciseDB.list) {
            val guide = ExerciseGuideEngine.getGuide(
                name = ex.name,
                muscle = ex.muscle,
                equipment = ex.equipment.joinToString(" / "),
                detail = ex.detail,
                exerciseId = ex.id
            )
            assertFalse("Guide for ${ex.id} (${ex.name}) fell back to generic!", guide.isGenericFallback)
            assertTrue("Missing whatToDo for ${ex.id}", guide.whatToDo.isNotBlank())
            assertTrue("Missing mistakeToAvoid for ${ex.id}", guide.mistakeToAvoid.isNotBlank())
            assertTrue("Missing breathing for ${ex.id}", guide.breathing.isNotBlank())
            assertTrue(
                "Expected 3..5 steps for ${ex.id}, got ${guide.steps.size}",
                guide.steps.size in 3..5
            )
            assertTrue("Blank step found for ${ex.id}", guide.steps.all { it.isNotBlank() })
        }
    }

    @Test
    fun `exercise db contains difficulty tags and new calisthenics band bodyweight and dumbbell moves`() {
        val validLevels = setOf("beginner", "intermediate", "advanced")
        for (ex in ExerciseDB.list) {
            assertTrue(
                "Invalid level '${ex.level}' on ${ex.id}",
                ex.level.lowercase() in validLevels
            )
        }

        val expectedNewIds = listOf(
            "cal_fl1", "cal_fl2", "cal_fl3", "cal_ps1", "cal_ps2",
            "cal_ap1", "cal_pp1", "cal_ls1", "cal_ls2", "cal_hl1",
            "cal_hl2", "cal_mu1", "cal_mu2", "cal_hs1", "cal_nc1",
            "bnd_sh1", "bnd_sh2", "bnd_bk1", "bnd_ch1", "bnd_bi1", "bnd_tr1",
            "hm_gl1", "hm_gl2", "hm_gl3", "hm_gl4", "hm_qd1",
            "hm_cr1", "hm_cr2", "hm_cr3",
            "db_ch1", "db_ch2", "db_hm1", "db_qd1", "db_qd2",
            "db_bk1", "db_bk2", "db_tr1", "db_sh1", "db_sh2", "db_bi1"
        )
        val byId = ExerciseDB.list.associateBy { it.id }
        for (id in expectedNewIds) {
            assertNotNull("Expected new exercise $id missing from ExerciseDB", byId[id])
        }
    }

    @Test
    fun `every exercise and warmup drill maps to a valid movement pattern with caption`() {
        assertEquals(14, MovementPattern.entries.size)
        for (pattern in MovementPattern.entries) {
            assertTrue(pattern.displayName.isNotBlank())
            assertTrue(pattern.defaultCaption.isNotBlank())
        }

        for (ex in ExerciseDB.list) {
            assertNotNull("Missing movementPattern on ${ex.id}", ex.movementPattern)
        }
        val warmups = WarmupMobilityEngine.allWarmupExercises()
        assertTrue("Expected >= 25 warmup drills", warmups.size >= 25)
        for (wu in warmups) {
            assertNotNull("Missing movementPattern on warmup ${wu.id}", wu.movementPattern)
        }
    }

    @Test
    fun `automatic demo matcher normalizes synonyms, boosts equipment and muscles, and gates low confidence`() {
        val sampleCatalog = listOf(
            RemoteExerciseEntry(
                id = "Barbell_Bench_Press_-_Medium_Grip",
                name = "Barbell Bench Press - Medium Grip",
                images = listOf(
                    "Barbell_Bench_Press_-_Medium_Grip/0.jpg",
                    "Barbell_Bench_Press_-_Medium_Grip/1.jpg"
                ),
                equipment = "barbell",
                primaryMuscles = listOf("chest"),
                secondaryMuscles = listOf("triceps", "shoulders")
            ),
            RemoteExerciseEntry(
                id = "Pullups",
                name = "Pullups",
                images = listOf("Pullups/0.jpg", "Pullups/1.jpg"),
                equipment = "body only",
                primaryMuscles = listOf("lats")
            ),
            RemoteExerciseEntry(
                id = "Pushups",
                name = "Pushups",
                images = listOf("Pushups/0.jpg", "Pushups/1.jpg"),
                equipment = "body only",
                primaryMuscles = listOf("chest")
            ),
            RemoteExerciseEntry(
                id = "Butterfly",
                name = "Butterfly",
                images = listOf("Butterfly/0.jpg", "Butterfly/1.jpg"),
                equipment = "machine",
                primaryMuscles = listOf("chest")
            ),
            RemoteExerciseEntry(
                id = "Dumbbell_Bicep_Curl",
                name = "Dumbbell Bicep Curl",
                images = listOf("Dumbbell_Bicep_Curl/0.jpg", "Dumbbell_Bicep_Curl/1.jpg"),
                equipment = "dumbbell",
                primaryMuscles = listOf("biceps")
            )
        )

        val benchMatch = ExerciseDemoMatcher.matchSingle(
            auraId = "c1",
            auraName = "Barbell Bench Press",
            auraMuscle = "chest",
            auraEquipment = listOf("Barbell", "Bench"),
            catalog = sampleCatalog
        )
        assertNotNull(benchMatch)
        assertEquals("Barbell_Bench_Press_-_Medium_Grip", benchMatch!!.remoteId)
        assertEquals(
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/Barbell_Bench_Press_-_Medium_Grip/0.jpg",
            benchMatch.startImageUrl
        )
        assertEquals(
            "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/Barbell_Bench_Press_-_Medium_Grip/1.jpg",
            benchMatch.endImageUrl
        )
        assertTrue(benchMatch.confidence >= ExerciseDemoMatcher.CONFIDENCE_THRESHOLD)
        assertTrue(benchMatch.equipmentMatched)
        assertTrue(benchMatch.muscleMatched)

        val pullupMatch = ExerciseDemoMatcher.matchSingle(
            auraId = "req_b2",
            auraName = "Pull-Up",
            auraMuscle = "back",
            auraEquipment = listOf("Pull-up Bar", "Bodyweight"),
            catalog = sampleCatalog
        )
        assertNotNull(pullupMatch)
        assertEquals("Pullups", pullupMatch!!.remoteId)

        val pecDeckMatch = ExerciseDemoMatcher.matchSingle(
            auraId = "req_c3",
            auraName = "Machine Pec Deck",
            auraMuscle = "chest",
            auraEquipment = listOf("Machine", "Pec Deck"),
            catalog = sampleCatalog
        )
        assertNotNull(pecDeckMatch)
        assertEquals("Butterfly", pecDeckMatch!!.remoteId)

        // Unmatched niche move falls back cleanly to null (which triggers AnimatedExerciseFigure)
        val noMatch = ExerciseDemoMatcher.matchSingle(
            auraId = "cal_fl3",
            auraName = "Straddle Front Lever Hold",
            auraMuscle = "back",
            auraEquipment = listOf("Pull-up Bar"),
            catalog = sampleCatalog
        )
        assertNull(noMatch)

        // Verify JSON parser + coverage report contract
        val parsed = ExerciseDemoRepository.parseRemoteCatalogJson(
            """
            [
              {
                "id": "Pushups",
                "name": "Pushups",
                "equipment": "body only",
                "primaryMuscles": ["chest"],
                "images": ["Pushups/0.jpg", "Pushups/1.jpg"]
              }
            ]
            """.trimIndent()
        )
        assertEquals(1, parsed.size)
        assertEquals("Pushups", parsed.first().id)

        val coverage = ExerciseDemoRepository.buildCoverageReport()
        assertEquals(
            ExerciseDB.list.size + WarmupMobilityEngine.allWarmupExercises().size,
            coverage.size
        )
    }

    @Test
    fun `swap alternatives honor muscle group equipment injuries and exclusions`() {
        val homeProfile = UserProfile(
            name = "Test",
            environment = "Home Workout",
            homeEquipment = listOf("Dumbbells", "Resistance Bands"),
            injuries = listOf("Shoulder"),
            experience = "Beginner"
        )
        val target = WorkoutBlueprintEngine.createBlueprintExercise(
            ex = ExerciseDB.list.first { it.id == "db_ch1" },
            profile = homeProfile
        )
        val alts = WorkoutBlueprintEngine.findSwapAlternatives(
            targetExercise = target,
            excludedIdsOrNames = setOf("c3"),
            profile = homeProfile,
            limit = 3
        )
        assertTrue("Expected up to 3 alternatives, got ${alts.size}", alts.size in 1..3)
        assertTrue("All alternatives must hit chest", alts.all { it.muscle == "chest" })
        assertTrue("Excluded exercise c3 must not appear", alts.none { it.id == "c3" || it.id == "db_ch1" })
        val equipPool = WorkoutBlueprintEngine.buildEquipmentPool(homeProfile)
        assertTrue(
            "All alternatives must match home equipment and injury constraints",
            alts.all { WorkoutBlueprintEngine.isExerciseAllowedForEnvironment(it, homeProfile.environment, equipPool, homeProfile.injuries) }
        )
    }

    @Test
    fun `timed hold exercises preserve sec duration and parse countdown targets`() {
        val plank = ExerciseDB.list.first { it.id == "cr2" }
        val wallSit = ExerciseDB.list.first { it.id == "hm_qd1" }
        val bench = ExerciseDB.list.first { it.id == "c1" }

        assertTrue(plank.isTimedHold())
        assertTrue(wallSit.isTimedHold())
        assertFalse(bench.isTimedHold())

        val profile = UserProfile(name = "Timed", experience = "Beginner", environment = "Calisthenics")
        val plankBp = WorkoutBlueprintEngine.createBlueprintExercise(plank, profile)
        assertTrue(plankBp.isTimedHold())
        assertTrue("Expected sec prescription preserved, got ${plankBp.reps}", plankBp.reps.contains("sec", ignoreCase = true))
        assertEquals(45, plankBp.targetHoldSeconds())

        val wallSitBp = WorkoutBlueprintEngine.createBlueprintExercise(wallSit, profile)
        assertTrue(wallSitBp.isTimedHold())
        assertEquals(45, wallSitBp.targetHoldSeconds())
    }

    @Test
    fun `water target calculates 35 ml per kg plus 0_5 L on workout days rounded to 0_25 L`() {
        // 70 kg rest day: 70 * 0.035 = 2.45 L -> rounds to 2.50 L (2500 ml)
        assertEquals(2.5, FitnessCalcEngine.calcWaterTargetLiters(70.0, isWorkoutDay = false), 0.001)
        assertEquals(2500, FitnessCalcEngine.calcWaterTargetMl(70.0, isWorkoutDay = false))

        // 70 kg workout day: 2.45 + 0.50 = 2.95 L -> rounds to 3.00 L (3000 ml)
        assertEquals(3.0, FitnessCalcEngine.calcWaterTargetLiters(70.0, isWorkoutDay = true), 0.001)
        assertEquals(3000, FitnessCalcEngine.calcWaterTargetMl(70.0, isWorkoutDay = true))

        // 82 kg rest day: 2.87 -> 2.75 L; workout day: 3.37 -> 3.25 L
        assertEquals(2.75, FitnessCalcEngine.calcWaterTargetLiters(82.0, isWorkoutDay = false), 0.001)
        assertEquals(3.25, FitnessCalcEngine.calcWaterTargetLiters(82.0, isWorkoutDay = true), 0.001)
    }

    @Test
    fun `protein and calorie safety never drop below BMR and cap deficit at 20 percent`() {
        assertEquals(2.0, FitnessCalcEngine.proteinPerKgForGoal("Muscle Gain"), 0.001)
        assertEquals(2.2, FitnessCalcEngine.proteinPerKgForGoal("Fat Loss"), 0.001)
        assertEquals(1.6, FitnessCalcEngine.proteinPerKgForGoal("General Fitness"), 0.001)

        // Small user where TDEE * 0.80 < BMR -> must stay at BMR
        val lowTdeeSafety = FitnessCalcEngine.calcSafeCalories(
            tdee = 1600,
            goal = "Fat Loss",
            bmr = 1400,
            weightKg = 55.0,
            targetWeightKg = 48.0,
            durationMonths = 1
        )
        assertTrue("Target calories (${lowTdeeSafety.targetCals}) must never be below BMR (1400)", lowTdeeSafety.targetCals >= 1400)
        assertTrue(lowTdeeSafety.hitBmrFloor)

        // Higher TDEE where aggressive target tries to exceed 20% deficit -> capped at 80% TDEE
        val cappedSafety = FitnessCalcEngine.calcSafeCalories(
            tdee = 2800,
            goal = "Fat Loss",
            bmr = 1750,
            weightKg = 85.0,
            targetWeightKg = 70.0,
            durationMonths = 1
        )
        assertEquals((2800 * 0.80).toInt(), cappedSafety.targetCals)
        assertTrue(cappedSafety.hitDeficitCap)
        assertNotNull(cappedSafety.note)

        // Pace warning triggered above 0.75% BW/week
        val aggressiveCutPace = FitnessCalcEngine.calcPaceAssessment(
            currentW = 80.0,
            targetW = 70.0,
            durationMonths = 2,
            goal = "Fat Loss"
        )
        assertTrue(aggressiveCutPace.isWarning)

        val safeCutPace = FitnessCalcEngine.calcPaceAssessment(
            currentW = 80.0,
            targetW = 76.0,
            durationMonths = 3,
            goal = "Fat Loss"
        )
        assertFalse(safeCutPace.isWarning)

        val profile = UserProfile(name = "Alex", weight = 75.0, height = 178.0, age = 27, goal = "Fat Loss", daysPerWeek = 4)
        val why = FitnessCalcEngine.buildWhyTheseNumbersSummary(
            profile = profile,
            bmr = 1720,
            tdee = 2365,
            targetCals = 1892,
            proteinG = 165,
            waterLiters = 3.25,
            isWorkoutDay = true
        )
        assertTrue(why.contains("1720"))
        assertTrue(why.contains("165g"))
        assertTrue(why.contains("35 ml/kg"))
    }

    @Test
    fun `equipment filtering never prescribes machines or stations the user did not select`() {
        // Case 1: Home workout with ONLY Resistance Bands (no Dumbbells, no Barbell, no Pull-up Bar)
        val bandsOnlyProfile = UserProfile(
            name = "BandsOnly",
            environment = "Home Workout",
            homeEquipment = listOf("Resistance Bands"),
            daysPerWeek = 3,
            trainingSplit = "Full Body",
            goal = "General Fitness"
        )
        val bandsBlueprint = WorkoutBlueprintEngine.generate(bandsOnlyProfile)
        val bandsPool = WorkoutBlueprintEngine.buildEquipmentPool(bandsOnlyProfile)
        val byName = ExerciseDB.list.associateBy { it.name }
        for (day in bandsBlueprint.week.filter { !it.isRest }) {
            assertTrue("Expected non-empty exercises on ${day.dayTitle}", day.exercises.isNotEmpty())
            for (bpEx in day.exercises) {
                val dbEx = byName[bpEx.name]
                if (dbEx != null) {
                    assertTrue(
                        "Exercise '${dbEx.name}' (${dbEx.equipment}) should NOT be prescribed for BandsOnly",
                        WorkoutBlueprintEngine.isExerciseAllowedForEnvironment(dbEx, bandsOnlyProfile.environment, bandsPool, emptyList())
                    )
                    assertFalse(
                        "Cable/Machine/Pull-up Bar must not appear for BandsOnly: ${dbEx.name}",
                        dbEx.equipment.any { it.contains("Cable", true) || it.contains("Smith", true) || it.contains("Pull-up Bar", true) || it.contains("Dumbbell", true) }
                    )
                }
            }
        }

        // Case 2: Gym user who only checked Barbell + Dumbbells (no Cable, Smith, Leg Press, Machines)
        val minimalGymProfile = UserProfile(
            name = "FreeWeightsGym",
            environment = "Full Gym",
            gymEquipment = listOf("Barbell", "Dumbbells"),
            daysPerWeek = 4,
            trainingSplit = "Push / Pull / Legs",
            goal = "Muscle Gain"
        )
        val gymBlueprint = WorkoutBlueprintEngine.generate(minimalGymProfile)
        val gymPool = WorkoutBlueprintEngine.buildEquipmentPool(minimalGymProfile)
        for (day in gymBlueprint.week.filter { !it.isRest }) {
            for (bpEx in day.exercises) {
                val dbEx = byName[bpEx.name]
                if (dbEx != null) {
                    assertTrue(
                        "Exercise '${dbEx.name}' violates free-weights-only gym filter",
                        WorkoutBlueprintEngine.isExerciseAllowedForEnvironment(dbEx, minimalGymProfile.environment, gymPool, emptyList())
                    )
                }
            }
        }

        // Case 3: Calisthenics with empty equipment -> must exclude Pull-up Bar and Dip Station moves
        val floorCalProfile = UserProfile(
            name = "FloorCal",
            environment = "Calisthenics",
            calisthenicsEquipment = emptyList(),
            daysPerWeek = 3,
            trainingSplit = "Full Body",
            goal = "Recomposition"
        )
        val floorPool = WorkoutBlueprintEngine.buildEquipmentPool(floorCalProfile)
        val pullUp = ExerciseDB.list.first { it.id == "b2" } // Pull-up
        val pushUp = ExerciseDB.list.first { it.id == "c3" } // Push-up
        assertFalse(WorkoutBlueprintEngine.isExerciseAllowedForEnvironment(pullUp, floorCalProfile.environment, floorPool, emptyList()))
        assertTrue(WorkoutBlueprintEngine.isExerciseAllowedForEnvironment(pushUp, floorCalProfile.environment, floorPool, emptyList()))
    }

    @Test
    fun `workout schedule suggests free slot outside work hours and aligns preferred days and reminder work`() {
        // Work 9:00 AM - 6:00 PM (540..1080), wake 7:00 AM (420), sleep 11:00 PM (1380), 45 min workout
        val suggestion = WorkoutScheduleEngine.suggestBestWorkoutTime(
            wakeTimeMinutes = 420,
            hasWorkOrSchoolHours = true,
            workStartMinutes = 540,
            workEndMinutes = 1080,
            sleepTimeMinutes = 1380,
            sessionDurationMinutes = 45
        )
        assertTrue("Suggested time should be after work or before work", suggestion.suggestedMinutes >= 1095 || suggestion.suggestedMinutes + 45 <= 525)
        assertTrue(suggestion.freeSlotsMinutes.isNotEmpty())

        // Preferred days alignment in Blueprint
        val scheduledProfile = UserProfile(
            name = "Sched",
            daysPerWeek = 3,
            preferredWorkoutDays = listOf(1, 3, 5), // Mon, Wed, Fri
            preferredWorkoutMinutes = suggestion.suggestedMinutes,
            startDate = "2026-09-28", // Monday
            environment = "Full Gym"
        )
        val blueprint = WorkoutBlueprintEngine.generate(scheduledProfile)
        assertEquals(3, blueprint.week.count { !it.isRest })
        assertFalse(blueprint.week[0].isRest) // Mon
        assertTrue(blueprint.week[1].isRest)  // Tue
        assertFalse(blueprint.week[2].isRest) // Wed
        assertTrue(blueprint.week[3].isRest)  // Thu
        assertFalse(blueprint.week[4].isRest) // Fri
        assertTrue(blueprint.week[5].isRest)  // Sat
        assertTrue(blueprint.week[6].isRest)  // Sun

        // WorkManager daily reminder queueing & cancellation
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        val workManager = WorkManager.getInstance(context)

        val enabledProfile = scheduledProfile.copy(onboardingCompleted = true, workoutReminderEnabled = true)
        WorkoutReminderScheduler.syncReminderSchedule(context, enabledProfile)
        val infos = workManager.getWorkInfosForUniqueWork(WorkoutReminderScheduler.UNIQUE_WORK_NAME).get()
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.first().state)

        val disabledProfile = enabledProfile.copy(workoutReminderEnabled = false)
        WorkoutReminderScheduler.syncReminderSchedule(context, disabledProfile)
        val afterCancel = workManager.getWorkInfosForUniqueWork(WorkoutReminderScheduler.UNIQUE_WORK_NAME).get()
        assertTrue(afterCancel.isEmpty() || afterCancel.all { it.state == WorkInfo.State.CANCELLED })
    }

    @Test
    fun `all 26 diagnostics engine checks continue to pass`() {
        val items = DiagnosticsEngine.runAll()
        assertEquals(26, items.size)
        val failures = items.filter { !it.passed }
        assertTrue(
            "Diagnostics failures: ${failures.joinToString { "${it.name}: ${it.detail}" }}",
            failures.isEmpty()
        )
    }

    @Test
    fun `double progression increases barbell dumbbell and bodyweight targets and reduces on 2 missed sessions`() {
        val barbellBench = BlueprintExercise(
            id = "c1",
            name = "Barbell Bench Press",
            muscle = "chest",
            detail = "Compound",
            type = "Compound",
            equipment = "Barbell",
            isBodyweight = false,
            sets = 3,
            reps = "8-12",
            restSeconds = 90,
            recommendedLoadKg = 60.0,
            prescribedSets = emptyList(),
            isPriority = false
        )
        // Hit 12 reps on all 3 sets -> +2.5 kg next time with plain note
        val hitTopLogs = listOf(1, 2, 3).map { s ->
            ExerciseLogEntity(
                sessionId = 1,
                date = "2026-10-01",
                exerciseId = "c1",
                exerciseName = "Barbell Bench Press",
                equipment = "Barbell",
                variation = "Compound",
                setNumber = s,
                weightKg = 60.0,
                reps = 12,
                completed = true
            )
        }
        val upPlan = ProgressionEngine.buildExerciseCoaching(barbellBench, hitTopLogs, todayDateKey = "2026-10-05")
        assertEquals(ProgressionAction.INCREASE_WEIGHT, upPlan.action)
        assertEquals(62.5, upPlan.prefilledSets.first().prescribedWeightKg, 0.01)
        assertEquals(8, upPlan.prefilledSets.first().loggedReps)
        assertTrue(upPlan.shortNote.contains("Up 2.5 kg"))

        // Dumbbell progression (+1 to +2 kg)
        val dbCurl = barbellBench.copy(
            id = "a1",
            name = "Dumbbell Bicep Curl",
            muscle = "biceps",
            equipment = "Dumbbell",
            recommendedLoadKg = 10.0
        )
        val dbHitLogs = listOf(1, 2, 3).map { s ->
            ExerciseLogEntity(
                sessionId = 2,
                date = "2026-10-01",
                exerciseId = "a1",
                exerciseName = "Dumbbell Bicep Curl",
                equipment = "Dumbbell",
                variation = "Isolation",
                setNumber = s,
                weightKg = 10.0,
                reps = 12,
                completed = true
            )
        }
        val dbUpPlan = ProgressionEngine.buildExerciseCoaching(dbCurl, dbHitLogs, todayDateKey = "2026-10-05")
        assertEquals(ProgressionAction.INCREASE_WEIGHT, dbUpPlan.action)
        assertEquals(11.0, dbUpPlan.prefilledSets.first().prescribedWeightKg, 0.01)

        // Miss lower rep range (<8) two sessions in a row -> drop 2.5 kg with explanation
        val twoMissLogs = listOf(1, 2, 3).map { s ->
            ExerciseLogEntity(
                sessionId = 3,
                date = "2026-10-01",
                exerciseId = "c1",
                exerciseName = "Barbell Bench Press",
                equipment = "Barbell",
                variation = "Compound",
                setNumber = s,
                weightKg = 60.0,
                reps = 6,
                completed = true
            )
        } + listOf(1, 2, 3).map { s ->
            ExerciseLogEntity(
                sessionId = 4,
                date = "2026-10-04",
                exerciseId = "c1",
                exerciseName = "Barbell Bench Press",
                equipment = "Barbell",
                variation = "Compound",
                setNumber = s,
                weightKg = 60.0,
                reps = 7,
                completed = true
            )
        }
        val downPlan = ProgressionEngine.buildExerciseCoaching(barbellBench, twoMissLogs, todayDateKey = "2026-10-07")
        assertEquals(ProgressionAction.REDUCE_WEIGHT, downPlan.action)
        assertEquals(57.5, downPlan.prefilledSets.first().prescribedWeightKg, 0.01)
        assertTrue(downPlan.shortNote.contains("two sessions in a row"))

        // Bodyweight progression: reps first -> then harder variation (Diamond Push-Up -> Archer Push-Up)
        val pushup = BlueprintExercise(
            id = "t4",
            name = "Diamond Push-Up",
            muscle = "chest",
            detail = "Bodyweight",
            type = "Compound",
            equipment = "Bodyweight",
            isBodyweight = true,
            sets = 3,
            reps = "8-12",
            restSeconds = 60,
            recommendedLoadKg = 0.0,
            prescribedSets = emptyList(),
            isPriority = false
        )
        val masteredBwLogs = listOf(1, 2, 3).map { s ->
            ExerciseLogEntity(
                sessionId = 5,
                date = "2026-10-02",
                exerciseId = "t4",
                exerciseName = "Diamond Push-Up",
                equipment = "Bodyweight",
                variation = "Bodyweight",
                setNumber = s,
                weightKg = 0.0,
                reps = 15,
                completed = true
            )
        }
        val bwVariationPlan = ProgressionEngine.buildExerciseCoaching(pushup, masteredBwLogs, todayDateKey = "2026-10-07")
        assertEquals(ProgressionAction.SUGGEST_VARIATION, bwVariationPlan.action)
        assertEquals("Archer Push-Up", bwVariationPlan.suggestedVariation?.name)

        // Deload mode cuts sets by ~40%
        val deloadPlan = ProgressionEngine.buildExerciseCoaching(
            exercise = barbellBench.copy(sets = 5),
            allLogs = hitTopLogs,
            isDeloadActive = true,
            todayDateKey = "2026-10-07"
        )
        assertEquals(ProgressionAction.DELOAD, deloadPlan.action)
        assertEquals(3, deloadPlan.targetSets)
        assertEquals(3, deloadPlan.prefilledSets.size)
    }

    @Test
    fun `analytics builds weight trend PBs weekly summary 12 week heatmap smart streak and 15 achievements`() {
        val profile = UserProfile(
            name = "Taylor",
            weight = 76.0,
            targetWeight = 73.0,
            daysPerWeek = 3,
            preferredWorkoutDays = listOf(1, 3, 5), // Mon, Wed, Fri
            startDate = "2026-09-01"
        )

        val checkIns = listOf(
            BodyCheckIn(id = 1, dateKey = "2026-10-01", timestampMillis = 100L, weightKg = 77.5),
            BodyCheckIn(id = 2, dateKey = "2026-10-12", timestampMillis = 200L, weightKg = 76.8),
            BodyCheckIn(id = 3, dateKey = "2026-10-17", timestampMillis = 300L, weightKg = 76.0)
        )
        assertEquals(2, ProgressAnalyticsEngine.buildWeightTrend(checkIns, 7, todayKey = "2026-10-18").size)
        assertEquals(3, ProgressAnalyticsEngine.buildWeightTrend(checkIns, 30, todayKey = "2026-10-18").size)
        assertFalse(ProgressAnalyticsEngine.isWeighInDue(checkIns, todayKey = "2026-10-18"))
        assertTrue(ProgressAnalyticsEngine.isWeighInDue(checkIns, todayKey = "2026-10-25"))

        val logs = listOf(
            ExerciseLogEntity(1, 1, "2026-10-10", "c1", "Barbell Bench Press", "Barbell", "Flat", 1, 70.0, 8, true),
            ExerciseLogEntity(2, 2, "2026-10-17", "c1", "Barbell Bench Press", "Barbell", "Flat", 1, 75.0, 8, true)
        )
        assertTrue(ProgressAnalyticsEngine.isNewPersonalBestSet("Barbell Bench Press", 77.5, 8, logs, "2026-10-18"))
        val pbs = ProgressAnalyticsEngine.buildPersonalBests(logs)
        assertEquals(1, pbs.size)
        assertEquals(75.0, pbs.first().bestWeightKg, 0.01)
        assertEquals("2026-10-17", pbs.first().dateKey)

        // Smart streak with planned Mon/Wed/Fri (1,3,5) allowing 1 missed planned workout day
        // Suppose today is Sunday 2026-10-18 (rest), Fri 10-16 done, Wed 10-14 missed (1 rest grace!), Mon 10-12 done -> streak = 2
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sundayCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }
        val habitsMap = mapOf(
            "2026-10-16" to DailyHabitStatus("2026-10-16", workout = true, diet = true, water = true),
            "2026-10-12" to DailyHabitStatus("2026-10-12", workout = true, diet = true, water = true)
        )
        val smartStreak = ProgressAnalyticsEngine.calculateSmartStreak(profile, emptyList(), habitsMap, todayCal = sundayCal)
        assertEquals(2, smartStreak)

        val heatmap = ProgressAnalyticsEngine.build12WeekHeatmap(profile, emptyList(), habitsMap, todayCal = sundayCal)
        assertEquals(12, heatmap.size)
        assertTrue(heatmap.all { it.size == 7 })

        val summary = ProgressAnalyticsEngine.buildWeeklySummary(profile, null, emptyList(), habitsMap, logs, nowCal = sundayCal)
        assertTrue(summary.isSunday)
        assertEquals(2, summary.workoutsDone)
        assertEquals(3, summary.workoutsPlanned)

        val badges = AchievementEngine.evaluate(profile, emptyList(), habitsMap, logs, checkIns, emptyList())
        assertEquals(15, badges.size)
        assertTrue(badges.first { it.definition.id == "first_workout" }.isUnlocked)
        assertTrue(badges.first { it.definition.id == "first_pb" }.isUnlocked)
        assertTrue(badges.first { it.definition.id == "first_checkin" }.isUnlocked)
        assertFalse(sdf.format(sundayCal.time).isBlank())
    }

    @Test
    fun `room migration 8 to 9 preserves user profile and backup json round trips cleanly`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_test_${System.currentTimeMillis()}.db"

        // Create minimal v8 schema database with a row in user_profile
        val helper = object : SQLiteOpenHelper(context, dbName, null, 8) {
            override fun onCreate(db: SQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER NOT NULL PRIMARY KEY,
                        `name` TEXT NOT NULL, `age` INTEGER NOT NULL, `height` REAL NOT NULL, `weight` REAL NOT NULL,
                        `gender` TEXT NOT NULL, `goal` TEXT NOT NULL, `targetWeight` REAL, `durationMonths` INTEGER NOT NULL,
                        `experience` TEXT NOT NULL, `environment` TEXT NOT NULL, `homeEquipmentCsv` TEXT NOT NULL,
                        `gymEquipmentCsv` TEXT NOT NULL, `calisthenicsEquipmentCsv` TEXT NOT NULL,
                        `daysPerWeek` INTEGER NOT NULL, `trainingSplit` TEXT NOT NULL, `customSplitCsv` TEXT NOT NULL,
                        `injuriesCsv` TEXT NOT NULL, `focusBodyPartsCsv` TEXT NOT NULL, `dietPreference` TEXT NOT NULL,
                        `trackCycle` INTEGER NOT NULL, `periodWeekActive` INTEGER NOT NULL, `themeAccent` TEXT NOT NULL,
                        `themeMode` TEXT NOT NULL, `navTransparency` REAL NOT NULL, `navBlur` REAL NOT NULL,
                        `sessionDurationMinutes` INTEGER NOT NULL, `wakeTimeMinutes` INTEGER NOT NULL,
                        `hasWorkOrSchoolHours` INTEGER NOT NULL, `workStartMinutes` INTEGER NOT NULL, `workEndMinutes` INTEGER NOT NULL,
                        `sleepTimeMinutes` INTEGER NOT NULL, `preferredWorkoutMinutes` INTEGER NOT NULL,
                        `preferredWorkoutDaysCsv` TEXT NOT NULL, `workoutReminderEnabled` INTEGER NOT NULL,
                        `sleepHours` REAL NOT NULL, `sleepQuality` TEXT NOT NULL, `stressLevel` TEXT NOT NULL,
                        `lifestyleActivity` TEXT NOT NULL, `cardioPreference` TEXT NOT NULL, `postureFocus` TEXT NOT NULL,
                        `warmupPreference` TEXT NOT NULL, `budgetTier` TEXT NOT NULL, `mealsPerDay` INTEGER NOT NULL,
                        `waterLiters` REAL NOT NULL, `allergiesCsv` TEXT NOT NULL, `proteinSupplement` INTEGER NOT NULL,
                        `startDate` TEXT NOT NULL, `createdAt` TEXT NOT NULL, `onboardingCompleted` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE TABLE IF NOT EXISTS `blueprint_cache` (`id` INTEGER NOT NULL PRIMARY KEY, `splitName` TEXT NOT NULL, `derivedAt` TEXT NOT NULL, `workoutJson` TEXT NOT NULL, `nutritionJson` TEXT NOT NULL, `metricsJson` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `workout_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT NOT NULL, `dayNumber` INTEGER NOT NULL, `dayTitle` TEXT NOT NULL, `status` TEXT NOT NULL, `isFullyCompleted` INTEGER NOT NULL, `setsCompleted` INTEGER NOT NULL, `totalSets` INTEGER NOT NULL, `logsJson` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `daily_habits` (`dateKey` TEXT NOT NULL PRIMARY KEY, `workout` INTEGER NOT NULL, `diet` INTEGER NOT NULL, `water` INTEGER NOT NULL, `steps` INTEGER NOT NULL, `sleep` INTEGER NOT NULL, `waterMl` INTEGER NOT NULL, `loggedCalories` INTEGER NOT NULL, `loggedProteinG` INTEGER NOT NULL, `eatenMealsCsv` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `exercise_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `date` TEXT NOT NULL, `exerciseId` TEXT NOT NULL, `exerciseName` TEXT NOT NULL, `equipment` TEXT NOT NULL, `variation` TEXT NOT NULL, `setNumber` INTEGER NOT NULL, `weightKg` REAL NOT NULL, `reps` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `exercise_substitutions` (`originalExerciseId` TEXT NOT NULL PRIMARY KEY, `substitutedExerciseId` TEXT NOT NULL, `substitutedExerciseName` TEXT NOT NULL, `substitutedDate` TEXT NOT NULL)")
                db.execSQL(
                    """
                    INSERT INTO user_profile VALUES (
                        1, 'Sam', 29, 178.0, 75.0, 'Male', 'Muscle Gain', 80.0, 3, 'Intermediate', 'Full Gym',
                        'Dumbbells', 'Barbell,Dumbbells', 'Pull-up Bar,Dip Bars', 4, 'Upper / Lower – 4 Days', '',
                        'None', '', 'Balanced', 0, 0, '#CCFF00', 'dark', 0.85, 0.75, 60, 420, 1, 540, 1020, 1380,
                        1080, '1,2,4,5', 1, 7.5, 'Good', 'Moderate', 'Moderately Active', 'LISS Walking',
                        'Neutral Spine', 'Full Dynamic Mobility Primer', 'Moderate', 3, 3.0, 'None', 1,
                        '2026-10-01', '', 1
                    )
                    """.trimIndent()
                )
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        helper.writableDatabase.close()

        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_7_8, AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10)
            .allowMainThreadQueries()
            .build()

        try {
            val dao = db.auraDao()
            val migratedUser = dao.getUserProfileOnce()
            assertNotNull(migratedUser)
            assertEquals("Sam", migratedUser!!.name)
            assertTrue(migratedUser.weighInReminderEnabled)
            assertFalse(migratedUser.deloadActive)

            val repo = AuraRepository(dao)
            repo.saveSingleSetLog(
                dateKey = "2026-10-18",
                exerciseId = "c1",
                exerciseName = "Barbell Bench Press",
                equipment = "Barbell",
                variation = "Flat",
                setNumber = 1,
                weightKg = 80.0,
                reps = 10,
                completed = true
            )
            repo.saveBodyCheckIn(BodyCheckIn(dateKey = "2026-10-18", timestampMillis = 1000L, weightKg = 76.2, waistCm = 81.0))

            val json = AuraBackupManager.exportToJson(dao)
            repo.resetAllData()
            assertNull(dao.getUserProfileOnce())

            val restored = AuraBackupManager.importFromJson(dao, json)
            assertTrue(restored.isSuccess)
            assertEquals("Sam", dao.getUserProfileOnce()?.name)
            assertEquals(1, dao.getAllExerciseLogsOnce().size)
            assertEquals(1, dao.getAllBodyCheckInsOnce().size)
        } finally {
            db.close()
            context.deleteDatabase(dbName)
        }
    }
}


