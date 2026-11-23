package com.example.aura.engine

import com.example.aura.data.model.MovementPattern
import com.example.aura.data.model.WorkoutDayPlan

data class WarmupExercise(
    val id: String,
    val name: String,
    val targetJoints: String,
    val durationOrReps: String,
    val cue: String,
    val category: String, // "Joint Mobility", "Dynamic Stretch", "Neural Activation", "Decompression"
    val iconEmoji: String,
    val defaultTimerSeconds: Int = 45,
    val movementPattern: MovementPattern = MovementPattern.CARDIO_JUMP
)

data class WarmupProtocol(
    val title: String,
    val subtitle: String,
    val estimatedMinutes: Int,
    val targetMusclesLabel: String,
    val protocolBadge: String,
    val exercises: List<WarmupExercise>
)

object WarmupMobilityEngine {
    fun allWarmupExercises(): List<WarmupExercise> = (
        listOf(
            getChestPushProtocol(),
            getBackPullProtocol(),
            getLowerBodyProtocol(),
            getShouldersArmsProtocol(),
            getFullBodyProtocol(),
            getRestRecoveryProtocol()
        ).flatMap { it.exercises } +
            getThoracicExtensionDrills() +
            getAnteriorPelvicTiltDrills() +
            getShoulderRetractionDrills()
        ).distinctBy { it.id }

    fun getWarmupProtocol(dayPlan: WorkoutDayPlan): WarmupProtocol {
        return getWarmupProtocol(
            dayPlan = dayPlan,
            warmupPreference = "Full Dynamic Mobility Primer",
            postureFocus = "Neutral Spine"
        )
    }

    fun getWarmupProtocol(
        dayPlan: WorkoutDayPlan,
        warmupPreference: String,
        postureFocus: String = "Neutral Spine"
    ): WarmupProtocol {
        val prefLower = warmupPreference.trim().lowercase()
        val isSkip = prefLower.contains("skip") || prefLower == "none" || prefLower.contains("no warm")
        val isShort = prefLower.contains("quick") || prefLower.contains("short") || prefLower.contains("brief") || prefLower.contains("2-min")

        if (isSkip && !dayPlan.isRest) {
            return WarmupProtocol(
                title = "Warm-up Skipped",
                subtitle = "Skipped per your warm-up preference",
                estimatedMinutes = 0,
                targetMusclesLabel = "Jump straight into light ramp-up sets",
                protocolBadge = "SKIPPED",
                exercises = emptyList()
            )
        }

        val baseProtocol = if (dayPlan.isRest) {
            getRestRecoveryProtocol()
        } else {
            val focusLower = dayPlan.focus.lowercase()
            val muscles = dayPlan.exercises.map { it.muscle.lowercase() }.toSet()

            val hasChest = muscles.contains("chest") || focusLower.contains("chest") || focusLower.contains("push")
            val hasBack = muscles.contains("back") || muscles.contains("lats") || focusLower.contains("back") || focusLower.contains("pull")
            val hasLegs = muscles.any { it.contains("quad") || it.contains("hamstring") || it.contains("glute") || it.contains("calv") } ||
                focusLower.contains("leg") || focusLower.contains("lower") || focusLower.contains("squat")
            val hasShoulders = muscles.contains("shoulders") || focusLower.contains("shoulder") || focusLower.contains("deltoid")
            val hasArms = (muscles.contains("biceps") || muscles.contains("triceps")) && !hasChest && !hasBack

            when {
                (hasChest && hasLegs) || (hasBack && hasLegs) || focusLower.contains("full body") -> getFullBodyProtocol()
                hasLegs -> getLowerBodyProtocol()
                hasChest -> getChestPushProtocol()
                hasBack -> getBackPullProtocol()
                hasShoulders || hasArms || focusLower.contains("arm") -> getShouldersArmsProtocol()
                else -> getFullBodyProtocol()
            }
        }

        val trimmedBase = if (isShort) {
            baseProtocol.copy(
                title = "Quick ${baseProtocol.title}",
                subtitle = "Condensed 2-minute primer for busy sessions",
                estimatedMinutes = 2,
                protocolBadge = "QUICK PRIMER",
                exercises = baseProtocol.exercises.take(2)
            )
        } else {
            baseProtocol
        }

        val correctiveDrills = getPostureCorrectiveDrills(postureFocus)
        if (correctiveDrills.isEmpty()) {
            return trimmedBase
        }

        return trimmedBase.copy(
            subtitle = "${trimmedBase.subtitle} • +${correctiveDrills.size} ${postureFocus} drills",
            estimatedMinutes = trimmedBase.estimatedMinutes + 2,
            exercises = (trimmedBase.exercises + correctiveDrills).distinctBy { it.id }
        )
    }

