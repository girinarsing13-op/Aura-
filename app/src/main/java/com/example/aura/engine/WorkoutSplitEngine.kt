package com.example.aura.engine

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

enum class SplitType(
    val id: String,
    val title: String,
    val shortName: String,
    val primarySessions: Int,
    val displayFrequency: String
) {
    UPPER_LOWER(
        id = "UPPER_LOWER",
        title = "Upper / Lower (UL)",
        shortName = "UL",
        primarySessions = 4,
        displayFrequency = "4 Sessions / Rotation"
    ),
    PPL(
        id = "PPL",
        title = "Push / Pull / Legs (PPL)",
        shortName = "PPL",
        primarySessions = 6,
        displayFrequency = "6 Sessions / Rotation"
    ),
    ULPPL(
        id = "ULPPL",
        title = "Upper / Lower / Push / Pull / Legs (ULPPL)",
        shortName = "ULPPL",
        primarySessions = 5,
        displayFrequency = "5 Primary Training Sessions"
    ),
    BRO_SPLIT(
        id = "BRO_SPLIT",
        title = "Bro Split",
        shortName = "Bro Split",
        primarySessions = 5,
        displayFrequency = "5 Sessions / Rotation"
    ),
    FULL_BODY(
        id = "FULL_BODY",
        title = "Full Body",
        shortName = "Full Body",
        primarySessions = 3,
        displayFrequency = "3 Sessions / Rotation"
    ),
    UPPER_LOWER_4(
        id = "UPPER_LOWER_4",
        title = "4-Day Upper / Lower",
        shortName = "4-Day UL",
        primarySessions = 4,
        displayFrequency = "4 Sessions / Rotation"
    ),
    UPPER_LOWER_6(
        id = "UPPER_LOWER_6",
        title = "6-Day Upper / Lower",
        shortName = "6-Day UL",
        primarySessions = 6,
        displayFrequency = "6 Sessions / Rotation"
    ),
    FULL_BODY_4(
        id = "FULL_BODY_4",
        title = "4-Day Full Body",
        shortName = "4-Day Full Body",
        primarySessions = 4,
        displayFrequency = "4 Sessions / Rotation"
    ),
    FULL_BODY_6(
        id = "FULL_BODY_6",
        title = "6-Day Full Body",
        shortName = "6-Day Full Body",
        primarySessions = 6,
        displayFrequency = "6 Sessions / Rotation"
    );

    companion object {
        fun fromIdOrTitle(raw: String): SplitType {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return UPPER_LOWER
            entries.find { it.id.equals(trimmed, ignoreCase = true) }?.let { return it }
            entries.find { it.title.equals(trimmed, ignoreCase = true) }?.let { return it }
            val upper = trimmed.uppercase()
            return when {
                upper.contains("ULPPL") || (upper.contains("UPPER") && upper.contains("LOWER") && upper.contains("PUSH")) -> ULPPL
                upper == "PPL" || upper.startsWith("PUSH / PULL / LEGS") || upper.startsWith("PUSH/PULL/LEGS") || upper == "PPL_6" -> PPL
                upper == "BRO_SPLIT" || upper.contains("BRO SPLIT") || upper == "BRO" -> BRO_SPLIT
                upper.contains("6-DAY UPPER") || upper == "UPPER_LOWER_6" || upper.contains("6-DAY UL") -> UPPER_LOWER_6
                upper.contains("4-DAY UPPER") || upper.contains("UPPER / LOWER – 4 DAYS") || upper == "UPPER_LOWER_4" || upper == "4-DAY UL" -> UPPER_LOWER_4
                upper.contains("4-DAY FULL") || upper == "FULL_BODY_4" -> FULL_BODY_4
                upper.contains("6-DAY FULL") || upper == "FULL_BODY_6" -> FULL_BODY_6
                upper.contains("FULL BODY") || upper == "FULL_BODY" || upper == "FB" -> FULL_BODY
                upper.contains("UPPER") || upper.contains("LOWER") || upper == "UL" -> UPPER_LOWER
                else -> UPPER_LOWER
            }
        }
    }
}

