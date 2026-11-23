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
import com.example.aura.engine.ProgressionEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeighInReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).auraDao()
        val profile = dao.getUserProfileOnce() ?: return Result.success()

        if (!profile.onboardingCompleted || !profile.weighInReminderEnabled) {
            return Result.success()
        }

        val checkIns = dao.getAllBodyCheckInsOnce()
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val latestKey = checkIns.maxByOrNull { it.dateKey }?.dateKey
        if (latestKey != null && ProgressionEngine.daysBetween(latestKey, todayKey) < 6) {
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

        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1002,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Weekly weigh-in check-in")
            .setContentText("Log today's weight in Progress to keep your trend accurate.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "aura_weighin_reminders"
        const val CHANNEL_NAME = "Weekly Weigh-In Reminders"
        const val NOTIFICATION_ID = 7402

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return
                if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Gentle weekly reminder to log a body weight check-in."
                    }
                    manager.createNotificationChannel(channel)
                }
            }
        }
    }
}
