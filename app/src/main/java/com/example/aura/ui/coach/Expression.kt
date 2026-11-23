package com.example.aura.ui.coach

import com.example.aura.engine.CoachMoodResult

/**
 * Expression Category grouping facial expressions by affective tone.
 */
enum class ExpressionCategory(val displayName: String) {
    NEUTRAL("Neutral"),
    POSITIVE("Positive"),
    CONCERNED("Concerned"),
    NEGATIVE("Negative"),
    RECOVERY("Recovery"),
    INTENSITY("Intensity")
}

/**
 * Facial Expression Library for the AURA Coach System.
 * Defines 52 distinct facial states (40+ required) mapping engine evaluation
 * outputs directly to specific assets, drawables, and canvas rendering targets.
 */
enum class Expression(
    val id: String,
    val displayName: String,
    val category: ExpressionCategory,
    val assetPath: String,
    val assetKey: String = "expression_$id",
    val drawableResName: String = "ic_expression_$id",
    val defaultMood: CoachMood,
    val description: String = ""
) {
    // ==================== NEUTRAL / BASELINE (9 states) ====================
    NEUTRAL(
        id = "neutral",
        displayName = "Neutral",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/neutral.png",
        defaultMood = CoachMood.NEUTRAL,
        description = "Calm baseline neutral gaze with balanced horizontal brows."
    ),
    CALM(
        id = "calm",
        displayName = "Calm",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/calm.png",
        defaultMood = CoachMood.CALM,
        description = "Relaxed serene face with gently closed resting eye arcs."
    ),
    OBSERVING(
        id = "observing",
        displayName = "Observing",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/observing.png",
        defaultMood = CoachMood.OBSERVING,
        description = "Attentive analytical look with slightly narrowed eyes."
    ),
    THINKING(
        id = "thinking",
        displayName = "Thinking",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/thinking.png",
        defaultMood = CoachMood.THINKING,
        description = "Pondering gaze looking upward with arched intellectual brow."
    ),
    WAITING(
        id = "waiting",
        displayName = "Waiting",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/waiting.png",
        defaultMood = CoachMood.WAITING,
        description = "Patient expectant expression awaiting workout set logging."
    ),
    AWARE(
        id = "aware",
        displayName = "Aware",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/aware.png",
        defaultMood = CoachMood.AWARE,
        description = "Alert baseline with widened receptive pupils."
    ),
    CURIOUS(
        id = "curious",
        displayName = "Curious",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/curious.png",
        defaultMood = CoachMood.CURIOUS,
        description = "Inquisitive expression with subtle head tilt and raised brow."
    ),
    BLANK(
        id = "blank",
        displayName = "Blank",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/blank.png",
        defaultMood = CoachMood.NEUTRAL,
        description = "Completely flat unexpressive baseline state before initialization."
    ),
    FOCUSED(
        id = "focused",
        displayName = "Focused",
        category = ExpressionCategory.NEUTRAL,
        assetPath = "coach/expressions/focused.png",
        defaultMood = CoachMood.OBSERVING,
        description = "Centered concentration with steady locked-in gaze."
    ),

    // ==================== POSITIVE / REWARD (13 states) ====================
    PLEASED(
        id = "pleased",
        displayName = "Pleased",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/pleased.png",
        defaultMood = CoachMood.PLEASED,
        description = "Satisfied warm smile with soft approving eye curves."
    ),
    HAPPY(
        id = "happy",
        displayName = "Happy",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/happy.png",
        defaultMood = CoachMood.HAPPY,
        description = "Bright uplifting grin with joyful eye crescents."
    ),
    JOYFUL(
        id = "joyful",
        displayName = "Joyful",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/joyful.png",
        defaultMood = CoachMood.JOYFUL,
        description = "Radiant joyful smile celebrating streak milestones."
    ),
    PROUD(
        id = "proud",
        displayName = "Proud",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/proud.png",
        defaultMood = CoachMood.PROUD,
        description = "Beaming elevated chin and proud approving brows for 100% adherence."
    ),
    EXCITED(
        id = "excited",
        displayName = "Excited",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/excited.png",
        defaultMood = CoachMood.EXCITED,
        description = "Energetic wide-eyed grin with dynamic bounce."
    ),
    CELEBRATING(
        id = "celebrating",
        displayName = "Celebrating",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/celebrating.png",
        defaultMood = CoachMood.CELEBRATING,
        description = "Personal record triumph with celebratory spark accents."
    ),
    IMPRESSED(
        id = "impressed",
        displayName = "Impressed",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/impressed.png",
        defaultMood = CoachMood.IMPRESSED,
        description = "Admiring nod with wide impressed eyes over progressive overload."
    ),
    RELIEVED(
        id = "relieved",
        displayName = "Relieved",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/relieved.png",
        defaultMood = CoachMood.RELIEVED,
        description = "Exhaling relief with curved resting eyes and soft smile upon return."
    ),
    CONFIDENT(
        id = "confident",
        displayName = "Confident",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/confident.png",
        defaultMood = CoachMood.CONFIDENT,
        description = "Self-assured slight smirk with steady focused brows."
    ),
    ENCOURAGING(
        id = "encouraging",
        displayName = "Encouraging",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/encouraging.png",
        defaultMood = CoachMood.ENCOURAGING,
        description = "Warm supportive cheer with welcoming eyes."
    ),
    PLAYFUL(
        id = "playful",
        displayName = "Playful",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/playful.png",
        defaultMood = CoachMood.PLAYFUL,
        description = "Cheeky wink with playful curved mouth."
    ),
    TRIUMPHANT(
        id = "triumphant",
        displayName = "Triumphant",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/triumphant.png",
        defaultMood = CoachMood.CELEBRATING,
        description = "Victorious power pose expression with glowing aura."
    ),
    ECSTATIC(
        id = "ecstatic",
        displayName = "Ecstatic",
        category = ExpressionCategory.POSITIVE,
        assetPath = "coach/expressions/ecstatic.png",
        defaultMood = CoachMood.JOYFUL,
        description = "Overjoyed high-energy expression with starburst eyes."
    ),

    // ==================== CONCERNED / VIGILANT (10 states) ====================
    CONCERNED(
        id = "concerned",
        displayName = "Concerned",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/concerned.png",
        defaultMood = CoachMood.CONCERNED,
        description = "Gentle worried brow pinch showing watchful care for safety."
    ),
    WORRIED(
        id = "worried",
        displayName = "Worried",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/worried.png",
        defaultMood = CoachMood.WORRIED,
        description = "Anxious furrowed brow with sweat bead over excessive load jumps."
    ),
    UNCERTAIN(
        id = "uncertain",
        displayName = "Uncertain",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/uncertain.png",
        defaultMood = CoachMood.UNCERTAIN,
        description = "Wavy mouth with hesitant uneven brows."
    ),
    SKEPTICAL(
        id = "skeptical",
        displayName = "Skeptical",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/skeptical.png",
        defaultMood = CoachMood.SKEPTICAL,
        description = "One brow cocked high scrutinizing physiologically unrealistic goals."
    ),
    DOUBTFUL(
        id = "doubtful",
        displayName = "Doubtful",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/doubtful.png",
        defaultMood = CoachMood.DOUBTFUL,
        description = "Sideward glance questioning questionable training inputs."
    ),
    SURPRISED(
        id = "surprised",
        displayName = "Surprised",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/surprised.png",
        defaultMood = CoachMood.SURPRISED,
        description = "Wide circular eyes with rounded mouth of sudden surprise."
    ),
    CONFUSED(
        id = "confused",
        displayName = "Confused",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/confused.png",
        defaultMood = CoachMood.CONFUSED,
        description = "Tilted brows and off-center query expression."
    ),
    CAUTIOUS(
        id = "cautious",
        displayName = "Cautious",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/cautious.png",
        defaultMood = CoachMood.CAUTIOUS,
        description = "Guarded vigilant expression advising safety against joint injury."
    ),
    SUSPICIOUS(
        id = "suspicious",
        displayName = "Suspicious",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/suspicious.png",
        defaultMood = CoachMood.SKEPTICAL,
        description = "Narrowed eyes detecting inconsistent input logs or missed sets."
    ),
    PERPLEXED(
        id = "perplexed",
        displayName = "Perplexed",
        category = ExpressionCategory.CONCERNED,
        assetPath = "coach/expressions/perplexed.png",
        defaultMood = CoachMood.CONFUSED,
        description = "Asymmetrical gaze grappling with sudden erratic volume swings."
    ),

    // ==================== NEGATIVE / DISCIPLINE (9 states) ====================
    DISAPPOINTED(
        id = "disappointed",
        displayName = "Disappointed",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/disappointed.png",
        defaultMood = CoachMood.DISAPPOINTED,
        description = "Downturned frown and lowered disheartened brows for skipped workouts."
    ),
    FRUSTRATED(
        id = "frustrated",
        displayName = "Frustrated",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/frustrated.png",
        defaultMood = CoachMood.FRUSTRATED,
        description = "Tightly clenched brows with visible tension over recurring misses."
    ),
    ANNOYED(
        id = "annoyed",
        displayName = "Annoyed",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/annoyed.png",
        defaultMood = CoachMood.ANNOYED,
        description = "Flat sideways grimace at skipped warmups or repeated errors."
    ),
    SAD(
        id = "sad",
        displayName = "Sad",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/sad.png",
        defaultMood = CoachMood.SAD,
        description = "Melancholy expression with soft tear shimmer."
    ),
    SERIOUS(
        id = "serious",
        displayName = "Serious",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/serious.png",
        defaultMood = CoachMood.SERIOUS,
        description = "Steely unyielding expression for prolonged absences."
    ),
    UPSET(
        id = "upset",
        displayName = "Upset",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/upset.png",
        defaultMood = CoachMood.UPSET,
        description = "Deep frown and aggrieved angled brows."
    ),
    SHOCKED(
        id = "shocked",
        displayName = "Shocked",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/shocked.png",
        defaultMood = CoachMood.SHOCKED,
        description = "Sudden jarring drop in volume or drastic safety risk."
    ),
    STERN(
        id = "stern",
        displayName = "Stern",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/stern.png",
        defaultMood = CoachMood.SERIOUS,
        description = "Firm authoritative discipline reminding user of commitment."
    ),
    DISAPPROVAL(
        id = "disapproval",
        displayName = "Disapproval",
        category = ExpressionCategory.NEGATIVE,
        assetPath = "coach/expressions/disapproval.png",
        defaultMood = CoachMood.DISAPPOINTED,
        description = "Quiet head-shake disapproval over broken consistency."
    ),

    // ==================== RECOVERY / FATIGUE (7 states) ====================
    SLEEPY(
        id = "sleepy",
        displayName = "Sleepy",
        category = ExpressionCategory.RECOVERY,
        assetPath = "coach/expressions/sleepy.png",
        defaultMood = CoachMood.SLEEPY,
        description = "Drooping heavy lids indicating approaching bedtime."
    ),
    SLEEPING(
        id = "sleeping",
        displayName = "Sleeping",
        category = ExpressionCategory.RECOVERY,
        assetPath = "coach/expressions/sleeping.png",
        defaultMood = CoachMood.SLEEPING,
        description = "Deep sleep arcs with floating zzz icons during rest period."
    ),
    TIRED(
        id = "tired",
        displayName = "Tired",
        category = ExpressionCategory.RECOVERY,
        assetPath = "coach/expressions/tired.png",
        defaultMood = CoachMood.TIRED,
        description = "Fatigued eyes with mild dark circles calling for deload."
    ),
    EXHAUSTED(
        id = "exhausted",
        displayName = "Exhausted",
        category = ExpressionCategory.RECOVERY,
        assetPath = "coach/expressions/exhausted.png",
        defaultMood = CoachMood.EXHAUSTED,
        description = "Drained face with faint steam suggesting high CNS strain."
    ),
    RELAXED(
        id = "relaxed",
        displayName = "Relaxed",
        category = ExpressionCategory.RECOVERY,
        assetPath = "coach/expressions/relaxed.png",
        defaultMood = CoachMood.RELAXED,
        description = "Mellow soothing smile during restorative rest days."
    ),
    RESTING(
        id = "resting",
        displayName = "Resting",
        category = ExpressionCategory.RECOVERY,
        assetPath = "coach/expressions/resting.png",
        defaultMood = CoachMood.RELAXED,
        description = "Peaceful stillness recovering muscular and neural tissue."
    ),
    MEDITATING(
        id = "meditating",
        displayName = "Meditating",
        category = ExpressionCategory.RECOVERY,
        assetPath = "coach/expressions/meditating.png",
        defaultMood = CoachMood.CALM,
        description = "Centered calm mindfulness state with closed eyes."
    ),

    // ==================== INTENSITY / PROGRESS (4 states) ====================
    DETERMINED(
        id = "determined",
        displayName = "Determined",
        category = ExpressionCategory.INTENSITY,
        assetPath = "coach/expressions/determined.png",
        defaultMood = CoachMood.DETERMINED,
        description = "Fierce angled brows locked into progressive overload."
    ),
    INTENSE(
        id = "intense",
        displayName = "Intense",
        category = ExpressionCategory.INTENSITY,
        assetPath = "coach/expressions/intense.png",
        defaultMood = CoachMood.DETERMINED,
        description = "High-focus laser gaze during maximum effort compound sets."
    ),
    HYPED(
        id = "hyped",
        displayName = "Hyped",
        category = ExpressionCategory.INTENSITY,
        assetPath = "coach/expressions/hyped.png",
        defaultMood = CoachMood.EXCITED,
        description = "Pumped-up adrenaline surge before hitting heavy sets."
    ),
    RELENTLESS(
        id = "relentless",
        displayName = "Relentless",
        category = ExpressionCategory.INTENSITY,
        assetPath = "coach/expressions/relentless.png",
        defaultMood = CoachMood.DETERMINED,
        description = "Unwavering grit enduring difficult training blocks."
    );

    companion object {
        /**
         * Fast O(1) lookup cache indexed by lowercase string ID.
         */
        private val idLookup: Map<String, Expression> by lazy {
            entries.associateBy { it.id.lowercase() }
        }

        /**
         * Fast O(1) lookup cache indexed by enum name.
         */
        private val nameLookup: Map<String, Expression> by lazy {
            entries.associateBy { it.name.uppercase() }
        }

        /**
         * Direct lookup from CoachMood to Expression.
         */
        private val moodLookup: Map<CoachMood, Expression> by lazy {
            val map = mutableMapOf<CoachMood, Expression>()
            for (expr in entries) {
                // Prioritize exact name match
                if (expr.name == expr.defaultMood.name) {
                    map[expr.defaultMood] = expr
                } else if (!map.containsKey(expr.defaultMood)) {
                    map[expr.defaultMood] = expr
                }
            }
            map
        }

        /**
         * Total number of distinct facial expressions available in the library.
         */
        val count: Int get() = entries.size

        /**
         * Resolves an Expression from a string ID (e.g. "neutral", "pleased", "concerned").
         * Falls back to [NEUTRAL] if unrecognized or null.
         */
        fun fromId(id: String?): Expression {
            if (id.isNullOrBlank()) return NEUTRAL
            val clean = id.trim().lowercase()
            return idLookup[clean]
                ?: nameLookup[clean.uppercase()]
                ?: NEUTRAL
        }

        /**
         * Resolves an Expression from a [CoachMood].
         */
        fun fromMood(mood: CoachMood?): Expression {
            if (mood == null) return NEUTRAL
            return moodLookup[mood]
                ?: idLookup[mood.name.lowercase()]
                ?: NEUTRAL
        }

        /**
         * Resolves an Expression from an enum name (case-insensitive).
         */
        fun fromName(name: String?): Expression {
            if (name.isNullOrBlank()) return NEUTRAL
            val upper = name.trim().uppercase()
            return nameLookup[upper]
                ?: idLookup[upper.lowercase()]
                ?: NEUTRAL
        }

        /**
         * Resolves Expression from either expression string or CoachMood.
         */
        fun fromEngineOutput(expressionStr: String?, mood: CoachMood? = null): Expression {
            if (!expressionStr.isNullOrBlank()) {
                val found = fromId(expressionStr)
                if (found != NEUTRAL || expressionStr.trim().lowercase() == "neutral") {
                    return found
                }
            }
            return if (mood != null) fromMood(mood) else NEUTRAL
        }

        /**
         * Maps an Expression to its specific relative asset path.
         */
        fun mapToAsset(expression: Expression): String = expression.assetPath

        /**
         * Maps a CoachMood to its specific relative asset path.
         */
        fun mapToAsset(mood: CoachMood): String = fromMood(mood).assetPath

        /**
         * Maps an expression ID string directly to its relative asset path.
         */
        fun mapToAsset(expressionId: String?): String = fromId(expressionId).assetPath

        /**
         * Maps an Expression to its drawable resource identifier name.
         */
        fun mapToDrawableResName(expression: Expression): String = expression.drawableResName

        /**
         * Maps a CoachMood to its drawable resource identifier name.
         */
        fun mapToDrawableResName(mood: CoachMood): String = fromMood(mood).drawableResName

        /**
         * Maps an expression ID string to its drawable resource identifier name.
         */
        fun mapToDrawableResName(expressionId: String?): String = fromId(expressionId).drawableResName

        /**
         * Returns all expressions within a specific category.
         */
        fun getByCategory(category: ExpressionCategory): List<Expression> {
            return entries.filter { it.category == category }
        }

        /**
         * Returns true if the given expression ID exists in the library.
         */
        fun isValidId(id: String?): Boolean {
            if (id.isNullOrBlank()) return false
            return idLookup.containsKey(id.trim().lowercase())
        }
    }
}

