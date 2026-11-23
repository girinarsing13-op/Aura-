package com.example.aura.ui.character

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Fitness-related triggers published throughout the application
 */
sealed interface FitnessEvent {
    data class WorkoutStarted(val title: String, val dayNumber: Int = 1) : FitnessEvent
    data class SetCompleted(
        val exerciseName: String,
        val setIndex: Int,
        val totalSets: Int,
        val isPR: Boolean = false,
        val isDecline: Boolean = false,
        val isExcessiveWeightJump: Boolean = false
    ) : FitnessEvent
    data class WorkoutCompleted(
        val durationMinutes: Int,
        val totalVolumeKg: Double,
        val prCount: Int = 0
    ) : FitnessEvent
    data class StreakUpdated(val streakDays: Int, val isImproving: Boolean) : FitnessEvent
    data class RestTimerStarted(val durationSeconds: Int = 90) : FitnessEvent
    data object UserIdle : FitnessEvent
    data object UserReturned : FitnessEvent
}

/**
 * Lightweight, thread-safe internal Event Bus for publishing and listening to fitness triggers
 */
object AuraEventBus {
    private val _events = MutableSharedFlow<FitnessEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<FitnessEvent> = _events.asSharedFlow()

    fun post(event: FitnessEvent) {
        _events.tryEmit(event)
    }
}