    fun getPostureCorrectiveDrills(postureFocus: String): List<WarmupExercise> {
        val p = postureFocus.trim().lowercase()
        if (p.isBlank() || p.contains("neutral")) return emptyList()
        return when {
            p.contains("thoracic") || p.contains("kyphosis") || p.contains("hunch") -> getThoracicExtensionDrills()
            p.contains("pelvic") || p.contains("apt") || p.contains("lordosis") || p.contains("hip") -> getAnteriorPelvicTiltDrills()
            p.contains("shoulder") || p.contains("retraction") || p.contains("rounded") || p.contains("neck") -> getShoulderRetractionDrills()
            else -> emptyList()
        }
    }

    private fun getThoracicExtensionDrills(): List<WarmupExercise> = listOf(
        WarmupExercise(
            id = "post_tx_1",
            name = "Bench Thoracic Extension Opener",
            targetJoints = "Mid-Back & Thoracic Spine",
            durationOrReps = "8 slow reps – 3s hold",
            cue = "Rest elbows on a bench, sink chest gently toward the floor while breathing out to open upper back.",
            category = "Posture Corrective",
            iconEmoji = "🧘",
            defaultTimerSeconds = 45,
            movementPattern = MovementPattern.VERTICAL_PULL
        ),
        WarmupExercise(
            id = "post_tx_2",
            name = "Side-Lying Open Book T-Spine Twist",
            targetJoints = "Thoracic Rotation & Ribcage",
            durationOrReps = "8 reps per side",
            cue = "Keep stacked knees pinned to the floor as your top arm opens wide across your chest.",
            category = "Posture Corrective",
            iconEmoji = "📖",
            defaultTimerSeconds = 45,
            movementPattern = MovementPattern.CRUNCH_LEG_RAISE
        ),
        WarmupExercise(
            id = "post_tx_3",
            name = "Prone Y-T-W Thoracic Lift",
            targetJoints = "Lower Traps & Thoracic Extensors",
            durationOrReps = "10 controlled reps",
            cue = "Lie face down, thumbs up, lift arms slightly off the floor while keeping neck relaxed.",
            category = "Posture Corrective",
            iconEmoji = "✨",
            defaultTimerSeconds = 40,
            movementPattern = MovementPattern.RAISE
        )
    )

    private fun getAnteriorPelvicTiltDrills(): List<WarmupExercise> = listOf(
        WarmupExercise(
            id = "post_apt_1",
            name = "Dead Bug Posterior Pelvic Tilt",
            targetJoints = "Deep Core & Lumbar Pelvis",
            durationOrReps = "10 slow alternating reps",
            cue = "Press lower back flat into the floor before extending opposite arm and leg.",
            category = "Posture Corrective",
            iconEmoji = "🪲",
            defaultTimerSeconds = 45,
            movementPattern = MovementPattern.CRUNCH_LEG_RAISE
        ),
        WarmupExercise(
            id = "post_apt_2",
            name = "Half-Kneeling Hip Flexor Stretch",
            targetJoints = "Psoas & Rectus Femoris",
            durationOrReps = "35 sec per side",
            cue = "Tuck tailbone under and squeeze back glute tightly before shifting forward.",
            category = "Posture Corrective",
            iconEmoji = "🦵",
            defaultTimerSeconds = 45,
            movementPattern = MovementPattern.LUNGE_SPLIT_SQUAT
        ),
        WarmupExercise(
            id = "post_apt_3",
            name = "Posterior-Tilt Glute Bridge Hold",
            targetJoints = "Glutes & Hamstrings",
            durationOrReps = "12 reps – 2s squeeze",
            cue = "Tuck pelvis first, drive through heels, and stop before arching the lower back.",
            category = "Posture Corrective",
            iconEmoji = "🔥",
            defaultTimerSeconds = 45,
            movementPattern = MovementPattern.HIP_HINGE
        )
    )

