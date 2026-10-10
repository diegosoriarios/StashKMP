package org.diego.stash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.diego.stash.domain.model.ReminderSettings
import org.diego.stash.domain.repository.FoodRepository
import org.diego.stash.domain.repository.SettingsRepository
import org.diego.stash.platform.NotificationScheduler

data class SettingsState(
    val remind30Days: Boolean = true,
    val remind7Days: Boolean = true,
    val remind1Day: Boolean = true,
    val isClearing: Boolean = false,
    val isCleared: Boolean = false
)

class SettingsViewModel(
    private val repository: FoodRepository,
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun observeState(onChange: (SettingsState) -> Unit): AutoCloseable {
        val job = viewModelScope.launch {
            state.collect { onChange(it) }
        }
        return object : AutoCloseable {
            override fun close() {
                job.cancel()
            }
        }
    }

    init {
        viewModelScope.launch {
            val settings = settingsRepository.getReminderSettings()
            _state.update {
                it.copy(
                    remind30Days = settings.remind30Days,
                    remind7Days = settings.remind7Days,
                    remind1Day = settings.remind1Day
                )
            }
        }
    }

    fun toggleRemind30Days() = toggle { it.copy(remind30Days = !it.remind30Days) }

    fun toggleRemind7Days() = toggle { it.copy(remind7Days = !it.remind7Days) }

    fun toggleRemind1Day() = toggle { it.copy(remind1Day = !it.remind1Day) }

    fun clearAllData() {
        viewModelScope.launch {
            _state.update { it.copy(isClearing = true) }
            repository.clearAll()
            notificationScheduler.cancelAll()
            _state.update { it.copy(isClearing = false, isCleared = true) }
        }
    }

    private fun toggle(reducer: (SettingsState) -> SettingsState) {
        val newState = reducer(_state.value)
        _state.value = newState
        viewModelScope.launch {
            val settings = ReminderSettings(
                remind30Days = newState.remind30Days,
                remind7Days = newState.remind7Days,
                remind1Day = newState.remind1Day
            )
            settingsRepository.saveReminderSettings(settings)
            val items = repository.getAllItems().first()
            notificationScheduler.rescheduleAll(items, settings)
        }
    }
}
