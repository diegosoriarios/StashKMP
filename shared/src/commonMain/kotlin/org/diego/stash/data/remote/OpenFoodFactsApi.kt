package org.diego.stash.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.diego.stash.data.remote.model.ProductResponse

class OpenFoodFactsApi(private val client: HttpClient) {
    suspend fun getProduct(barcode: String): ProductResponse {
        val response = client.get("https://world.openfoodfacts.org/api/v0/product/$barcode.json")
        if (!response.status.isSuccess()) {
            throw OpenFoodFactsException("HTTP ${response.status.value}")
        }
        return response.body()
    }
}

class OpenFoodFactsException(message: String) : Exception(message)

fun createHttpClient() = HttpClient {
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
    }
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }
}
