package org.diego.stash.platform

import android.content.Context
import kotlinx.datetime.LocalTime
import org.diego.stash.domain.model.FoodItem

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {
    override fun scheduleExpiryReminders(item: FoodItem) {
        // Implement with AlarmManager
    }

    override fun cancelExpiryReminders(item: FoodItem) {
        // Implement with AlarmManager
    }

    override fun updateDailySummary(time: LocalTime, enabled: Boolean) {
        // Implement with WorkManager or AlarmManager
    }

    override fun rescheduleAll(items: List<FoodItem>) {
        items.forEach { scheduleExpiryReminders(it) }
    }
}