    private fun getShoulderRetractionDrills(): List<WarmupExercise> = listOf(
        WarmupExercise(
            id = "post_sr_1",
            name = "Banded Pull-Aparts (Thumbs Out)",
            targetJoints = "Rhomboids & Rear Delts",
            durationOrReps = "15 controlled reps",
            cue = "Keep ribs down and pull shoulder blades back and down without shrugging.",
            category = "Posture Corrective",
            iconEmoji = "🎯",
            defaultTimerSeconds = 40,
            movementPattern = MovementPattern.HORIZONTAL_PULL
        ),
        WarmupExercise(
            id = "post_sr_2",
            name = "Wall Angels with Chin Tuck",
            targetJoints = "Scapular Stabilizers & Neck",
            durationOrReps = "10 slow wall slides",
            cue = "Keep head, upper back, and wrists gently contacting the wall as you glide arms upward.",
            category = "Posture Corrective",
            iconEmoji = "🧗",
            defaultTimerSeconds = 45,
            movementPattern = MovementPattern.RAISE
        ),
        WarmupExercise(
            id = "post_sr_3",
            name = "Prone Scapular Retraction Hold",
            targetJoints = "Mid-Traps & Posterior Shoulder",
            durationOrReps = "8 reps – 3s hold",
            cue = "Pinch shoulder blades toward your spine as if holding a pencil between them.",
            category = "Posture Corrective",
            iconEmoji = "💪",
            defaultTimerSeconds = 40,
            movementPattern = MovementPattern.HORIZONTAL_PULL
        )
    )

    private fun getChestPushProtocol(): WarmupProtocol {
        return WarmupProtocol(
            title = "Chest & Push Mobility Priming",
            subtitle = "Joint lubrication & neuromuscular activation for heavy pressing",
            estimatedMinutes = 5,
            targetMusclesLabel = "Pectorals, Anterior Delts, Triceps & Scapula",
            protocolBadge = "PUSH PRIMER",
            exercises = listOf(
                WarmupExercise(
                    id = "push_1",
                    name = "Banded / PVC Shoulder Dislocates",
                    targetJoints = "Glenohumeral Joint & Pec Minor",
                    durationOrReps = "12-15 smooth passes",
                    cue = "Keep arms straight and locked. Move smoothly overhead to touch lower back, keeping ribcage down without arching lumbar spine.",
                    category = "Joint Mobility",
                    iconEmoji = "🔄",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.RAISE
                ),
                WarmupExercise(
                    id = "push_2",
                    name = "Scapular Push-ups with Protraction",
                    targetJoints = "Serratus Anterior & Scapulothoracic Joint",
                    durationOrReps = "12 reps – 2s hold at peak",
                    cue = "In high plank, keep elbows locked. Pinch shoulder blades together, then actively press the floor away to dome upper back.",
                    category = "Neural Activation",
                    iconEmoji = "⚡",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.HORIZONTAL_PUSH
                ),
                WarmupExercise(
                    id = "push_3",
                    name = "Thoracic Spine Rotations (Thread the Needle)",
                    targetJoints = "T-Spine Extension & Ribcage Expansion",
                    durationOrReps = "8 reps per side",
                    cue = "On hands and knees, reach one arm under torso, then rotate chest open toward ceiling, following fingertips with your eyes.",
                    category = "Dynamic Stretch",
                    iconEmoji = "🧘",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.CRUNCH_LEG_RAISE
                ),
                WarmupExercise(
                    id = "push_4",
                    name = "Dynamic Arm Hugs & Pec Cross-Sweeps",
                    targetJoints = "Sternal Pectoralis & Biceps Tendon",
                    durationOrReps = "15 dynamic swings",
                    cue = "Swing arms wide open to feel an elastic chest stretch, then cross across chest alternating top arm each repetition.",
                    category = "Dynamic Stretch",
                    iconEmoji = "👐",
                    defaultTimerSeconds = 30,
                    movementPattern = MovementPattern.RAISE
                ),
                WarmupExercise(
                    id = "push_5",
                    name = "Wrist Flexor & Extensor Mobility Waves",
                    targetJoints = "Wrist Joint & Forearm Extensors",
                    durationOrReps = "10 reps each direction",
                    cue = "On all fours with palms down, gently rock weight forward past wrists, then reverse hands so fingers point toward knees.",
                    category = "Joint Mobility",
                    iconEmoji = "🖐️",
                    defaultTimerSeconds = 30,
                    movementPattern = MovementPattern.CARDIO_JUMP
                )
            )
        )
    }

