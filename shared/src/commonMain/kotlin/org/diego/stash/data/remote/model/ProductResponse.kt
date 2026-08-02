package org.diego.stash.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val status: Int,
    val product: Product? = null
)

@Serializable
data class Product(
    @SerialName("product_name") val productName: String? = null,
    @SerialName("categories_tags") val categoriesTags: List<String>? = null
)
