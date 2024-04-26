package com.damc.driver_action.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.Users
import com.damc.driver_action.domain.models.TripMetrics

@Dao
interface OnDataBaseActions {

    @Query("SELECT * FROM trip ORDER BY trip_id DESC LIMIT 1")
    suspend fun getLatestTrip(): Trip
    @Query("SELECT * FROM trip_metrics WHERE trip_id = :tripId AND user_id = :userId")
    fun getTripMetrics(tripId: Int, userId: Int): LiveData<List<TripMetrics>>
    @Query("SELECT * FROM trip WHERE trip_id = :tripId AND user_id = :userId")
    fun getTrips(tripId: Int, userId: Int): LiveData<List<Trip>>

    @Insert
    suspend fun insertUser(users: Users)

    @Insert
    suspend fun insertTrip(trip: Trip): Long

    @Insert
    suspend fun insertTripMetrics(tripMetrics: TripMetrics)

    @Query("UPDATE trip_metrics SET maxSpeed = :maxSpeed, averageSpeed = :averageSpeed, tripDuration = :tripDuration, tripDistance = :tripDistance, speedingInstances = :speedingInstances, hardAccelerationInstances = :hardAccelerationInstances, hardBrakingInstances = :hardBrakingInstances WHERE trip_id = :tripId")
    suspend fun updateTripMetrics(maxSpeed: Double, averageSpeed: Double, tripDuration: Double, tripDistance: Double, speedingInstances: Int, hardAccelerationInstances: Int, hardBrakingInstances: Int, tripId: Int)


    @Query("SELECT COUNT(*) FROM users WHERE username LIKE :username LIMIT 1")
    fun isUsernameInDb(username: String): Int

    @Query("SELECT * FROM users WHERE username LIKE :username AND password LIKE :password LIMIT 1")
    suspend fun userLogin(username: String, password: String): Users

    @Query("SELECT * FROM users WHERE username LIKE :username LIMIT 1")
    suspend fun userLoginBio(username: String): Users

    @Query("SELECT * FROM action_data WHERE user_id LIKE :userID AND date LIKE :date LIMIT 1")
    suspend fun dateIsRegisteredInDb(userID: Int, date: String): ActionData

    @Insert
    suspend fun insetAction(actionData: ActionData)

    @Update
    suspend fun updateAction(actionData: ActionData)

    @Query("SELECT * FROM action_data WHERE user_id LIKE :userID")
    suspend fun getUserActions(userID: Int): List<ActionData>

    @Update
    suspend fun upDateUser(users: Users)

}