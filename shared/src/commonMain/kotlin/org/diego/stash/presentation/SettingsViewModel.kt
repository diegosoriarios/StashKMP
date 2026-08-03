package org.diego.stash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.diego.stash.platform.NotificationScheduler

data class SettingsState(
    val remind30Days: Boolean = true,
    val remind7Days: Boolean = true,
    val remind1Day: Boolean = true
)

class SettingsViewModel(private val notificationScheduler: NotificationScheduler) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun toggleRemind30Days() {
        _state.update { it.copy(remind30Days = !it.remind30Days) }
        // In a real app, we would update the scheduler or preferences
    }

    fun toggleRemind7Days() {
        _state.update { it.copy(remind7Days = !it.remind7Days) }
    }

    fun toggleRemind1Day() {
        _state.update { it.copy(remind1Day = !it.remind1Day) }
    }
}