data class SplitOption(
    val id: String,
    val title: String,
    val shortName: String,
    val explanation: String,
    val defaultDays: Int,
    val daysSummary: String = "",
    val distribution: String = "",
    val advantages: String = "",
    val limitations: String = "",
    val suitableFor: String = ""
) {
    val splitType: SplitType
        get() = when (id) {
            "ul" -> SplitType.UPPER_LOWER
            "ppl" -> SplitType.PPL
            "ulppl" -> SplitType.ULPPL
            "bro" -> SplitType.BRO_SPLIT
            "fb" -> SplitType.FULL_BODY
            "ul_4d" -> SplitType.UPPER_LOWER_4
            "ul_6d" -> SplitType.UPPER_LOWER_6
            "fb_4d" -> SplitType.FULL_BODY_4
            "fb_6d" -> SplitType.FULL_BODY_6
            else -> SplitType.fromIdOrTitle(title)
        }
}

data class SplitRecommendation(
    val splitTitle: String,
    val splitShortName: String,
    val whyExplanation: String
)

data class TimelineEstimate(
    val minMonths: Int,
    val maxMonths: Int,
    val estimatedRangeText: String,
    val rateText: String,
    val whyExplanation: String,
    val keyFactors: List<String>,
    val honestyDisclaimer: String
)

