package com.example.aura.engine

data class ExerciseGuide(
    val whatToDo: String,
    val setup: String,
    val execution: String,
    val formCue: String,
    val mistakeToAvoid: String,
    val breathing: String,
    val steps: List<String>,
    val primaryMuscles: String,
    val equipmentRequired: String,
    val isGenericFallback: Boolean = false
)

object ExerciseGuideEngine {

    private fun g(
        whatToDo: String,
        setup: String,
        execution: String,
        formCue: String,
        mistakeToAvoid: String,
        breathing: String,
        steps: List<String>,
        primaryMuscles: String,
        equipmentRequired: String
    ) = ExerciseGuide(
        whatToDo = whatToDo,
        setup = setup,
        execution = execution,
        formCue = formCue,
        mistakeToAvoid = mistakeToAvoid,
        breathing = breathing,
        steps = steps,
        primaryMuscles = primaryMuscles,
        equipmentRequired = equipmentRequired,
        isGenericFallback = false
    )

    private val guidesById: Map<String, ExerciseGuide> = mapOf(
        // ==================== CHEST ====================
        "c1" to g(
            whatToDo = "Press a barbell upward from mid-chest to full arm extension on a flat bench.",
            setup = "Lie with eyes under the bar, feet planted flat, and shoulder blades pinched tight into the bench.",
            execution = "Lower the bar smoothly to your sternum with elbows angled at 45 degrees, then drive straight up.",
            formCue = "Leg drive into the floor keeps your upper back locked and bar path crisp.",
            mistakeToAvoid = "Flaring your elbows straight out to 90 degrees or bouncing the bar off your ribs.",
            breathing = "Inhale as you lower the bar; exhale steadily as you press past the sticking point.",
            steps = listOf(
                "Plant both feet flat and retract your shoulder blades into the bench pad.",
                "Grip the bar slightly wider than shoulder-width and unrack over your chest.",
                "Lower the barbell under control for 2 seconds to lightly touch mid-chest.",
                "Press the bar up in a slight arc toward your upper chest until elbows lock."
            ),
            primaryMuscles = "Mid Chest, Front Delts, Triceps",
            equipmentRequired = "Olympic Barbell, Flat Bench"
        ),
        "req_c1" to g(
            whatToDo = "Execute controlled flat barbell bench presses to build upper-body pushing strength.",
            setup = "Set feet flat, arch your upper back slightly, and grip evenly just outside shoulder width.",
            execution = "Bring the bar down softly to the nipple line and press powerfully back over your shoulders.",
            formCue = "Pull the bar apart subtly with your hands to engage lats and stabilize your shoulders.",
            mistakeToAvoid = "Lifting your hips off the bench or letting wrists bend far backward.",
            breathing = "Take a big brace breath at the top and exhale once you finish the upward press.",
            steps = listOf(
                "Set your grip evenly and pull shoulder blades back and down.",
                "Unrack with straight arms over your shoulder joint.",
                "Lower to mid-chest over 2 seconds without bouncing.",
                "Drive feet down and press the bar smoothly to lockout."
            ),
            primaryMuscles = "Chest, Front Delts, Triceps",
            equipmentRequired = "Barbell, Flat Bench & Rack"
        ),
        "c_m1" to g(
            whatToDo = "Press machine handles straight forward from chest height for stable pectoral loading.",
            setup = "Adjust seat height so handles align with mid-chest and keep your back pressed into the pad.",
            execution = "Push forward smoothly without shrugging shoulders, pause at lockout, and return slowly.",
            formCue = "Think of bringing your biceps together toward your midline as you press.",
            mistakeToAvoid = "Letting your shoulders roll forward off the seat pad at the end of each rep.",
            breathing = "Exhale as you push the handles forward; inhale on the slow return.",
            steps = listOf(
                "Adjust the seat so handles sit level with mid-chest.",
                "Pin your shoulder blades flat against the backrest and grip both handles.",
                "Press forward until arms are just shy of snapping straight.",
                "Control the handles back for 2 seconds until a full chest stretch."
            ),
            primaryMuscles = "Mid Chest, Front Delts, Triceps",
            equipmentRequired = "Chest Press Machine"
        ),
        "c_m2" to g(
            whatToDo = "Press the incline machine handles upward and forward to target the upper chest.",
            setup = "Set the seat so the handles sit across your upper chest and plant your feet firmly.",
            execution = "Drive handles up to near extension, hold 1 second, then lower under control.",
            formCue = "Keep your chest lifted high so the upper pecs carry the load instead of front delts.",
            mistakeToAvoid = "Shrugging shoulders up toward your ears during the press.",
            breathing = "Exhale on the press upward; inhale cleanly as you lower.",
            steps = listOf(
                "Set the seat height so the handles align with your collarbone line.",
                "Pull shoulder blades down into the pad and grip evenly.",
                "Press handles upward cleanly without locking elbows aggressively.",
                "Lower under control until you feel a stretch across the upper chest."
            ),
            primaryMuscles = "Upper Chest, Front Delts, Triceps",
            equipmentRequired = "Incline Chest Press Machine"
        ),
        "c2" to g(
            whatToDo = "Press dumbbells from upper chest to overhead on a 30-degree incline bench.",
            setup = "Set the bench to 30 degrees, kick dumbbells up to shoulders, and plant feet wide.",
            execution = "Lower dumbbells outside your upper chest to a deep stretch, then press up in a subtle arc.",
            formCue = "Keep forearms vertical at the bottom and elbows tucked about 45 degrees.",
            mistakeToAvoid = "Setting the bench angle above 45 degrees, which turns the lift into shoulder work.",
            breathing = "Inhale smoothly during the descent; exhale as you press the weights up.",
            steps = listOf(
                "Set bench to a gentle 30-degree incline and plant both feet flat.",
                "Pin shoulders down and lower the dumbbells beside your upper chest.",
                "Pause briefly in the stretched bottom position without bouncing.",
                "Press dumbbells up together above your collarbone without clanking them."
            ),
            primaryMuscles = "Upper Chest, Front Delts, Triceps",
            equipmentRequired = "Dumbbells, Incline Bench"
        ),
        "req_c2" to g(
            whatToDo = "Press a barbell from your upper chest on a shallow incline bench.",
            setup = "Position the bench at 30 degrees under the rack and anchor your feet and upper back.",
            execution = "Lower the barbell calmly to just below your collarbone, then press back over shoulders.",
            formCue = "Lift your sternum toward the bar on the descent to maximize upper pec stretch.",
            mistakeToAvoid = "Flaring elbows wide or lowering the bar too low toward the stomach.",
            breathing = "Inhale on the lowering phase; exhale firmly as you press the bar up.",
            steps = listOf(
                "Adjust bench to 30 degrees and place hands slightly outside shoulders.",
                "Unrack with locked wrists and stabilize bar over your upper chest.",
                "Lower smoothly to upper chest level with tucked elbows.",
                "Press straight back up to full arm extension."
            ),
            primaryMuscles = "Upper Chest, Front Delts, Triceps",
            equipmentRequired = "Barbell, Incline Bench"
        ),
        "c_m3" to g(
            whatToDo = "Sweep machine arms together in front of your chest to isolate the pectorals.",
            setup = "Set the seat so your arms move horizontally at chest height with elbows slightly bent.",
            execution = "Squeeze handles together across your chest, hold 1 second, and open slowly to a stretch.",
            formCue = "Lead the sweep with your inner elbows and hands rather than bending your wrists.",
            mistakeToAvoid = "Rounding your shoulders forward or rushing the opening phase.",
            breathing = "Exhale as your arms close together; inhale steadily as you open wide.",
            steps = listOf(
                "Adjust seat height so handles align with mid-chest.",
                "Press your upper back flat into the pad with elbows softly unlocked.",
                "Draw the pads together until they nearly meet in front of your chest.",
                "Open slowly until you feel a comfortable stretch across your pecs."
            ),
            primaryMuscles = "Sternal Pectorals, Inner Chest",
            equipmentRequired = "Pec Deck Fly Machine"
        ),
        "req_c3" to g(
            whatToDo = "Perform strict Pec Deck adductions to overload the chest without tricep fatigue.",
            setup = "Sit upright against the pad with shoulders down and forearms resting on the pads.",
            execution = "Bring forearms together deliberately, squeeze for a full count, and resist the open return.",
            formCue = "Keep your chest proud as arms open so tension stays purely in the pecs.",
            mistakeToAvoid = "Letting your arms swing too far behind your torso where shoulders take over.",
            breathing = "Exhale on the squeeze inward; inhale on the controlled return.",
            steps = listOf(
                "Set handle stops so your elbows stop comfortably parallel to your torso.",
                "Anchor your shoulder blades back before initiating the first rep.",
                "Sweep arms inward smoothly and hold peak chest tension for 1 second.",
                "Control the opening phase over 2-3 seconds."
            ),
            primaryMuscles = "Chest, Front Delts",
            equipmentRequired = "Machine Pec Deck"
        ),
        "c_c1" to g(
            whatToDo = "Bring two chest-height cable handles together in a smooth hugging arc.",
            setup = "Set pulleys at mid-chest height, take one step forward into a split stance, and keep chest tall.",
            execution = "Sweep arms inward with soft elbows until hands meet, pause, then open slowly.",
            formCue = "Maintain the exact same slight elbow bend from start to finish like hugging a barrel.",
            mistakeToAvoid = "Turning the fly into a heavy cable press by bending elbows excessively.",
            breathing = "Exhale as handles meet in front of your chest; inhale as arms open.",
            steps = listOf(
                "Step slightly forward from the cable stack with one foot ahead for balance.",
                "Hold handles with palms forward and a gentle bend at the elbows.",
                "Sweep both hands in an arc to meet at sternum height.",
                "Slowly open arms back to torso line under continuous cable tension."
            ),
            primaryMuscles = "Pectorals, Front Delts",
            equipmentRequired = "Dual Cable Machine"
        ),
        "c_c2" to g(
            whatToDo = "Pull high cable pulleys downward and across your lower chest for complete pec fiber recruitment.",
            setup = "Set pulleys above head height, step forward slightly, and hinge forward 10 degrees.",
            execution = "Pull handles downward and inward until wrists cross slightly near waist height, then open.",
            formCue = "Push your shoulders down away from your ears throughout the entire arc.",
            mistakeToAvoid = "Jerking your torso back and forth to move too much weight.",
            breathing = "Exhale as handles sweep down together; inhale smoothly as arms rise.",
            steps = listOf(
                "Set both pulleys near the top and step forward into a staggered stance.",
                "Start with arms out wide and elbows softly bent.",
                "Sweep hands down and inward in front of your hips to squeeze lower pecs.",
                "Return arms along the same wide arc with control."
            ),
            primaryMuscles = "Lower & Sternal Chest, Front Delts",
            equipmentRequired = "Cable Machine"
        ),
        "c3" to g(
            whatToDo = "Lower your chest smoothly toward the floor from a rigid plank and press back up.",
            setup = "Place hands slightly wider than shoulders, squeeze glutes, and brace your abs flat.",
            execution = "Descend until your chest hovers an inch above floor with elbows angled 45 degrees back, then push up.",
            formCue = "Keep your body moving as a single stiff plank from heels to crown.",
            mistakeToAvoid = "Letting your lower back sag downward or elbows flare out wide.",
            breathing = "Inhale as you lower to the floor; exhale forcefully as you press up.",
            steps = listOf(
                "Set hands under and slightly outside shoulders with fingers spread wide.",
                "Tighten glutes and abs to lock a straight line from head to ankles.",
                "Lower smoothly until chest nearly touches the floor.",
                "Push the floor away cleanly to full arm extension."
            ),
            primaryMuscles = "Chest, Triceps, Front Delts, Core",
            equipmentRequired = "Bodyweight"
        ),
        "c4" to g(
            whatToDo = "Press dumbbells upward from the floor, stopping safely as upper arms touch the ground.",
            setup = "Lie flat on the floor with knees bent, feet planted, and upper arms resting at 45 degrees.",
            execution = "Pause lightly with triceps touching the floor, then drive dumbbells straight up together.",
            formCue = "Touch elbows gently on the mat—do not bounce or lose shoulder tightness.",
            mistakeToAvoid = "Slamming elbows into the floor between reps or flaring arms straight sideways.",
            breathing = "Inhale as elbows come down to the floor; exhale as you press up.",
            steps = listOf(
                "Lie flat on a mat with bent knees and hold dumbbells over your chest.",
                "Lower arms smoothly until the backs of your upper arms tap the floor.",
                "Pause for half a second while keeping chest and core braced.",
                "Press dumbbells directly upward to lockout."
            ),
            primaryMuscles = "Mid Chest, Triceps, Front Delts",
            equipmentRequired = "Dumbbells"
        ),
        "req_c4" to g(
            whatToDo = "Lower yourself between parallel dip bars with a slight forward torso lean, then press back up.",
            setup = "Grip parallel bars firmly, lock elbows out, lean torso forward 15-20 degrees, and cross ankles.",
            execution = "Descend until shoulders drop slightly below elbow height, then press back up strongly.",
            formCue = "Maintain a consistent forward lean to emphasize lower chest over pure triceps.",
            mistakeToAvoid = "Dropping too fast into the bottom or swinging your legs for momentum.",
            breathing = "Inhale on the controlled descent; exhale strongly as you push back up.",
            steps = listOf(
                "Support your bodyweight on parallel bars with shoulders depressed down.",
                "Lean torso gently forward and tuck feet behind you.",
                "Bend elbows to lower until upper arms reach parallel to the floor.",
                "Press back to top lockout without shrugging shoulders."
            ),
            primaryMuscles = "Lower Chest, Triceps, Front Delts",
            equipmentRequired = "Dip Station (Optional Weight Belt)"
        ),
        "cal_ap1" to g(
            whatToDo = "Shift weight onto one working arm during a push-up while extending the opposite arm straight out.",
            setup = "Set hands very wide with fingers angled outward and lock your core into a rigid plank.",
            execution = "Bend one elbow to lower your chest toward that hand while keeping the other arm straight, then press up.",
            formCue = "Keep your hips level and torso straight—avoid twisting sideways to cheat the rep.",
            mistakeToAvoid = "Bending the assisting straight arm instead of loading the primary pressing side.",
            breathing = "Inhale as you lower to one side; exhale firmly as you press back to center.",
            steps = listOf(
                "Assume a wide plank stance with hands well outside shoulder width.",
                "Shift chest weight over one hand while keeping the opposite arm straight.",
                "Lower your chest smoothly down toward the working hand.",
                "Push back up to center and alternate sides cleanly."
            ),
            primaryMuscles = "Chest, Triceps, Front Delts, Core",
            equipmentRequired = "Bodyweight"
        ),
        "cal_pp1" to g(
            whatToDo = "Perform push-ups with hands set near waist level and shoulders leaned well forward over wrists.",
            setup = "Start in a plank, turn fingers slightly outward for wrist comfort, and lean shoulders past your hands.",
            execution = "Hold the forward lean as you lower your chest cleanly toward the floor and press back up.",
            formCue = "Protract your shoulder blades at the top and keep the forward lean at the bottom.",
            mistakeToAvoid = "Shifting hips backward as you get tired so it becomes a standard push-up.",
            breathing = "Inhale on the slow forward-lean descent; exhale smoothly on the press.",
            steps = listOf(
                "Place hands at mid-rib level with fingers rotated slightly outward.",
                "Lean forward until shoulders sit visibly ahead of your wrists.",
                "Lower steadily while keeping elbows tucked tightly along your ribs.",
                "Press up powerfully back into a protracted hollow plank."
            ),
            primaryMuscles = "Chest, Anterior Delts, Triceps, Core",
            equipmentRequired = "Bodyweight"
        ),
        "bnd_ch1" to g(
            whatToDo = "Anchor a resistance band behind your upper back and press both ends forward like a chest press.",
            setup = "Loop band around upper back under your armpits, step into a staggered stance, and tuck elbows slightly.",
            execution = "Press hands straight forward until arms extend and squeeze your chest for 1 second.",
            formCue = "Keep the band flat below shoulder height so it doesn't slide upward toward your neck.",
            mistakeToAvoid = "Letting the band snap your hands backward at the start of the return.",
            breathing = "Exhale as you press forward against band tension; inhale on the slow return.",
            steps = listOf(
                "Wrap the band across your upper back and grip ends securely at chest level.",
                "Step one foot ahead into a stable split stance.",
                "Press hands forward and slightly inward until arms reach full extension.",
                "Control the hands back calmly to rib height."
            ),
            primaryMuscles = "Chest, Front Delts, Triceps",
            equipmentRequired = "Resistance Bands"
        ),
        "db_ch1" to g(
            whatToDo = "Press two dumbbells from outside mid-chest straight overhead while lying on a flat bench.",
            setup = "Lie flat with feet planted, shoulder blades tucked back, and dumbbells held at chest level.",
            execution = "Lower dumbbells smoothly to chest sides for a stretch, then press up until weights hover together.",
            formCue = "Keep wrists stacked directly over your elbows at every point of the lift.",
            mistakeToAvoid = "Flaring elbows straight out sideways or letting dumbbells drift toward your neck.",
            breathing = "Inhale as you lower dumbbells beside your ribs; exhale as you press up.",
            steps = listOf(
                "Lie back on a flat bench with feet pressed firmly into the ground.",
                "Set dumbbells outside your mid-chest with elbows angled at 45 degrees.",
                "Drive weights smoothly upward in a gentle inward path.",
                "Lower for 2 seconds back to the deep chest-level starting point."
            ),
            primaryMuscles = "Mid Chest, Front Delts, Triceps",
            equipmentRequired = "Dumbbells, Flat Bench"
        ),
        "db_ch2" to g(
            whatToDo = "Open dumbbells wide with softly bent elbows on a flat bench, then hug them back above your chest.",
            setup = "Hold dumbbells above sternum with palms facing each other and a gentle 15-degree elbow bend.",
            execution = "Lower arms outward in a wide arc until chest stretches, then squeeze chest to draw weights back up.",
            formCue = "Lock your elbow angle—imagine only your shoulders hinging open and closed.",
            mistakeToAvoid = "Dropping hands too deep below shoulder level or bending elbows into a press.",
            breathing = "Inhale deeply as your arms open outward; exhale smoothly as weights rise together.",
            steps = listOf(
                "Lie flat and start with dumbbells held together above mid-chest.",
                "Keep a soft fixed elbow bend and open arms outward in a wide arc.",
                "Stop once hands reach roughly torso depth and feel a pec stretch.",
                "Contract your chest to bring dumbbells back up in the same arc."
            ),
            primaryMuscles = "Pectoralis Major, Front Delts",
            equipmentRequired = "Dumbbells, Flat Bench"
        ),

        // ==================== BACK ====================
        "b1" to g(
            whatToDo = "Lift a loaded barbell from the floor by driving hips and legs until standing fully upright.",
            setup = "Stand with mid-foot under the bar, grip just outside shins, pull chest up, and lock lats tight.",
            execution = "Push the floor away with legs until bar passes knees, then drive hips forward smoothly to stand tall.",
            formCue = "Take the slack out of the bar until you hear it click before pulling off the ground.",
            mistakeToAvoid = "Rounding your lower back or jerking the bar violently off the floor.",
            breathing = "Take a deep belly brace breath before pulling; exhale once upright or after lowering.",
            steps = listOf(
                "Step up so the bar crosses mid-foot and grip evenly outside knees.",
                "Drop hips slightly, lift chest, and tighten armpits to set a flat back.",
                "Leg-press the floor away while dragging the bar along your shins.",
                "Finish tall with glutes squeezed, then hinge hips back to set the bar down."
            ),
            primaryMuscles = "Posterior Chain, Glutes, Hamstrings, Erectors",
            equipmentRequired = "Olympic Barbell & Plates"
        ),
        "req_b1" to g(
            whatToDo = "Execute strict conventional deadlifts to develop total posterior chain and back thickness.",
            setup = "Feet hip-width, shins upright near the bar, flat spine, and arms locked long like hooks.",
            execution = "Push through mid-foot as bar tracks straight up your legs, lock hips cleanly, then lower with control.",
            formCue = "Think about pushing the floor down rather than lifting with your lower back.",
            mistakeToAvoid = "Hyperextending backwards at the top lockout or letting the bar drift away from shins.",
            breathing = "Hold a firm diaphragmatic brace throughout each pull; breathe between reps.",
            steps = listOf(
                "Align mid-foot under bar and set a shoulder-width double-overhand or mixed grip.",
                "Brace your core hard and wedge hips into pulling tension.",
                "Drive smoothly upward keeping the bar brushing shins and thighs.",
                "Hinge hips back first before bending knees to return bar to floor."
            ),
            primaryMuscles = "Glutes, Hamstrings, Spinal Erectors, Upper Back",
            equipmentRequired = "Barbell & Plates"
        ),
        "b_m1" to g(
            whatToDo = "Pull a wide cable bar down to your upper collarbone while seated with thighs anchored.",
            setup = "Lock knees snug under thigh pads, grip wider than shoulder-width, and lean back slightly 10 degrees.",
            execution = "Drive elbows straight down toward your ribs to touch bar near upper chest, pause, and reach tall.",
            formCue = "Lead the pull from your elbows down into your back pockets rather than bending wrists.",
            mistakeToAvoid = "Swinging your entire torso backward to yank the stack down with momentum.",
            breathing = "Exhale as you pull the bar down to your upper chest; inhale smoothly as arms rise.",
            steps = listOf(
                "Adjust the thigh pad tight and take an even overhand grip outside shoulder width.",
                "Lift your chest slightly and depress your shoulder blades to start.",
                "Pull elbows downward until the bar nears your collarbone.",
                "Let lats stretch fully at the top before pulling the next rep."
            ),
            primaryMuscles = "Lats, Teres Major, Rhomboids, Biceps",
            equipmentRequired = "Lat Pulldown Machine"
        ),
        "b_c1" to g(
            whatToDo = "Pull a horizontal cable handle into your midsection while seated tall.",
            setup = "Brace feet flat on footplates with knees softly bent and torso vertical.",
            execution = "Draw handles to your lower ribs while pinching shoulder blades together, then extend arms smoothly.",
            formCue = "Keep your chest tall at the stretch so your lower back doesn't slump.",
            mistakeToAvoid = "Leaning way back on every pull or shrugging traps toward your ears.",
            breathing = "Exhale as you row into your ribs; inhale on the controlled stretch forward.",
            steps = listOf(
                "Sit upright with feet braced and grab the handle with both hands.",
                "Allow shoulders to glide forward comfortably without rounding your lower spine.",
                "Drive elbows straight back beside your torso and pinch shoulder blades.",
                "Control the weight back forward to full arm extension."
            ),
            primaryMuscles = "Mid Back, Rhomboids, Lats, Biceps",
            equipmentRequired = "Seated Cable Row Machine"
        ),
        "b_m2" to g(
            whatToDo = "Row heavy handles backward with your chest resting stably against an angled support pad.",
            setup = "Adjust seat height so the top of the chest pad sits right under your collarbone.",
            execution = "Pull handles smoothly back past your ribs, squeeze shoulder blades for 1 second, and release slowly.",
            formCue = "Keep your sternum glued to the pad so zero lower-back momentum helps.",
            mistakeToAvoid = "Lifting your chest off the pad at the finish to heave heavier weight.",
            breathing = "Exhale firmly during the pull; inhale smoothly as arms reach forward.",
            steps = listOf(
                "Set seat height so arms align horizontally with the handles.",
                "Rest chest solidly against the support pad with feet grounded.",
                "Row elbows backward smoothly and hold peak back contraction briefly.",
                "Extend arms completely for a controlled lat stretch."
            ),
            primaryMuscles = "Mid Traps, Rhomboids, Lats, Rear Delts",
            equipmentRequired = "Chest-Supported Row Machine"
        ),
        "b_m3" to g(
            whatToDo = "Pull high-mounted machine handles down and back at a 45-degree angle.",
            setup = "Lock seat and chest pad snugly so your arms angle upward at 45 degrees at full stretch.",
            execution = "Drive elbows downward and backward toward your hips, squeeze upper lats, and return steadily.",
            formCue = "Pull elbows down and in rather than flaring them straight out sideways.",
            mistakeToAvoid = "Shrugging shoulders up during the stretch or short-cutting the bottom squeeze.",
            breathing = "Exhale as handles travel toward your chest; inhale as arms extend upward.",
            steps = listOf(
                "Anchor chest solidly against the pad and grab the high handles.",
                "Depress shoulder blades slightly before pulling elbows down.",
                "Pull handles smoothly into upper torso level while squeezing lats.",
                "Return along the angled arc until arms fully straighten."
            ),
            primaryMuscles = "Upper Lats, Rear Delts, Teres Major",
            equipmentRequired = "High Row Machine"
        ),
        "b_c2" to g(
            whatToDo = "Sweep a straight bar or rope down from overhead to thighs with straight arms to isolate lats.",
            setup = "Set pulley high, step back two paces, hinge torso forward 30 degrees, and brace core.",
            execution = "With arms nearly straight, push the bar in an arc down to your thighs, pause, and raise slowly.",
            formCue = "Think of squeezing oranges in your armpits as the bar reaches your pockets.",
            mistakeToAvoid = "Bending elbows into a tricep pushdown instead of moving purely from the shoulder.",
            breathing = "Exhale as you sweep the bar down to thighs; inhale as lats stretch overhead.",
            steps = listOf(
                "Grip bar or rope shoulder-width and hinge smoothly forward with a flat back.",
                "Start with arms angled overhead and lats lengthened.",
                "Sweep straight arms downward in an arc to brush your thighs.",
                "Control the upward return until lats reach full stretch."
            ),
            primaryMuscles = "Latissimus Dorsi, Teres Major",
            equipmentRequired = "High Cable Pulley"
        ),
        "b_m4" to g(
            whatToDo = "Perform vertical pull-ups with machine counterweight support to master full-range lat mechanics.",
            setup = "Select counterweight, kneel or stand smoothly on the platform, and take a wider-than-shoulder grip.",
            execution = "Pull chest cleanly up toward the handles, pause at the top, and lower smoothly to a dead hang.",
            formCue = "Keep legs straight and hips aligned underneath the bar—don't kick.",
            mistakeToAvoid = "Dropping off the pad too fast or stopping halfway down on each rep.",
            breathing = "Exhale as you rise toward the handles; inhale smoothly on the lowering phase.",
            steps = listOf(
                "Set the desired assistance pin and step onto the moving pad carefully.",
                "Grip outer handles with palms forward and let arms straighten cleanly.",
                "Pull elbows downward until chin clears handle height.",
                "Lower slowly for 2 seconds to full arm extension."
            ),
            primaryMuscles = "Lats, Upper Back, Biceps",
            equipmentRequired = "Assisted Pull-up Machine"
        ),
        "b2" to g(
            whatToDo = "Hang from an overhead bar and pull your chest cleanly up to bar height.",
            setup = "Grip slightly outside shoulder width, brace core slightly forward, and cross ankles behind.",
            execution = "Engage shoulder blades first, drive elbows straight down to pull chest up, then lower fully.",
            formCue = "Lead with your chest toward the bar rather than craning your chin forward.",
            mistakeToAvoid = "Kipping with your legs or cutting the bottom hang short.",
            breathing = "Exhale smoothly as you pull up; inhale calmly as you lower to full hang.",
            steps = listOf(
                "Hang at full arm extension with hands evenly placed outside shoulders.",
                "Depress shoulder blades away from ears to initiate the pull.",
                "Drive elbows downward until chest nears the bar.",
                "Lower smoothly over 2 seconds back into a full hang."
            ),
            primaryMuscles = "Latissimus Dorsi, Upper Back, Biceps",
            equipmentRequired = "Pull-up Bar"
        ),
        "req_b2" to g(
            whatToDo = "Execute strict overhand pull-ups for maximal lat width and upper-body pulling strength.",
            setup = "Grasp pull-up bar with overhand grip slightly outside shoulders and engage core lightly.",
            execution = "Pull smoothly until chin passes bar level without swinging, then control the descent.",
            formCue = "Imagine bending the bar down toward the floor with your hands as you rise.",
            mistakeToAvoid = "Rounding shoulders forward at the top or half-repping at the bottom.",
            breathing = "Exhale during the ascent; inhale as you lower under control.",
            steps = listOf(
                "Set grip slightly outside shoulder width and engage active shoulders.",
                "Drive elbows straight down toward your ribs.",
                "Hold 1 second once chin comfortably clears the bar.",
                "Lower smoothly until elbows reach straight extension."
            ),
            primaryMuscles = "Lats, Upper Back, Biceps",
            equipmentRequired = "Pull-up Bar"
        ),
        "b3" to g(
            whatToDo = "Row two dumbbells simultaneously while lying chest-down on a 30-degree incline bench.",
            setup = "Set bench at 30 degrees, lie prone with chest anchored, and let dumbbells hang vertically.",
            execution = "Row dumbbells back toward your hips while squeezing shoulder blades together, then lower slowly.",
            formCue = "Pull toward your waist pockets—not straight up to shoulders—to fire your lats.",
            mistakeToAvoid = "Using momentum off the bench or shrugging traps aggressively.",
            breathing = "Exhale as dumbbells travel up; inhale as arms extend downward.",
            steps = listOf(
                "Lie chest-down on an incline bench with arms hanging straight below shoulders.",
                "Hold dumbbells with neutral or slightly angled palms.",
                "Row elbows back beside your hips and pause briefly at peak contraction.",
                "Lower weights calmly to a complete shoulder stretch."
            ),
            primaryMuscles = "Mid Traps, Rhomboids, Lats, Rear Delts",
            equipmentRequired = "Dumbbells, Incline Bench"
        ),
        "b4" to g(
            whatToDo = "Pull your chest up toward a low bar or sturdy anchor while keeping your body straight beneath it.",
            setup = "Grip bar overhead at waist height, walk feet forward underneath, and squeeze glutes into a rigid line.",
            execution = "Row your sternum straight up to touch the bar, pause for 1 second, and lower slowly.",
            formCue = "Keep hips aligned with ankles and shoulders so your torso doesn't sag.",
            mistakeToAvoid = "Thrusting hips upward early to cheat the upper portion of the pull.",
            breathing = "Exhale as your chest pulls to the bar; inhale on the slow return.",
            steps = listOf(
                "Set under a waist-high bar with arms extended and heels planted on floor.",
                "Brace abs and glutes into a reverse-plank straight body line.",
                "Pull shoulder blades together and draw chest smoothly to the bar.",
                "Lower slowly until arms lock straight again."
            ),
            primaryMuscles = "Upper Back, Rhomboids, Lats, Biceps",
            equipmentRequired = "Low Bar / Bodyweight"
        ),
        "req_b3" to g(
            whatToDo = "Isolate the lats on a pullover machine by driving elbows down in a smooth arc.",
            setup = "Adjust seat so your shoulder joint lines up with the machine's pivot cam.",
            execution = "Press elbow pads downward through a full arc toward your torso, squeeze lats, and return slowly.",
            formCue = "Keep grip light and think purely about driving through your elbows.",
            mistakeToAvoid = "Hyperextending your lower back off the pad to chase extra overhead reach.",
            breathing = "Exhale as elbows sweep downward; inhale calmly as arms return overhead.",
            steps = listOf(
                "Align shoulders with the machine pivot point and buckle lap belt if present.",
                "Place elbows securely on the pads and lightly grasp the handles.",
                "Sweep elbows smoothly downward until arms sit beside your ribs.",
                "Control the upward arc until a comfortable lat stretch."
            ),
            primaryMuscles = "Latissimus Dorsi, Teres Major",
            equipmentRequired = "Lat Pullover Machine"
        ),
        "req_b4" to g(
            whatToDo = "Row heavy loads on a chest-supported T-bar machine to thicken the entire mid-back.",
            setup = "Step onto foot platform, rest sternum firmly on chest pad, and grab handles at full reach.",
            execution = "Pull handles into your lower chest by squeezing shoulder blades together, then return smoothly.",
            formCue = "Allow shoulder blades to protract gently at the bottom, then squeeze hard at top.",
            mistakeToAvoid = "Shortening the stretch or lifting your chest off the pad on heavy pulls.",
            breathing = "Exhale as you pull weight up; inhale on the smooth stretch downward.",
            steps = listOf(
                "Position feet comfortably and anchor your chest solidly on the pad.",
                "Choose wide or neutral handles and unrack safely.",
                "Row elbows back smoothly to squeeze mid traps, rhomboids, and lats.",
                "Extend arms completely under control without losing shoulder stability."
            ),
            primaryMuscles = "Mid Back, Lats, Rhomboids, Mid Traps",
            equipmentRequired = "Chest-Supported T-Bar Row Machine"
        ),
        "cal_fl1" to g(
            whatToDo = "Hold your body horizontal beneath a pull-up bar with knees tucked tightly to chest and arms straight.",
            setup = "Hang from bar shoulder-width, pull knees tightly to chest, and depress shoulders downward.",
            execution = "With completely locked elbows, push bar downward until your back sits horizontal to the floor and hold.",
            formCue = "Lock elbows straight—think of doing a straight-arm pulldown with your entire body.",
            mistakeToAvoid = "Bending your elbows or letting hips drop well below shoulder height.",
            breathing = "Take short, steady breaths while keeping your core and lats rigidly braced.",
            steps = listOf(
                "Grip the bar shoulder-width and pull knees tightly into your abdomen.",
                "Keep elbows completely locked while pushing the bar downward.",
                "Raise your hips until your flat back sits parallel to the ground.",
                "Hold steady for the target duration before lowering safely."
            ),
            primaryMuscles = "Lats, Core, Rear Delts",
            equipmentRequired = "Pull-up Bar"
        ),
        "cal_fl2" to g(
            whatToDo = "Hold a horizontal lever beneath the bar with knees shifted forward to form a 90-degree hip angle.",
            setup = "Enter a tuck front lever with straight arms, then slide knees away until thighs point vertically.",
            execution = "Keep shins tucked, back flat, and shoulder blades depressed horizontally for the full hold.",
            formCue = "Push elbows straight and actively drive hips upward to avoid hollowing.",
            mistakeToAvoid = "Rounding your upper back or letting knees drop down below horizontal.",
            breathing = "Breathe rhythmically through tight core bracing throughout the isometric hold.",
            steps = listOf(
                "Grip pull-up bar firmly with locked arms and depress scapulae.",
                "Pull into horizontal torso position with knees initially tucked.",
                "Open hips until thighs reach 90 degrees while keeping back flat.",
                "Hold steady for prescribed seconds before lowering cleanly."
            ),
            primaryMuscles = "Lats, Core, Rhomboids, Rear Delts",
            equipmentRequired = "Pull-up Bar"
        ),
        "cal_fl3" to g(
            whatToDo = "Hold a full horizontal front lever with legs extended straight out in a wide straddle.",
            setup = "Hang with straight arms, engage lats heavily, and pull straight up into an open straddle lever.",
            execution = "Hold body straight from shoulders to toes with wide straddled legs parallel to the floor.",
            formCue = "Squeeze glutes and point toes outward to shorten the lever arm symmetrically.",
            mistakeToAvoid = "Allowing hips to pike up or drop down while arms bend.",
            breathing = "Use controlled shallow breaths while keeping maximal lat and core tension.",
            steps = listOf(
                "Set grip shoulder-width and lock elbows completely straight.",
                "Drive bar downward with lats while extending legs into a wide V-straddle.",
                "Align ankles, hips, and shoulders along a crisp horizontal plane.",
                "Hold the static line for the target seconds and lower under control."
            ),
            primaryMuscles = "Lats, Core, Posterior Delts, Mid Back",
            equipmentRequired = "Pull-up Bar"
        ),
        "cal_mu1" to g(
            whatToDo = "Pull explosively from a dead hang until your lower chest clears or touches the pull-up bar.",
            setup = "Take an overhand shoulder-width grip in an active hang with ribs tucked lightly.",
            execution = "Drive elbows rapidly down and slightly back so your chest rises cleanly above the bar.",
            formCue = "Pull fast from the bottom rather than slowing down before your chin clears.",
            mistakeToAvoid = "Rolling shoulders forward or using excessive leg kip to reach height.",
            breathing = "Exhale sharply as you explode upward; inhale deeply at the bottom hang.",
            steps = listOf(
                "Hang evenly from the bar with core braced and legs together.",
                "Pull powerfully downward with straight wrists and fast elbows.",
                "Sweep chest up past bar level without neck craning.",
                "Lower calmly back into a full straight-arm hang."
            ),
            primaryMuscles = "Lats, Upper Back, Biceps, Forearms",
            equipmentRequired = "Pull-up Bar"
        ),
        "cal_mu2" to g(
            whatToDo = "Start in top dip position above the bar and lower slowly through the muscle-up transition to hang.",
            setup = "Jump or step onto support above a straight bar with hands in a slightly turned-in false grip.",
            execution = "Lower through the dip, lean shoulders slightly back slowly through the transition, and descend to hang.",
            formCue = "Fight the transition slowly for 3-5 seconds rather than dropping suddenly under the bar.",
            mistakeToAvoid = "Dropping violently through the elbow transition without control.",
            breathing = "Inhale bracing at the top; exhale slowly as you control the eccentric descent.",
            steps = listOf(
                "Start locked out above the bar in a straight-bar top support position.",
                "Bend elbows slowly to lower chest down toward the bar.",
                "Rotate wrists over the bar cleanly as you ease below bar height.",
                "Finish descending over 3-5 seconds into a full hang."
            ),
            primaryMuscles = "Lats, Chest, Triceps, Biceps",
            equipmentRequired = "Pull-up Bar"
        ),
        "bnd_bk1" to g(
            whatToDo = "Loop a resistance band around feet while seated and pull handles back into your midsection.",
            setup = "Sit tall on the floor with legs extended slightly bent and band anchored securely around mid-feet.",
            execution = "Row elbows back past your torso, squeeze shoulder blades together for 1 second, and return calmly.",
            formCue = "Sit upright like a statue—don't round forward when reaching toward your feet.",
            mistakeToAvoid = "Letting the band slip off your shoes or shrugging your upper traps.",
            breathing = "Exhale as you pull toward your ribs; inhale on the smooth forward reach.",
            steps = listOf(
                "Loop a sturdy resistance band evenly around both mid-feet.",
                "Sit tall with neutral lower back and chest proud.",
                "Pull hands back toward lower ribs while squeezing shoulder blades.",
                "Extend arms slowly back to starting tension."
            ),
            primaryMuscles = "Mid Back, Lats, Biceps, Rhomboids",
            equipmentRequired = "Resistance Bands"
        ),
        "db_bk1" to g(
            whatToDo = "Support one hand on a bench and row a dumbbell smoothly into your hip with the opposite arm.",
            setup = "Brace one knee and hand on a flat bench with spine level and free foot planted wide.",
            execution = "Pull the dumbbell back in an arc toward your hip pocket, squeeze lat briefly, and lower fully.",
            formCue = "Think of sawing toward your back pocket rather than pulling straight up to your shoulder.",
            mistakeToAvoid = "Twisting your torso upward violently to swing the weight.",
            breathing = "Exhale as you pull the dumbbell toward your hip; inhale as you lower.",
            steps = listOf(
                "Plant one hand and knee on a bench with a flat, stable back.",
                "Hold the dumbbell in the opposite hand letting the shoulder stretch down slightly.",
                "Drive elbow backward toward your hip without twisting torso.",
                "Lower slowly to full arm extension before next rep."
            ),
            primaryMuscles = "Lats, Mid Back, Biceps",
            equipmentRequired = "Dumbbell, Bench"
        ),
        "db_bk2" to g(
            whatToDo = "Lie across or along a bench holding one dumbbell overhead and sweep it in an arc to rib height.",
            setup = "Hold one dumbbell securely with both palms under the top plate above chest with hips low.",
            execution = "With elbows softly unlocked, lower weight backward overhead to a lat stretch, then pull back up.",
            formCue = "Keep ribs tucked slightly down so the stretch hits lats instead of arching lower back.",
            mistakeToAvoid = "Bending elbows deeply into a skull crusher or dropping hips too aggressively.",
            breathing = "Inhale deeply as weight sweeps overhead; exhale as you pull back over chest.",
            steps = listOf(
                "Lie back securely on a bench holding one dumbbell overhead with soft elbows.",
                "Brace your abs slightly so your ribcage stays stacked over hips.",
                "Lower the dumbbell back overhead until you feel a clean lat stretch.",
                "Pull through your lats to bring the weight smoothly back above chest."
            ),
            primaryMuscles = "Latissimus Dorsi, Chest, Serratus",
            equipmentRequired = "Dumbbell, Flat Bench"
        ),

        // ==================== SHOULDERS ====================
        "s_m1" to g(
            whatToDo = "Press overhead machine handles upward until arms extend comfortably above your shoulders.",
            setup = "Adjust seat so handles sit right at chin or shoulder height and plant your back firmly.",
            execution = "Press upward in a smooth line without slamming lockout, then lower back to ear level.",
            formCue = "Keep your lower back pressed lightly against the pad throughout each rep.",
            mistakeToAvoid = "Arching forward or shrugging traps aggressively at the top.",
            breathing = "Exhale as you press overhead; inhale smoothly as handles lower.",
            steps = listOf(
                "Adjust seat so handles start near shoulder or chin height.",
                "Keep chest proud, back against the pad, and wrists stacked straight.",
                "Press handles overhead without locking elbows sharply.",
                "Control the descent back down to shoulder level."
            ),
            primaryMuscles = "Anterior Delts, Lateral Delts, Triceps",
            equipmentRequired = "Shoulder Press Machine"
        ),
        "s1" to g(
            whatToDo = "Stand tall holding dumbbells at shoulder level and press them straight overhead together.",
            setup = "Set feet hip-width, brace abs and glutes tightly, and angle elbows slightly forward (30 degrees).",
            execution = "Press dumbbells overhead until biceps sit beside ears, pause, and lower smoothly to shoulders.",
            formCue = "Squeeze your glutes tight so your lower back cannot sway backward.",
            mistakeToAvoid = "Using leg bounce or flaring elbows straight sideways in line with your torso.",
            breathing = "Brace breath before pressing; exhale at top or on the descent.",
            steps = listOf(
                "Bring dumbbells up to shoulder level with elbows angled slightly forward.",
                "Lock abs and glutes firmly into a tall standing posture.",
                "Press both dumbbells straight overhead without clanking them together.",
                "Lower under 2-second control back to shoulder height."
            ),
            primaryMuscles = "Anterior & Lateral Delts, Triceps",
            equipmentRequired = "Dumbbells"
        ),
        "req_s1" to g(
            whatToDo = "Press a loaded barbell from your upper chest directly overhead with locked core stability.",
            setup = "Grip barbell just outside shoulders, rest it across upper chest/front delts, and brace glutes.",
            execution = "Tuck chin back slightly as bar passes face, press straight overhead, and bring head gently through.",
            formCue = "Push your head forward slightly through the window once the bar clears your forehead.",
            mistakeToAvoid = "Pushing the bar forward in front of your body or over-arching the lumbar spine.",
            breathing = "Take a firm breath before pressing; breathe out cleanly once locked out overhead.",
            steps = listOf(
                "Stand with feet hip-width, forearms vertical, and elbows slightly ahead of the bar.",
                "Brace core and glutes hard before initiating the drive.",
                "Press bar straight upward past your nose, then lock overhead.",
                "Lower smoothly back down to collarbone height."
            ),
            primaryMuscles = "Deltoids, Triceps, Upper Chest",
            equipmentRequired = "Barbell & Rack"
        ),
        "s_c1" to g(
            whatToDo = "Sweep a single low-pulley cable handle outward to shoulder height to load side delts continuously.",
            setup = "Set pulley near ankle height, stand sideways to machine, and hold handle with far hand.",
            execution = "Lead with your elbow to sweep arm outward to shoulder level, pause, and lower slowly.",
            formCue = "Reach your hand toward the opposite wall rather than shrugging upward.",
            mistakeToAvoid = "Swinging your hips or lifting well above shoulder height with neck muscles.",
            breathing = "Exhale as your arm raises to shoulder height; inhale on the smooth lower.",
            steps = listOf(
                "Anchor pulley at bottom setting and stand tall beside the cable column.",
                "Hold handle across in front of your thigh with a slight elbow bend.",
                "Raise arm outward in a wide arc until parallel with floor.",
                "Lower slowly back across your thigh without dropping tension."
            ),
            primaryMuscles = "Lateral Delts",
            equipmentRequired = "Cable Machine"
        ),
        "s2" to g(
            whatToDo = "Raise two dumbbells outward from your sides until arms reach parallel with the floor.",
            setup = "Hold dumbbells at sides with soft elbows and lean torso forward about 5 degrees.",
            execution = "Lift weights outward slightly forward in the scapular plane to shoulder height, then lower calmly.",
            formCue = "Lead with your elbows and imagine pouring water gently at the top.",
            mistakeToAvoid = "Heaving your torso or shrugging upper traps to swing oversized dumbbells.",
            breathing = "Exhale gently as arms lift outward; inhale on the controlled 2-second descent.",
            steps = listOf(
                "Stand tall with dumbbells resting lightly at your outer thighs.",
                "Keep soft elbows and lift arms out 30 degrees ahead of straight sideways.",
                "Stop at shoulder height and hold briefly.",
                "Lower smoothly without slamming weights into your hips."
            ),
            primaryMuscles = "Lateral / Side Delts",
            equipmentRequired = "Dumbbells"
        ),
        "req_s2" to g(
            whatToDo = "Perform strict lateral raises to maximize side deltoid width and shoulder cap definition.",
            setup = "Hold dumbbells or cables at your sides, keep chest tall, and depress shoulders downward.",
            execution = "Raise arms outward smoothly to shoulder parallel without swinging, pause, and control downward.",
            formCue = "Sweep outward toward the walls rather than straight up toward your ears.",
            mistakeToAvoid = "Bending elbows 90 degrees or using torso momentum.",
            breathing = "Exhale on the raise; inhale slowly on the 2-second lowering phase.",
            steps = listOf(
                "Set shoulders low away from ears and brace core gently.",
                "Sweep arms outward slightly ahead of your torso line.",
                "Pause for half a second once hands reach shoulder height.",
                "Lower steadily keeping tension on side delts."
            ),
            primaryMuscles = "Lateral / Side Delts",
            equipmentRequired = "Dumbbells / Cable / Lateral Raise Machine"
        ),
        "s_m2" to g(
            whatToDo = "Sit facing the Pec Deck pad and sweep handles backward in a wide arc to work rear delts.",
            setup = "Adjust seat so arms align horizontally with shoulders and rest chest gently on the front pad.",
            execution = "Drive wrists backward with straight soft arms until parallel with torso, then return calmly.",
            formCue = "Sweep wrists outward in an arc rather than pinching shoulder blades hard together.",
            mistakeToAvoid = "Pulling with your mid-back/traps instead of isolating posterior deltoids.",
            breathing = "Exhale as arms sweep backward; inhale smoothly as handles return forward.",
            steps = listOf(
                "Sit facing the backrest pad and grab handles with neutral or overhand grip.",
                "Keep elbows softly locked throughout the entire arc.",
                "Sweep arms outward and back until inline with your torso.",
                "Return steadily forward under control without letting stack slam."
            ),
            primaryMuscles = "Rear Delts, Infraspinatus",
            equipmentRequired = "Reverse Pec Deck Machine"
        ),
        "req_s3" to g(
            whatToDo = "Perform Reverse Pec Deck sweeps with steady tempo for balanced shoulder posture.",
            setup = "Rest chest against pad with handles set back at full forward reach and shoulders depressed.",
            execution = "Abduct arms backward evenly until parallel with torso, hold 1 second, and return slowly.",
            formCue = "Think of pushing the back of your hands toward the wall behind you.",
            mistakeToAvoid = "Leaning torso backward off the pad to yank heavy stacks.",
            breathing = "Exhale on the backward sweep; inhale smoothly on the forward return.",
            steps = listOf(
                "Adjust seat height so arms move cleanly level with rear shoulders.",
                "Maintain light chest contact with the front pad.",
                "Open arms wide backward until shoulder line and hold briefly.",
                "Resist the machine smoothly back to start."
            ),
            primaryMuscles = "Rear Delts, Mid Traps, Rhomboids",
            equipmentRequired = "Reverse Pec Deck"
        ),
        "s_c2" to g(
            whatToDo = "Pull a high-mounted cable rope toward your forehead while rotating hands outward.",
            setup = "Set pulley at face height, hold rope with thumbs pointing back toward you, and step back.",
            execution = "Pull rope toward forehead while flaring elbows high and pulling hands apart beside ears.",
            formCue = "Finish every rep in a double-bicep pose with thumbs pointing behind you.",
            mistakeToAvoid = "Pulling downward toward your chest or leaning backward aggressively.",
            breathing = "Exhale as rope splits beside your ears; inhale as arms extend forward.",
            steps = listOf(
                "Grasp rope ends with thumbs back and step into a stable split stance.",
                "Draw rope straight toward your nose/forehead keeping elbows high.",
                "Rotate wrists back at the end range and squeeze rear delts for 1 second.",
                "Return arms forward slowly under full control."
            ),
            primaryMuscles = "Rear Delts, Rotator Cuff, Mid Traps",
            equipmentRequired = "Cable Machine & Rope"
        ),
        "s3" to g(
            whatToDo = "Bend at hips into an inverted V and lower the crown of your head forward between your hands.",
            setup = "Start in a high pike with hips lifted high, hands shoulder-width, and heels light.",
            execution = "Lower forehead forward ahead of hands until near floor with tucked elbows, then press back up.",
            formCue = "Look at your toes and aim your head slightly ahead of your hands like a tripod.",
            mistakeToAvoid = "Dropping head straight down between wrists or flaring elbows out wide.",
            breathing = "Inhale as you lower head forward; exhale strongly as you press back into pike.",
            steps = listOf(
                "Assume a downward-V pike with hands shoulder-width apart.",
                "Tip onto toes slightly and angle torso over your shoulders.",
                "Bend elbows to lower your forehead ahead of fingertips.",
                "Press floor away back into full overhead extension."
            ),
            primaryMuscles = "Anterior Delts, Upper Chest, Triceps",
            equipmentRequired = "Bodyweight"
        ),
        "req_s4" to g(
            whatToDo = "Hold dumbbells at your sides and elevate shoulders cleanly straight up toward your ears.",
            setup = "Stand tall with feet hip-width, dumbbells resting slightly beside outer thighs, and arms straight.",
            execution = "Shrug straight upward without bending elbows, hold 1-2 seconds at peak, and lower fully.",
            formCue = "Move shoulders straight vertically up and slightly back—never roll in circles.",
            mistakeToAvoid = "Rolling shoulders forward or bending elbows to lift heavier weight.",
            breathing = "Exhale as you elevate your traps; inhale smoothly as shoulders lower.",
            steps = listOf(
                "Hold dumbbells firmly at sides with straight elbows and proud chest.",
                "Lift shoulder girdle straight upward toward ears.",
                "Hold peak trap contraction for a crisp 1-second pause.",
                "Lower weights completely until traps stretch downward."
            ),
            primaryMuscles = "Upper Trapezius, Forearms",
            equipmentRequired = "Dumbbells"
        ),
        "req_s5" to g(
            whatToDo = "Strengthen cervical flexors and extensors gently through controlled neck flexion and extension.",
            setup = "Lie with head just off a flat bench edge, holding a light towel-padded plate calmly if weighted.",
            execution = "Move chin smoothly toward chest (curls) or lift head gently backward (extensions) under zero jerk.",
            formCue = "Use light load and slow 2-second tempo—never rush or crank your neck.",
            mistakeToAvoid = "Using sudden ballistic jerks or overloading past controlled range.",
            breathing = "Breathe continuously and evenly; never hold breath while flexing neck.",
            steps = listOf(
                "Start unloaded or with a light padded weight positioned securely.",
                "Move head steadily through full comfortable flexion or extension.",
                "Pause for 1 second at the contracted position without straining.",
                "Return smoothly to neutral alignment on every rep."
            ),
            primaryMuscles = "Neck Flexors & Extensors, Upper Traps",
            equipmentRequired = "Bodyweight / Light Weight Plate"
        ),
        "cal_hs1" to g(
            whatToDo = "Hold a vertical handstand braced with heels or chest gently against a wall.",
            setup = "Place hands shoulder-width 15-30 cm from a wall with fingers spread wide for balance.",
            execution = "Push aggressively through your shoulders into the floor, squeeze ribs and glutes, and hold.",
            formCue = "Shrug tall upward into the floor and gaze steadily at a spot between your thumbs.",
            mistakeToAvoid = "Arching your lower back deeply into a banana shape instead of staying hollow.",
            breathing = "Take calm, steady breaths through your nose while maintaining full shoulder push.",
            steps = listOf(
                "Plant hands firmly near the wall and walk feet up cleanly.",
                "Push floor away maximally through straight locked elbows.",
                "Tuck ribs slightly down and squeeze glutes into a straight line.",
                "Hold for target seconds, then walk or cartwheel safely down."
            ),
            primaryMuscles = "Shoulders, Triceps, Upper Traps, Core",
            equipmentRequired = "Bodyweight & Wall"
        ),
        "bnd_sh1" to g(
            whatToDo = "Hold a light band at shoulder height in front of you and pull it wide until it touches your chest.",
            setup = "Grip band shoulder-width with palms down and arms extended straight ahead.",
            execution = "Pull hands straight outward keeping elbows locked until band taps chest, then control back.",
            formCue = "Pin shoulder blades back and keep shoulders down away from your neck.",
            mistakeToAvoid = "Arching your lower back or bending elbows into a row.",
            breathing = "Exhale as you pull the band across your chest; inhale on return.",
            steps = listOf(
                "Hold band at chest height with arms straight ahead and palms down.",
                "Keep arms straight and pull hands outward toward opposite walls.",
                "Squeeze rear delts and upper back as band touches mid-chest.",
                "Return arms calmly together without losing band tension."
            ),
            primaryMuscles = "Rear Delts, Rhomboids, Mid Traps",
            equipmentRequired = "Resistance Bands"
        ),
        "bnd_sh2" to g(
            whatToDo = "Anchor a band at eye level and pull toward your forehead while flaring hands outward.",
            setup = "Loop band around a sturdy post at face height and step back until tension begins.",
            execution = "Pull band toward your eyes while opening thumbs backward beside your ears, then return smoothly.",
            formCue = "Keep elbows lifted at shoulder height and finish with hands pointing behind you.",
            mistakeToAvoid = "Leaning backward or dropping elbows below shoulder line.",
            breathing = "Exhale on the external rotation pull; inhale smoothly as arms extend.",
            steps = listOf(
                "Anchor band securely at eye level and take an athletic stance.",
                "Pull band directly toward forehead with high elbows.",
                "Finish with forearms vertical in a double-bicep shape.",
                "Return arms straight ahead smoothly without snapping."
            ),
            primaryMuscles = "Rear Delts, Rotator Cuff, Mid Traps",
            equipmentRequired = "Resistance Bands"
        ),
        "db_sh1" to g(
            whatToDo = "Hinge at your hips near 45-70 degrees and sweep dumbbells outward to target posterior delts.",
            setup = "Hinge hips back with a flat spine, knees soft, and dumbbells hanging under chest.",
            execution = "Sweep arms wide outward with slightly bent elbows until upper arms align with torso, then lower.",
            formCue = "Raise dumbbells out in a wide wing arc rather than squeezing shoulder blades together hard.",
            mistakeToAvoid = "Swinging your upper body up and down or bending elbows into a row.",
            breathing = "Exhale as arms lift wide; inhale on the smooth 2-second descent.",
            steps = listOf(
                "Hinge hips deeply with a neutral spine and light dumbbells below chest.",
                "Keep elbows gently unlocked and thumbs angled slightly inward.",
                "Raise dumbbells outward in a wide arc until parallel with floor.",
                "Lower slowly back underneath your chest."
            ),
            primaryMuscles = "Rear Delts, Rhomboids, Mid Traps",
            equipmentRequired = "Dumbbells"
        ),
        "db_sh2" to g(
            whatToDo = "Start dumbbells in front of shoulders with palms facing you, rotate outward, and press overhead.",
            setup = "Stand or sit upright holding dumbbells at chin height with palms turned toward your face.",
            execution = "Open elbows outward as you rotate palms forward and press dumbbells straight overhead.",
            formCue = "Move in one fluid rotation upward rather than splitting it into two jerky steps.",
            mistakeToAvoid = "Arching lower back excessively or banging dumbbells together at top.",
            breathing = "Exhale as you rotate and press up; inhale as you rotate back down cleanly.",
            steps = listOf(
                "Hold dumbbells in front of your upper chest with palms facing you.",
                "Rotate wrists and flare elbows outward as you initiate the press.",
                "Press straight overhead until arms extend comfortably.",
                "Reverse the rotation smoothly back to chin height."
            ),
            primaryMuscles = "Anterior & Lateral Delts, Triceps",
            equipmentRequired = "Dumbbells"
        ),

        // ==================== QUADS & LEGS ====================
        "q1" to g(
            whatToDo = "Squat downward with a barbell across your upper back until thighs reach parallel, then drive up.",
            setup = "Set bar securely across upper traps, feet shoulder-width with toes angled slightly out, brace core.",
            execution = "Sit hips back and bend knees cleanly in line with toes to parallel, then push floor away.",
            formCue = "Keep your chest proud and drive knees outward over your pinky toes.",
            mistakeToAvoid = "Letting knees cave inward or heels lift off the ground at the bottom.",
            breathing = "Take a big brace breath before descending; exhale smoothly near the top lockout.",
            steps = listOf(
                "Unrack bar cleanly onto upper traps and step back into a shoulder-width stance.",
                "Brace your core firmly and point toes out 15-30 degrees.",
                "Descend smoothly until hip crease drops level with knees.",
                "Drive through mid-foot cleanly to stand tall."
            ),
            primaryMuscles = "Quads, Glutes, Adductors, Core",
            equipmentRequired = "Barbell & Squat Rack"
        ),
        "req_q1" to g(
            whatToDo = "Perform full-range bilateral squats to build foundational leg mass and strength.",
            setup = "Rest barbell on upper back or front rack, root feet evenly, and tighten your torso.",
            execution = "Lower under 2-second control to parallel depth, then accelerate smoothly upward.",
            formCue = "Keep weight balanced evenly across your big toe, pinky toe, and heel.",
            mistakeToAvoid = "Rising hips faster than shoulders out of the bottom hole.",
            breathing = "Brace breath tightly before dropping down; exhale smoothly finishing the ascent.",
            steps = listOf(
                "Set stance width slightly outside hips with braced trunk.",
                "Unlock hips and knees together to descend smoothly.",
                "Reach full parallel depth with upright chest and knees tracking toes.",
                "Push floor evenly away to stand back up."
            ),
            primaryMuscles = "Quads, Glutes, Lower Back",
            equipmentRequired = "Barbell & Squat Rack"
        ),
        "q_m1" to g(
            whatToDo = "Lower the weighted leg press sled toward your chest and press it smoothly back out.",
            setup = "Plant feet shoulder-width mid-platform, keep lower back and glutes flat on seat, unlock safeties.",
            execution = "Lower sled smoothly until knees pass 90 degrees without pelvis lifting, then drive through feet.",
            formCue = "Stop lowering the sled just before your lower back begins to peel off the pad.",
            mistakeToAvoid = "Slamming knees into hyperextension lockout or letting knees buckle inward.",
            breathing = "Inhale as sled comes toward you; exhale firmly as you press it away.",
            steps = listOf(
                "Set feet shoulder-width in the middle of the platform and unlatch handles.",
                "Keep hips and lower back glued flat against the backrest pad.",
                "Lower sled slowly until knees reach 90-100 degrees of bend.",
                "Press smoothly through mid-foot stopping just shy of locking knees out."
            ),
            primaryMuscles = "Quads, Glutes, Hamstrings",
            equipmentRequired = "45-Degree Leg Press"
        ),
        "q_m2" to g(
            whatToDo = "Squat deeply inside a guided hack squat sled for focused quadriceps development.",
            setup = "Press upper back flat into pads, set feet low-to-mid on footplate, and unlatch safety pins.",
            execution = "Bend knees as deep as ankle mobility allows keeping heels flat, then drive sled up.",
            formCue = "Let your knees travel cleanly forward over your toes while heels stay glued down.",
            mistakeToAvoid = "Lifting heels off the footplate or lifting lower back away from the pad.",
            breathing = "Inhale on the deep descent; exhale cleanly as you drive up.",
            steps = listOf(
                "Rest shoulders snugly under pads and plant feet shoulder-width.",
                "Release safety stops and brace your trunk against the backrest.",
                "Lower deep under control keeping heels anchored flat.",
                "Drive hard through the footplate to stand without snapping knees."
            ),
            primaryMuscles = "Quads, Glutes",
            equipmentRequired = "Hack Squat Machine"
        ),
        "q_m3" to g(
            whatToDo = "Extend your shins upward on a seated leg extension machine to isolate the quadriceps.",
            setup = "Align knee joint directly with the machine's red pivot axis and set shin pad above ankles.",
            execution = "Lift weight to full knee straightness, squeeze quads hard for 1 second, and lower slowly.",
            formCue = "Pull yourself down gently into the seat using side handles so hips don't lift.",
            mistakeToAvoid = "Kicking the pad up rapidly with momentum or letting stack crash down.",
            breathing = "Exhale as legs extend straight; inhale during the slow lowering phase.",
            steps = listOf(
                "Adjust backrest and shin pad so knees line up cleanly with the pivot.",
                "Grip side handles firmly to anchor hips into the seat.",
                "Extend legs smoothly to straight lockout and squeeze quads for 1 second.",
                "Lower slowly over 2 seconds before starting the next rep."
            ),
            primaryMuscles = "Quadriceps (Rectus Femoris)",
            equipmentRequired = "Leg Extension Machine"
        ),
        "req_q3" to g(
            whatToDo = "Execute crisp leg extensions with a top squeeze to maximize quad contraction.",
            setup = "Sit upright against backrest with pad resting comfortably across your lower shins.",
            execution = "Drive shins up smoothly to peak extension, hold briefly, and control the weight back.",
            formCue = "Keep toes pulled gently back toward shins for full quad engagement.",
            mistakeToAvoid = "Dropping the weight fast on the negative or swinging hips off seat.",
            breathing = "Exhale during upward extension; inhale calmly on the descent.",
            steps = listOf(
                "Set seat and ankle roller snugly for comfortable alignment.",
                "Anchor pelvis into the seat cushion with side handles.",
                "Extend knees smoothly until legs reach straight alignment.",
                "Control the eccentric lowering for 2 full seconds."
            ),
            primaryMuscles = "Quads",
            equipmentRequired = "Leg Extension Machine"
        ),
        "q_m4" to g(
            whatToDo = "Squat along fixed vertical Smith rails with feet set slightly forward for upright quad bias.",
            setup = "Step slightly ahead of the bar line so torso stays upright, unrack wrists, and brace core.",
            execution = "Sink hips straight down until thighs reach parallel, then push steadily back up.",
            formCue = "Keep heels anchored down and let knees track smoothly straight ahead.",
            mistakeToAvoid = "Putting feet too far behind the bar or collapsing onto toes.",
            breathing = "Inhale deeply before descending; exhale firmly on the rise.",
            steps = listOf(
                "Place bar comfortably across upper traps and plant feet slightly forward.",
                "Rotate wrists to unhook bar safely from rails.",
                "Lower smoothly to parallel thigh depth while staying upright.",
                "Press evenly through feet to stand tall."
            ),
            primaryMuscles = "Quads, Glutes",
            equipmentRequired = "Smith Machine"
        ),
        "q2" to g(
            whatToDo = "Hold one dumbbell vertically against your upper chest and squat smoothly between your knees.",
            setup = "Cup the top head of one dumbbell against your chest with elbows tucked down and feet shoulder-width.",
            execution = "Sit hips straight down until elbows graze inner thighs, keep chest tall, and stand back up.",
            formCue = "Use the front-loaded weight to keep your torso upright and sink deep.",
            mistakeToAvoid = "Letting your elbows flare or heels lift off the floor at the bottom.",
            breathing = "Inhale as you lower between your legs; exhale as you drive upward.",
            steps = listOf(
                "Hold one dumbbell upright beneath your chin with tucked elbows.",
                "Set feet just outside shoulder width with toes turned slightly out.",
                "Lower smoothly until thighs pass parallel while keeping chest lifted.",
                "Push floor away evenly to stand tall."
            ),
            primaryMuscles = "Quads, Glutes, Upper Back, Core",
            equipmentRequired = "Single Dumbbell"
        ),
        "q3" to g(
            whatToDo = "Rest your back foot on a bench behind you and squat vertically with your front leg.",
            setup = "Place shoelaces lightly on a knee-height bench behind you and hop front foot slightly forward.",
            execution = "Drop back knee smoothly toward the floor with a slight torso lean, then drive through front heel.",
            formCue = "Think of sitting your hips diagonally back toward the bench behind you.",
            mistakeToAvoid = "Stepping too narrow like a tightrope or pushing mostly off the back foot.",
            breathing = "Inhale cleanly on the descent; exhale smoothly as your front leg drives up.",
            steps = listOf(
                "Rest top of rear foot on a bench roughly two steps behind you.",
                "Keep feet hip-width apart laterally for sturdy balance.",
                "Lower straight down until back knee nears the floor.",
                "Drive hard through your front mid-foot to rise."
            ),
            primaryMuscles = "Quads, Glutes, Core",
            equipmentRequired = "Bodyweight + Bench"
        ),
        "q4" to g(
            whatToDo = "Perform smooth bodyweight squats to build lower-body endurance and clean squat mechanics.",
            setup = "Stand with feet shoulder-width apart, toes turned slightly out, and arms extended forward for balance.",
            execution = "Sink hips back and down below parallel keeping heels flat, then stand cleanly up.",
            formCue = "Keep your chest proud and knees tracking evenly over your mid-toes.",
            mistakeToAvoid = "Bouncing rapidly at the bottom or caving knees inward together.",
            breathing = "Inhale smoothly as you squat down; exhale cleanly as you stand.",
            steps = listOf(
                "Stand shoulder-width with tall posture and braced abs.",
                "Reach hands ahead as you bend knees and sit hips down.",
                "Reach full parallel depth smoothly without heels lifting.",
                "Drive through feet cleanly to full standing posture."
            ),
            primaryMuscles = "Quads, Glutes",
            equipmentRequired = "Bodyweight"
        ),
        "req_q2" to g(
            whatToDo = "Step forward cleanly into alternating lunges, lowering your back knee toward the floor on each stride.",
            setup = "Stand tall holding dumbbells at your sides or hands on hips with space ahead.",
            execution = "Step forward on hip-width tracks, drop back knee gently near floor, and push forward into next step.",
            formCue = "Step out slightly wide—never cross one foot straight in front of the other.",
            mistakeToAvoid = "Crashing your back knee onto the floor or letting front heel pop up.",
            breathing = "Inhale as you step down into the lunge; exhale as you drive up to stand.",
            steps = listOf(
                "Stand tall with dumbbells hanging smoothly at your sides.",
                "Take a controlled forward stride landing softly through mid-foot.",
                "Lower until both knees approach 90 degrees with upright trunk.",
                "Drive through front leg cleanly to bring feet together or step ahead."
            ),
            primaryMuscles = "Quads, Glutes, Hamstrings",
            equipmentRequired = "Dumbbells or Bodyweight"
        ),
        "cal_ps1" to g(
            whatToDo = "Perform single-leg squats down to a box or bench so you master pistol balance and depth.",
            setup = "Stand on one leg just ahead of a knee-height box with opposite leg held straight out in front.",
            execution = "Sit hips back smoothly to tap the box lightly without plopping, then drive up on one leg.",
            formCue = "Reach arms straight forward for counterbalance and keep your planted heel flat.",
            mistakeToAvoid = "Dropping heavily onto the box or letting your standing knee dive inward.",
            breathing = "Inhale as you lower to tap the box; exhale forcefully driving back up.",
            steps = listOf(
                "Stand on one leg a few inches in front of a sturdy bench or box.",
                "Extend your opposite leg straight ahead and reach arms forward.",
                "Sit back slowly until glutes lightly kiss the bench without relaxing.",
                "Press powerfully through your planted foot to stand up cleanly."
            ),
            primaryMuscles = "Quads, Glutes, Ankle & Core Stabilizers",
            equipmentRequired = "Bodyweight + Box/Bench"
        ),
        "cal_ps2" to g(
            whatToDo = "Squat all the way down on one leg with the opposite leg held straight out in front of the floor.",
            setup = "Stand balanced on one foot, lift free leg forward, and reach arms straight out ahead.",
            execution = "Descend steadily as deep as possible keeping planted heel grounded, then drive straight back up.",
            formCue = "Keep toes and knees pointing straight ahead and drive strongly out of the bottom.",
            mistakeToAvoid = "Letting your heel peel off the ground or bouncing off the calf at bottom.",
            breathing = "Inhale bracing into the descent; exhale sharply as you drive up.",
            steps = listOf(
                "Balance on one foot with opposite leg lifted straight forward.",
                "Reach arms straight out ahead for counterweight.",
                "Lower all the way into full single-leg squat depth under control.",
                "Drive evenly through your planted foot back to standing."
            ),
            primaryMuscles = "Quads, Glutes, Core",
            equipmentRequired = "Bodyweight"
        ),
        "hm_qd1" to g(
            whatToDo = "Press your back flat against a wall and hold a seated 90-degree thigh angle statically.",
            setup = "Lean smoothly against a wall and walk feet forward about two feet shoulder-width apart.",
            execution = "Slide hips down until knees form 90 degrees over ankles, press palms off thighs, and hold.",
            formCue = "Keep heels pressed firmly into the floor and lower back gently touching the wall.",
            mistakeToAvoid = "Resting hands on your knees or sliding hips too high above parallel.",
            breathing = "Breathe slowly and calmly—avoid holding your breath while quads burn.",
            steps = listOf(
                "Stand against a smooth wall and step feet forward shoulder-width.",
                "Slide torso down until thighs sit parallel with the floor.",
                "Hang arms beside you or cross over chest—keep hands off thighs.",
                "Hold the isometric 90-degree angle for the full prescribed timer."
            ),
            primaryMuscles = "Quadriceps, Glutes",
            equipmentRequired = "Bodyweight & Wall"
        ),
        "db_qd1" to g(
            whatToDo = "Hold dumbbells at your sides while executing rear-foot-elevated Bulgarian split squats.",
            setup = "Hang two dumbbells along your hips, rest rear laces on a bench, and plant front foot firmly.",
            execution = "Lower straight down keeping front shin stable until back knee nears floor, then drive up.",
            formCue = "Lean torso forward slightly 10-15 degrees if targeting glutes, or upright for quads.",
            mistakeToAvoid = "Bouncing off the back foot or letting dumbbells swing around your thighs.",
            breathing = "Inhale smoothly during the lowering phase; exhale hard as you press up.",
            steps = listOf(
                "Hold dumbbells at sides and place top of rear foot on a bench behind.",
                "Find stable hip-width spacing with your front foot.",
                "Lower steadily until front thigh reaches parallel or slightly deeper.",
                "Drive upward firmly through your front mid-foot."
            ),
            primaryMuscles = "Quads, Glutes, Hamstrings",
            equipmentRequired = "Dumbbells & Bench"
        ),
        "db_qd2" to g(
            whatToDo = "Step backward smoothly into a reverse lunge holding dumbbells to protect knees and load quads.",
            setup = "Stand tall holding dumbbells at your sides with space directly behind you.",
            execution = "Step one foot backward softly onto its ball, drop back knee toward floor, and drive up via front heel.",
            formCue = "Keep your weight centered over the front foot—your back foot only stabilizes.",
            mistakeToAvoid = "Over-striding too far back or slamming your back knee into the ground.",
            breathing = "Inhale as you step back and lower; exhale as you pull back forward.",
            steps = listOf(
                "Stand upright holding dumbbells comfortably along your hips.",
                "Step one leg smoothly backward until both knees reach 90 degrees.",
                "Keep your front shin steady and chest tall.",
                "Drive hard through the front heel to return to standing."
            ),
            primaryMuscles = "Quads, Glutes, Hamstrings",
            equipmentRequired = "Dumbbells"
        ),

        // ==================== HAMSTRINGS & GLUTES ====================
        "h1" to g(
            whatToDo = "Push hips straight backward holding a barbell with soft knees until torso hinges near horizontal.",
            setup = "Hold barbell at thighs with shoulder-width grip, chest proud, and knees softly bent 15 degrees.",
            execution = "Slide bar right down your thighs to mid-shin by pushing hips back, then drive hips forward.",
            formCue = "Stop lowering the moment your hips stop moving backward—do not round your spine for depth.",
            mistakeToAvoid = "Rounding your lower back or bending knees progressively into a squat.",
            breathing = "Inhale as hips push back smoothly; exhale firmly as hips snap forward.",
            steps = listOf(
                "Stand hip-width holding the barbell against your upper thighs.",
                "Unlock knees slightly and lock that exact knee angle.",
                "Push hips straight back as the bar shaves down to mid-shin.",
                "Squeeze hamstrings and glutes to drive hips upright again."
            ),
            primaryMuscles = "Hamstrings, Glutes, Lower Back",
            equipmentRequired = "Barbell"
        ),
        "req_h1" to g(
            whatToDo = "Perform controlled Romanian deadlifts to stretch and strengthen hamstrings under hip hinge load.",
            setup = "Root feet hip-width, retract shoulder blades slightly, and hold bar or dumbbells snug to thighs.",
            execution = "Hinge hips back smoothly for 2-3 seconds until deep hamstring tension, then drive hips through.",
            formCue = "Imagine closing a car door behind you with your glutes on every rep.",
            mistakeToAvoid = "Letting weights drift forward away from your legs or craning neck upward.",
            breathing = "Inhale while hinging down; exhale cleanly as you stand upright.",
            steps = listOf(
                "Brace core with a neutral spine and soft knees.",
                "Push hips backward slowly keeping bar brushing thighs and shins.",
                "Pause cleanly at maximum hamstring stretch right below knees.",
                "Drive hips forward briskly to stand tall without leaning back."
            ),
            primaryMuscles = "Hamstrings, Glutes, Spinal Erectors",
            equipmentRequired = "Barbell or Dumbbells"
        ),
        "h_m1" to g(
            whatToDo = "Sit upright in the leg curl machine and flex your knees downward against the ankle pad.",
            setup = "Align knees with machine pivot point, secure thigh lap pad snug, and set ankle roller behind heels.",
            execution = "Curl heels smoothly back beneath seat, pause for 1 second, and raise shins slowly up.",
            formCue = "Lock the lap pad down tightly so hips cannot rotate while curling.",
            mistakeToAvoid = "Letting the ankle roller slam upward quickly or arching lower back.",
            breathing = "Exhale as you curl heels down beneath seat; inhale on the smooth upward return.",
            steps = listOf(
                "Sit flat and lock the thigh restraint snugly just above knees.",
                "Align knees with the pivot and place ankles atop the lower pad.",
                "Pull heels smoothly downward toward the underside of the chair.",
                "Control the upward extension completely for a full stretch."
            ),
            primaryMuscles = "Hamstrings, Calves",
            equipmentRequired = "Seated Leg Curl Machine"
        ),
        "req_h2" to g(
            whatToDo = "Use seated leg curls to load hamstrings at 90-degree hip flexion for peak hypertrophy.",
            setup = "Adjust backrest so knees align cleanly with the pivot axis and lock lap pad snug.",
            execution = "Flex knees smoothly downward through full range, hold 1 second, and resist the return.",
            formCue = "Keep toes pointing neutrally forward and press lower back gently into seat.",
            mistakeToAvoid = "Using sudden jerking kicks to start each rep from the top.",
            breathing = "Exhale as you curl under; inhale slowly as legs straighten.",
            steps = listOf(
                "Set seat depth and clamp the thigh pad snugly.",
                "Start with legs extended and light hamstring stretch.",
                "Curl heels calmly all the way under the seat and pause.",
                "Return smoothly to straight position over 2 seconds."
            ),
            primaryMuscles = "Hamstrings",
            equipmentRequired = "Seated Leg Curl Machine"
        ),
        "h_m2" to g(
            whatToDo = "Lie prone on the bench and curl both heels upward toward your glutes.",
            setup = "Align knees just past the bench edge, hook roller above heels, and grip front handles.",
            execution = "Curl roller smoothly toward glutes while keeping hips pinned flat, then lower slowly.",
            formCue = "Press your hips firmly downward into the pad—do not let your butt lift upward.",
            mistakeToAvoid = "Arching your lower back or swinging torso as heels approach glutes.",
            breathing = "Exhale during the curl upward; inhale smoothly as legs lower back down.",
            steps = listOf(
                "Lie face down with kneecaps sitting just past the bench pad edge.",
                "Pin hips downward cleanly against the pad and hold front handles.",
                "Curl heels smoothly toward glutes for 1-second peak tension.",
                "Lower weight calmly without dropping your feet rapidly."
            ),
            primaryMuscles = "Hamstrings",
            equipmentRequired = "Lying Leg Curl Machine"
        ),
        "h2" to g(
            whatToDo = "Drive hips upward off the floor using one planted heel while holding the other leg lifted.",
            setup = "Lie back with knees bent, plant one foot firmly near glutes, and hug or extend opposite leg.",
            execution = "Push through your planted heel to lift hips into a straight line, squeeze glute, and lower.",
            formCue = "Keep both hip bones level—don't let the raised side drop down.",
            mistakeToAvoid = "Hyperextending your lower back at the top instead of squeezing glutes.",
            breathing = "Exhale as you drive hips upward; inhale as hips lower smoothly.",
            steps = listOf(
                "Lie flat on your back with one foot planted and opposite knee lifted.",
                "Tuck ribs lightly down and brace core.",
                "Drive through the planted heel until hips align with knee and shoulder.",
                "Lower slowly just shy of resting fully on the floor."
            ),
            primaryMuscles = "Glutes, Hamstrings",
            equipmentRequired = "Bodyweight"
        ),
        "h3" to g(
            whatToDo = "Hold a dumbbell across your chest or shoulders and hinge hips smoothly back into a good morning.",
            setup = "Stand hip-width holding one dumbbell tight to upper chest, knees softly unlocked, and flat back.",
            execution = "Push hips steadily back keeping shins nearly vertical until torso nears parallel, then stand tall.",
            formCue = "Keep the dumbbell hugged tight to your chest and neck aligned with spine.",
            mistakeToAvoid = "Rounding your upper back or dipping forward into your toes.",
            breathing = "Inhale during the hip hinge back; exhale firmly driving hips upright.",
            steps = listOf(
                "Hug a dumbbell firmly against your upper chest or shoulders.",
                "Keep a gentle 15-degree bend in knees and brace your trunk.",
                "Push hips straight back until deep hamstring tension stops descent.",
                "Drive glutes forward cleanly to stand straight."
            ),
            primaryMuscles = "Hamstrings, Glutes, Erectors",
            equipmentRequired = "Dumbbell"
        ),
        "req_h3" to g(
            whatToDo = "Support upper back on the pad and drive the belt or barbell upward via hip extension.",
            setup = "Rest shoulder blades squarely on the pad, buckle belt tight, and set shins vertical at top.",
            execution = "Drive hips upward until torso and thighs align flat horizontally, pause 1 second, and lower.",
            formCue = "Keep your chin tucked slightly forward and stare down your torso—avoid craning back.",
            mistakeToAvoid = "Over-arching your lower back at top instead of finishing with posterior pelvic tilt.",
            breathing = "Exhale sharply as hips lock horizontally; inhale smoothly on the descent.",
            steps = listOf(
                "Set shoulder blades on the bench pad and place feet flat shoulder-width.",
                "Secure lap belt or barbell smoothly across your hip crease.",
                "Drive evenly through heels until torso forms a flat tabletop.",
                "Lower hips steadily under control before the next rep."
            ),
            primaryMuscles = "Gluteus Maximus, Hamstrings",
            equipmentRequired = "Glute Drive Machine or Barbell & Bench"
        ),
        "cal_nc1" to g(
            whatToDo = "Anchor ankles and lower your straight torso slowly toward the floor using pure hamstring braking.",
            setup = "Kneel on a thick mat with ankles hooked safely under a bar, strap, or heavy anchor.",
            execution = "Keep hips fully extended and lean forward as slowly as possible until catching softly with hands.",
            formCue = "Hold a straight line from knees to head—do not fold backward at the waist.",
            mistakeToAvoid = "Bending at the hips into a hinge instead of keeping glutes squeezed.",
            breathing = "Inhale steadily bracing your core while resisting gravity all the way down.",
            steps = listOf(
                "Kneel on a padded mat with ankles clamped firmly under an anchor.",
                "Squeeze glutes and brace abs so hips stay completely straight.",
                "Lower your chest smoothly forward as slowly as possible for 3-5 seconds.",
                "Catch yourself gently with hands on floor and press lightly back up."
            ),
            primaryMuscles = "Hamstrings, Glutes",
            equipmentRequired = "Bodyweight + Ankle Anchor"
        ),
        "hm_gl1" to g(
            whatToDo = "Lie flat with bent knees and lift hips upward into full extension by squeezing glutes.",
            setup = "Lie flat on back with feet planted flat roughly a foot from hips, arms resting beside torso.",
            execution = "Push smoothly through heels until knees, hips, and shoulders align, pause 1 second, and lower.",
            formCue = "Tuck tailbone gently upward at peak instead of flaring your ribs.",
            mistakeToAvoid = "Pushing through toes or arching lower spine excessively at the top.",
            breathing = "Exhale as hips lift to the top; inhale cleanly as you lower.",
            steps = listOf(
                "Lie back with feet hip-width apart and shins near vertical at top.",
                "Brace abs lightly so lower back stays neutral.",
                "Drive evenly through heels and squeeze glutes tightly for 1 second.",
                "Lower smoothly back down to tap the floor."
            ),
            primaryMuscles = "Glutes, Hamstrings",
            equipmentRequired = "Bodyweight"
        ),
        "hm_gl2" to g(
            whatToDo = "Wear a mini-band right above knees and drive upward into a glute bridge while pushing knees out.",
            setup = "Loop resistance band just above knees, lie back with bent knees, and apply gentle outward tension.",
            execution = "Press knees gently outward into the band as you bridge hips up, pause, and lower slowly.",
            formCue = "Maintain outward knee pressure during both the lift and the lowering phase.",
            mistakeToAvoid = "Letting knees collapse inward under band pull or rushing reps.",
            breathing = "Exhale strongly at top glute squeeze; inhale smoothly on descent.",
            steps = listOf(
                "Place band just above knees and plant feet hip-width apart.",
                "Push knees outward lightly until shins stay vertical.",
                "Drive through heels to extend hips fully and hold 1 second.",
                "Lower slowly while keeping steady outward knee tension."
            ),
            primaryMuscles = "Glute Max, Glute Medius, Hamstrings",
            equipmentRequired = "Resistance Band"
        ),
        "hm_gl3" to g(
            whatToDo = "Rest upper back against a sturdy couch or bench and thrust hips upward through full range.",
            setup = "Anchor shoulder blades against bench edge, set feet flat so shins stay vertical at top.",
            execution = "Drop hips low toward floor keeping chin tucked, then thrust powerfully to flat tabletop.",
            formCue = "Move torso and hips as one hinged unit pivoted on your mid-back.",
            mistakeToAvoid = "Letting feet slip forward into a hamstring-dominant push or arching neck.",
            breathing = "Exhale cleanly at top hip lockout; inhale as hips lower steadily.",
            steps = listOf(
                "Position shoulder blades cleanly along a bench edge with feet planted.",
                "Keep eyes forward over your knees to lock ribcage down.",
                "Lower hips evenly toward the ground for a full stretch.",
                "Drive straight up through heels and hold top glute squeeze for 1 second."
            ),
            primaryMuscles = "Glutes, Hamstrings",
            equipmentRequired = "Bodyweight + Bench"
        ),
        "hm_gl4" to g(
            whatToDo = "Elevate upper back on a bench and thrust hips up strongly on one planted leg.",
            setup = "Anchor mid-back on bench, plant one foot firmly, and lift opposite knee toward chest.",
            execution = "Drive single heel through floor until pelvis aligns flat, hold 1 second, and lower steadily.",
            formCue = "Keep your pelvis level without tilting toward the floating side.",
            mistakeToAvoid = "Letting your planted foot creep too far out or twisting hips sideways.",
            breathing = "Exhale at the top squeeze; inhale cleanly on the slow descent.",
            steps = listOf(
                "Set shoulder blades onto bench and tuck one knee up cleanly.",
                "Root the planted foot flat directly under your knee at top.",
                "Thrust through heel until hips reach full extension.",
                "Lower smoothly under control before repeating."
            ),
            primaryMuscles = "Glutes, Hamstrings, Core",
            equipmentRequired = "Bodyweight + Bench"
        ),
        "db_hm1" to g(
            whatToDo = "Slide two dumbbells straight down the front of your thighs via a strict hip hinge.",
            setup = "Hold dumbbells beside thighs with neutral or overhand grip, soft knees, and proud chest.",
            execution = "Push hips straight back as dumbbells lower to mid-shin, then drive hips smoothly upright.",
            formCue = "Keep dumbbells grazing close to your shins throughout the entire rep.",
            mistakeToAvoid = "Rounding your upper back to touch the floor with weights.",
            breathing = "Inhale on the slow hinge downward; exhale as glutes lock hips forward.",
            steps = listOf(
                "Stand hip-width with soft locked knees and dumbbells at thighs.",
                "Push hips backward smoothly keeping your spine completely flat.",
                "Pause once hamstrings feel fully stretched around mid-shin.",
                "Squeeze glutes to stand back upright without leaning back."
            ),
            primaryMuscles = "Hamstrings, Glutes, Lower Back",
            equipmentRequired = "Dumbbells"
        ),

        // ==================== CALVES ====================
        "ca_m1" to g(
            whatToDo = "Stand under shoulder pads with balls of feet on the edge and drive high onto your big toes.",
            setup = "Set shoulder pads snug, align balls of feet cleanly on block, and keep knees virtually straight.",
            execution = "Lower heels for a deep 2-second stretch, then press maximally up and hold 1 second at peak.",
            formCue = "Pause cleanly at the very bottom stretch to kill Achilles tendon bounce.",
            mistakeToAvoid = "Bouncing quickly through half-reps at the bottom or bending knees.",
            breathing = "Inhale during the deep heel drop; exhale smoothly pressing high onto toes.",
            steps = listOf(
                "Set balls of feet securely on the machine block with heels hanging free.",
                "Lower heels smoothly into a full ankle stretch and pause 1 second.",
                "Press powerfully straight up onto the balls of your big toes.",
                "Hold top contraction for 1 full second before lowering slowly."
            ),
            primaryMuscles = "Gastrocnemius, Soleus",
            equipmentRequired = "Standing Calf Raise Machine"
        ),
        "req_cl1" to g(
            whatToDo = "Perform full-range standing calf raises pausing cleanly in both stretched and peak positions.",
            setup = "Position balls of feet firmly on block or step with straight knees and braced core.",
            execution = "Drop heels low slowly, pause briefly at full stretch, then rise straight up onto big toes.",
            formCue = "Push evenly through the ball of your big toe rather than rolling ankles outward.",
            mistakeToAvoid = "Rushing reps with elastic ankle rebound.",
            breathing = "Inhale on the controlled lowering stretch; exhale pressing up high.",
            steps = listOf(
                "Align balls of feet comfortably on the edge of the step or plate.",
                "Descend slowly for 2 seconds into a deep calf stretch.",
                "Drive upward smoothly onto your highest tiptoe.",
                "Hold peak squeeze for 1 second before repeating."
            ),
            primaryMuscles = "Gastrocnemius, Soleus",
            equipmentRequired = "Calf Raise Machine / Step / Smith Machine"
        ),
        "ca1" to g(
            whatToDo = "Use a stair step or ledge to lower heels deeply and raise tall onto your toes using bodyweight.",
            setup = "Stand with balls of feet on a sturdy step and rest one hand lightly on a wall for balance.",
            execution = "Lower heels smoothly below step level, pause 1 second, then drive high onto big toes.",
            formCue = "Move deliberately—2 seconds down, 1-second pause at bottom, 1 second squeeze at top.",
            mistakeToAvoid = "Holding tight to the wall to assist upward with your arms.",
            breathing = "Inhale as heels sink below the step; exhale driving upward.",
            steps = listOf(
                "Stand upright with balls of feet on a stable step or block.",
                "Lower heels slowly until a comfortable stretch builds.",
                "Pause for 1 second at bottom without bouncing.",
                "Press straight upward as high as possible onto your toes."
            ),
            primaryMuscles = "Calves (Gastrocnemius & Soleus)",
            equipmentRequired = "Step or Ledge"
        ),
        "ca2" to g(
            whatToDo = "Hold a dumbbell at your side while performing single-leg or bilateral standing calf raises on a block.",
            setup = "Stand on a step or plate holding one dumbbell with free hand steadied lightly on a rail.",
            execution = "Lower heel deeply into stretch, pause briefly, and press smoothly upward onto the big toe.",
            formCue = "Keep your knee softly straight so the upper gastrocnemius works maximally.",
            mistakeToAvoid = "Rolling weight onto your outer pinky toes or rushing the bottom stretch.",
            breathing = "Inhale dropping into stretch; exhale cleanly rising onto tiptoes.",
            steps = listOf(
                "Stand with ball of foot on an elevated edge holding a dumbbell.",
                "Steady torso upright using a light touch on a wall or rack.",
                "Sink heel deep downward for 2 seconds and pause briefly.",
                "Drive high onto the ball of your foot and hold peak contraction."
            ),
            primaryMuscles = "Gastrocnemius, Soleus",
            equipmentRequired = "Dumbbell & Step"
        ),
        "ca3" to g(
            whatToDo = "Sit upright on a bench with dumbbells resting atop your lower thighs near knees and raise heels.",
            setup = "Sit with knees bent at 90 degrees, balls of feet on a weight plate, and dumbbells perched near knees.",
            execution = "Drop heels downward into a deep stretch, pause, then drive high onto toes.",
            formCue = "Keep knees bent at 90 degrees throughout to isolate the deep soleus muscle.",
            mistakeToAvoid = "Leaning back and lifting weights with your hands instead of calves.",
            breathing = "Inhale on the deep heel drop; exhale pressing heels upward.",
            steps = listOf(
                "Place balls of feet atop a raised block while seated upright.",
                "Rest dumbbells steadily atop lower thighs right above knees.",
                "Lower heels slowly below plate level into full stretch.",
                "Press upward firmly through toes and pause at top."
            ),
            primaryMuscles = "Soleus (Lower Calf)",
            equipmentRequired = "Dumbbells, Bench & Weight Plate"
        ),
        "ca4" to g(
            whatToDo = "Raise yourself smoothly on one foot at a time on a step for unilateral calf strength and balance.",
            setup = "Balance on one foot's forefoot on a step with opposite ankle tucked cleanly behind.",
            execution = "Drop heel deeply below step height, pause for 1 second, and drive straight up to full height.",
            formCue = "Stay tall through your hip—don't hike one hip sideways for leverage.",
            mistakeToAvoid = "Cutting bottom range short or letting ankle wobble outward.",
            breathing = "Inhale slowly descending; exhale firmly pressing upward.",
            steps = listOf(
                "Step onto a block on one foot with light fingertip wall balance.",
                "Lower heel below step height calmly over 2 seconds.",
                "Pause for 1 second at bottom to remove elastic bounce.",
                "Press straight upward to full tiptoe and hold 1 second."
            ),
            primaryMuscles = "Gastrocnemius, Soleus, Ankle Stabilizers",
            equipmentRequired = "Bodyweight & Step"
        ),
        "ca5" to g(
            whatToDo = "Place balls of feet on the bottom edge of the leg press sled and press with your ankles.",
            setup = "Sit securely in the leg press, set feet low so heels hang clear of the sled, and keep knees slightly soft.",
            execution = "Allow toes to flex smoothly toward shins for a full stretch, then push sled away with ankles.",
            formCue = "Keep your knees slightly unlocked but stationary—never bend and extend legs to help.",
            mistakeToAvoid = "Letting feet slip off the bottom edge of the sled.",
            breathing = "Inhale as sled stretches ankles back; exhale pressing through toes.",
            steps = listOf(
                "Position balls of feet firmly on bottom lip of sled shoulder-width apart.",
                "Engage safety stops or keep hands gripping rails with soft locked knees.",
                "Let weight gently push toes back into deep ankle stretch.",
                "Push sled smoothly outward through your toes and squeeze."
            ),
            primaryMuscles = "Gastrocnemius, Soleus",
            equipmentRequired = "Leg Press Machine"
        ),

        // ==================== TRICEPS ====================
        "t_c1" to g(
            whatToDo = "Pin elbows tight to your ribs and push a high cable rope or bar smoothly down to lockout.",
            setup = "Set pulley high, grab rope ends, lean slightly forward 10 degrees, and lock elbows against sides.",
            execution = "Press rope downward and pull ends slightly apart beside thighs at lockout, then raise calmly.",
            formCue = "Keep your upper arms frozen like hinges—only forearms move.",
            mistakeToAvoid = "Hunching chest forward over the bar or letting elbows swing behind/ahead of ribs.",
            breathing = "Exhale as arms press down straight; inhale returning to 90 degrees.",
            steps = listOf(
                "Grip rope at chest height with elbows pinned beside your ribcage.",
                "Press downward smoothly until arms fully straighten.",
                "Split rope ends gently outward at bottom and squeeze triceps for 1 second.",
                "Return calmly upward until forearms reach just past parallel."
            ),
            primaryMuscles = "Triceps (Lateral & Medial Heads)",
            equipmentRequired = "Cable Machine & Rope"
        ),
        "t_c2" to g(
            whatToDo = "Face away from a high pulley and press the rope forward overhead from behind your head.",
            setup = "Step forward into a staggered stance with elbows pointing high beside your head.",
            execution = "Keep upper arms fixed and extend elbows forward/upward to lockout, then return deeply behind head.",
            formCue = "Let the rope stretch deeply behind your neck on every rep without arching lower back.",
            mistakeToAvoid = "Flaring elbows wide out to the sides or rocking torso to heave reps.",
            breathing = "Exhale as arms extend overhead; inhale during the deep return stretch.",
            steps = listOf(
                "Hold rope over shoulders facing away from the pulley in a split stance.",
                "Lean torso gently forward 30 degrees with abs braced.",
                "Extend elbows cleanly overhead until arms reach straight.",
                "Bend only at elbows to lower hands deep behind your head."
            ),
            primaryMuscles = "Triceps Long Head",
            equipmentRequired = "Cable Machine & Rope"
        ),
        "req_t1" to g(
            whatToDo = "Extend a cable rope overhead to train the triceps long head in its maximally lengthened position.",
            setup = "Anchor a cable near waist or top height, face away in a split stance, and keep elbows beside ears.",
            execution = "Extend arms smoothly forward and upward to full lockout, pause, then sink deeply behind head.",
            formCue = "Keep your ribs tucked downward so your back stays flat as arms bend behind.",
            mistakeToAvoid = "Dropping elbows downward during the extension or swinging shoulders.",
            breathing = "Exhale pushing forward to lockout; inhale stretching deeply behind.",
            steps = listOf(
                "Take a staggered stance facing away from the cable stack.",
                "Pin upper arms upward beside your head with elbows pointing ahead.",
                "Extend forearms cleanly forward until triceps lock out tight.",
                "Control the eccentric deeply behind head for 2 seconds."
            ),
            primaryMuscles = "Triceps Long Head",
            equipmentRequired = "Cable Machine"
        ),
        "t1" to g(
            whatToDo = "Hold one dumbbell overhead with both hands and lower it smoothly behind your head before pressing up.",
            setup = "Sit upright or stand braced, cup the top plate of one dumbbell overhead with elbows tucked.",
            execution = "Bend only elbows to sink weight deep behind your neck, then squeeze triceps to extend upward.",
            formCue = "Keep elbows pointing roughly forward rather than flaring wide sideways.",
            mistakeToAvoid = "Arching your lower spine forward as the dumbbell drops behind you.",
            breathing = "Inhale smoothly as weight lowers behind head; exhale pressing straight up.",
            steps = listOf(
                "Support one dumbbell vertically overhead with both palms inside top plate.",
                "Brace abs firmly and keep elbows narrow beside temples.",
                "Lower dumbbell behind head until a deep tricep stretch.",
                "Drive hands straight back overhead to smooth lockout."
            ),
            primaryMuscles = "Triceps Long Head",
            equipmentRequired = "Dumbbell"
        ),
        "t2" to g(
            whatToDo = "Lie flat holding a bar or dumbbells above your chest, bend elbows toward forehead, and press up.",
            setup = "Lie back flat, angle straight arms 10-15 degrees back behind shoulders, and brace ribs down.",
            execution = "Hinge purely at elbows to lower weight near hairline/crown, then extend cleanly back up.",
            formCue = "Keep upper arms angled slightly back so triceps stay loaded even at top lockout.",
            mistakeToAvoid = "Dropping elbows forward toward knees or flaring elbows wide.",
            breathing = "Inhale as weight lowers calmly to crown; exhale pressing back to start.",
            steps = listOf(
                "Lie back with feet flat and angle arms slightly back behind vertical.",
                "Bend only at elbows to lower bar smoothly past your forehead.",
                "Pause cleanly at deep tricep stretch without bouncing.",
                "Extend elbows forcefully back along the exact same arc."
            ),
            primaryMuscles = "Triceps Long & Medial Heads",
            equipmentRequired = "EZ-Bar, Barbell, or Dumbbells + Flat Bench"
        ),
        "t3" to g(
            whatToDo = "Press a barbell on a flat bench with hands set shoulder-width to overload triceps with compound weight.",
            setup = "Grip barbell straight over shoulders (not overly narrow), plant feet, and tuck elbows close to ribs.",
            execution = "Lower bar steadily down to lower chest keeping forearms vertical, then drive powerfully up.",
            formCue = "Keep wrists stacked straight and slide elbows gently along your ribcage.",
            mistakeToAvoid = "Putting hands inches apart which twists wrists without helping triceps.",
            breathing = "Inhale lowering to lower sternum; exhale firmly pressing bar up.",
            steps = listOf(
                "Grip barbell shoulder-width apart and retract shoulder blades gently.",
                "Lower bar toward your lower sternum keeping elbows tucked 30 degrees.",
                "Touch lower chest lightly without bouncing.",
                "Press powerfully straight back up squeezing triceps at lockout."
            ),
            primaryMuscles = "Triceps, Inner Chest, Front Delts",
            equipmentRequired = "Barbell & Flat Bench"
        ),
        "t_m1" to g(
            whatToDo = "Press parallel dip handles straight down on an assisted or seated dip machine keeping torso upright.",
            setup = "Set seat or counterweight, keep torso straight upright, and place elbows pointing backward.",
            execution = "Lower smoothly until upper arms reach parallel with floor, then push straight to lockout.",
            formCue = "Keep shoulders pressed down away from ears even at the bottom stretch.",
            mistakeToAvoid = "Hunching shoulders upward or dropping deeper than comfortable shoulder mobility.",
            breathing = "Inhale on the descent; exhale cleanly pressing handles downward.",
            steps = listOf(
                "Take a neutral grip on dip handles with torso held upright.",
                "Depress shoulder blades away from ears before bending elbows.",
                "Lower smoothly until elbows reach roughly 90 degrees.",
                "Press back to clean straight-arm lockout."
            ),
            primaryMuscles = "Triceps, Lower Chest",
            equipmentRequired = "Dip Machine / Assisted Dip"
        ),
        "a2" to g(
            whatToDo = "Rest hands on a sturdy bench edge behind you, lower hips straight down, and press back up.",
            setup = "Place palms on bench edge facing forward just outside hips and walk feet out ahead.",
            execution = "Keep back close to bench as elbows bend backward to 90 degrees, then push through heels of hands.",
            formCue = "Keep shoulders pulled back and chest lifted—never let shoulders roll forward.",
            mistakeToAvoid = "Dropping far too deep past 90 degrees where front shoulders strain.",
            breathing = "Inhale smoothly descending along the bench; exhale pressing upright.",
            steps = listOf(
                "Grip bench edge behind hips and extend legs slightly ahead.",
                "Keep upper back grazing close to the bench edge.",
                "Bend elbows straight backward until upper arms hit parallel.",
                "Push cleanly through palms back to straight arm support."
            ),
            primaryMuscles = "Triceps, Front Delts",
            equipmentRequired = "Bench or Sturdy Chair"
        ),
        "t4" to g(
            whatToDo = "Assume a push-up plank with thumbs and index fingers touching closely beneath your sternum.",
            setup = "Set hands close beneath chest forming a diamond or narrow triangle and tighten abs and glutes.",
            execution = "Lower chest smoothly toward your hands keeping elbows tucked close to ribs, then push up.",
            formCue = "Tuck elbows toward your ribcage—don't let them point straight sideways.",
            mistakeToAvoid = "Sagging lower hips or spreading elbows out to 90 degrees.",
            breathing = "Inhale cleanly on the descent; exhale sharply as you press away.",
            steps = listOf(
                "Set hands tightly beneath your chest inside shoulder width.",
                "Lock trunk and glutes into a solid straight plank.",
                "Lower chest steadily until hovering right above hands.",
                "Press strongly back up to full triceps lockout."
            ),
            primaryMuscles = "Triceps, Inner Chest",
            equipmentRequired = "Bodyweight"
        ),
        "t5" to g(
            whatToDo = "Hinge forward with elbow pinned high beside your ribcage and extend the dumbbell straight back.",
            setup = "Brace one hand on a bench with torso flat, lift working elbow level with your back.",
            execution = "Keep elbow locked in space while pressing dumbbell straight behind you to lockout, hold 1 second.",
            formCue = "Keep your elbow glued higher than your torso—never let it drop downward.",
            mistakeToAvoid = "Swinging the dumbbell from the shoulder like a pendulum.",
            breathing = "Exhale as you straighten your arm backward; inhale returning to 90 degrees.",
            steps = listOf(
                "Hinge smoothly forward and raise working upper arm parallel to floor.",
                "Pin elbow still beside your torso.",
                "Extend forearm backward until completely straight and squeeze 1 second.",
                "Lower smoothly back to 90-degree bend without swinging."
            ),
            primaryMuscles = "Triceps (Lateral & Long Heads)",
            equipmentRequired = "Dumbbell"
        ),
        "bnd_tr1" to g(
            whatToDo = "Stand on one end of a resistance band and extend the other overhead behind your head.",
            setup = "Anchor band firmly under back heel, bring band overhead with elbows close beside head.",
            execution = "Extend forearms cleanly straight upward against band tension, squeeze, and lower slowly behind.",
            formCue = "Point elbows straight toward the ceiling and keep ribs braced tight.",
            mistakeToAvoid = "Flaring elbows outward or leaning forward to dodge the top stretch.",
            breathing = "Exhale pressing hands overhead; inhale during the deep lowering stretch.",
            steps = listOf(
                "Step firmly onto one end of the band in a staggered stance.",
                "Hold opposite end behind neck with elbows lifted high.",
                "Extend arms straight overhead smoothly to lockout.",
                "Control the band slowly back behind head."
            ),
            primaryMuscles = "Triceps Long Head",
            equipmentRequired = "Resistance Bands"
        ),
        "db_tr1" to g(
            whatToDo = "Lie flat with two dumbbells held neutral-grip overhead, hinge elbows to temples, and press up.",
            setup = "Lie back flat with feet planted, palms facing inward, and upper arms tilted slightly back.",
            execution = "Lower dumbbells smoothly alongside temples without elbows flaring, then extend back cleanly.",
            formCue = "Keep wrists neutral and let weights kiss shoulders gently at bottom stretch.",
            mistakeToAvoid = "Dropping upper arms forward toward hips as you press upward.",
            breathing = "Inhale lowering beside temples; exhale firmly pressing arms straight.",
            steps = listOf(
                "Hold dumbbells overhead facing palms inward with arms angled back 10 degrees.",
                "Hinge elbows cleanly to lower weights outside your ears.",
                "Pause briefly at deep tricep stretch.",
                "Drive dumbbells smoothly back up along the same hinge arc."
            ),
            primaryMuscles = "Triceps",
            equipmentRequired = "Dumbbells & Flat Bench"
        ),

        // ==================== BICEPS ====================
        "a_c1" to g(
            whatToDo = "Stand facing a low cable pulley and curl the bar smoothly up toward shoulders under steady pull.",
            setup = "Set pulley to lowest notch, hold straight or EZ handle underhand, and pin elbows at ribs.",
            execution = "Curl bar smoothly up without rocking torso, squeeze biceps 1 second, and lower cleanly.",
            formCue = "Keep your elbows pinned right along your ribcage from bottom to top.",
            mistakeToAvoid = "Hunching shoulders forward or leaning back to heave heavier stacks.",
            breathing = "Exhale smoothly as you curl upward; inhale during the controlled lower.",
            steps = listOf(
                "Stand upright one step back from the low pulley with arms straight.",
                "Pin upper arms securely at your sides.",
                "Curl bar upward to shoulder level and hold 1-second peak squeeze.",
                "Lower slowly to full arm extension keeping cable tension."
            ),
            primaryMuscles = "Biceps Brachii, Forearms",
            equipmentRequired = "Low Cable Pulley"
        ),
        "a_m1" to g(
            whatToDo = "Rest upper arms snugly over the preacher pad and curl the machine handles up smoothly.",
            setup = "Adjust seat so armpits rest firmly at the top edge of the angled pad.",
            execution = "Curl smoothly upward without lifting elbows off pad, squeeze hard, and lower gently to stretch.",
            formCue = "Stop just short of vertical so your biceps never lose tension at the top.",
            mistakeToAvoid = "Hyper-snapping elbows at the bottom or lifting chest off the pad.",
            breathing = "Exhale on the upward curl; inhale calmly lowering down the pad.",
            steps = listOf(
                "Set seat height so upper arms lie completely flat on the sloped pad.",
                "Take an even underhand grip and brace shoulders back gently.",
                "Curl handles cleanly upward until forearms approach vertical.",
                "Lower smoothly over 2-3 seconds into full bicep length."
            ),
            primaryMuscles = "Biceps Brachii (Short Head)",
            equipmentRequired = "Preacher Curl Machine"
        ),
        "req_bc1" to g(
            whatToDo = "Perform strict Preacher curls on an angled bench to eliminate body swing completely.",
            setup = "Rest upper arms flush against the preacher pad with shoulder blades tucked slightly.",
            execution = "Curl EZ-bar or dumbbell up until just before resting tension drops, then lower slowly.",
            formCue = "Press the backs of your triceps flat against the pad on every single millimeter.",
            mistakeToAvoid = "Snapping bottom lockout fast or leaning your torso backward.",
            breathing = "Exhale curling upward; inhale slowly lowering along the pad.",
            steps = listOf(
                "Seat yourself comfortably with armpits resting over top of the pad.",
                "Grip EZ-bar or dumbbell underhand and lengthen arms steadily.",
                "Curl upward smoothly squeezing biceps tightly before forearms pass vertical.",
                "Control the descent over 3 slow seconds."
            ),
            primaryMuscles = "Biceps Brachii, Brachialis",
            equipmentRequired = "EZ-Bar or Dumbbell + Preacher Bench"
        ),
        "a_b1" to g(
            whatToDo = "Curl an angled EZ-bar standing upright to load heavy biceps while sparing wrist strain.",
            setup = "Stand tall holding outer angled grips with palms turned slightly inward and elbows pinned.",
            execution = "Curl smoothly to upper chest level without swinging hips, pause, and lower slowly.",
            formCue = "Keep your chest proud and elbows pointing straight down at the floor.",
            mistakeToAvoid = "Pulling elbows high forward or bending wrists backward.",
            breathing = "Exhale as bar rises; inhale smoothly on the 2-second return.",
            steps = listOf(
                "Grip EZ-bar naturally along the angled camber shoulder-width apart.",
                "Pin elbows firmly beside your waist.",
                "Curl bar upward smoothly to squeeze biceps strongly at top.",
                "Lower cleanly down to full straight-arm length."
            ),
            primaryMuscles = "Biceps Brachii, Brachialis, Forearms",
            equipmentRequired = "EZ-Bar"
        ),
        "a1" to g(
            whatToDo = "Sit back on an incline bench letting arms hang behind torso and curl dumbbells upward.",
            setup = "Set bench at 45-60 degrees, pin shoulder blades back, and let dumbbells hang vertically down.",
            execution = "Curl both weights upward keeping upper arms pinned pointing straight downward.",
            formCue = "Enjoy the deep long-head stretch at the bottom—don't bring elbows forward.",
            mistakeToAvoid = "Shrugging shoulders or swinging elbows ahead during the curl.",
            breathing = "Exhale smoothly curling weights up; inhale during the deep lowering stretch.",
            steps = listOf(
                "Sit firmly against a 45-60 degree incline bench with arms hanging straight.",
                "Rotate palms forward while keeping upper arms still behind torso.",
                "Curl dumbbells upward smoothly without swinging elbows ahead.",
                "Lower all the way down into a clean long-head stretch."
            ),
            primaryMuscles = "Biceps Long Head",
            equipmentRequired = "Dumbbells & Incline Bench"
        ),
        "a3" to g(
            whatToDo = "Curl dumbbells upward using a neutral thumbs-up grip to target forearm thickness and brachialis.",
            setup = "Stand tall holding dumbbells at sides with palms facing thighs and shoulders relaxed.",
            execution = "Curl dumbbells up keeping thumbs pointed straight ahead throughout, pause, and lower smoothly.",
            formCue = "Keep wrists stiff and straight—never twist palms upward.",
            mistakeToAvoid = "Swinging hips backward or letting elbows flare behind your torso.",
            breathing = "Exhale lifting weights upward; inhale calmly on the descent.",
            steps = listOf(
                "Hold dumbbells vertically at sides with thumbs pointing forward.",
                "Pin elbows gently against ribs and brace your core.",
                "Curl weights upward until forearms squeeze into biceps.",
                "Lower smoothly back down to full arm extension."
            ),
            primaryMuscles = "Brachialis, Brachioradialis, Biceps",
            equipmentRequired = "Dumbbells"
        ),
        "a4" to g(
            whatToDo = "Curl a straight barbell cleanly from thighs up to shoulder height keeping torso completely upright.",
            setup = "Grip barbell shoulder-width underhand, stand tall with abs braced, and pin elbows at sides.",
            execution = "Curl bar smoothly up to chest height, hold peak squeeze 1 second, and lower calmly over 2 seconds.",
            formCue = "Tighten glutes and abs so your hips cannot swing forward or back.",
            mistakeToAvoid = "Leaning far backward to swing heavier weight up.",
            breathing = "Exhale on the upward curl; inhale during the controlled descent.",
            steps = listOf(
                "Stand hip-width with underhand shoulder-width grip on barbell.",
                "Lock ribs down over hips and keep elbows pinned beside waist.",
                "Curl barbell smoothly upward without shifting elbows ahead.",
                "Lower with deliberate control to straight arms."
            ),
            primaryMuscles = "Biceps Brachii, Forearms",
            equipmentRequired = "Barbell"
        ),
        "a5" to g(
            whatToDo = "Sit with feet wide, brace your upper arm against your inner thigh, and curl one dumbbell.",
            setup = "Sit near bench edge, press back of working elbow firmly inside knee, and straighten arm.",
            execution = "Curl dumbbell steadily toward your shoulder while twisting pinky up slightly, then lower fully.",
            formCue = "Keep the thigh brace immovable so zero shoulder swing sneaks in.",
            mistakeToAvoid = "Pulling arm away from thigh or twisting torso to assist.",
            breathing = "Exhale curling toward chest; inhale smoothly lowering downward.",
            steps = listOf(
                "Sit comfortably with wide stance and brace elbow flat against inner thigh.",
                "Hang dumbbell smoothly downward to full arm extension.",
                "Curl steadily upward rotating pinky slightly toward your shoulder.",
                "Lower completely under 2-second control."
            ),
            primaryMuscles = "Biceps Peak (Short & Long Heads)",
            equipmentRequired = "Single Dumbbell & Bench"
        ),
        "a6" to g(
            whatToDo = "Hang with underhand shoulder-width palms and pull your chin cleanly above the bar.",
            setup = "Grip pull-up bar with palms facing you at shoulder width and brace abs lightly.",
            execution = "Drive elbows downward and back to bring upper chest toward bar, then lower to full extension.",
            formCue = "Think of curling your chest upward toward your hands.",
            mistakeToAvoid = "Swinging your legs upward or stopping before arms straighten fully.",
            breathing = "Exhale as you pull upward; inhale smoothly lowering to hang.",
            steps = listOf(
                "Take an underhand shoulder-width grip from a dead hang.",
                "Depress shoulders slightly and keep legs quiet underneath.",
                "Pull smoothly until chin clears the bar and biceps contract hard.",
                "Lower slowly for 2 seconds back to full extension."
            ),
            primaryMuscles = "Biceps, Lats, Upper Back",
            equipmentRequired = "Pull-up Bar"
        ),
        "req_bc2" to g(
            whatToDo = "Step forward away from a low cable pulley so your curling arm trails behind your torso.",
            setup = "Set cable pulley at hip/thigh height, face away, take a step ahead so arm stretches behind hip.",
            execution = "Keep elbow anchored behind your torso line while curling handle forward up toward shoulder.",
            formCue = "Keep your elbow anchored behind your rib—do not let it swing forward as you curl.",
            mistakeToAvoid = "Stepping too close so tension drops, or turning shoulders sideways.",
            breathing = "Exhale as you curl forward; inhale slowly feeling the deep behind-torso stretch.",
            steps = listOf(
                "Stand facing away from a low cable stack with working arm behind hip.",
                "Step forward cleanly until cable pulls your bicep into stretch.",
                "Keeping upper arm anchored slightly behind torso, curl forearm upward.",
                "Lower back behind hip smoothly under continuous pull."
            ),
            primaryMuscles = "Biceps Long Head",
            equipmentRequired = "Cable Machine"
        ),
        "req_bc3" to g(
            whatToDo = "Rest forearms flat along thighs or a bench and flex/extend wrists smoothly with light dumbbells.",
            setup = "Kneel beside or sit on a bench letting wrists drape off the edge holding light dumbbells.",
            execution = "Curl knuckles upward (extensions) or fingers upward toward forearms (curls) slowly.",
            formCue = "Keep forearms pinned completely still—only your wrist joints move.",
            mistakeToAvoid = "Jerking heavy weights upward with elbows or forearm lift.",
            breathing = "Breathe rhythmically throughout higher-rep forearm sets.",
            steps = listOf(
                "Rest forearms steadily across a bench with wrists hanging freely over edge.",
                "Curl wrists upward cleanly through full range and pause briefly.",
                "Lower slowly toward fingertips for a complete forearm stretch.",
                "Perform both palms-up (flexion) and palms-down (extension) sets."
            ),
            primaryMuscles = "Forearm Flexors & Extensors, Grip",
            equipmentRequired = "Light Dumbbells & Bench"
        ),
        "bnd_bi1" to g(
            whatToDo = "Stand firmly over the center of a resistance band and curl both handles upward together.",
            setup = "Anchor band beneath mid-feet hip-width apart, pin elbows at ribs, and turn palms forward.",
            execution = "Curl upward smoothly against climbing band resistance, hold 1 second at top, and return slowly.",
            formCue = "Control the top third cleanly instead of letting wrists snap backward downward.",
            mistakeToAvoid = "Rocking hips forward or shifting elbows away from ribs.",
            breathing = "Exhale curling upward; inhale steadily lowering back down.",
            steps = listOf(
                "Step firmly onto band center with feet hip-width apart.",
                "Hold handles or band evenly with palms forward and straight wrists.",
                "Curl hands smoothly toward shoulders without moving upper arms.",
                "Lower slowly against band pull back to full extension."
            ),
            primaryMuscles = "Biceps, Forearms",
            equipmentRequired = "Resistance Bands"
        ),
        "db_bi1" to g(
            whatToDo = "Alternate curling each dumbbell smoothly upward while rotating your palm fully toward your shoulder.",
            setup = "Stand tall holding dumbbells at outer thighs with palms neutral and elbows anchored.",
            execution = "Curl one arm up while supinating palm to ceiling, pause briefly, and lower before switching.",
            formCue = "Wait until one dumbbell returns cleanly to your hip before starting the other side.",
            mistakeToAvoid = "Swinging your shoulders side-to-side to create momentum.",
            breathing = "Exhale on each upward curl; inhale cleanly as the dumbbell lowers.",
            steps = listOf(
                "Stand upright holding dumbbells comfortably at your sides.",
                "Curl one dumbbell upward while rotating palm steadily face-up.",
                "Hold peak contraction briefly at upper chest height.",
                "Lower slowly back to side and repeat evenly on opposite arm."
            ),
            primaryMuscles = "Biceps Brachii, Forearms",
            equipmentRequired = "Dumbbells"
        ),

        // ==================== ABS & CORE ====================
        "cr_c1" to g(
            whatToDo = "Kneel below a high cable rope, lock hands beside temples, and curl ribs down toward your pelvis.",
            setup = "Kneel two feet from pulley, hold rope ends firmly beside ears, and freeze hips above knees.",
            execution = "Flex spine downward curving elbows toward mid-thighs by crunching abs hard, then uncurl.",
            formCue = "Imagine curling your ribs into your belly button—never sit hips back onto heels.",
            mistakeToAvoid = "Hinging passively at hips like a bow instead of rounding your spine with abs.",
            breathing = "Exhale completely as you crunch downward to deepen ab contraction; inhale uncurling.",
            steps = listOf(
                "Kneel comfortably on a mat holding cable rope beside your head.",
                "Lock hips upright so only your trunk curls downward.",
                "Crunch ribs down toward pelvis, squeezing abs hard for 1 second.",
                "Uncurl slowly back to upright stretch without sitting onto heels."
            ),
            primaryMuscles = "Rectus Abdominis (Upper & Mid Abs)",
            equipmentRequired = "Cable Machine & Rope"
        ),
        "req_ab1" to g(
            whatToDo = "Perform weighted kneeling cable crunches to progressively overload spinal flexion.",
            setup = "Anchor rope beside forehead, keep hips still at 90 degrees over thighs, and brace slightly.",
            execution = "Round your upper and mid spine smoothly downward until abs reach peak squeeze, then return.",
            formCue = "Keep hands nailed beside temples so your arms never pull down the weight.",
            mistakeToAvoid = "Bowing straight with a flat back from the hip joints.",
            breathing = "Forcefully exhale air out at the bottom of every crunch.",
            steps = listOf(
                "Kneel upright gripping cable rope beside cheeks/temples.",
                "Keep thighs stationary perpendicular to the ground.",
                "Flex spine cleanly curling forehead toward knees.",
                "Control the upward uncurl until abs stretch upright."
            ),
            primaryMuscles = "Abdominals, Obliques",
            equipmentRequired = "Cable Machine & Rope"
        ),
        "cr_b1" to g(
            whatToDo = "Hang from a pull-up bar and curl knees or legs upward by rolling your pelvis toward ribs.",
            setup = "Hang steadily shoulder-width with active shoulders and no lower-body swing.",
            execution = "Lift knees or straight legs smoothly past hip height while curling pelvis upward, then lower.",
            formCue = "Roll your tailbone forward and upward—just lifting thighs only works hip flexors.",
            mistakeToAvoid = "Swinging like a pendulum at the bottom or dropping legs suddenly.",
            breathing = "Exhale as pelvis curls upward; inhale calmly on the slow descent.",
            steps = listOf(
                "Take a steady hang from the pull-up bar with shoulders gently engaged.",
                "Tuck knees or raise straight legs upward while curling pelvis up.",
                "Pause for 1 second at top contraction.",
                "Lower slowly stopping smoothly at hang with zero swing."
            ),
            primaryMuscles = "Lower Abs, Hip Flexors",
            equipmentRequired = "Pull-up Bar"
        ),
        "cr1" to g(
            whatToDo = "Lie flat on back, press lower spine into floor, and hold legs and shoulders lifted in a shallow dish.",
            setup = "Press lower back completely flush against mat with zero gap underneath.",
            execution = "Extend legs low and reach arms overhead while holding lower back pinned firmly to the floor.",
            formCue = "If your lower back arches off the mat even slightly, raise your legs slightly higher.",
            mistakeToAvoid = "Letting your lumbar spine lift off the mat as legs fatigue.",
            breathing = "Take short, rhythmic breaths without relaxing abdominal tension.",
            steps = listOf(
                "Lie supine and press lower spine solidly into the mat.",
                "Lift shoulder blades slightly and raise straight legs off the floor.",
                "Adjust leg height until lower back stays glued down cleanly.",
                "Hold the static banana/dish position for full prescribed seconds."
            ),
            primaryMuscles = "Transverse Abdominis, Rectus Abdominis",
            equipmentRequired = "Bodyweight"
        ),
        "cr2" to g(
            whatToDo = "Support your body cleanly on forearms and toes, holding a straight rigid line from head to heels.",
            setup = "Stack elbows right below shoulders, clasp hands lightly, and step feet hip-width back.",
            execution = "Squeeze glutes, brace abs hard, and pull elbows gently toward toes to hold max isometric tension.",
            formCue = "Tuck pelvis slightly under and gaze calmly straight down between your wrists.",
            mistakeToAvoid = "Letting your hips sag toward the floor or piking glutes high up.",
            breathing = "Breathe deeply and steadily—never hold your breath during holds.",
            steps = listOf(
                "Place forearms parallel with elbows directly underneath shoulders.",
                "Extend legs back onto toes and align head, spine, and heels.",
                "Squeeze abs and glutes firmly while pulling elbows gently backward.",
                "Hold completely still until the countdown finishes."
            ),
            primaryMuscles = "Transverse Abdominis, Rectus Abdominis, Core",
            equipmentRequired = "Bodyweight"
        ),
        "cr3" to g(
            whatToDo = "Sit balanced on your sit-bones with chest lifted and rotate your shoulders smoothly side to side.",
            setup = "Sit with knees bent, lean back 45 degrees keeping chest proud, and hold hands or a weight lightly.",
            execution = "Twist ribcage purposefully side to side touching beside each hip while knees stay quiet.",
            formCue = "Rotate from your ribs and shoulders—don't just swing your hands across a still chest.",
            mistakeToAvoid = "Rounding your lower back or slamming knees side to side.",
            breathing = "Exhale sharply on each side twist; inhale steadily transitioning across center.",
            steps = listOf(
                "Sit with knees bent and lean torso slightly back with flat spine.",
                "Lift heels gently off floor for added challenge or keep planted for stability.",
                "Rotate shoulders cleanly to touch beside one hip, then the other.",
                "Move smoothly under control rather than rushing fast."
            ),
            primaryMuscles = "Obliques, Rectus Abdominis",
            equipmentRequired = "Bodyweight or Light Dumbbell"
        ),
        "cr4" to g(
            whatToDo = "Lie flat and pedal legs smoothly while rotating opposite elbow cleanly toward the incoming knee.",
            setup = "Lie back pressing lower spine flat, fingertips resting lightly behind ears, knees lifted at 90 degrees.",
            execution = "Extend one leg straight low while curling opposite shoulder smoothly toward the bent knee.",
            formCue = "Lead the rotation with your shoulder blade lifting off the floor—not pulling on your neck.",
            mistakeToAvoid = "Tugging your neck forward with clasped hands or racing uncontrollably.",
            breathing = "Exhale rhythmically on every cross-body rotation.",
            steps = listOf(
                "Press lower back into floor with fingertips lightly behind ears.",
                "Lift shoulder blades off mat while rotating right elbow toward left knee.",
                "Extend right leg straight out hovering calmly above floor.",
                "Switch smoothly side to side with controlled 1-second twists."
            ),
            primaryMuscles = "Obliques, Rectus Abdominis",
            equipmentRequired = "Bodyweight"
        ),
        "cr5" to g(
            whatToDo = "Lie flat with arms and bent legs lifted and slowly extend opposite arm and leg without arching back.",
            setup = "Press lower spine firmly onto floor, reach arms straight to ceiling, and stack knees over hips.",
            execution = "Reach right arm overhead while straightening left leg just above floor, pause, and return smoothly.",
            formCue = "Crush the floor gently beneath your lower back throughout every single extension.",
            mistakeToAvoid = "Dropping leg so fast that your lumbar spine arches away from the mat.",
            breathing = "Exhale slowly as arm and leg extend apart; inhale returning to center.",
            steps = listOf(
                "Lie back pressing lower spine flat with knees bent 90 degrees above hips.",
                "Extend both arms straight overhead toward the ceiling.",
                "Lower one leg straight and opposite arm back slowly just off floor.",
                "Return smoothly to center before alternating sides."
            ),
            primaryMuscles = "Transverse Abdominis, Deep Core Stabilizers",
            equipmentRequired = "Bodyweight"
        ),
        "cr6" to g(
            whatToDo = "Hold one dumbbell at your side, bend cleanly sideways toward the weight, and pull upright with obliques.",
            setup = "Stand tall holding one dumbbell in one hand with free hand resting lightly on your hip.",
            execution = "Slide dumbbell smoothly straight down your thigh without leaning forward, then pull upright.",
            formCue = "Imagine sliding strictly sideways between two narrow glass walls.",
            mistakeToAvoid = "Using two heavy dumbbells at once, which cancels the torque cleanly, or twisting forward.",
            breathing = "Inhale bending sideways; exhale pulling torso upright cleanly.",
            steps = listOf(
                "Hold a single dumbbell at one side while standing hip-width apart.",
                "Bend smoothly laterally toward the weighted side without folding forward.",
                "Stop at comfortable lateral stretch along your side.",
                "Contract opposite obliques forcefully to pull torso upright."
            ),
            primaryMuscles = "External & Internal Obliques",
            equipmentRequired = "Single Dumbbell"
        ),
        "cal_ls1" to g(
            whatToDo = "Press palms into parallel bars or floor blocks, lift body clear, and hold tucked knees up to chest.",
            setup = "Grip parallel bars or dip station cleanly with straight locked elbows and depressed shoulders.",
            execution = "Push aggressively downward into handles and pull knees up as tight as possible toward chest.",
            formCue = "Push shoulders completely down away from your ears—don't sink into your neck.",
            mistakeToAvoid = "Bending elbows or letting knees drop down below hip level.",
            breathing = "Take short, smooth breaths while pushing hard through straight arms.",
            steps = listOf(
                "Place hands firmly on dip handles or parallettes with locked elbows.",
                "Push shoulders downward strongly to lift hips clear of support.",
                "Tuck knees cleanly up toward your chest.",
                "Hold steady for full timer seconds before setting feet down."
            ),
            primaryMuscles = "Lower Abs, Hip Flexors, Triceps, Scapular Depressors",
            equipmentRequired = "Dip Station / Parallettes / Floor"
        ),
        "cal_ls2" to g(
            whatToDo = "Support bodyweight on straight arms and hold legs extended straight forward at 90 degrees.",
            setup = "Support evenly on parallel bars or dip station with shoulders pressed down and locked elbows.",
            execution = "Lift both straight legs cleanly together until parallel to floor and hold crisp posture.",
            formCue = "Point your toes forward and squeeze quads hard to keep legs completely straight.",
            mistakeToAvoid = "Leaning torso far backward or letting knees bend downward.",
            breathing = "Maintain steady nasal breaths through rigid core bracing.",
            steps = listOf(
                "Press downward onto handles until elbows lock straight and shoulders depress.",
                "Engage lower abs and quads strongly to raise both straight legs.",
                "Hold legs horizontally forming a sharp 90-degree L with torso.",
                "Hold calmly until target timer expires."
            ),
            primaryMuscles = "Abs, Hip Flexors, Quads, Triceps",
            equipmentRequired = "Dip Station / Parallettes"
        ),
        "cal_hl1" to g(
            whatToDo = "Hang from a pull-up bar and raise completely straight legs up toward horizontal or bar height.",
            setup = "Take a shoulder-width active hang with straight knees and toes pointed forward.",
            execution = "Lift straight legs cleanly upward curling hips slightly at top, then lower slowly without swinging.",
            formCue = "Think of pushing downward slightly against the bar with straight arms to control swing.",
            mistakeToAvoid = "Kicking upward off the bottom or bending knees halfway up.",
            breathing = "Exhale as straight legs rise; inhale calmly on the slow controlled descent.",
            steps = listOf(
                "Hang evenly from pull-up bar with locked knees and quiet core.",
                "Raise straight legs cleanly up until parallel or higher.",
                "Curl pelvis lightly at peak contraction.",
                "Lower straight legs steadily over 2 seconds to full hang."
            ),
            primaryMuscles = "Lower Abs, Hip Flexors, Forearms",
            equipmentRequired = "Pull-up Bar"
        ),
        "cal_hl2" to g(
            whatToDo = "Hang from a pull-up bar with feet raised overhead and sweep legs side to side like windshield wipers.",
            setup = "Hang from bar, pull torso back slightly, and raise feet up near bar height.",
            execution = "Sweep raised legs cleanly in an arc from left to right with control, keeping shoulders steady.",
            formCue = "Move only your legs and hips across the arc—don't drop chest down early.",
            mistakeToAvoid = "Flopping legs violently side to side with uncontrolled momentum.",
            breathing = "Exhale across each sweep; inhale briefly passing through top center.",
            steps = listOf(
                "Hang steadily and raise legs up toward bar level.",
                "Keep lats engaged so torso sits slightly reclined.",
                "Sweep legs cleanly down to one side under control, then arc to other side.",
                "Maintain steady grip and smooth tempo throughout."
            ),
            primaryMuscles = "Obliques, Rectus Abdominis, Lats, Forearms",
            equipmentRequired = "Pull-up Bar"
        ),
        "hm_cr1" to g(
            whatToDo = "Start on all fours and extend opposite arm and leg straight out to horizontal with a motionless spine.",
            setup = "Place hands directly under shoulders and knees right under hips with a flat tabletop back.",
            execution = "Reach right arm ahead and kick left leg straight back to hip height, hold 2 seconds, and switch.",
            formCue = "Imagine balancing a full cup of coffee smoothly on your lower back.",
            mistakeToAvoid = "Arching lower spine upward or swinging your lifted heel above hip height.",
            breathing = "Exhale smoothly as you extend out and hold; inhale returning to center.",
            steps = listOf(
                "Set hands under shoulders and knees under hips in neutral tabletop.",
                "Brace abs lightly before lifting limbs.",
                "Extend opposite arm and leg horizontally without shifting hips.",
                "Pause 2 seconds at full reach, return calmly, and alternate."
            ),
            primaryMuscles = "Deep Core, Lower Back, Glutes",
            equipmentRequired = "Bodyweight"
        ),
        "hm_cr2" to g(
            whatToDo = "Stack your body sideways propped on one forearm and hold hips lifted cleanly off the floor.",
            setup = "Rest forearm directly beneath shoulder with legs stacked straight and hips lifted off mat.",
            execution = "Press bottom forearm into ground and push hips high into a crisp side-line isometric hold.",
            formCue = "Keep your body stacked cleanly like resting between two flat walls—hips forward.",
            mistakeToAvoid = "Letting your hips sag toward the mat or rolling chest downward.",
            breathing = "Breathe calmly and evenly through tight oblique tension.",
            steps = listOf(
                "Align elbow straight beneath shoulder with forearm forward.",
                "Stack top foot directly over bottom foot (or stagger for balance).",
                "Drive hips upward until head, hips, and ankles align straight.",
                "Hold full prescribed seconds, then switch cleanly to other side."
            ),
            primaryMuscles = "Obliques, Glute Medius, Deep Core",
            equipmentRequired = "Bodyweight"
        ),
        "hm_cr3" to g(
            whatToDo = "Hold a rigid hollow body curve on your back and rock smoothly back and forth without breaking form.",
            setup = "Press lower spine into floor in a hollow hold with arms stretched overhead and legs lifted straight.",
            execution = "Initiate a tiny controlled rocking motion from hips to upper back keeping body shape frozen.",
            formCue = "Rock like a curved wooden rocking chair—if your shape bends at the waist, pause and reset.",
            mistakeToAvoid = "Flapping arms and legs independently instead of moving as one solid piece.",
            breathing = "Take short, steady breaths while locking rib-to-pelvis alignment.",
            steps = listOf(
                "Lock into a tight hollow hold with lower back pressed flush to mat.",
                "Squeeze quads, glutes, and abs into one solid curved arc.",
                "Rock softly from lower ribs toward hips without kicking legs.",
                "Continue smooth, tiny rocks for the prescribed duration."
            ),
            primaryMuscles = "Rectus Abdominis, Transverse Abdominis",
            equipmentRequired = "Bodyweight"
        )
    )

