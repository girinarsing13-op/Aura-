package com.example.aura.data.model

/**
 * Canonical biomechanical movement patterns used by [com.example.aura.ui.components.AnimatedExerciseFigure]
 * to render an interpolated stick-figure demonstration for any exercise or warm-up drill.
 */
enum class MovementPattern(
    val displayName: String,
    val defaultCaption: String
) {
    HORIZONTAL_PUSH(
        displayName = "Horizontal Push",
        defaultCaption = "Lower for 2 sec, press up."
    ),
    INCLINE_PUSH(
        displayName = "Incline Push",
        defaultCaption = "Control to upper chest, drive up."
    ),
    VERTICAL_PUSH(
        displayName = "Vertical Push",
        defaultCaption = "Brace core, press straight overhead."
    ),
    VERTICAL_PULL(
        displayName = "Vertical Pull",
        defaultCaption = "Lead with elbows, pull chest to bar."
    ),
    HORIZONTAL_PULL(
        displayName = "Horizontal Pull",
        defaultCaption = "Pull to ribs, squeeze shoulder blades."
    ),
    SQUAT(
        displayName = "Squat",
        defaultCaption = "Sit hips back and down, drive up."
    ),
    LUNGE_SPLIT_SQUAT(
        displayName = "Lunge / Split Squat",
        defaultCaption = "Lower under control, drive front heel."
    ),
    HIP_HINGE(
        displayName = "Hip Hinge",
        defaultCaption = "Push hips back flat-backed, snap through."
    ),
    CURL(
        displayName = "Curl",
        defaultCaption = "Pin elbows still, curl with control."
    ),
    TRICEP_EXT_DIP(
        displayName = "Tricep Ext / Dip",
        defaultCaption = "Keep elbows tucked, extend to lockout."
    ),
    RAISE(
        displayName = "Lateral / Front Raise",
        defaultCaption = "Sweep with soft elbows to shoulder height."
    ),
    CRUNCH_LEG_RAISE(
        displayName = "Crunch / Leg Raise",
        defaultCaption = "Exhale as you curl torso smoothly."
    ),
    PLANK_HOLD(
        displayName = "Plank / Hold",
        defaultCaption = "Brace core and glutes, breathe steadily."
    ),
    CARDIO_JUMP(
        displayName = "Cardio / Mobility",
        defaultCaption = "Move smoothly with controlled rhythm."
    );

    companion object {
        fun inferFrom(name: String, muscle: String = ""): MovementPattern {
            val n = name.lowercase()
            val m = muscle.lowercase()
            return when {
                n.contains("plank") || n.contains("l-sit") || n.contains("wall sit") ||
                    n.contains("front lever") || n.contains("handstand") ||
                    n.contains("hollow body floor") || n.contains("dead hang") ||
                    n.contains("breathing") -> PLANK_HOLD
                n.contains("incline") && (n.contains("press") || n.contains("chest")) -> INCLINE_PUSH
                n.contains("overhead") && n.contains("tricep") || n.contains("skull") ||
                    n.contains("pushdown") || n.contains("kickback") || n.contains("dip") ||
                    (n.contains("tricep") && n.contains("extension")) -> TRICEP_EXT_DIP
                n.contains("overhead") || n.contains("shoulder press") || n.contains("arnold") ||
                    n.contains("pike push") || n.contains("cuban press") -> VERTICAL_PUSH
                n.contains("bench press") || n.contains("chest press") || n.contains("floor press") ||
                    n.contains("push-up") || n.contains("pushup") || n.contains("pec deck") ||
                    n.contains("chest fly") || n.contains("crossover") -> HORIZONTAL_PUSH
                n.contains("pull-up") || n.contains("chin-up") || n.contains("pulldown") ||
                    n.contains("pullover") || n.contains("muscle-up") -> VERTICAL_PULL
                n.contains("row") || n.contains("face pull") -> HORIZONTAL_PULL
                n.contains("lunge") || n.contains("split squat") || n.contains("cossack") -> LUNGE_SPLIT_SQUAT
                n.contains("squat") || n.contains("leg press") || n.contains("leg extension") -> SQUAT
                n.contains("deadlift") || n.contains("rdl") || n.contains("good morning") ||
                    n.contains("glute bridge") || n.contains("hip thrust") || n.contains("nordic") ||
                    n.contains("leg curl") || n.contains("inchworm") -> HIP_HINGE
                n.contains("curl") && !n.contains("leg curl") -> CURL
                n.contains("lateral raise") || n.contains("front raise") || n.contains("rear delt") ||
                    n.contains("shrug") || n.contains("pull-apart") || n.contains("dislocate") ||
                    n.contains("wall slide") || n.contains("swimmer") || n.contains("arm hug") -> RAISE
                n.contains("crunch") || n.contains("leg raise") || n.contains("windshield") ||
                    n.contains("russian twist") || n.contains("dead bug") || n.contains("bird dog") ||
                    n.contains("side bend") || n.contains("hollow body rock") ||
                    n.contains("cat-cow") || n.contains("thread the needle") -> CRUNCH_LEG_RAISE
                n.contains("calf") || n.contains("swing") || n.contains("wrist") ||
                    n.contains("stretch") || n.contains("figure-4") || n.contains("90/90") ||
                    n.contains("lizard") || n.contains("elbow") -> CARDIO_JUMP
                m.contains("chest") -> HORIZONTAL_PUSH
                m.contains("back") -> HORIZONTAL_PULL
                m.contains("shoulder") -> RAISE
                m.contains("bicep") -> CURL
                m.contains("tricep") -> TRICEP_EXT_DIP
                m.contains("quad") -> SQUAT
                m.contains("hamstring") || m.contains("glute") -> HIP_HINGE
                m.contains("abs") || m.contains("core") -> CRUNCH_LEG_RAISE
                else -> CARDIO_JUMP
            }
        }
    }
}
