package com.example.aura.engine

import com.example.aura.data.model.Exercise
import java.util.Locale

data class RemoteExerciseEntry(
    val id: String,
    val name: String,
    val images: List<String>,
    val equipment: String = "",
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList()
)

data class ExerciseDemoMatch(
    val auraId: String,
    val auraName: String,
    val remoteId: String,
    val remoteName: String,
    val startImageUrl: String,
    val endImageUrl: String,
    val confidence: Double,
    val equipmentMatched: Boolean,
    val muscleMatched: Boolean
)

/**
 * Automatically matches AURA exercises and warm-up drills against the public-domain
 * `yuhonas/free-exercise-db` JSON catalog using normalized token overlap, equipment/muscle
 * agreement boosts, synonym expansion, and a strict acceptance confidence threshold.
 */
object ExerciseDemoMatcher {
    const val IMAGE_BASE_URL =
        "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
    const val CONFIDENCE_THRESHOLD = 0.56

    private val STOP_WORDS = setOf(
        "the", "a", "an", "and", "or", "with", "on", "in", "to", "for", "of",
        "machine", "grip", "medium", "wide", "narrow", "standard", "regular",
        "basic", "exercise", "movement", "position", "dynamic", "controlled",
        "strict", "deep", "full", "bodyweight", "free", "weight", "loaded"
    )

    private val EQUIPMENT_TOKENS = setOf(
        "dumbbell", "barbell", "cable", "ezbar", "tbar", "bands",
        "smith", "kettlebell", "lever", "sled", "plate"
    )

    private val PHRASE_SYNONYMS: List<Pair<Regex, String>> = listOf(
        Regex("\\bpush[-\\s]?ups?\\b") to "pushups",
        Regex("\\bpull[-\\s]?ups?\\b") to "pullups",
        Regex("\\bchin[-\\s]?ups?\\b") to "chinups",
        Regex("\\bsit[-\\s]?ups?\\b") to "situps",
        Regex("\\bstep[-\\s]?ups?\\b") to "stepups",
        Regex("\\bt[-\\s]?bar\\b") to "tbar",
        Regex("\\be[-\\s]?z(?:[-\\s]?curl)?(?:[-\\s]?bar)?\\b") to "ezbar",
        Regex("\\bez[-\\s]?bar\\b") to "ezbar",
        Regex("\\bpull[-\\s]?apart\\b") to "pullapart",
        Regex("\\bstraight[-\\s]?arm\\b") to "straightarm",
        Regex("\\bclose[-\\s]?grip\\b") to "closegrip",
        Regex("\\bone[-\\s]?arm\\b") to "onearm",
        Regex("\\bsingle[-\\s]?arm\\b") to "onearm",
        Regex("\\bsingle[-\\s]?leg\\b") to "singleleg",
        Regex("\\bone[-\\s]?leg\\b") to "singleleg",
        Regex("\\bskull\\s?crushers?\\b") to "skullcrusher",
        Regex("\\bpec[-\\s]?deck\\b") to "butterfly",
        Regex("\\bhip\\s?thrusts?\\b") to "hipthrust",
        Regex("\\bglute\\s?bridges?\\b") to "glutebridge",
        Regex("\\bface\\s?pulls?\\b") to "facepull",
        Regex("\\bwall\\s?sit\\b") to "wallsit",
        Regex("\\bdead\\s?bug\\b") to "deadbug",
        Regex("\\bbird\\s?dog\\b") to "birddog",
        Regex("\\bair\\s?bike\\b") to "bicycle",
        Regex("\\blying\\s?triceps\\s?press\\b") to "skullcrusher",
        Regex("\\bneck\\s?curls?\\b") to "neck",
        Regex("\\brdl\\b") to "romanian deadlift",
        Regex("\\bbody\\s?only\\b") to "bodyweight",
        Regex("\\bresistance\\s?bands?\\b") to "bands"
    )

    private val TOKEN_SYNONYMS: Map<String, String> = mapOf(
        "db" to "dumbbell",
        "dumbbells" to "dumbbell",
        "dumbbell" to "dumbbell",
        "bb" to "barbell",
        "barbells" to "barbell",
        "barbell" to "barbell",
        "bw" to "bodyweight",
        "band" to "bands",
        "bands" to "bands",
        "bicep" to "biceps",
        "biceps" to "biceps",
        "tricep" to "triceps",
        "triceps" to "triceps",
        "quad" to "quads",
        "quadriceps" to "quads",
        "abs" to "abdominals",
        "ab" to "abdominals",
        "core" to "abdominals",
        "calves" to "calf",
        "calf" to "calf",
        "delts" to "delt",
        "deltoid" to "delt",
        "deltoids" to "delt",
        "shoulder" to "shoulders",
        "shoulders" to "shoulders",
        "glute" to "glutes",
        "glutes" to "glutes",
        "hamstring" to "hamstrings",
        "hamstrings" to "hamstrings",
        "lats" to "lat",
        "latissimus" to "lat",
        "flye" to "fly",
        "flyes" to "fly",
        "flies" to "fly",
        "raises" to "raise",
        "curls" to "curl",
        "extensions" to "extension",
        "presses" to "press",
        "squats" to "squat",
        "lunges" to "lunge",
        "rows" to "row",
        "dips" to "dip",
        "crunches" to "crunch",
        "planks" to "plank",
        "shrugs" to "shrug",
        "pulldowns" to "pulldown",
        "pushdowns" to "pushdown",
        "kickbacks" to "kickback",
        "deadlifts" to "deadlift",
        "bridges" to "bridge",
        "thrusts" to "thrust",
        "twists" to "twist",
        "pullovers" to "pullover",
        "alternating" to "alternate",
        "bicycle" to "bicycle",
        "bench" to "bench",
        "butterfly" to "butterfly",
        "fly" to "fly",
        "press" to "press",
        "pullup" to "pullups",
        "pullups" to "pullups",
        "pushup" to "pushups",
        "pushups" to "pushups",
        "chinup" to "chinups",
        "chinups" to "chinups"
    )

