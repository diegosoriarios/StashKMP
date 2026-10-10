package org.diego.stash.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

class SettingsViewModelTest {

    private lateinit var repository: FakeFoodRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var scheduler: FakeNotificationScheduler
    private lateinit var viewModel: SettingsViewModel

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        repository = FakeFoodRepository()
        settingsRepository = FakeSettingsRepository()
        scheduler = FakeNotificationScheduler()
        viewModel = SettingsViewModel(repository, settingsRepository, scheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `settings load from repository on init`() = runTest {
        settingsRepository.saved = ReminderSettings(
            remind30Days = false,
            remind7Days = true,
            remind1Day = false
        )
        val vm = SettingsViewModel(repository, settingsRepository, scheduler)


        val state = vm.state.value
        assertFalse(state.remind30Days)
        assertTrue(state.remind7Days)
        assertFalse(state.remind1Day)
    }

    @Test
    fun `toggling reminder persists and reschedules with settings`() = runTest {
        repository.insertItem(createItem("Milk"))

        viewModel.toggleRemind7Days()

        val state = viewModel.state.value
        assertFalse(state.remind7Days)
        assertFalse(settingsRepository.saved.remind7Days)
        assertEquals(1, scheduler.scheduledItems.size)
        assertFalse(scheduler.scheduledItems[0].second.remind7Days)
    }

    @Test
    fun `clearAllData removes items and cancels notifications`() = runTest {
        repository.insertItem(createItem("Milk"))

        viewModel.clearAllData()

        val state = viewModel.state.value
        assertTrue(state.isCleared)
        assertEquals(0, repository.getAllItems().first().size)
        assertTrue(scheduler.cancelledAll)
        assertEquals(0, scheduler.scheduledItems.size)
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
