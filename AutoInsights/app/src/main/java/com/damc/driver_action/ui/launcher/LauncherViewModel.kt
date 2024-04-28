package com.damc.driver_action.ui.launcher

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.PreferenceRepository
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Users
import com.damc.driver_action.ui.BaseViewModel
import com.damc.driver_action.utils.Utils
import com.damc.driver_action.utils.Utils.Companion.showToast
import kotlinx.coroutines.launch

class LauncherViewModel(
    val database: LocalRepostories,
    val preferenceRepository: PreferenceRepository
) : BaseViewModel() {

    fun loginToRegister() {
        navigate(LauncherFragmentDirections.actionLoginToRegister())
    }

    fun loginToHome() {
        navigate(LauncherFragmentDirections.actionLoginToHome())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun isUserDetailsOk(
        username: String,
        password: String,
        biometrics: Boolean,
        application: AssignmentApplication
    ): Boolean {
        var user: Users? = null
        if (biometrics) {
            user = database.userLoginBio(username)
        } else {
            user = database.userLogin(username, password)
        }

        if (user != null) {
            var actionData: ActionData? =
                database.dateIsRegisteredInDb(user.userId, Utils.getCurrentDateAsString())
            application.setLoginUser(user)
            if (actionData != null) {
                application.setActionData(actionData)
            } else {
                actionData = ActionData(
                    userId = user.userId,
                    date = Utils.getCurrentDateAsString(),
                    hardStopCount = 0,
                    goodStopCount = 0,
                    mediumStopCount = 0,
                    fastAcceleration = 0,
                    goodAcceleration = 0,
                    mediumAcceleration = 0,
                    highestSpeed = 0.0F,
                )
                database.insertAction(
                    actionData
                )
                application.setActionData(actionData)
            }
        }
        return user != null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun validateInputs(
        username: String,
        password: String,
        context: Context,
        biometrics: Boolean,
        application: AssignmentApplication
    ): Boolean {
        var b = false
        viewModelScope.launch {
            if (!biometrics) {
                if (username.isEmpty() || password.isEmpty()) {
                    showToast("Fields cannot be empty", context)
                } else if (isUserDetailsOk(username, password, biometrics, application)) {
                    showToast("Login Successful", context)
                    preferenceRepository.saveUsername(username)
                    loginToHome()
                } else {
                    showToast("Invalid Credentials", context)
                }
            } else {
                if (username.isEmpty()) {
                    showToast("Username cannot be empty", context)
                } else if (isUserDetailsOk(username, password, biometrics, application)) {
                    b = true
                    showToast("Login Successful", context)
                    preferenceRepository.saveUsername(username)
                    loginToHome()
                } else {
                    showToast("Invalid Credentials", context)
                }
            }

        }

        return b
    }

}