package org.diego.stash.presentation

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.diego.stash.data.repository.FakeFoodRepository
import org.diego.stash.domain.model.*
import org.diego.stash.platform.FakeNotificationScheduler
import kotlin.test.*

class AddEditViewModelTest {

    private val repository = FakeFoodRepository()
    private val scheduler = FakeNotificationScheduler()
    private val viewModel = AddEditViewModel(repository, scheduler)

    @Test
    fun `name is required to save`() = runTest {
        viewModel.onNameChanged("")
        viewModel.saveItem()

        viewModel.state.test {
            val state = awaitItem()
            assertNotNull(state.nameError)
        }
        
        repository.getAllItems().test {
            assertEquals(0, awaitItem().size)
        }
    }

    @Test
    fun `successful save updates repository and schedules notification`() = runTest {
        viewModel.onNameChanged("Banana")
        viewModel.onCategoryChanged(Category.PRODUCE)
        
        viewModel.saveItem()

        viewModel.state.test {
            assertTrue(awaitItem().isSaved)
        }

        repository.getAllItems().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Banana", items[0].name)
        }

        assertEquals(1, scheduler.scheduledItems.size)
        assertEquals("Banana", scheduler.scheduledItems[0].name)
    }

    @Test
    fun `loading existing item updates state`() = runTest {
        val item = createItem("Orange")
        repository.insertItem(item)
        
        viewModel.loadItem(1) // Fake repository uses sequential IDs starting at 1

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Orange", state.name)
            assertEquals(1, state.id)
        }
    }

    private fun createItem(name: String) = FoodItem(
        id = 0,
        name = name,
        category = Category.OTHER,
        quantity = 1.0,
        unit = FoodUnit.UNITS,
        storageLocation = StorageLocation.PANTRY,
        purchaseDate = null,
        expiryDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        expiryType = ExpiryType.BEST_BEFORE,
        createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
        updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )
}
