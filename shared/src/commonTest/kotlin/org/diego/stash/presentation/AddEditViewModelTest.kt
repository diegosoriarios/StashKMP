package org.diego.stash.presentation

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.diego.stash.data.repository.FakeFoodRepository
import org.diego.stash.data.repository.FakeSettingsRepository
import org.diego.stash.domain.model.*
import org.diego.stash.platform.FakeNotificationScheduler
import org.junit.After
import org.junit.Before
import kotlin.test.*

class AddEditViewModelTest {

    private val repository = FakeFoodRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val scheduler = FakeNotificationScheduler()
    private val viewModel = AddEditViewModel(repository, settingsRepository, scheduler)

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

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
        assertEquals("Banana", scheduler.scheduledItems[0].first.name)
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

    @Test
    fun `scanBarcode found product fills name and category`() = runTest {
        repository.nextLookupResult = BarcodeLookupResult.Found(
            createItem("Nutella").copy(category = Category.PANTRY)
        )

        viewModel.scanBarcode("3017620422003")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Nutella", state.name)
        assertEquals(Category.PANTRY, state.category)
        assertEquals("3017620422003", state.barcode)
        assertNull(state.lookupMessage)
        assertFalse(state.isScanning)
        assertEquals("3017620422003", repository.lastScannedBarcode)
    }

    @Test
    fun `scanBarcode not found shows info message and keeps barcode`() = runTest {
        repository.nextLookupResult = BarcodeLookupResult.NotFound

        viewModel.scanBarcode("5601234567890")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.lookupMessage)
        assertFalse(state.lookupIsError)
        assertEquals("5601234567890", state.barcode)
        assertFalse(state.isScanning)
    }

    @Test
    fun `scanBarcode network error shows error message and keeps barcode`() = runTest {
        repository.nextLookupResult = BarcodeLookupResult.NetworkError

        viewModel.scanBarcode("123456")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.lookupMessage)
        assertTrue(state.lookupIsError)
        assertEquals("123456", state.barcode)
        assertFalse(state.isScanning)
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
