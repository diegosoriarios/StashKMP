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
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class OpenFoodFactsApiTest {

    private fun mockClient(status: HttpStatusCode, content: String = "{}") = HttpClient(MockEngine { _ ->
        respond(
            content = content,
            status = status,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Test
    fun `getProduct returns product when status is 1`() = runTest {
        val api = OpenFoodFactsApi(
            mockClient(
                HttpStatusCode.OK,
                """{"status": 1, "product": {"product_name": "Test Product", "categories_tags": ["en:test"]}}"""
            )
        )

        val response = api.getProduct("123456")

        assertEquals(1, response.status)
        assertEquals("Test Product", response.product?.productName)
    }

    @Test
    fun `getProduct returns not found response when status is 0`() = runTest {
        val api = OpenFoodFactsApi(mockClient(HttpStatusCode.OK, """{"status": 0}"""))

        val response = api.getProduct("123456")

        assertNotNull(response)
        assertEquals(0, response.status)
    }

    @Test
    fun `getProduct throws on non 2xx response`() = runTest {
        val api = OpenFoodFactsApi(mockClient(HttpStatusCode.InternalServerError))

        assertFailsWith<OpenFoodFactsException> { api.getProduct("123456") }
    }
}
