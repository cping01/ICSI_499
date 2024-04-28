package com.damc.driver_action.domain

import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Users

interface LocalRepostories {
    suspend fun insertUser(users: Users)

    suspend fun isUsernameInDb(username: String): Int

    suspend fun userLogin(username: String, password: String): Users?

    suspend fun dateIsRegisteredInDb(userID: Int, date: String): ActionData

    suspend fun insertAction(actionData: ActionData)

    suspend fun updateAction(actionData: ActionData)

    suspend fun getUserActions(userID: Int): List<ActionData>
}