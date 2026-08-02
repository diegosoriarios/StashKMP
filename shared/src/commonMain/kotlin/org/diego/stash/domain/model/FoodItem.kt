package org.diego.stash.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

data class FoodItem(
    val id: Long = 0,
    val name: String,
    val category: Category,
    val quantity: Double,
    val unit: FoodUnit,
    val storageLocation: StorageLocation,
    val purchaseDate: LocalDate?,
    val expiryDate: LocalDate,
    val expiryType: ExpiryType,
    val status: FoodItemStatus = FoodItemStatus.ACTIVE,
    val discardReason: String? = null,
    val barcode: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
