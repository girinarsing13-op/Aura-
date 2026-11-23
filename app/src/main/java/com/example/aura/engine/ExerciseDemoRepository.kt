package com.example.aura.engine

import android.content.Context
import com.example.aura.data.model.MovementPattern
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class DemoCoverageEntry(
    val id: String,
    val name: String,
    val source: String, // "ExerciseDB" or "Warm-up"
    val movementPattern: MovementPattern,
    val match: ExerciseDemoMatch?
)

data class DemoCatalogState(
    val matchesById: Map<String, ExerciseDemoMatch> = emptyMap(),
    val matchesByNormalizedName: Map<String, ExerciseDemoMatch> = emptyMap(),
    val remoteCatalogCount: Int = 0,
    val lastFetchedAtMillis: Long = 0L,
    val isRefreshing: Boolean = false,
    val lastError: String? = null
)

/**
 * Downloads, caches, and weekly-refreshes `free-exercise-db/dist/exercises.json` using OkHttp,
 * persists the raw JSON + match results in app-private storage, and exposes reactive demo matches
 * plus a live diagnostics coverage report across all ExerciseDB and WarmupMobilityEngine moves.
 */
object ExerciseDemoRepository {
    const val CATALOG_URL =
        "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json"
    private const val CATALOG_FILE_NAME = "free_exercise_db_exercises.json"
    private const val MATCH_CACHE_FILE_NAME = "exercise_demo_match_cache.json"
    private const val PREFS_NAME = "exercise_demo_catalog_prefs"
    private const val KEY_LAST_FETCH_MS = "last_fetch_ms"
    private const val WEEKLY_REFRESH_MS = 7L * 24L * 60L * 60L * 1000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val _state = MutableStateFlow(DemoCatalogState())
    val state: StateFlow<DemoCatalogState> = _state.asStateFlow()

    @Volatile
    private var initStarted = false

    fun ensureInitialized(context: Context) {
        if (initStarted) return
        synchronized(this) {
            if (initStarted) return
            initStarted = true
        }
        val appContext = context.applicationContext
        scope.launch {
            loadLocalCaches(appContext)
            val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastFetch = prefs.getLong(KEY_LAST_FETCH_MS, 0L)
            val catalogFile = File(appContext.filesDir, CATALOG_FILE_NAME)
            val needsRefresh = !catalogFile.exists() ||
                catalogFile.length() == 0L ||
                (System.currentTimeMillis() - lastFetch) >= WEEKLY_REFRESH_MS
            if (needsRefresh) {
                refreshCatalogInternal(appContext)
            }
        }
    }

