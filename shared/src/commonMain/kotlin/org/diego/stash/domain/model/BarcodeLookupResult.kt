package org.diego.stash.domain.model

sealed interface BarcodeLookupResult {
    data class Found(val item: FoodItem) : BarcodeLookupResult
    data object NotFound : BarcodeLookupResult
    data object NetworkError : BarcodeLookupResult
}