object WorkoutSplitEngine {
    val ALL_SPLITS: List<SplitOption> = listOf(
        SplitOption(
            id = "ul",
            title = "Upper / Lower (UL)",
            shortName = "UL",
            explanation = "Upper-body and lower-body training alternated across the week. Good balance of frequency, recovery and simplicity.",
            defaultDays = 4,
            daysSummary = "Normally 4 days / week (e.g., Mon, Tue, Thu, Fri)",
            distribution = "Alternates Upper body (chest, back, delts, arms) and Lower body (quads, hamstrings, glutes, calves, abs).",
            advantages = "Hits each muscle group 2x weekly with optimal recovery time between sessions; minimal systemic fatigue overlap.",
            limitations = "Upper body sessions can be lengthy if high isolation/arm volume is programmed.",
            suitableFor = "Beginners to advanced lifters wanting balanced muscle growth and consistent recovery."
        ),
        SplitOption(
            id = "ppl",
            title = "Push / Pull / Legs (PPL)",
            shortName = "PPL",
            explanation = "Push muscles, pull muscles and legs are trained separately. Good for users who can consistently train 5–6 days/week.",
            defaultDays = 6,
            daysSummary = "Normally 6 days / week (or 3 days on a rotational schedule)",
            distribution = "Push (chest, front/side delts, triceps) – Pull (lats, mid/upper back, rear delts, biceps) – Legs (quads, hamstrings, glutes, calves).",
            advantages = "Synergistic muscle grouping avoids joint interference; high per-muscle volume and specialization.",
            limitations = "Requires strict adherence and high recovery capacity when run 6 days per week.",
            suitableFor = "Intermediate to advanced lifters able to commit to 5–6 training days consistently."
        ),
        SplitOption(
            id = "ulppl",
            title = "Upper / Lower / Push / Pull / Legs (ULPPL)",
            shortName = "ULPPL",
            explanation = "Upper, Lower, Push, Pull, Legs with an optional sixth/recovery day. Good for users wanting higher training frequency.",
            defaultDays = 5,
            daysSummary = "Normally 5 days / week (with optional 6th active recovery day)",
            distribution = "Day 1 Upper (strength) – Day 2 Lower (strength) – Day 3 Push (hypertrophy) – Day 4 Pull (hypertrophy) – Day 5 Legs (hypertrophy).",
            advantages = "Combines heavy compound strength work early in the week with high-volume bodybuilding pump work later.",
            limitations = "Requires thoughtful weekly fatigue management to prevent lower back and shoulder fatigue accumulation.",
            suitableFor = "Lifters with 5 available days wanting the ultimate blend of strength progression and muscle hypertrophy."
        ),
        SplitOption(
            id = "bro",
            title = "Bro Split",
            shortName = "Bro Split",
            explanation = "A different major muscle group is emphasized on different days. Simple and familiar, but generally lower training frequency per muscle.",
            defaultDays = 5,
            daysSummary = "Normally 5 days / week (e.g., Chest, Back, Shoulders, Arms, Legs)",
            distribution = "Single dedicated day per major body part (Chest – Back – Shoulders – Arms – Legs & Core).",
            advantages = "Maximum per-muscle session focus and extreme muscle pump; simple and mentally satisfying.",
            limitations = "Only 1x/week frequency per muscle group, which may be suboptimal for natural muscle protein synthesis cycles.",
            suitableFor = "Bodybuilders who enjoy exhausting one muscle group completely per workout."
        ),
        SplitOption(
            id = "fb",
            title = "Full Body",
            shortName = "Full Body",
            explanation = "Most major muscle groups are trained during each session. Excellent for beginners or people training fewer days per week.",
            defaultDays = 3,
            daysSummary = "Normally 3 days / week (e.g., Mon, Wed, Fri with rest days between)",
            distribution = "Each session stimulates chest, back, quads, hamstrings, shoulders, and core with compound movements.",
            advantages = "Maximum protein synthesis frequency (3x/week); highly flexible and forgiving if a session is missed.",
            limitations = "Session volume per muscle is capped to keep total fatigue manageable.",
            suitableFor = "Beginners, busy athletes, or anyone training 2–3 days per week."
        ),
        SplitOption(
            id = "ul_4d",
            title = "4-Day Upper / Lower",
            shortName = "4-Day UL",
            explanation = "Four focused sessions (2 Upper, 2 Lower) with dedicated midweek rest. Gold standard for balancing frequency and recovery.",
            defaultDays = 4,
            daysSummary = "Strictly 4 days / week (Upper A, Lower A, Upper B, Lower B)",
            distribution = "Upper A (horizontal emphasis), Lower A (squat emphasis), Upper B (vertical emphasis), Lower B (hinge emphasis).",
            advantages = "3 complete rest days weekly; allows maximum intensity without central nervous system burnout.",
            limitations = "Limited room for high-volume isolation accessories on individual days.",
            suitableFor = "The gold-standard baseline for natural athletes balancing work, life, and heavy training."
        ),
        SplitOption(
            id = "ul_6d",
            title = "6-Day Upper / Lower",
            shortName = "6-Day UL",
            explanation = "High-frequency rotation alternating upper and lower sessions across 6 days for athletes with high recovery capacity.",
            defaultDays = 6,
            daysSummary = "6 days / week (Upper A, Lower A, Upper B, Lower B, Upper C, Lower C)",
            distribution = "Repeated Upper / Lower rotations with undulating set volumes and exercise variations.",
            advantages = "High total weekly volume distributed across manageable daily bite-sized workouts.",
            limitations = "Requires pristine sleep, nutrition, and stress management to prevent overreaching.",
            suitableFor = "Experienced lifters with superior recovery capacity seeking maximum training frequency."
        ),
        SplitOption(
            id = "fb_4d",
            title = "4-Day Full Body",
            shortName = "4-Day Full Body",
            explanation = "Four full-body sessions distributing total weekly volume evenly, perfect for metabolic conditioning and general fitness.",
            defaultDays = 4,
            daysSummary = "4 days / week (Full Body A, B, C, D with alternating intensities)",
            distribution = "Compounds targeting upper and lower musculature each session with undulating intensity.",
            advantages = "Distributes systemic weekly volume into shorter sessions; high calorie expenditure.",
            limitations = "Requires careful exercise selection so consecutive training days do not overload the same joints.",
            suitableFor = "Athletes prioritizing athletic conditioning, body recomposition, or general functional fitness."
        ),
        SplitOption(
            id = "fb_6d",
            title = "6-Day Full Body",
            shortName = "6-Day Full Body",
            explanation = "High-frequency, low-per-session volume format stimulating all major muscle groups daily with undulating intensities.",
            defaultDays = 6,
            daysSummary = "6 days / week (Micro-dose daily volume A through F)",
            distribution = "1–2 compound exercises per muscle group daily, varying between heavy, moderate, and light tempos.",
            advantages = "Frequent motor pattern practice; workouts can be short (35–45 min); very high weekly frequency.",
            limitations = "Requires strict discipline to not exceed prescribed per-session volume.",
            suitableFor = "Advanced lifters who thrive on short, daily workouts and high neuromuscular frequency."
        )
    )

