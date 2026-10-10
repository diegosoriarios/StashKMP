package org.diego.stash.domain.repository

import kotlinx.coroutines.flow.Flow
import org.diego.stash.domain.model.BarcodeLookupResult
import org.diego.stash.domain.model.FoodItem
import kotlinx.datetime.LocalDate

interface FoodRepository {
    fun getAllItems(): Flow<List<FoodItem>>
    suspend fun getItemById(id: Long): FoodItem?
    suspend fun insertItem(item: FoodItem)
    suspend fun updateItem(item: FoodItem)
    suspend fun deleteItem(id: Long)
    suspend fun updateQuantity(id: Long, quantity: Double)
    suspend fun markAsConsumed(id: Long)
    suspend fun markAsDiscarded(id: Long, reason: String?)

    suspend fun getProductByBarcode(barcode: String): BarcodeLookupResult

    fun getExpiringSoon(limitDate: LocalDate): Flow<List<FoodItem>>
    suspend fun countExpired(date: LocalDate): Long
    suspend fun countExpiringBetween(start: LocalDate, end: LocalDate): Long
}
