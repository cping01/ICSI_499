package com.damc.driver_action.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Users

@Dao
interface OnDataBaseActions {
    @Insert
    suspend fun insertUser(users: Users)

    @Query("SELECT COUNT(*) FROM users WHERE username LIKE :username LIMIT 1")
    fun isUsernameInDb(username: String): Int

    @Query("SELECT * FROM users WHERE username LIKE :username AND password LIKE :password LIMIT 1")
    suspend fun userLogin(username: String, password: String): Users

    @Query("SELECT * FROM action_data WHERE user_id LIKE :userID AND date LIKE :date LIMIT 1")
    suspend fun dateIsRegisteredInDb(userID: Int, date: String): ActionData

    @Insert
    suspend fun insetAction(actionData: ActionData)

    @Update
    suspend fun updateAction(actionData: ActionData)

    @Query("SELECT * FROM action_data WHERE user_id LIKE :userID")
    suspend fun getUserActions(userID: Int): List<ActionData>
}