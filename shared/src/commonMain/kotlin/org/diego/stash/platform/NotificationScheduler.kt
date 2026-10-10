package org.diego.stash.platform

import org.diego.stash.domain.model.FoodItem
import org.diego.stash.domain.model.ReminderSettings
import kotlinx.datetime.LocalTime

interface NotificationScheduler {
    fun scheduleExpiryReminders(item: FoodItem, settings: ReminderSettings)
    fun cancelExpiryReminders(item: FoodItem)
    fun cancelAll()
    fun rescheduleAll(items: List<FoodItem>, settings: ReminderSettings)
    fun updateDailySummary(time: LocalTime, enabled: Boolean)
}