    fun refreshNow(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            refreshCatalogInternal(appContext)
        }
    }

    fun resolveMatch(exerciseId: String, exerciseName: String): ExerciseDemoMatch? {
        val snapshot = _state.value
        if (exerciseId.isNotBlank()) {
            snapshot.matchesById[exerciseId]?.let { return it }
        }
        val key = ExerciseDemoMatcher.normalizeName(exerciseName)
        return snapshot.matchesByNormalizedName[key]
    }

    fun buildCoverageReport(): List<DemoCoverageEntry> {
        val snapshot = _state.value
        val dbEntries = ExerciseDB.list.map { ex ->
            DemoCoverageEntry(
                id = ex.id,
                name = ex.name,
                source = "ExerciseDB",
                movementPattern = ex.movementPattern,
                match = snapshot.matchesById[ex.id]
            )
        }
        val warmupEntries = WarmupMobilityEngine.allWarmupExercises().map { wu ->
            DemoCoverageEntry(
                id = wu.id,
                name = wu.name,
                source = "Warm-up",
                movementPattern = wu.movementPattern,
                match = snapshot.matchesById[wu.id]
            )
        }
        return dbEntries + warmupEntries
    }

    private fun loadLocalCaches(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastFetch = prefs.getLong(KEY_LAST_FETCH_MS, 0L)
        val matchFile = File(context.filesDir, MATCH_CACHE_FILE_NAME)
        val catalogFile = File(context.filesDir, CATALOG_FILE_NAME)

        if (matchFile.exists() && matchFile.length() > 0L) {
            try {
                val cached = parseMatchCacheJson(matchFile.readText())
                val count = if (catalogFile.exists()) countJsonArraySafe(catalogFile) else 0
                if (cached.isNotEmpty()) {
                    publishMatches(cached, count, lastFetch, isRefreshing = false, error = null)
                    return
                }
            } catch (_: Exception) {
                // Fall through to rebuild from catalog JSON
            }
        }

        if (catalogFile.exists() && catalogFile.length() > 0L) {
            try {
                val catalog = parseRemoteCatalogJson(catalogFile.readText())
                val matches = ExerciseDemoMatcher.matchCatalog(
                    exercises = ExerciseDB.list,
                    warmups = WarmupMobilityEngine.allWarmupExercises(),
                    catalog = catalog
                )
                saveMatchCache(matchFile, matches)
                publishMatches(matches, catalog.size, lastFetch, isRefreshing = false, error = null)
            } catch (_: Exception) {
                // Keep empty match map; AnimatedExerciseFigure acts as guaranteed fallback
            }
        }
    }

    private fun refreshCatalogInternal(context: Context) {
        _state.value = _state.value.copy(isRefreshing = true, lastError = null)
        try {
            val request = Request.Builder()
                .url(CATALOG_URL)
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        lastError = "HTTP ${response.code}"
                    )
                    return
                }
                val body = response.body?.string()
                if (body.isNullOrBlank()) {
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        lastError = "Empty catalog payload"
                    )
                    return
                }

                val catalog = parseRemoteCatalogJson(body)
                if (catalog.isEmpty()) {
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        lastError = "No valid entries in catalog"
                    )
                    return
                }

                val catalogFile = File(context.filesDir, CATALOG_FILE_NAME)
                catalogFile.writeText(body)

                val matches = ExerciseDemoMatcher.matchCatalog(
                    exercises = ExerciseDB.list,
                    warmups = WarmupMobilityEngine.allWarmupExercises(),
                    catalog = catalog
                )
                val matchFile = File(context.filesDir, MATCH_CACHE_FILE_NAME)
                saveMatchCache(matchFile, matches)

                val now = System.currentTimeMillis()
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(KEY_LAST_FETCH_MS, now)
                    .apply()

                publishMatches(matches, catalog.size, now, isRefreshing = false, error = null)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isRefreshing = false,
                lastError = e.message ?: "Offline"
            )
        }
    }

    private fun publishMatches(
        matches: Map<String, ExerciseDemoMatch>,
        catalogCount: Int,
        lastFetchedAt: Long,
        isRefreshing: Boolean,
        error: String?
    ) {
        val byName = LinkedHashMap<String, ExerciseDemoMatch>(matches.size)
        for (m in matches.values) {
            byName[ExerciseDemoMatcher.normalizeName(m.auraName)] = m
        }
        _state.value = DemoCatalogState(
            matchesById = matches,
            matchesByNormalizedName = byName,
            remoteCatalogCount = catalogCount,
            lastFetchedAtMillis = lastFetchedAt,
            isRefreshing = isRefreshing,
            lastError = error
        )
    }

    fun parseRemoteCatalogJson(rawJson: String): List<RemoteExerciseEntry> {
        val arr = JSONArray(rawJson)
        val entries = ArrayList<RemoteExerciseEntry>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val id = obj.optString("id", "").trim()
            val name = obj.optString("name", "").trim()
            val imagesJson = obj.optJSONArray("images") ?: continue
            val images = ArrayList<String>(imagesJson.length())
            for (j in 0 until imagesJson.length()) {
                val p = imagesJson.optString(j, "").trim()
                if (p.isNotBlank()) images.add(p)
            }
            if (id.isBlank() || name.isBlank() || images.isEmpty()) continue

            val equipment = obj.optString("equipment", "").trim()
            val primaryJson = obj.optJSONArray("primaryMuscles")
            val primaryMuscles = mutableListOf<String>()
            if (primaryJson != null) {
                for (j in 0 until primaryJson.length()) {
                    val m = primaryJson.optString(j, "").trim()
                    if (m.isNotBlank()) primaryMuscles.add(m)
                }
            }
            val secondaryJson = obj.optJSONArray("secondaryMuscles")
            val secondaryMuscles = mutableListOf<String>()
            if (secondaryJson != null) {
                for (j in 0 until secondaryJson.length()) {
                    val m = secondaryJson.optString(j, "").trim()
                    if (m.isNotBlank()) secondaryMuscles.add(m)
                }
            }

            entries.add(
                RemoteExerciseEntry(
                    id = id,
                    name = name,
                    images = images,
                    equipment = equipment,
                    primaryMuscles = primaryMuscles,
                    secondaryMuscles = secondaryMuscles
                )
            )
        }
        return entries
    }

    private fun countJsonArraySafe(file: File): Int {
        return try {
            JSONArray(file.readText()).length()
        } catch (_: Exception) {
            0
        }
    }

    private fun saveMatchCache(file: File, matches: Map<String, ExerciseDemoMatch>) {
        try {
            val arr = JSONArray()
            for (m in matches.values) {
                val obj = JSONObject()
                obj.put("auraId", m.auraId)
                obj.put("auraName", m.auraName)
                obj.put("remoteId", m.remoteId)
                obj.put("remoteName", m.remoteName)
                obj.put("startImageUrl", m.startImageUrl)
                obj.put("endImageUrl", m.endImageUrl)
                obj.put("confidence", m.confidence)
                obj.put("equipmentMatched", m.equipmentMatched)
                obj.put("muscleMatched", m.muscleMatched)
                arr.put(obj)
            }
            file.writeText(arr.toString())
        } catch (_: Exception) {
            // Non-fatal cache write error
        }
    }

    private fun parseMatchCacheJson(rawJson: String): Map<String, ExerciseDemoMatch> {
        val arr = JSONArray(rawJson)
        val map = LinkedHashMap<String, ExerciseDemoMatch>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val auraId = obj.optString("auraId", "")
            if (auraId.isBlank()) continue
            map[auraId] = ExerciseDemoMatch(
                auraId = auraId,
                auraName = obj.optString("auraName", ""),
                remoteId = obj.optString("remoteId", ""),
                remoteName = obj.optString("remoteName", ""),
                startImageUrl = obj.optString("startImageUrl", ""),
                endImageUrl = obj.optString("endImageUrl", ""),
                confidence = obj.optDouble("confidence", 0.0),
                equipmentMatched = obj.optBoolean("equipmentMatched", false),
                muscleMatched = obj.optBoolean("muscleMatched", false)
            )
        }
        return map
    }
}
