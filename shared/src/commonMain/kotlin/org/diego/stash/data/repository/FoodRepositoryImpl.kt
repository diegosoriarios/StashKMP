package org.diego.stash.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import org.diego.stash.data.remote.OpenFoodFactsApi
import org.diego.stash.database.Food_item
import org.diego.stash.database.StashDatabase
import org.diego.stash.domain.model.*
import org.diego.stash.domain.repository.FoodRepository

class FoodRepositoryImpl(
    private val database: StashDatabase,
    private val api: OpenFoodFactsApi
) : FoodRepository {

    private val queries = database.stashDatabaseQueries

    override fun getAllItems(): Flow<List<FoodItem>> {
        return queries.selectAllItems()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getItemById(id: Long): FoodItem? {
        return queries.selectItemById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insertItem(item: FoodItem) {
        queries.insertItem(
            name = item.name,
            category = item.category.name,
            quantity = item.quantity,
            unit = item.unit.name,
            storage_location = item.storageLocation.name,
            purchase_date = item.purchaseDate?.toString(),
            expiry_date = item.expiryDate.toString(),
            expiry_type = item.expiryType.name,
            status = item.status.name,
            discard_reason = item.discardReason,
            barcode = item.barcode,
            notes = item.notes,
            created_at = item.createdAt.toString(),
            updated_at = item.updatedAt.toString()
        )
    }

    override suspend fun updateItem(item: FoodItem) {
        queries.updateItem(
            name = item.name,
            category = item.category.name,
            quantity = item.quantity,
            unit = item.unit.name,
            storage_location = item.storageLocation.name,
            purchase_date = item.purchaseDate?.toString(),
            expiry_date = item.expiryDate.toString(),
            expiry_type = item.expiryType.name,
            status = item.status.name,
            discard_reason = item.discardReason,
            barcode = item.barcode,
            notes = item.notes,
            updated_at = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            id = item.id
        )
    }

    override suspend fun deleteItem(id: Long) {
        queries.deleteItem(id)
    }

    override suspend fun updateQuantity(id: Long, quantity: Double) {
        queries.updateQuantity(
            quantity = quantity,
            updated_at = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            id = id
        )
    }

    override suspend fun markAsConsumed(id: Long) {
        queries.markAsConsumed(
            updated_at = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            id = id
        )
    }

    override suspend fun markAsDiscarded(id: Long, reason: String?) {
        queries.markAsDiscarded(
            discard_reason = reason,
            updated_at = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
            id = id
        )
    }

    override suspend fun getProductByBarcode(barcode: String): FoodItem? {
        val response = api.getProduct(barcode)
        if (response?.status == 1 && response.product != null) {
            val product = response.product
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            return FoodItem(
                name = product.productName ?: "Unknown",
                category = mapCategoriesToEnum(product.categoriesTags),
                quantity = 1.0,
                unit = FoodUnit.UNITS,
                storageLocation = StorageLocation.PANTRY,
                purchaseDate = now.date,
                expiryDate = now.date.plus(7, DateTimeUnit.DAY), // Default 1 week
                expiryType = ExpiryType.BEST_BEFORE,
                barcode = barcode,
                createdAt = now,
                updatedAt = now
            )
        }
        return null
    }

    override fun getExpiringSoon(limitDate: LocalDate): Flow<List<FoodItem>> {
        return queries.selectExpiringSoon(limitDate.toString())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun countExpired(date: LocalDate): Long {
        return queries.countExpired(date.toString()).executeAsOne()
    }

    override suspend fun countExpiringBetween(start: LocalDate, end: LocalDate): Long {
        return queries.countExpiringBetween(start.toString(), end.toString()).executeAsOne()
    }

    private fun mapCategoriesToEnum(tags: List<String>?): Category {
        if (tags == null) return Category.OTHER
        // Very basic mapping logic
        val tagStr = tags.joinToString(", ").lowercase()
        return when {
            tagStr.contains("dairy") || tagStr.contains("milk") || tagStr.contains("cheese") -> Category.DAIRY
            tagStr.contains("meat") || tagStr.contains("fish") || tagStr.contains("seafood") -> Category.MEAT_FISH
            tagStr.contains("fruit") || tagStr.contains("vegetable") || tagStr.contains("produce") -> Category.PRODUCE
            tagStr.contains("frozen") -> Category.FROZEN
            tagStr.contains("bakery") || tagStr.contains("bread") -> Category.BAKERY
            tagStr.contains("beverage") || tagStr.contains("drink") || tagStr.contains("soda") || tagStr.contains("juice") -> Category.BEVERAGES
            tagStr.contains("snack") || tagStr.contains("candy") || tagStr.contains("cookie") -> Category.SNACKS
            tagStr.contains("pantry") || tagStr.contains("canned") || tagStr.contains("pasta") || tagStr.contains("rice") -> Category.PANTRY
            else -> Category.OTHER
        }
    }

    private fun Food_item.toDomain(): FoodItem {
        return FoodItem(
            id = id,
            name = name,
            category = category?.let { Category.valueOf(it.uppercase()) } ?: Category.OTHER,
            quantity = quantity ?: 0.0,
            unit = unit?.let { FoodUnit.valueOf(it.uppercase()) } ?: FoodUnit.UNITS,
            storageLocation = storage_location?.let { StorageLocation.valueOf(it.uppercase()) } ?: StorageLocation.PANTRY,
            purchaseDate = purchase_date?.let { LocalDate.parse(it) },
            expiryDate = LocalDate.parse(expiry_date),
            expiryType = expiry_type?.let { ExpiryType.valueOf(it.uppercase()) } ?: ExpiryType.BEST_BEFORE,
            status = FoodItemStatus.valueOf(status.uppercase()),
            discardReason = discard_reason,
            barcode = barcode,
            notes = notes,
            createdAt = LocalDateTime.parse(created_at),
            updatedAt = LocalDateTime.parse(updated_at)
        )
    }
}
