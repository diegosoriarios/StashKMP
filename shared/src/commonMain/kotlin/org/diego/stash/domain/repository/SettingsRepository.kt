package org.diego.stash.domain.repository

import org.diego.stash.domain.model.ReminderSettings

interface SettingsRepository {
    suspend fun getReminderSettings(): ReminderSettings
    suspend fun saveReminderSettings(settings: ReminderSettings)
}