/**
 * Extension to convert [CoachMood] to [Expression].
 */
fun CoachMood.toExpression(): Expression = Expression.fromMood(this)

/**
 * Extension to retrieve the asset path directly from [CoachMood].
 */
val CoachMood.assetPath: String get() = toExpression().assetPath

/**
 * Extension to retrieve the asset key directly from [CoachMood].
 */
val CoachMood.expressionAssetKey: String get() = toExpression().assetKey

/**
 * Extension to retrieve the drawable resource name directly from [CoachMood].
 */
val CoachMood.expressionDrawableName: String get() = toExpression().drawableResName

/**
 * Extension to resolve the strongly typed [Expression] from [CoachFeedback].
 */
val CoachFeedback.expressionState: Expression
    get() = Expression.fromEngineOutput(expression, mood)

/**
 * Extension to resolve the specific asset path from [CoachFeedback].
 */
val CoachFeedback.assetPath: String
    get() = expressionState.assetPath

/**
 * Extension to resolve the strongly typed [Expression] from [CoachMoodResult].
 */
val CoachMoodResult.expressionState: Expression
    get() = Expression.fromEngineOutput(expression, mood)

/**
 * Extension to resolve the specific asset path from [CoachMoodResult].
 */
val CoachMoodResult.assetPath: String
    get() = expressionState.assetPath
