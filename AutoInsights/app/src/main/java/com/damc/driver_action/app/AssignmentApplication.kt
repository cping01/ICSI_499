package com.damc.driver_action.app

import android.app.Application
import androidx.room.Room
import com.damc.driver_action.data.local.LocalRepositoryImpl
import com.damc.driver_action.data.local.room.AppDataBase
import com.damc.driver_action.data.local.room.DatabaseClient
import com.damc.driver_action.di.appModule
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.domain.models.Users
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AssignmentApplication : Application() {
    private var loginUser: Users? = null
    private var actionData: ActionData? = null
    private var trip: Trip? = null
    private var tripMetrics: TripMetrics? = null

    lateinit var database: LocalRepostories
    lateinit var appDatabase: AppDataBase

    override fun onCreate() {
        super.onCreate()
        appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDataBase::class.java, "database-name"
        ).build()

        database = LocalRepositoryImpl(appDatabase.OnDataBaseActions())
    }





    fun setLoginUser(user: Users) {
        this.loginUser = user
    }

    fun getLoginUser(): Users {
        return loginUser!!
    }

    fun setActionData(actionData: ActionData) {
        this.actionData = actionData
    }

    fun getActionData(): ActionData {
        return actionData!!
    }

    fun setTrip(trip: Trip) {
        this.trip = trip
    }

    fun getTrip(): Trip {
        return trip!!
    }

    fun setTripMetrics(tripMetrics: TripMetrics) {
        this.tripMetrics = tripMetrics
    }

    fun getTripMetrics(): TripMetrics {
        return tripMetrics!!
    }
}