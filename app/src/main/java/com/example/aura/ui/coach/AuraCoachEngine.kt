package com.example.aura.ui.coach

import com.example.aura.data.model.Blueprint
import com.example.aura.data.model.DailyHabitStatus
import com.example.aura.data.model.SetLog
import com.example.aura.data.model.UserProfile
import com.example.aura.data.model.WorkoutSessionRecord

/**
 * Authoritative Coach Engine facade delegating to CoachMoodEngine
 */
object AuraCoachEngine {
    fun evaluate(
        profile: UserProfile?,
        blueprint: Blueprint?,
        history: List<WorkoutSessionRecord>,
        habits: Map<String, DailyHabitStatus>,
        isSessionActive: Boolean = false,
        sessionLogs: Map<Int, List<SetLog>> = emptyMap(),
        activeDayNumber: Int = 1
    ): CoachFeedback {
        return CoachMoodEngine.evaluateUserState(
            profile = profile,
            blueprint = blueprint,
            history = history,
            habits = habits,
            isSessionActive = isSessionActive,
            sessionLogs = sessionLogs,
            activeDayNumber = activeDayNumber
        )
    }
}
