package org.diego.stash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.diego.stash.domain.model.*
import org.diego.stash.domain.repository.FoodRepository

data class AddEditState(
    val id: Long = 0,
    val name: String = "",
    val category: Category = Category.OTHER,
    val quantity: Double = 1.0,
    val unit: FoodUnit = FoodUnit.UNITS,
    val storageLocation: StorageLocation = StorageLocation.PANTRY,
    val purchaseDate: LocalDate? = null,
    val expiryDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val expiryType: ExpiryType = ExpiryType.BEST_BEFORE,
    val notes: String = "",
    val barcode: String = "",
    val isScanning: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

class AddEditViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _state = MutableStateFlow(AddEditState())
    val state: StateFlow<AddEditState> = _state.asStateFlow()

    fun onNameChanged(name: String) = _state.update { it.copy(name = name) }
    fun onCategoryChanged(category: Category) = _state.update { it.copy(category = category) }
    fun onQuantityChanged(quantity: Double) = _state.update { it.copy(quantity = quantity) }
    fun onUnitChanged(unit: FoodUnit) = _state.update { it.copy(unit = unit) }
    fun onLocationChanged(location: StorageLocation) = _state.update { it.copy(storageLocation = location) }
    fun onPurchaseDateChanged(date: LocalDate?) = _state.update { it.copy(purchaseDate = date) }
    fun onExpiryDateChanged(date: LocalDate) = _state.update { it.copy(expiryDate = date) }
    fun onExpiryTypeChanged(type: ExpiryType) = _state.update { it.copy(expiryType = type) }
    fun onNotesChanged(notes: String) = _state.update { it.copy(notes = notes) }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, barcode = barcode) }
            val item = repository.getProductByBarcode(barcode)
            if (item != null) {
                _state.update { it.copy(
                    name = item.name,
                    category = item.category,
                    isScanning = false
                ) }
            } else {
                _state.update { it.copy(isScanning = false, error = "Product not found") }
            }
        }
    }

    fun saveItem() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val item = FoodItem(
                id = _state.value.id,
                name = _state.value.name,
                category = _state.value.category,
                quantity = _state.value.quantity,
                unit = _state.value.unit,
                storageLocation = _state.value.storageLocation,
                purchaseDate = _state.value.purchaseDate,
                expiryDate = _state.value.expiryDate,
                expiryType = _state.value.expiryType,
                notes = _state.value.notes,
                barcode = _state.value.barcode,
                createdAt = now,
                updatedAt = now
            )
            
            if (item.id == 0L) {
                repository.insertItem(item)
            } else {
                repository.updateItem(item)
            }
            _state.update { it.copy(isSaving = false) }
        }
    }
}