    fun normalizeName(raw: String): String {
        var text = raw.lowercase(Locale.US)
            .replace("/", " ")
            .replace("&", " ")
            .replace(",", " ")
            .replace("(", " ")
            .replace(")", " ")
            .replace(".", " ")
        for ((pattern, replacement) in PHRASE_SYNONYMS) {
            text = text.replace(pattern, " $replacement ")
        }
        return text.replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun tokenize(raw: String): Set<String> {
        val normalized = normalizeName(raw)
        if (normalized.isBlank()) return emptySet()
        return normalized.split(" ")
            .mapNotNull { token ->
                val mapped = TOKEN_SYNONYMS[token] ?: token
                if (mapped.isBlank() || mapped in STOP_WORDS) null else mapped
            }
            .toSet()
    }

    private fun normalizeEquipment(raw: String): String {
        val n = raw.lowercase(Locale.US)
        return when {
            n.contains("dumbbell") || n == "db" -> "dumbbell"
            n.contains("barbell") -> "barbell"
            n.contains("ez") || n.contains("e-z") -> "ezbar"
            n.contains("cable") -> "cable"
            n.contains("band") -> "bands"
            n.contains("body only") || n.contains("bodyweight") || n.contains("pull-up bar") || n.contains("dip") -> "bodyweight"
            n.contains("machine") || n.contains("leg press") || n.contains("pec deck") || n.contains("hack") ||
                n.contains("leg extension") || n.contains("leg curl") || n.contains("calf raise") ||
                n.contains("smith") || n.contains("pulldown") || n.contains("row machine") ||
                n.contains("glute drive") -> "machine"
            else -> ""
        }
    }

    private fun normalizeMuscleGroup(raw: String): Set<String> {
        val n = raw.lowercase(Locale.US)
        val out = mutableSetOf<String>()
        if (n.contains("chest") || n.contains("pector")) out.add("chest")
        if (n.contains("back") || n.contains("lat") || n.contains("rhomboid") || n.contains("erector")) out.add("back")
        if (n.contains("trap")) {
            out.add("traps")
            out.add("shoulders")
            out.add("back")
        }
        if (n.contains("shoulder") || n.contains("delt") || n.contains("rotator")) out.add("shoulders")
        if (n.contains("bicep") || n.contains("brachial") || n.contains("forearm")) out.add("biceps")
        if (n.contains("tricep")) out.add("triceps")
        if (n.contains("quad") || n.contains("knee") || n.contains("ankle") || n.contains("hip")) out.add("quads")
        if (n.contains("hamstring")) out.add("hamstrings")
        if (n.contains("glute")) out.add("glutes")
        if (n.contains("calv") || n.contains("calf") || n.contains("gastroc") || n.contains("soleus")) out.add("calves")
        if (n.contains("abs") || n.contains("abdom") || n.contains("oblique") || n.contains("core") || n.contains("spine")) out.add("abs")
        if (n.contains("neck")) out.add("neck")
        return out
    }

    fun buildImageUrl(relativePath: String): String {
        val cleaned = relativePath.trimStart('/')
        return if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            cleaned
        } else {
            "$IMAGE_BASE_URL$cleaned"
        }
    }

