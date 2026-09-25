package org.diego.stash.presentation

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.diego.stash.data.repository.FakeFoodRepository
import org.diego.stash.domain.model.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class InventoryViewModelTest {

    private val repository = FakeFoodRepository()

    @BeforeTest
    fun setUp() {
        // viewModelScope launches on Main; give it an eager test dispatcher
        // so init/load effects settle synchronously before assertions
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = InventoryViewModel(repository)

    @Test
    fun `load items correctly`() = runTest {
        repository.insertItem(createItem("Apple"))
        repository.insertItem(createItem("Milk"))

        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(2, state.items.size)
            assertEquals(2, state.filteredItems.size)
        }
    }

    @Test
    fun `search filters items`() = runTest {
        repository.insertItem(createItem("Apple"))
        repository.insertItem(createItem("Milk"))
        val viewModel = createViewModel()

        viewModel.onSearchQueryChanged("App")

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.filteredItems.size)
            assertEquals("Apple", state.filteredItems[0].name)
        }
    }

    @Test
    fun `category selection filters items`() = runTest {
        repository.insertItem(createItem("Apple").copy(category = Category.PRODUCE))
        repository.insertItem(createItem("Milk").copy(category = Category.DAIRY))
        val viewModel = createViewModel()

        viewModel.onCategorySelected(Category.DAIRY)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.filteredItems.size)
            assertEquals("Milk", state.filteredItems[0].name)
        }
    }

    @Test
    fun `sorting updates list order`() = runTest {
        repository.insertItem(createItem("Zebra"))
        repository.insertItem(createItem("Apple"))
        val viewModel = createViewModel()

        viewModel.onSortByChanged(SortOption.NAME)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Apple", state.filteredItems[0].name)
            assertEquals("Zebra", state.filteredItems[1].name)
        }
    }

    @Test
    fun `reduce quantity updates repository`() = runTest {
        val item = createItem("Apple").copy(id = 1, quantity = 5.0)
        repository.insertItem(item)
        val viewModel = createViewModel()

        viewModel.reduceQuantity(1, 5.0)

        assertEquals(4.0, repository.getItemById(1)?.quantity)
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
