package org.diego.stash.di

import org.diego.stash.data.local.DatabaseDriverFactory
import org.diego.stash.platform.AndroidNotificationScheduler
import org.diego.stash.platform.NotificationScheduler
import org.koin.dsl.module

actual fun platformModule() = module {
    single { DatabaseDriverFactory(get()).createDriver() }
    single<NotificationScheduler> { AndroidNotificationScheduler(get()) }
}
