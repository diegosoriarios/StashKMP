package org.diego.stash.domain.model

data class ReminderSettings(
    val remind30Days: Boolean = true,
    val remind7Days: Boolean = true,
    val remind1Day: Boolean = true
)
