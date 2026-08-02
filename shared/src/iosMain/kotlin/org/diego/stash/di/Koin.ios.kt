package org.diego.stash.di

import org.diego.stash.data.local.DatabaseDriverFactory
import org.diego.stash.platform.IosNotificationScheduler
import org.diego.stash.platform.NotificationScheduler
import org.koin.dsl.module

actual fun platformModule() = module {
    single { DatabaseDriverFactory().createDriver() }
    single<NotificationScheduler> { IosNotificationScheduler() }
}
