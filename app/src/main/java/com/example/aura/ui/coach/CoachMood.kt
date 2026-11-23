package com.example.aura.ui.coach

/**
 * Large Emotional State Library for AURA
 * Covers 38 distinct emotional states across Neutral, Positive, Concerned,
 * Negative, Recovery, and Progress-Specific categories.
 */
enum class CoachMood(
    val displayName: String,
    val category: String
) {
    // --- NEUTRAL ---
    NEUTRAL("Neutral", "Neutral"),
    CALM("Calm", "Neutral"),
    OBSERVING("Observing", "Neutral"),
    THINKING("Thinking", "Neutral"),
    WAITING("Waiting", "Neutral"),
    AWARE("Aware", "Neutral"),
    CURIOUS("Curious", "Neutral"),

    // --- POSITIVE ---
    PLEASED("Pleased", "Positive"),
    HAPPY("Happy", "Positive"),
    JOYFUL("Joyful", "Positive"),
    PROUD("Proud", "Positive"),
    EXCITED("Excited", "Positive"),
    CELEBRATING("Celebrating", "Positive"),
    IMPRESSED("Impressed", "Positive"),
    RELIEVED("Relieved", "Positive"),
    CONFIDENT("Confident", "Positive"),
    ENCOURAGING("Encouraging", "Positive"),
    PLAYFUL("Playful", "Positive"),

    // --- CONCERNED ---
    CONCERNED("Concerned", "Concerned"),
    WORRIED("Worried", "Concerned"),
    UNCERTAIN("Uncertain", "Concerned"),
    SKEPTICAL("Skeptical", "Concerned"),
    DOUBTFUL("Doubtful", "Concerned"),
    SURPRISED("Surprised", "Concerned"),
    CONFUSED("Confused", "Concerned"),
    CAUTIOUS("Cautious", "Concerned"),

    // --- NEGATIVE ---
    DISAPPOINTED("Disappointed", "Negative"),
    FRUSTRATED("Frustrated", "Negative"),
    ANNOYED("Annoyed", "Negative"),
    SAD("Sad", "Negative"),
    SERIOUS("Serious", "Negative"),
    UPSET("Upset", "Negative"),
    SHOCKED("Shocked", "Negative"),

    // --- RECOVERY ---
    SLEEPY("Sleepy", "Recovery"),
    SLEEPING("Sleeping", "Recovery"),
    TIRED("Tired", "Recovery"),
    EXHAUSTED("Exhausted", "Recovery"),
    RELAXED("Relaxed", "Recovery"),

    // --- PROGRESS-SPECIFIC ---
    DETERMINED("Determined", "Progress")
}

/**
 * Authoritative Evaluation Result returned by CoachMoodEngine
 */
data class CoachFeedback(
    val mood: CoachMood,
    val expression: String = mood.name.lowercase(),
    val headline: String,
    val message: String,
    val reason: String = "",
    val severity: String = "low", // "none", "low", "medium", "high"
    val suggestedAction: String? = null,
    val secondaryAdvice: String? = suggestedAction,
    val tag: String = "AURA COACH",
    val priority: Int = 10
)
