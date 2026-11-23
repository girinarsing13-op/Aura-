package com.example.aura.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.aura.data.db.AppDatabase
import com.example.aura.data.repository.AuraRepository
import com.example.aura.engine.WorkoutScheduleEngine

class WorkoutReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).auraDao()
        val repo = AuraRepository(dao)
        val profile = repo.getUserProfileOnce() ?: return Result.success()

        if (!profile.onboardingCompleted || !profile.workoutReminderEnabled) {
            return Result.success()
        }

        if (!WorkoutScheduleEngine.isWorkoutDayForCalendar(profile)) {
            return Result.success()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return Result.success()
            }
        }

        ensureNotificationChannel(applicationContext)

        val blueprint = repo.getBlueprintOnce(profile)
        val todayKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val todayShort = java.text.SimpleDateFormat("EEE", java.util.Locale.US).format(java.util.Date())
        val weekdayIdx = (WorkoutScheduleEngine.calendarToWeekdayIndex() - 1).coerceIn(0, 6)
        val todayPlan = blueprint.workout.week.find { it.dateKey == todayKey }
            ?: blueprint.workout.week.find { it.scheduledDate.startsWith(todayShort, ignoreCase = true) }
            ?: blueprint.workout.week.getOrNull(weekdayIdx)
        if (todayPlan?.isRest == true) {
            return Result.success()
        }

        val focusLabel = todayPlan?.focus?.takeIf { it.isNotBlank() } ?: "Today's workout"
        val timeLabel = WorkoutScheduleEngine.formatTimeMinutes(profile.preferredWorkoutMinutes)

        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1001,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time to train: $focusLabel")
            .setContentText("Your ${profile.sessionDurationMinutes}-min session is set for $timeLabel.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "aura_workout_reminders"
        const val CHANNEL_NAME = "Workout Reminders"
        const val NOTIFICATION_ID = 7401

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return
                val existing = manager.getNotificationChannel(CHANNEL_ID)
                if (existing == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Daily workout reminders at your chosen training time."
                    }
                    manager.createNotificationChannel(channel)
                }
            }
        }
    }
}
