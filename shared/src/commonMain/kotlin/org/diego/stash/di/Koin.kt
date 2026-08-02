package org.diego.stash.di

import org.diego.stash.data.remote.OpenFoodFactsApi
import org.diego.stash.data.remote.createHttpClient
import org.diego.stash.data.repository.FoodRepositoryImpl
import org.diego.stash.database.StashDatabase
import org.diego.stash.domain.repository.FoodRepository
import org.diego.stash.presentation.AddEditViewModel
import org.diego.stash.presentation.DashboardViewModel
import org.diego.stash.presentation.InventoryViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule(), platformModule())
    }

// ios
fun initKoin() = initKoin {}

fun commonModule() = module {
    single { createHttpClient() }
    single { OpenFoodFactsApi(get()) }
    single<FoodRepository> { FoodRepositoryImpl(get(), get()) }
    single { StashDatabase(get()) }
    
    factory { DashboardViewModel(get()) }
    factory { InventoryViewModel(get()) }
    factory { AddEditViewModel(get()) }
}

expect fun platformModule(): Module
