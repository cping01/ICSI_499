package com.damc.driver_action.data.local

import androidx.lifecycle.LiveData
import com.damc.driver_action.data.local.room.OnDataBaseActions
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.domain.models.Users

class LocalRepositoryImpl(val dataBase: OnDataBaseActions) : LocalRepostories {


    override suspend fun insertTrip(trip: Trip): Long {
        try {
            return dataBase.insertTrip(trip)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun insertTripMetrics(tripMetrics: TripMetrics) {
        try {
            dataBase.insertTripMetrics(tripMetrics)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun updateTripMetrics(maxSpeed: Double, averageSpeed: Double, tripDuration: Double, tripDistance: Double, speedingInstances: Int, hardAccelerationInstances: Int, hardBrakingInstances: Int, tripId: Int) {
        try {
            dataBase.updateTripMetrics(maxSpeed, averageSpeed, tripDuration, tripDistance, speedingInstances, hardAccelerationInstances, hardBrakingInstances, tripId)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getTripMetrics(tripId: Int, userId: Int): LiveData<List<TripMetrics>> {
        try {
            return dataBase.getTripMetrics(tripId, userId)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }



    override fun getTrips(tripId: Int,userId: Int): LiveData<List<Trip>> {
        try {
            return dataBase.getTrips(tripId,userId)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun getLatestTrip(): Trip {
        try {
        return dataBase.getLatestTrip()
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }
    }


    override suspend fun insertUser(users: Users) {
        try {
            dataBase.insertUser(users)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun isUsernameInDb(username: String): Int {
        try {
            return dataBase.isUsernameInDb(username)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun userLogin(username: String, password: String): Users? {
        try {
            return dataBase.userLogin(username, password)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun dateIsRegisteredInDb(userID: Int, date: String): ActionData {
        try {
            return dataBase.dateIsRegisteredInDb(userID, date)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun insertAction(actionData: ActionData) {
        try {
            dataBase.insetAction(actionData)
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    override suspend fun updateAction(actionData: ActionData) {
        try {
            dataBase.updateAction(actionData)
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    override suspend fun getUserActions(userID: Int): List<ActionData> {
        try {
            return dataBase.getUserActions(userID)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun upDateUserData(users: Users) {
        try {
            return dataBase.upDateUser(users)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun userLoginBio(username: String): Users {
        try {
            return dataBase.userLoginBio(username)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


}