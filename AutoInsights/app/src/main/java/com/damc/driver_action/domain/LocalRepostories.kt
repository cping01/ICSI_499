package com.damc.driver_action.domain

import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.domain.models.Users

interface LocalRepostories {

    suspend fun insertTrip(trip: Trip): Long
    suspend fun insertTripMetrics(tripMetrics: TripMetrics)
    suspend fun updateTripMetrics(maxSpeed: Double, averageSpeed: Double, tripDuration: Double, tripDistance: Double, speedingInstances: Int, hardAccelerationInstances: Int, hardBrakingInstances: Int, tripId: Int)
    suspend fun getTripMetrics(tripId: Int): TripMetrics?
    suspend fun insertUser(users: Users)

    suspend fun isUsernameInDb(username: String): Int

    suspend fun userLogin(username: String, password: String): Users?

    suspend fun dateIsRegisteredInDb(userID: Int, date: String): ActionData

    suspend fun insertAction(actionData: ActionData)

    suspend fun updateAction(actionData: ActionData)

    suspend fun getUserActions(userID: Int): List<ActionData>

    suspend fun upDateUserData(users: Users)

    suspend fun userLoginBio(username: String): Users
}