    fun matchSingle(
        auraId: String,
        auraName: String,
        auraMuscle: String,
        auraEquipment: List<String>,
        catalog: List<RemoteExerciseEntry>
    ): ExerciseDemoMatch? {
        if (catalog.isEmpty()) return null
        val qTokens = tokenize(auraName)
        if (qTokens.isEmpty()) return null

        val qEquipSet = auraEquipment.map { normalizeEquipment(it) }.filter { it.isNotBlank() }.toSet()
        val qMuscleSet = normalizeMuscleGroup(auraMuscle)
        val qActionTokens = qTokens - EQUIPMENT_TOKENS
        val normalizedQuery = normalizeName(auraName)

        var bestEntry: RemoteExerciseEntry? = null
        var bestScore = 0.0
        var bestEquipMatch = false
        var bestMuscleMatch = false

        for (entry in catalog) {
            if (entry.images.isEmpty()) continue
            val cTokens = tokenize(entry.name)
            if (cTokens.isEmpty()) continue

            val intersection = qTokens.intersect(cTokens)
            if (intersection.isEmpty()) continue

            // Require at least one non-equipment movement token in common so "Dumbbell Fly" doesn't match "Dumbbell Press"
            val nonEquipShared = intersection - EQUIPMENT_TOKENS
            if (nonEquipShared.isEmpty() && qActionTokens.isNotEmpty()) continue

            val qWeightSum = qTokens.sumOf { if (it in EQUIPMENT_TOKENS) 0.55 else 1.0 }
            val cWeightSum = cTokens.sumOf { if (it in EQUIPMENT_TOKENS) 0.55 else 1.0 }
            val sharedWeightSum = intersection.sumOf { if (it in EQUIPMENT_TOKENS) 0.55 else 1.0 }
            var score = (2.0 * sharedWeightSum) / (qWeightSum + cWeightSum)

            // Exact normalized match boost
            val normalizedCand = normalizeName(entry.name)
            if (normalizedQuery == normalizedCand) {
                score += 0.25
            } else if (cActionTokensMatchAll(qActionTokens, cTokens)) {
                score += 0.12
            }

            // Directional / conflicting stance guardrails
            if (("incline" in qTokens && "decline" in cTokens) ||
                ("decline" in qTokens && "incline" in cTokens)
            ) {
                score -= 0.45
            }
            if (("reverse" in qTokens && "reverse" !in cTokens) ||
                ("reverse" !in qTokens && "reverse" in cTokens)
            ) {
                score -= 0.18
            }
            if (("fly" in qTokens && "press" in cTokens && "fly" !in cTokens) ||
                ("press" in qTokens && "fly" in cTokens && "press" !in cTokens)
            ) {
                score -= 0.30
            }

            // Equipment boost / soft conflict
            val cEquip = normalizeEquipment(entry.equipment)
            val equipMatched = cEquip.isNotBlank() && cEquip in qEquipSet
            if (equipMatched) {
                score += 0.14
            } else if (cEquip.isNotBlank() && qEquipSet.isNotEmpty() &&
                !(cEquip == "bodyweight" && "bodyweight" in qEquipSet) &&
                !(cEquip == "machine" && "cable" in qEquipSet)
            ) {
                score -= 0.08
            }

            // Muscle agreement boost / mismatch penalty
            val cMuscles = (entry.primaryMuscles + entry.secondaryMuscles)
                .flatMap { normalizeMuscleGroup(it) }
                .toSet()
            val muscleMatched = qMuscleSet.isNotEmpty() && cMuscles.any { it in qMuscleSet }
            if (muscleMatched) {
                score += 0.14
            } else if (qMuscleSet.isNotEmpty() && cMuscles.isNotEmpty()) {
                score -= 0.16
            }

            if (score > bestScore) {
                bestScore = score
                bestEntry = entry
                bestEquipMatch = equipMatched
                bestMuscleMatch = muscleMatched
            }
        }

        val winner = bestEntry ?: return null
        val clampedConfidence = bestScore.coerceIn(0.0, 1.0)
        if (clampedConfidence < CONFIDENCE_THRESHOLD) return null

        val startPath = winner.images.first()
        val endPath = winner.images.getOrElse(1) { startPath }
        return ExerciseDemoMatch(
            auraId = auraId,
            auraName = auraName,
            remoteId = winner.id,
            remoteName = winner.name,
            startImageUrl = buildImageUrl(startPath),
            endImageUrl = buildImageUrl(endPath),
            confidence = clampedConfidence,
            equipmentMatched = bestEquipMatch,
            muscleMatched = bestMuscleMatch
        )
    }

    private fun cActionTokensMatchAll(qActionTokens: Set<String>, cTokens: Set<String>): Boolean {
        return qActionTokens.isNotEmpty() && qActionTokens.all { it in cTokens }
    }

    fun matchCatalog(
        exercises: List<Exercise>,
        warmups: List<WarmupExercise>,
        catalog: List<RemoteExerciseEntry>
    ): Map<String, ExerciseDemoMatch> {
        if (catalog.isEmpty()) return emptyMap()
        val results = LinkedHashMap<String, ExerciseDemoMatch>()
        for (ex in exercises) {
            val match = matchSingle(
                auraId = ex.id,
                auraName = ex.name,
                auraMuscle = "${ex.muscle} ${ex.primaryMuscle}",
                auraEquipment = ex.equipment,
                catalog = catalog
            )
            if (match != null) {
                results[ex.id] = match
            }
        }
        for (wu in warmups) {
            val match = matchSingle(
                auraId = wu.id,
                auraName = wu.name,
                auraMuscle = wu.targetJoints,
                auraEquipment = listOf("Bodyweight"),
                catalog = catalog
            )
            if (match != null) {
                results[wu.id] = match
            }
        }
        return results
    }
}
