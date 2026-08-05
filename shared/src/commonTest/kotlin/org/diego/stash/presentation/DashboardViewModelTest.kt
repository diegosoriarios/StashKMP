package org.diego.stash.presentation

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.diego.stash.data.repository.FakeFoodRepository
import org.diego.stash.domain.model.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DashboardViewModelTest {

    private val repository = FakeFoodRepository()
    private val viewModel = DashboardViewModel(repository)

    @Test
    fun `initial state is empty`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(0, initialState.expiredCount)
            assertEquals(0, initialState.urgentItems.size)
        }
    }

    @Test
    fun `refresh updates counts correctly`() = runTest {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        
        // Add expired item
        repository.insertItem(createItem("Expired", now.minus(1, DateTimeUnit.DAY)))
        // Add item expiring in 2 days
        repository.insertItem(createItem("Urgent", now.plus(2, DateTimeUnit.DAY)))
        // Add item expiring in 5 days
        repository.insertItem(createItem("Soon", now.plus(5, DateTimeUnit.DAY)))

        viewModel.refresh()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.expiredCount)
            assertEquals(1, state.expiring3DaysCount)
            assertEquals(2, state.expiring7DaysCount) // 3 days + 5 days
            assertEquals(2, state.urgentItems.size) // Urgent and Soon
        }
    }

    private fun createItem(name: String, expiry: LocalDate) = FoodItem(
        name = name,
        category = Category.OTHER,
        quantity = 1.0,
        unit = FoodUnit.UNITS,
        storageLocation = StorageLocation.PANTRY,
        purchaseDate = null,
        expiryDate = expiry,
        expiryType = ExpiryType.BEST_BEFORE,
        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )
}