    private fun getBackPullProtocol(): WarmupProtocol {
        return WarmupProtocol(
            title = "Back & Pull Mobility Priming",
            subtitle = "Decompressing spine & firing lats, rhomboids and rotator cuff",
            estimatedMinutes = 5,
            targetMusclesLabel = "Latissimus Dorsi, Rhomboids, Rear Delts & Biceps",
            protocolBadge = "PULL PRIMER",
            exercises = listOf(
                WarmupExercise(
                    id = "pull_1",
                    name = "World's Greatest Stretch with Lat Reach",
                    targetJoints = "Thoracic Spine, Lats & Hip Flexors",
                    durationOrReps = "5 reps per side – hold 3s",
                    cue = "In a deep runner's lunge, rotate inside elbow down toward instep, then reach straight up and overhead, lengthening the lat.",
                    category = "Full-Body Mobility",
                    iconEmoji = "🌍",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.LUNGE_SPLIT_SQUAT
                ),
                WarmupExercise(
                    id = "pull_2",
                    name = "Dead Hang to Active Scapular Pull-ups",
                    targetJoints = "Subacromial Space & Lower Traps",
                    durationOrReps = "8-10 reps – 2s hold",
                    cue = "Hang relaxed from pull-up bar. Without bending elbows, pull shoulder blades down and back, lifting torso 2-3 inches.",
                    category = "Joint Decompression",
                    iconEmoji = "🏋️",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.VERTICAL_PULL
                ),
                WarmupExercise(
                    id = "pull_3",
                    name = "Cat-Cow with Thoracic Wave",
                    targetJoints = "Full Spine & Segmental Rhomboids",
                    durationOrReps = "10 slow breathing cycles",
                    cue = "Inhale tilting pelvis forward and dropping belly; exhale tucking tailbone and curling spine high toward ceiling.",
                    category = "Joint Mobility",
                    iconEmoji = "🐈",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.CRUNCH_LEG_RAISE
                ),
                WarmupExercise(
                    id = "pull_4",
                    name = "Kneeling Lat Stretch on Bench",
                    targetJoints = "Latissimus Dorsi & Posterior Capsule",
                    durationOrReps = "45 sec dynamic hold",
                    cue = "Kneel before a bench with elbows resting on top, hands together in prayer. Sink head and chest between arms toward floor.",
                    category = "Dynamic Stretch",
                    iconEmoji = "🧎",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.VERTICAL_PULL
                ),
                WarmupExercise(
                    id = "pull_5",
                    name = "Banded Face Pulls & External Rotation",
                    targetJoints = "Infraspinatus, Teres Minor & Rear Delts",
                    durationOrReps = "15 controlled reps",
                    cue = "Pull band directly toward forehead while rotating thumbs backwards. Squeeze rear shoulders at full contraction.",
                    category = "Neural Activation",
                    iconEmoji = "🎯",
                    defaultTimerSeconds = 40,
                    movementPattern = MovementPattern.HORIZONTAL_PULL
                )
            )
        )
    }

