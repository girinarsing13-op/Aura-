package com.example.aura.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.aura.data.model.UserProfile
import com.example.aura.engine.WorkoutScheduleEngine
import java.util.concurrent.TimeUnit

object WorkoutReminderScheduler {

    const val UNIQUE_WORK_NAME = "aura_daily_workout_reminder"
    const val WEIGH_IN_WORK_NAME = "aura_weekly_weighin_reminder"

    fun syncReminderSchedule(context: Context, profile: UserProfile) {
        val workManager = try {
            WorkManager.getInstance(context)
        } catch (_: IllegalStateException) {
            return
        }

        if (!profile.onboardingCompleted || !profile.workoutReminderEnabled) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
        } else {
            WorkoutReminderWorker.ensureNotificationChannel(context)

            val delayMillis = WorkoutScheduleEngine.computeInitialDelayMillis(
                preferredWorkoutMinutes = profile.preferredWorkoutMinutes
            )

            val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        syncWeighInReminder(context, profile, workManager)
    }

    private fun syncWeighInReminder(
        context: Context,
        profile: UserProfile,
        workManager: WorkManager
    ) {
        if (!profile.onboardingCompleted || !profile.weighInReminderEnabled) {
            workManager.cancelUniqueWork(WEIGH_IN_WORK_NAME)
            return
        }
        WeighInReminderWorker.ensureNotificationChannel(context)
        val request = PeriodicWorkRequestBuilder<WeighInReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(7, TimeUnit.DAYS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            WEIGH_IN_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelReminders(context: Context) {
        try {
            val wm = WorkManager.getInstance(context)
            wm.cancelUniqueWork(UNIQUE_WORK_NAME)
            wm.cancelUniqueWork(WEIGH_IN_WORK_NAME)
        } catch (_: IllegalStateException) {
        }
    }
}
