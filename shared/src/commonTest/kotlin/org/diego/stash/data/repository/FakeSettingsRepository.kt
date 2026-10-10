package org.diego.stash.data.repository

import org.diego.stash.domain.model.ReminderSettings
import org.diego.stash.domain.repository.SettingsRepository

class FakeSettingsRepository : SettingsRepository {
    var saved: ReminderSettings = ReminderSettings()

    override suspend fun getReminderSettings(): ReminderSettings = saved

    override suspend fun saveReminderSettings(settings: ReminderSettings) {
        saved = settings
    }
}