    fun recommend(
        daysPerWeek: Int,
        goal: String,
        experience: String,
        environment: String = "Full Gym",
        priorityMuscles: List<String> = emptyList(),
        injuries: List<String> = emptyList()
    ): SplitRecommendation {
        val priorityNote = if (priorityMuscles.isNotEmpty()) {
            " while accommodating dedicated focus for your priority ${priorityMuscles.joinToString(" and ")}"
        } else ""

        val injuryNote = if (injuries.isNotEmpty() && !injuries.contains("None")) {
            " while safeguarding joint recovery for your ${injuries.filter { it != "None" }.joinToString(", ")}"
        } else ""

        return when {
            daysPerWeek <= 3 -> {
                SplitRecommendation(
                    splitTitle = "Full Body",
                    splitShortName = "Full Body",
                    whyExplanation = "Fits your $daysPerWeek-day availability and ${goal.lowercase()} goal by stimulating every major muscle group each session with 48h+ recovery between workouts$priorityNote$injuryNote."
                )
            }
            daysPerWeek == 4 -> {
                if (experience.equals("Beginner", ignoreCase = true) || goal.equals("General Fitness", ignoreCase = true)) {
                    SplitRecommendation(
                        splitTitle = "4-Day Full Body",
                        splitShortName = "4-Day Full Body",
                        whyExplanation = "Fits your 4-day schedule and beginner experience by distributing movement patterns evenly across the week for steady motor learning and recovery$priorityNote$injuryNote."
                    )
                } else {
                    SplitRecommendation(
                        splitTitle = "4-Day Upper / Lower",
                        splitShortName = "4-Day UL",
                        whyExplanation = "Fits your 4-day availability and ${goal.lowercase()} goal with the gold-standard 2x/week frequency per muscle group and dedicated recovery days$priorityNote$injuryNote."
                    )
                }
            }
            daysPerWeek == 5 -> {
                if (experience.equals("Beginner", ignoreCase = true)) {
                    SplitRecommendation(
                        splitTitle = "Bro Split",
                        splitShortName = "Bro Split",
                        whyExplanation = "Fits your 5-day schedule by focusing on one primary muscle group per workout, keeping individual sessions simple, focused, and manageable$priorityNote$injuryNote."
                    )
                } else {
                    SplitRecommendation(
                        splitTitle = "Upper / Lower / Push / Pull / Legs (ULPPL)",
                        splitShortName = "ULPPL",
                        whyExplanation = "Fits your 5-day availability and ${goal.lowercase()} goal by pairing heavy compound upper/lower strength with dedicated push/pull/legs hypertrophy$priorityNote$injuryNote."
                    )
                }
            }
            daysPerWeek >= 6 -> {
                if (goal.equals("Muscle Gain", ignoreCase = true)) {
                    SplitRecommendation(
                        splitTitle = "Push / Pull / Legs (PPL)",
                        splitShortName = "PPL",
                        whyExplanation = "Fits your 6-day availability and muscle-gain goal by rotating push, pull, and leg muscle groups twice per week with dedicated recovery between blocks$priorityNote$injuryNote."
                    )
                } else if (experience.equals("Advanced", ignoreCase = true)) {
                    SplitRecommendation(
                        splitTitle = "Upper / Lower / Push / Pull / Legs (ULPPL)",
                        splitShortName = "ULPPL",
                        whyExplanation = "Fits your 6-day availability and ${goal.lowercase()} goal while allowing each major muscle group to receive appropriate training frequency and recovery$priorityNote$injuryNote."
                    )
                } else {
                    SplitRecommendation(
                        splitTitle = "6-Day Upper / Lower",
                        splitShortName = "6-Day UL",
                        whyExplanation = "Fits your 6-day commitment by alternating upper and lower sessions with distributed daily volume for maximum stamina and recovery$priorityNote$injuryNote."
                    )
                }
            }
            else -> {
                SplitRecommendation(
                    splitTitle = "4-Day Upper / Lower",
                    splitShortName = "4-Day UL",
                    whyExplanation = "Fits your schedule and $goal goal with balanced frequency, recovery, and hypertrophy stimulus$priorityNote$injuryNote."
                )
            }
        }
    }

