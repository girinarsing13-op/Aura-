package com.example.aura.ui.components

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.aura.data.model.MovementPattern
import com.example.aura.engine.ExerciseDemoRepository
import com.example.aura.ui.theme.AuraTheme
import java.io.File

enum class DemoDisplayMode {
    AUTO,
    PHOTO,
    ANIMATION
}

private object ExerciseDemoImageLoader {
    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: ImageLoader.Builder(context.applicationContext)
                .respectCacheHeaders(false)
                .memoryCache {
                    MemoryCache.Builder(context.applicationContext)
                        .maxSizePercent(0.20)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(File(context.applicationContext.cacheDir, "exercise_demos"))
                        .maxSizeBytes(50L * 1024L * 1024L)
                        .build()
                }
                .build()
                .also { instance = it }
        }
    }
}

/**
 * Unified exercise demonstration component used across cards, guide sheets, thumbnails, swap sheets,
 * and warm-up drills. Automatically shows looping free-exercise-db photos (with Coil disk cache)
 * when matched, or seamlessly falls back to [AnimatedExerciseFigure] when unmatched, offline, or toggled.
 */
@Composable
fun ExerciseDemo(
    exerciseName: String,
    modifier: Modifier = Modifier,
    exerciseId: String = "",
    primaryMuscle: String = "",
    equipment: String = "",
    movementPattern: MovementPattern = MovementPattern.inferFrom(exerciseName, primaryMuscle),
    caption: String? = null,
    compact: Boolean = false,
    preferredMode: DemoDisplayMode = DemoDisplayMode.AUTO
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ExerciseDemoRepository.ensureInitialized(context)
    }

    val catalogState by ExerciseDemoRepository.state.collectAsState()
    val match = remember(catalogState, exerciseId, exerciseName) {
        ExerciseDemoRepository.resolveMatch(exerciseId = exerciseId, exerciseName = exerciseName)
    }

    val backdropColor = if (AuraTheme.colors.isDark) Color(0xFF0A0A12) else AuraTheme.colors.surfaceSecondary
    val cornerRadius = if (compact) 12.dp else 20.dp
    val shape = RoundedCornerShape(cornerRadius)
    val containerModifier = if (compact) {
        modifier
            .clip(shape)
            .background(AuraTheme.colors.surfaceSecondary)
            .border(1.dp, AuraTheme.colors.cardBorder, shape)
    } else {
        modifier
            .aspectRatio(4f / 3f)
            .clip(shape)
            .background(backdropColor)
            .border(1.dp, AuraTheme.colors.cardBorder, shape)
    }

    val offlineCaption = if (
        preferredMode != DemoDisplayMode.ANIMATION &&
        catalogState.remoteCatalogCount == 0 &&
        !catalogState.lastError.isNullOrBlank()
    ) {
        caption ?: "Demo will load when you're online"
    } else {
        caption
    }

    if (preferredMode == DemoDisplayMode.ANIMATION || match == null) {
        AnimatedExerciseFigure(
            movementPattern = movementPattern,
            equipment = equipment,
            caption = offlineCaption,
            compact = compact,
            modifier = containerModifier
                .fillMaxSize()
                .testTag("exercise_demo_anim_${exerciseId.ifBlank { exerciseName }}")
        )
        return
    }

    val imageLoader = remember(context) { ExerciseDemoImageLoader.get(context) }
    val url0 = match.startImageUrl
    val url1 = match.endImageUrl

    val request0 = remember(url0, match.remoteId, context) {
        ImageRequest.Builder(context)
            .data(url0)
            .diskCacheKey("${match.remoteId}_0")
            .memoryCacheKey("${match.remoteId}_0")
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(false)
            .build()
    }
    val request1 = remember(url1, match.remoteId, context) {
        ImageRequest.Builder(context)
            .data(url1)
            .diskCacheKey("${match.remoteId}_1")
            .memoryCacheKey("${match.remoteId}_1")
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(false)
            .build()
    }

    val painter0 = rememberAsyncImagePainter(model = request0, imageLoader = imageLoader)
    val painter1 = rememberAsyncImagePainter(model = request1, imageLoader = imageLoader)

    val isError = painter0.state is AsyncImagePainter.State.Error ||
        painter1.state is AsyncImagePainter.State.Error
    val isLoading = painter0.state is AsyncImagePainter.State.Loading ||
        painter1.state is AsyncImagePainter.State.Loading

    if (isError) {
        AnimatedExerciseFigure(
            movementPattern = movementPattern,
            equipment = equipment,
            caption = caption ?: "Demo will load when you're online",
            compact = compact,
            modifier = containerModifier
                .fillMaxSize()
                .testTag("exercise_demo_anim_${exerciseId.ifBlank { exerciseName }}")
        )
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "demo_crossfade_${match.remoteId}")
    val frameBlend by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "demo_frame_blend"
    )

    Box(
        modifier = containerModifier.testTag("exercise_demo_photo_${exerciseId.ifBlank { exerciseName }}"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter0,
            contentDescription = "$exerciseName start position",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painter1,
            contentDescription = "$exerciseName end position",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = frameBlend }
        )

        if (isLoading) {
            DemoShimmerPlaceholder(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun DemoShimmerPlaceholder(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "demo_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "demo_shimmer_offset"
    )
    val base = AuraTheme.colors.surfaceSecondary
    val highlight = AuraTheme.colors.cardBorder.copy(alpha = 0.55f)
    val brush = Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 300f, 300f)
    )
    Box(modifier = modifier.background(brush))
}
