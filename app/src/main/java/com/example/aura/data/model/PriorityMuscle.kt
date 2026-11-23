package com.example.aura.data.model

/**
 * Authoritative Canonical Priority Muscle Groups.
 * Single source of truth for priority muscle selection, display, and volume allocation.
 * Exactly 7 canonical muscle groups. Max 2 can be selected at any given time.
 */
enum class PriorityMuscle(
    val id: String,
    val displayName: String,
    val keywords: List<String>
) {
    CHEST(
        id = "chest",
        displayName = "Chest",
        keywords = listOf("chest", "pecs", "pectoral", "pectoralis")
    ),
    BACK(
        id = "back",
        displayName = "Back",
        keywords = listOf("back", "lats", "latissimus", "rhomboids", "upper back", "rear delt")
    ),
    SHOULDERS(
        id = "shoulders",
        displayName = "Shoulders",
        keywords = listOf("shoulders", "delts", "deltoid", "lateral delt", "anterior delt")
    ),
    ARMS(
        id = "arms",
        displayName = "Arms",
        keywords = listOf("arms", "biceps", "triceps", "bicep", "tricep", "forearms", "brachialis")
    ),
    QUADS(
        id = "quads",
        displayName = "Quads",
        keywords = listOf("quads", "quadriceps", "vastus", "rectus femoris")
    ),
    HAMSTRINGS_GLUTES(
        id = "hamstrings_glutes",
        displayName = "Hamstrings / Glutes",
        keywords = listOf("hamstrings", "glutes", "gluteus", "hamstring", "biceps femoris", "posterior", "hamstrings / glutes", "hamstrings & glutes")
    ),
    ABS_CORE(
        id = "abs_core",
        displayName = "Abs / Core",
        keywords = listOf("abs", "core", "abdominals", "obliques", "rectus abdominis", "abs_core", "abs & core", "abs / core", "abs and core")
    );

    companion object {
        val ALL: List<PriorityMuscle> = entries

        /**
         * Resolves any legacy string, raw ID, enum name, or display name to canonical PriorityMuscle.
         */
        fun fromString(raw: String): PriorityMuscle? {
            val clean = raw.trim()
            if (clean.isBlank() || clean.equals("none", ignoreCase = true) || clean.equals("balanced", ignoreCase = true)) return null

            val upper = clean.uppercase()
            // 1. Direct enum name or uppercase id match
            entries.find { it.name == upper || it.id.uppercase() == upper }?.let { return it }

            // 2. Exact display name (case-insensitive)
            entries.find { it.displayName.equals(clean, ignoreCase = true) }?.let { return it }

            // 3. Normalizing separator variations ('/' vs '&')
            val simplified = clean.lowercase().replace("/", "&").replace("  ", " ").trim()
            entries.find { it.displayName.lowercase().replace("/", "&").replace("  ", " ").trim() == simplified }?.let { return it }

            // 4. Keyword containment
            val lower = clean.lowercase()
            return entries.find { entry ->
                entry.keywords.any { keyword -> lower.contains(keyword) }
            }
        }

        /**
         * Normalizes a collection of strings into 0–2 unique canonical display names.
         * Eliminates duplicates, stale/unrecognized tokens, and empty/none placeholders.
         */
        fun normalize(rawList: List<String>): List<String> {
            return rawList
                .mapNotNull { fromString(it)?.displayName }
                .distinct()
                .take(2)
        }

        /**
         * Authoritative conversion to Set<PriorityMuscle> (max 2).
         * Guarantees zero ghost/hidden selections since every element is one of the 7 visible options.
         */
        fun toSet(rawList: List<String>): Set<PriorityMuscle> {
            return rawList
                .mapNotNull { fromString(it) }
                .distinct()
                .take(2)
                .toSet()
        }
    }
}

typealias MuscleGroup = PriorityMuscle
