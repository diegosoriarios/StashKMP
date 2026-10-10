package org.diego.stash.platform

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.diego.stash.domain.model.FoodItem
import org.diego.stash.domain.model.FoodItemStatus
import org.diego.stash.domain.model.ReminderSettings
import platform.Foundation.NSTimeInterval
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

class IosNotificationScheduler : NotificationScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override fun scheduleExpiryReminders(item: FoodItem, settings: ReminderSettings) {
        if (item.status != FoodItemStatus.ACTIVE) return
        ensureAuthorization {
            offsetsFor(settings).forEach { daysBefore ->
                val triggerDate = item.expiryDate.minus(daysBefore, DateTimeUnit.DAY)
                if (triggerDate >= today()) {
                    addRequest(item, triggerDate, daysBefore)
                }
            }
        }
    }

    override fun cancelExpiryReminders(item: FoodItem) {
        center.removePendingNotificationRequestsWithIdentifiers(identifiersFor(item.id))
    }

    override fun cancelAll() {
        center.removeAllPendingNotificationRequests()
    }

    override fun rescheduleAll(items: List<FoodItem>, settings: ReminderSettings) {
        cancelAll()
        items.forEach { scheduleExpiryReminders(it, settings) }
    }

    override fun updateDailySummary(time: LocalTime, enabled: Boolean) {
        // Daily digest is not surfaced in the UI yet
    }

    private fun offsetsFor(settings: ReminderSettings) = buildList {
        if (settings.remind30Days) add(30)
        if (settings.remind7Days) add(7)
        if (settings.remind1Day) add(1)
    }

    private fun identifiersFor(id: Long) = listOf(
        "expiry_${id}_30",
        "expiry_${id}_7",
        "expiry_${id}_1"
    )

    private fun today(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private fun addRequest(item: FoodItem, triggerDate: LocalDate, daysBefore: Int) {
        val zone = TimeZone.currentSystemDefault()
        val nowInstant = Clock.System.now()
        val fireAt = LocalDateTime(triggerDate.year, triggerDate.monthNumber, triggerDate.dayOfMonth, REMINDER_HOUR, 0)
        val secondsUntilFire = fireAt.toInstant(zone).epochSeconds - nowInstant.epochSeconds
        if (secondsUntilFire <= 0) return

        val content = UNMutableNotificationContent().apply {
            setTitle("Food expiring soon")
            setBody("${item.name} expires in $daysBefore day(s)")
            setSound(UNNotificationSound.defaultSound())
        }

        val trigger = UNTimeIntervalNotificationTrigger.Companion.triggerWithTimeInterval(
            timeInterval = secondsUntilFire.toDouble(),
            repeats = false
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "expiry_${item.id}_$daysBefore",
            content = content,
            trigger = trigger
        )
        center.addNotificationRequest(request) { _ -> }
    }

    private fun ensureAuthorization(onAuthorized: () -> Unit) {
        center.getNotificationSettingsWithCompletionHandler { settings ->
            when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional -> onAuthorized()
                UNAuthorizationStatusNotDetermined -> {
                    center.requestAuthorizationWithOptions(
                        options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound
                    ) { granted, _ ->
                        if (granted) onAuthorized()
                    }
                }
                else -> {}
            }
        }
    }

    private companion object {
        const val REMINDER_HOUR = 9
    }
}
