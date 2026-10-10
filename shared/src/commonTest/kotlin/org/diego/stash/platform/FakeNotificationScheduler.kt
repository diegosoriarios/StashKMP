package org.diego.stash.platform

import kotlinx.datetime.LocalTime
import org.diego.stash.domain.model.FoodItem
import org.diego.stash.domain.model.ReminderSettings

class FakeNotificationScheduler : NotificationScheduler {
    val scheduledItems = mutableListOf<Pair<FoodItem, ReminderSettings>>()
    var cancelledAll = false
        private set

    override fun scheduleExpiryReminders(item: FoodItem, settings: ReminderSettings) {
        scheduledItems.add(item to settings)
    }

    override fun cancelExpiryReminders(item: FoodItem) {
        scheduledItems.removeAll { it.first.id == item.id }
    }

    override fun cancelAll() {
        cancelledAll = true
        scheduledItems.clear()
    }

    override fun updateDailySummary(time: LocalTime, enabled: Boolean) {
        // No-op
    }

    override fun rescheduleAll(items: List<FoodItem>, settings: ReminderSettings) {
        scheduledItems.clear()
        items.forEach { scheduledItems.add(it to settings) }
    }
}
