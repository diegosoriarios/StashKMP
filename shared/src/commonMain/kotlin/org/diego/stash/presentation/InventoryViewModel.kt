package org.diego.stash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.diego.stash.domain.model.Category
import org.diego.stash.domain.model.FoodItem
import org.diego.stash.domain.model.StorageLocation
import org.diego.stash.domain.repository.FoodRepository

data class InventoryState(
    val items: List<FoodItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val selectedLocation: StorageLocation? = null,
    val isLoading: Boolean = false
)

class InventoryViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    init {
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            repository.getAllItems().collect { items ->
                _state.update { it.copy(items = items) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(category: Category?) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun onLocationSelected(location: StorageLocation?) {
        _state.update { it.copy(selectedLocation = location) }
    }

    fun consumeItem(id: Long) {
        viewModelScope.launch {
            repository.markAsConsumed(id)
        }
    }

    fun discardItem(id: Long, reason: String?) {
        viewModelScope.launch {
            repository.markAsDiscarded(id, reason)
        }
    }
    
    fun reduceQuantity(id: Long, currentQuantity: Double) {
        viewModelScope.launch {
            if (currentQuantity > 0) {
                repository.updateQuantity(id, currentQuantity - 1)
            }
        }
    }
}