    fun reconcileSplitForDays(
        currentSplit: String,
        targetDays: Int,
        goal: String = "",
        experience: String = ""
    ): String {
        val clean = currentSplit.trim()
        val splitType = SplitType.fromIdOrTitle(clean)
        return when (targetDays) {
            6 -> when (splitType) {
                SplitType.ULPPL -> SplitType.ULPPL.title
                SplitType.FULL_BODY, SplitType.FULL_BODY_4, SplitType.FULL_BODY_6 -> SplitType.FULL_BODY_6.title
                SplitType.UPPER_LOWER, SplitType.UPPER_LOWER_4, SplitType.UPPER_LOWER_6 -> SplitType.UPPER_LOWER_6.title
                SplitType.BRO_SPLIT -> SplitType.BRO_SPLIT.title
                SplitType.PPL -> SplitType.PPL.title
            }
            4 -> when (splitType) {
                SplitType.FULL_BODY, SplitType.FULL_BODY_4, SplitType.FULL_BODY_6 -> SplitType.FULL_BODY_4.title
                SplitType.BRO_SPLIT -> SplitType.BRO_SPLIT.title
                else -> SplitType.UPPER_LOWER_4.title
            }
            5 -> when (splitType) {
                SplitType.BRO_SPLIT -> SplitType.BRO_SPLIT.title
                SplitType.FULL_BODY, SplitType.FULL_BODY_4, SplitType.FULL_BODY_6 -> SplitType.FULL_BODY.title
                SplitType.PPL -> SplitType.PPL.title
                else -> SplitType.ULPPL.title
            }
            3 -> when (splitType) {
                SplitType.PPL -> SplitType.PPL.title
                SplitType.BRO_SPLIT -> SplitType.BRO_SPLIT.title
                else -> SplitType.FULL_BODY.title
            }
            2 -> SplitType.UPPER_LOWER.title
            7 -> if (splitType == SplitType.PPL) SplitType.PPL.title else SplitType.UPPER_LOWER_6.title
            else -> currentSplit
        }
    }
}