    private fun getLowerBodyProtocol(): WarmupProtocol {
        return WarmupProtocol(
            title = "Lower Body & Hip Capsule Priming",
            subtitle = "Hip socket lubrication, adductor release & ankle dorsiflexion",
            estimatedMinutes = 6,
            targetMusclesLabel = "Hips, Glutes, Hamstrings, Quads & Ankles",
            protocolBadge = "LEG PRIMER",
            exercises = listOf(
                WarmupExercise(
                    id = "legs_1",
                    name = "90/90 Hip Swivels & Shin Box Rise",
                    targetJoints = "Hip Internal & External Rotation",
                    durationOrReps = "8 swivels per side",
                    cue = "Sit on floor with both knees bent at 90°. Rotate knees to opposite side without lifting heels. Drive hips forward at peak.",
                    category = "Joint Mobility",
                    iconEmoji = "📐",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.HIP_HINGE
                ),
                WarmupExercise(
                    id = "legs_2",
                    name = "Deep Cossack Squats & Adductor Rockers",
                    targetJoints = "Groin Adductors & Ankle Dorsiflexion",
                    durationOrReps = "6-8 reps per side",
                    cue = "Take wide stance. Shift hips down and back onto one heel while keeping other leg straight with toes angled upward.",
                    category = "Dynamic Stretch",
                    iconEmoji = "🦀",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.LUNGE_SPLIT_SQUAT
                ),
                WarmupExercise(
                    id = "legs_3",
                    name = "Deep Squat Pry with Thoracic Reach",
                    targetJoints = "Ankle Mobility, Femoral Head & T-Spine",
                    durationOrReps = "60 sec dynamic pry",
                    cue = "Sit in bottom of deep squat. Use elbows to gently pry knees outward. Place one hand on floor, rotate opposite arm to ceiling.",
                    category = "Joint Mobility",
                    iconEmoji = "🧘‍♂️",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.SQUAT
                ),
                WarmupExercise(
                    id = "legs_4",
                    name = "Dynamic Leg Swings (Front-to-Back & Lateral)",
                    targetJoints = "Hamstrings, Hip Flexors & Tensor Fasciae",
                    durationOrReps = "12 swings per leg each plane",
                    cue = "Hold a wall or rack for stability. Swing leg like a smooth pendulum, gradually increasing range as muscles warm up.",
                    category = "Dynamic Stretch",
                    iconEmoji = "🦵",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.CARDIO_JUMP
                ),
                WarmupExercise(
                    id = "legs_5",
                    name = "Single-Leg Glute Bridge with Hold",
                    targetJoints = "Gluteus Maximus & Hamstring Origin",
                    durationOrReps = "10 reps per leg – 2s hold",
                    cue = "Lie on back, plant one foot. Drive through heel to lock hip out in full extension, holding at top without hyper-extending back.",
                    category = "Neural Activation",
                    iconEmoji = "🔥",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.HIP_HINGE
                )
            )
        )
    }

