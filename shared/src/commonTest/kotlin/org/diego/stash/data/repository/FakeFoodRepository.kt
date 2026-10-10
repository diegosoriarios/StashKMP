package org.diego.stash.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.diego.stash.domain.model.BarcodeLookupResult
import org.diego.stash.domain.model.FoodItem
import org.diego.stash.domain.repository.FoodRepository

class FakeFoodRepository : FoodRepository {
    private val items = MutableStateFlow<List<FoodItem>>(emptyList())

    var nextLookupResult: BarcodeLookupResult = BarcodeLookupResult.NotFound
    var lastScannedBarcode: String? = null
        private set

    override fun getAllItems(): Flow<List<FoodItem>> = items

    override suspend fun getItemById(id: Long): FoodItem? {
        return items.value.find { it.id == id }
    }

    override suspend fun insertItem(item: FoodItem) {
        items.value = items.value + item.copy(id = (items.value.size + 1).toLong())
    }

    override suspend fun updateItem(item: FoodItem) {
        items.value = items.value.map { if (it.id == item.id) item else it }
    }

    override suspend fun deleteItem(id: Long) {
        items.value = items.value.filter { it.id != id }
    }

    override suspend fun clearAll() {
        items.value = emptyList()
    }

    override suspend fun updateQuantity(id: Long, quantity: Double) {
        items.value = items.value.map { if (it.id == id) it.copy(quantity = quantity) else it }
    }

    override suspend fun markAsConsumed(id: Long) {
        items.value = items.value.filter { it.id != id }
    }

    override suspend fun markAsDiscarded(id: Long, reason: String?) {
        items.value = items.value.filter { it.id != id }
    }

    override suspend fun getProductByBarcode(barcode: String): BarcodeLookupResult {
        lastScannedBarcode = barcode
        return nextLookupResult
    }

    override fun getExpiringSoon(limitDate: LocalDate): Flow<List<FoodItem>> {
        return items.map { list -> list.filter { it.expiryDate <= limitDate } }
    }

    override suspend fun countExpired(date: LocalDate): Long {
        return items.value.count { it.expiryDate < date }.toLong()
    }

    override suspend fun countExpiringBetween(start: LocalDate, end: LocalDate): Long {
        return items.value.count { it.expiryDate in start..end }.toLong()
    }
}
