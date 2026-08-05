package org.diego.stash.platform

import kotlinx.datetime.LocalTime
import org.diego.stash.domain.model.FoodItem

class FakeNotificationScheduler : NotificationScheduler {
    val scheduledItems = mutableListOf<FoodItem>()

    override fun scheduleExpiryReminders(item: FoodItem) {
        scheduledItems.add(item)
    }

    override fun cancelExpiryReminders(item: FoodItem) {
        scheduledItems.removeAll { it.id == item.id }
    }

    override fun updateDailySummary(time: LocalTime, enabled: Boolean) {
        // No-op
    }

    override fun rescheduleAll(items: List<FoodItem>) {
        scheduledItems.clear()
        scheduledItems.addAll(items)
    }
}
