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
    val filteredItems: List<FoodItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: Category? = null,
    val selectedLocation: StorageLocation? = null,
    val sortBy: SortOption = SortOption.EXPIRY_DATE,
    val isLoading: Boolean = false
)

enum class SortOption {
    EXPIRY_DATE, NAME, CATEGORY
}

class InventoryViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    fun observeState(onChange: (InventoryState) -> Unit): AutoCloseable {
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
        loadItems()
        observeFilters()
    }

    private fun loadItems() {
        viewModelScope.launch {
            repository.getAllItems().collect { items ->
                _state.update { it.copy(items = items) }
                applyFilters()
            }
        }
    }

    private fun observeFilters() {
        viewModelScope.launch {
            combine(
                _state.map { it.searchQuery }.distinctUntilChanged(),
                _state.map { it.selectedCategory }.distinctUntilChanged(),
                _state.map { it.selectedLocation }.distinctUntilChanged(),
                _state.map { it.sortBy }.distinctUntilChanged()
            ) { _, _, _, _ ->
                applyFilters()
            }.collect()
        }
    }

    private fun applyFilters() {
        val currentState = _state.value
        var result = currentState.items

        if (currentState.searchQuery.isNotBlank()) {
            result = result.filter { it.name.contains(currentState.searchQuery, ignoreCase = true) }
        }

        if (currentState.selectedCategory != null) {
            result = result.filter { it.category == currentState.selectedCategory }
        }

        if (currentState.selectedLocation != null) {
            result = result.filter { it.storageLocation == currentState.selectedLocation }
        }

        result = when (currentState.sortBy) {
            SortOption.EXPIRY_DATE -> result.sortedBy { it.expiryDate }
            SortOption.NAME -> result.sortedBy { it.name }
            SortOption.CATEGORY -> result.sortedBy { it.category.name }
        }

        _state.update { it.copy(filteredItems = result) }
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

    fun onSortByChanged(option: SortOption) {
        _state.update { it.copy(sortBy = option) }
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
