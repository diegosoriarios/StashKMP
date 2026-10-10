package org.diego.stash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.diego.stash.domain.model.FoodItem
import org.diego.stash.domain.repository.FoodRepository

data class DashboardState(
    val expiredCount: Long = 0,
    val expiring3DaysCount: Long = 0,
    val expiring7DaysCount: Long = 0,
    val urgentItems: List<FoodItem> = emptyList()
)

class DashboardViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun observeState(onChange: (DashboardState) -> Unit): AutoCloseable {
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
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            
                repository.getExpiringSoon(now.plus(7, DateTimeUnit.DAY)).collect { items ->
                    val expired = repository.countExpired(now)
                    val in3Days = repository.countExpiringBetween(now, now.plus(3, DateTimeUnit.DAY))
                    val in7Days = repository.countExpiringBetween(now.plus(4, DateTimeUnit.DAY), now.plus(7, DateTimeUnit.DAY))

                    // Expired items are already reported via expiredCount;
                    // "urgent" means still-valid items expiring within 7 days
                    val urgent = items.filter { it.expiryDate >= now }

                    _state.value = DashboardState(
                        expiredCount = expired,
                        expiring3DaysCount = in3Days,
                        expiring7DaysCount = in3Days + in7Days,
                        urgentItems = urgent
                    )
                }
        }
    }
}
