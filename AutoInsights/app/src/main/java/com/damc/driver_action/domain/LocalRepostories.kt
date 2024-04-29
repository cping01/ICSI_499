package com.damc.driver_action.domain

import androidx.lifecycle.LiveData
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.domain.models.Users

interface LocalRepostories {

    suspend fun getTripMetrics(tripId: Int, userId: Int): List<TripMetrics>
    fun getTrips( tripId: Int, userId: Int,): List<Trip>

    suspend fun getTripMetricsForTripId(tripId: Int): TripMetrics
    suspend fun getLatestTripMetrics(userId: kotlin.Int): TripMetrics
    suspend fun insertTrip(trip: Trip): Long
    suspend fun insertTripMetrics(tripMetrics: TripMetrics)
    suspend fun updateTripMetrics(maxSpeed: Double, averageSpeed: Double, tripDuration: Double, tripDistance: Double, speedingInstances: Int, hardAccelerationInstances: Int, hardBrakingInstances: Int, tripId: Int)
    suspend fun getLatestTrip(userId: Int): Trip

    suspend fun insertUser(users: Users)

    suspend fun isUsernameInDb(username: String): Int

    suspend fun userLogin(username: String, password: String): Users?

    suspend fun dateIsRegisteredInDb(userID: Int, date: String): ActionData
    suspend fun getTripForUserAndDate(userID: Int, date: String): Trip
    suspend fun insertAction(actionData: ActionData)

    suspend fun updateAction(actionData: ActionData)

    suspend fun getUserActions(userID: Int): List<ActionData>

    suspend fun upDateUserData(users: Users)

    suspend fun userLoginBio(username: String): Users
}