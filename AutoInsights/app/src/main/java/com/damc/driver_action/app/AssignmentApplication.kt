package com.damc.driver_action.app

import android.app.Application
import com.damc.driver_action.di.appModule
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.domain.models.Users
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AssignmentApplication : Application() {

    private var user: Users? = null
    private var actionData: ActionData? = null
    private var trip: Trip? = null
    private var tripMetrics: TripMetrics? = null


    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@AssignmentApplication)
            modules(appModule)
        }
    }

    fun setTrip(trip: Trip?) {
        this.trip = trip
    }

    fun getTrip(): Trip? {
        return trip
    }

    fun setTripMetrics(tripMetrics: TripMetrics?) {
        this.tripMetrics = tripMetrics
    }

    fun getTripMetrics(): TripMetrics? {
        return tripMetrics
    }

    fun setLoginUser(users: Users) {
        this.user = users
    }

    fun getLoginUser(): Users {
        return user!!
    }

    fun setActionData(actionData: ActionData?) {
        this.actionData = actionData
    }

    fun getActionData(): ActionData? {
        return actionData
    }


}