package org.diego.stash.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.diego.stash.data.remote.model.ProductResponse

class OpenFoodFactsApi(private val client: HttpClient) {
    suspend fun getProduct(barcode: String): ProductResponse? {
        return try {
            client.get("https://world.openfoodfacts.org/api/v0/product/$barcode.json").body()
        } catch (e: Exception) {
            null
        }
    }
}

fun createHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }
}
