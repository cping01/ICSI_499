package com.damc.driver_action.app

import android.app.Application
import com.damc.driver_action.di.appModule
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Users
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AssignmentApplication : Application() {

    private var user: Users? = null
    private var actionData: ActionData? = null

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@AssignmentApplication)
            modules(appModule)
        }
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