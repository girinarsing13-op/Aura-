package com.example.aura.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.aura.data.db.AppDatabase
import com.example.aura.data.repository.AuraRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-registers WorkManager reminder schedules after device reboot or app update
 * whenever the user has completed onboarding and enabled workout/weigh-in reminders.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(appContext).auraDao()
                val profile = AuraRepository(dao).getUserProfileOnce()
                if (profile != null) {
                    WorkoutReminderScheduler.syncReminderSchedule(appContext, profile)
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