object RealisticTimelineEngine {
    fun calculate(
        goal: String,
        currentWeightKg: Double,
        targetWeightKg: Double?,
        experience: String,
        daysPerWeek: Int,
        trainingSplit: String,
        environment: String,
        diet: String,
        injuries: List<String>
    ): TimelineEstimate {
        val tw = targetWeightKg ?: currentWeightKg
        val weightDelta = abs(tw - currentWeightKg)
        val activeInjuries = injuries.filter { it != "None" }

        when (goal) {
            "Muscle Gain" -> {
                val delta = if (tw > currentWeightKg) tw - currentWeightKg else 4.0
                // Natural monthly lean mass gain rates (kg/month):
                val (monthlyRateMin, monthlyRateMax) = when (experience.lowercase()) {
                    "beginner" -> 0.7 to 1.1 // ~0.7 - 1.1 kg/mo
                    "advanced" -> 0.2 to 0.4 // ~0.2 - 0.4 kg/mo
                    else -> 0.4 to 0.7 // Intermediate: ~0.4 - 0.7 kg/mo
                }
                // Days multiplier: less than 4 days slightly elongates timeline
                val freqFactor = when {
                    daysPerWeek <= 3 -> 1.18
                    daysPerWeek >= 5 -> 0.95
                    else -> 1.0
                }
                // Injury factor
                val injuryFactor = if (activeInjuries.isNotEmpty()) 1.12 else 1.0

                val rawMinMonths = ((delta / monthlyRateMax) * freqFactor * injuryFactor).roundToInt().coerceIn(3, 24)
                val rawMaxMonths = ((delta / monthlyRateMin) * freqFactor * injuryFactor).roundToInt().coerceIn(rawMinMonths + 2, 28)

                val factors = mutableListOf<String>()
                factors.add("Natural muscle hypertrophy rate for $experience level: ~${monthlyRateMin}–${monthlyRateMax} kg/month.")
                factors.add("Selected $trainingSplit with $daysPerWeek days/week provides targeted mechanical tension and progressive overload.")
                if (diet == "Vegetarian" || diet == "Vegan") {
                    factors.add("Plant-forward nutrition requires diligent leucine and total daily protein target adherence.")
                } else {
                    factors.add("High-bioavailability protein diet supports continuous myofibrillar protein synthesis.")
                }
                if (activeInjuries.isNotEmpty()) {
                    factors.add("Paced deliberately to protect ${activeInjuries.joinToString(", ")} and avoid acute flare-ups.")
                }

                return TimelineEstimate(
                    minMonths = rawMinMonths,
                    maxMonths = rawMaxMonths,
                    estimatedRangeText = "approximately $rawMinMonths–$rawMaxMonths months",
                    rateText = "~$monthlyRateMin – $monthlyRateMax kg / month",
                    whyExplanation = "Aiming for +${String.format("%.1f", delta)} kg of quality lean muscle mass as an $experience lifter. Natural muscle protein synthesis produces realistic gains of $monthlyRateMin–$monthlyRateMax kg/month without excessive adipose gain. Your $trainingSplit with $daysPerWeek days/week provides the necessary stimulus and recovery windows.",
                    keyFactors = factors,
                    honestyDisclaimer = "Natural muscle building is a biological adaptation that cannot be rushed without gaining unwanted body fat. Aura does not offer unrealistic shortcuts; this timeline is grounded in verified natural physiology."
                )
            }
            "Fat Loss" -> {
                val delta = if (currentWeightKg > tw) currentWeightKg - tw else 6.0
                // Safe fat loss: 0.5% - 1.0% bodyweight per week (~1.5 to 2.5 kg / month)
                val monthlyLossMin = 1.5
                val monthlyLossMax = 2.4
                val freqFactor = if (daysPerWeek <= 3) 1.15 else 1.0

                val rawMinMonths = ((delta / monthlyLossMax) * freqFactor).roundToInt().coerceIn(2, 18)
                val rawMaxMonths = ((delta / monthlyLossMin) * freqFactor).roundToInt().coerceIn(rawMinMonths + 1, 24)

                val factors = mutableListOf<String>()
                factors.add("Safe, muscle-sparing caloric deficit targeting ~0.4–0.6 kg fat loss per week.")
                factors.add("$trainingSplit resistance training maintains resting metabolic rate and muscle retention.")
                factors.add("Target deficit prevents metabolic adaptation, hormonal crash, or chronic fatigue.")

                return TimelineEstimate(
                    minMonths = rawMinMonths,
                    maxMonths = rawMaxMonths,
                    estimatedRangeText = "approximately $rawMinMonths–$rawMaxMonths months",
                    rateText = "~$monthlyLossMin – $monthlyLossMax kg / month",
                    whyExplanation = "Safely reducing ${String.format("%.1f", delta)} kg of adipose tissue while fiercely protecting lean muscle mass. A moderate, calibrated deficit ensures hormonal stability, appetite control, and permanent fat loss rather than crash-diet rebound.",
                    keyFactors = factors,
                    honestyDisclaimer = "Sustainable fat loss preserves contractile tissue and thyroid health. Crash dieting causes muscle loss and metabolic slowdown; Aura enforces healthy, lasting body transformation."
                )
            }
            "Recomposition" -> {
                val minM = 6
                val maxM = 9
                return TimelineEstimate(
                    minMonths = minM,
                    maxMonths = maxM,
                    estimatedRangeText = "approximately $minM–$maxM months",
                    rateText = "Concurrent fat loss & muscle gain",
                    whyExplanation = "Body recomposition is an advanced metabolic protocol requiring simultaneous adipose mobilization and muscle protein synthesis at energy equilibrium. Measurable aesthetic and strength transformations typically manifest across 6 to 9 months of disciplined progressive overload.",
                    keyFactors = listOf(
                        "Energy balance near maintenance with high protein intake (${diet}).",
                        "Progressive overload driven by your $daysPerWeek-day $trainingSplit.",
                        "Body composition shifts visible in waist-to-shoulder ratio before major scale changes."
                    ),
                    honestyDisclaimer = "Recomposition requires patience and consistency. The scale may not move drastically, but body fat percentage and muscle density will visibly transform."
                )
            }
            else -> { // General Fitness
                val minM = 3
                val maxM = 6
                return TimelineEstimate(
                    minMonths = minM,
                    maxMonths = maxM,
                    estimatedRangeText = "approximately $minM–$maxM months",
                    rateText = "Systemic adaptation & stamina block",
                    whyExplanation = "Developing baseline athletic conditioning, cardiovascular stamina, joint resilience, and consistent lifestyle habits across a periodized 12-to-24 week training cycle.",
                    keyFactors = listOf(
                        "Balanced multi-joint movement patterns via $trainingSplit.",
                        "$daysPerWeek days per week establishes steady circadian rhythm and habit formation.",
                        "Gradual progressive loading prevents overtraining and supports longevity."
                    ),
                    honestyDisclaimer = "True fitness is an ongoing journey of health and vitality. This initial timeline builds your unbreakable foundation."
                )
            }
        }
    }
}
