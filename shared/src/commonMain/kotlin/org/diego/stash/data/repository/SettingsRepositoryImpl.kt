package org.diego.stash.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import org.diego.stash.database.StashDatabase
import org.diego.stash.domain.model.ReminderSettings
import org.diego.stash.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val database: StashDatabase
) : SettingsRepository {

    private val queries = database.settingsQueries

    override suspend fun getReminderSettings(): ReminderSettings {
        val entity = queries.selectSettings()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .first()
        return ReminderSettings(
            remind30Days = entity?.remind_30_days != 0L,
            remind7Days = entity?.remind_7_days != 0L,
            remind1Day = entity?.remind_1_day != 0L
        )
    }

    override suspend fun saveReminderSettings(settings: ReminderSettings) {
        queries.upsertSettings(
            id = 1,
            remind_30_days = if (settings.remind30Days) 1L else 0L,
            remind_7_days = if (settings.remind7Days) 1L else 0L,
            remind_1_day = if (settings.remind1Day) 1L else 0L
        )
    }
}