    private fun getShouldersArmsProtocol(): WarmupProtocol {
        return WarmupProtocol(
            title = "Shoulders & Arms Joint Prep",
            subtitle = "Rotator cuff activation, scapular tracking & elbow health",
            estimatedMinutes = 5,
            targetMusclesLabel = "Deltoids, Rotator Cuff, Biceps, Triceps & Forearms",
            protocolBadge = "ARM PRIMER",
            exercises = listOf(
                WarmupExercise(
                    id = "sh_1",
                    name = "Wall Slides with Retraction Lift-Off",
                    targetJoints = "Serratus Anterior & Lower Traps",
                    durationOrReps = "10 smooth slides",
                    cue = "Place forearms flat on wall in 'W' shape. Slide hands upward into 'Y', keeping lower back flat against wall. Lift off at top.",
                    category = "Scapular Mobility",
                    iconEmoji = "🧗",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.RAISE
                ),
                WarmupExercise(
                    id = "sh_2",
                    name = "Cuban Press Rotations with Band",
                    targetJoints = "Supraspinatus & Infraspinatus Rotators",
                    durationOrReps = "12 reps",
                    cue = "Hold light resistance with elbows at 90° shoulder height. Rotate forearms up until vertical, press overhead, and reverse.",
                    category = "Neural Activation",
                    iconEmoji = "🏹",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.VERTICAL_PUSH
                ),
                WarmupExercise(
                    id = "sh_3",
                    name = "Cross-Body Dynamic Deltoid Stretch",
                    targetJoints = "Posterior Shoulder Capsule",
                    durationOrReps = "10 pulses per arm",
                    cue = "Bring one arm across chest horizontally. Use opposite forearm to lightly pulse, releasing and gently deepening each pulse.",
                    category = "Dynamic Stretch",
                    iconEmoji = "🤝",
                    defaultTimerSeconds = 30,
                    movementPattern = MovementPattern.RAISE
                ),
                WarmupExercise(
                    id = "sh_4",
                    name = "Prone Swimmers / Blackburn Circles",
                    targetJoints = "Full Glenohumeral Articulation",
                    durationOrReps = "6 large controlled sweeps",
                    cue = "Lie face down with forehead hovering off floor. Sweep hands from small of lower back in wide arcs until overhead.",
                    category = "Joint Mobility",
                    iconEmoji = "🏊",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.RAISE
                ),
                WarmupExercise(
                    id = "sh_5",
                    name = "Elbow Joint Circling & Wrist Pumps",
                    targetJoints = "Elbow Joint Capsule & Brachioradialis",
                    durationOrReps = "15 reps per direction",
                    cue = "Clench loose fists, circle forearms in wide circles to lubricate synovial fluid through elbows before heavy curls and presses.",
                    category = "Joint Mobility",
                    iconEmoji = "💪",
                    defaultTimerSeconds = 30,
                    movementPattern = MovementPattern.CURL
                )
            )
        )
    }

    private fun getFullBodyProtocol(): WarmupProtocol {
        return WarmupProtocol(
            title = "Full-Body Kinetic Chain Priming",
            subtitle = "Comprehensive multi-joint dynamic sequence for total body power",
            estimatedMinutes = 6,
            targetMusclesLabel = "Total Body: Hips, Spine, Shoulders & Posterior Chain",
            protocolBadge = "FULL BODY PRIMER",
            exercises = listOf(
                WarmupExercise(
                    id = "fb_1",
                    name = "World's Greatest Stretch & T-Spine Twist",
                    targetJoints = "Hips, Groin, Hamstrings & Thoracic Spine",
                    durationOrReps = "5 reps per side",
                    cue = "Step into deep lunge. Place same-side elbow inside ankle, then sweep opposite arm open toward the ceiling.",
                    category = "Full-Body Mobility",
                    iconEmoji = "🌍",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.LUNGE_SPLIT_SQUAT
                ),
                WarmupExercise(
                    id = "fb_2",
                    name = "Inchworm to Plank & Downward Dog",
                    targetJoints = "Hamstrings, Calves & Scapular Stability",
                    durationOrReps = "6 full cycles",
                    cue = "Hinge from hips touching toes. Walk hands out to high plank, then press hips upward into downward dog, pedaling heels.",
                    category = "Dynamic Stretch",
                    iconEmoji = "🐛",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.HIP_HINGE
                ),
                WarmupExercise(
                    id = "fb_3",
                    name = "90/90 Hip Swivel to Shin Box Rise",
                    targetJoints = "Pelvic Girdle & Hip Rotators",
                    durationOrReps = "6 reps per side",
                    cue = "From seated 90/90 position, drive knees into floor, rise up onto knees thrusting hips forward. Lower slowly.",
                    category = "Joint Mobility",
                    iconEmoji = "📐",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.HIP_HINGE
                ),
                WarmupExercise(
                    id = "fb_4",
                    name = "Bird-Dog with Anti-Rotation Hold",
                    targetJoints = "Anterior Core, Glute & Posterior Oblique Sling",
                    durationOrReps = "8 reps per side – 3s hold",
                    cue = "On hands and knees, extend right arm and left leg simultaneously until parallel to floor. Hold steady without tilting hips.",
                    category = "Neural Activation",
                    iconEmoji = "🐕",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.CRUNCH_LEG_RAISE
                ),
                WarmupExercise(
                    id = "fb_5",
                    name = "Deep Bodyweight Squat Pry with Heartbeat Reach",
                    targetJoints = "Ankles, Hips & Upper Back",
                    durationOrReps = "10 reps",
                    cue = "Descend into rock-bottom squat. Push knees out with elbows, reach hands straight forward and stand up tall.",
                    category = "Joint Mobility",
                    iconEmoji = "❤️",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.SQUAT
                )
            )
        )
    }

