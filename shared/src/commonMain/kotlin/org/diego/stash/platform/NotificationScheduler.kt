package org.diego.stash.platform

import org.diego.stash.domain.model.FoodItem
import kotlinx.datetime.LocalTime

interface NotificationScheduler {
    fun scheduleExpiryReminders(item: FoodItem)
    fun cancelExpiryReminders(item: FoodItem)
    fun updateDailySummary(time: LocalTime, enabled: Boolean)
    fun rescheduleAll(items: List<FoodItem>)
}
