package org.diego.stash.data.remote

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OpenFoodFactsApiTest {

    @Test
    fun `getProduct returns success when status is 1`() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"status": 1, "product": {"product_name": "Test Product", "categories_tags": ["en:test"]}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val api = OpenFoodFactsApi(client)

        val response = api.getProduct("123456")

        assertNotNull(response)
        assertEquals(1, response.status)
        assertEquals("Test Product", response.product?.productName)
    }

    @Test
    fun `getProduct returns null on error`() = runTest {
        val mockEngine = MockEngine { request ->
            respondError(HttpStatusCode.InternalServerError)
        }
        val client = HttpClient(mockEngine)
        val api = OpenFoodFactsApi(client)

        val response = api.getProduct("123456")

        assertEquals(null, response)
    }
}