    private fun getRestRecoveryProtocol(): WarmupProtocol {
        return WarmupProtocol(
            title = "Active Restoration & Mobility Flow",
            subtitle = "Parasympathetic breathing, joint decompression & fascial release",
            estimatedMinutes = 8,
            targetMusclesLabel = "Restoration Flow: Spine, Hips, Diaphragm & Connective Tissue",
            protocolBadge = "RECOVERY FLOW",
            exercises = listOf(
                WarmupExercise(
                    id = "rec_1",
                    name = "Box Breathing with 90/90 Hip Elevation",
                    targetJoints = "Diaphragm, Pelvic Floor & Parasympathetic CNS",
                    durationOrReps = "2 minutes slow cadence",
                    cue = "Lie on back with lower legs resting on a bench or chair at 90°. Inhale 4s into abdomen, hold 4s, exhale 4s, hold 4s.",
                    category = "CNS Restoration",
                    iconEmoji = "🌬️",
                    defaultTimerSeconds = 90,
                    movementPattern = MovementPattern.PLANK_HOLD
                ),
                WarmupExercise(
                    id = "rec_2",
                    name = "Cat-Cow to Child's Pose Transition",
                    targetJoints = "Lumbar, Thoracic Spine & Latissimus Insertion",
                    durationOrReps = "8 slow flowing transitions",
                    cue = "Flow from arching and flexing the spine into sinking hips deep back onto heels, extending fingertips forward.",
                    category = "Decompression",
                    iconEmoji = "🌊",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.CRUNCH_LEG_RAISE
                ),
                WarmupExercise(
                    id = "rec_3",
                    name = "Supine Figure-4 Piriformis Stretch",
                    targetJoints = "Deep Hip Rotators & Sciatic Pathway",
                    durationOrReps = "60 sec hold per side",
                    cue = "Cross right ankle over left knee. Reach through and pull left hamstring toward chest, relaxing neck and shoulders.",
                    category = "Fascial Release",
                    iconEmoji = "🧘‍♀️",
                    defaultTimerSeconds = 60,
                    movementPattern = MovementPattern.CRUNCH_LEG_RAISE
                ),
                WarmupExercise(
                    id = "rec_4",
                    name = "Standing Side Bend & Lat Arc Breathing",
                    targetJoints = "Ribcage Intercostals & Quadratus Lumborum",
                    durationOrReps = "6 deep breaths per side",
                    cue = "Reach left arm overhead and lean gently to right. Inhale deeply expanding left ribs like an accordion, exhale slowly.",
                    category = "Fascial Mobility",
                    iconEmoji = "🎋",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.CARDIO_JUMP
                ),
                WarmupExercise(
                    id = "rec_5",
                    name = "Lizard Pose with Gentle Rocking",
                    targetJoints = "Deep Iliopsoas & Groin Capsules",
                    durationOrReps = "45 sec gentle exploration per side",
                    cue = "In low runner's lunge with both hands inside front foot, gently sway hips side to side and forward and back.",
                    category = "Hip Decompression",
                    iconEmoji = "🦎",
                    defaultTimerSeconds = 45,
                    movementPattern = MovementPattern.LUNGE_SPLIT_SQUAT
                )
            )
        )
    }
}
