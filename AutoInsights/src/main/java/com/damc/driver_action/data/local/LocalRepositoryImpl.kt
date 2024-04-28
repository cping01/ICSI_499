package com.damc.driver_action.data.local

import com.damc.driver_action.data.local.room.OnDataBaseActions
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Users

class LocalRepositoryImpl(val dataBase: OnDataBaseActions) : LocalRepostories {

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


}