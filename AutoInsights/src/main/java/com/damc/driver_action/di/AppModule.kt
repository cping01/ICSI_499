package com.damc.driver_action.di

import com.damc.driver_action.accelerationHelper.Accelerometer
import com.damc.driver_action.accelerationHelper.Gyroscope
import com.damc.driver_action.data.local.LocalRepositoryImpl
import com.damc.driver_action.data.local.room.DatabaseClient
import com.damc.driver_action.data.local.room.OnDataBaseActions
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.ui.home.HomeScreenViewModel
import com.damc.driver_action.ui.launcher.LauncherViewModel
import com.damc.driver_action.ui.register.RegisterViewModel
import com.damc.driver_action.ui.summerScreen.SummeryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {


    single<OnDataBaseActions> {
        DatabaseClient(get()).getAppDatabase()!!.OnDataBaseActions()
    }

    single<LocalRepostories> {
        LocalRepositoryImpl(get())
    }

    single<Accelerometer> {
        Accelerometer(get())
    }

    single<Gyroscope> {
        Gyroscope(get())
    }

    viewModel { LauncherViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { HomeScreenViewModel(get(), get(), get()) }
    viewModel { SummeryViewModel(get()) }
}