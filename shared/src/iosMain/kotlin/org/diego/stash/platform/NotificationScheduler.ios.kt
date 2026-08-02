package org.diego.stash.platform

import kotlinx.datetime.LocalTime
import org.diego.stash.domain.model.FoodItem

class IosNotificationScheduler : NotificationScheduler {
    override fun scheduleExpiryReminders(item: FoodItem) {
        // Implement with UNUserNotificationCenter
    }

    override fun cancelExpiryReminders(item: FoodItem) {
        // Implement with UNUserNotificationCenter
    }

    override fun updateDailySummary(time: LocalTime, enabled: Boolean) {
        // Implement with UNUserNotificationCenter
    }

    override fun rescheduleAll(items: List<FoodItem>) {
        items.forEach { scheduleExpiryReminders(it) }
    }
}