    fun getGuide(
        name: String,
        muscle: String,
        equipment: String,
        detail: String,
        exerciseId: String = ""
    ): ExerciseGuide {
        // 1. Exact ID lookup (covers 100% of ExerciseDB)
        if (exerciseId.isNotBlank()) {
            guidesById[exerciseId]?.let { return it }
        }

        // 2. Exact exercise name lookup from ExerciseDB
        val cleanName = name.trim()
        val matchedDbEx = ExerciseDB.list.find { it.name.equals(cleanName, ignoreCase = true) }
        if (matchedDbEx != null) {
            guidesById[matchedDbEx.id]?.let { return it }
        }

        val n = cleanName.lowercase()
        return when {
            n.contains("stretch") || n.contains("mobility") || n.contains("flow") || n.contains("windmills") || n.contains("circles") || n.contains("dislocations") || n.contains("swings") || n.contains("cat-cow") -> ExerciseGuide(
                whatToDo = "Move calmly and deliberately through the full mobility range, pairing relaxed breaths with joint motion.",
                setup = "Stand or kneel comfortably on a mat with space to move smoothly in all directions.",
                execution = "Ease smoothly into each end range for 2-3 seconds without bouncing, keeping joints relaxed.",
                formCue = "Move only through pain-free motion; never force a sharp pinch.",
                mistakeToAvoid = "Holding your breath or snapping fast through tight positions.",
                breathing = "Inhale smoothly preparing the movement; exhale deeply as you sink into the stretch.",
                steps = listOf(
                    "Start in a relaxed upright or quadruped posture.",
                    "Move steadily through the target joint arc without rushing.",
                    "Pause softly at comfortable end range on an exhale.",
                    "Repeat smoothly across all prescribed mobility reps."
                ),
                primaryMuscles = "Joint Capsules, Connective Tissue, Warmup Stabilizers",
                equipmentRequired = "Bodyweight / Mat"
            )
            else -> ExerciseGuide(
                whatToDo = "Control the lowering phase for 2 seconds, pause cleanly at peak contraction, and return smoothly.",
                setup = "Set up securely with ${equipment.ifBlank { "your equipment" }}, brace your core, and align joints.",
                execution = "Initiate directly from the target muscle ($muscle), completing full range without momentum.",
                formCue = "Move with deliberate tempo and steady breathing rather than swinging weights.",
                mistakeToAvoid = "Using sudden body swing or cutting the bottom stretch short.",
                breathing = "Exhale smoothly during effort; inhale calmly on the lowering phase.",
                steps = listOf(
                    "Set stable posture with joints aligned and core braced.",
                    "Move through the full working range using controlled tempo.",
                    "Squeeze the target muscle cleanly for 1 second at peak.",
                    "Lower smoothly under control back to start."
                ),
                primaryMuscles = "${muscle.replaceFirstChar { it.uppercase() }} ($detail)",
                equipmentRequired = equipment.ifBlank { "Standard Equipment" },
                isGenericFallback = true
            )
        }
    }
}
