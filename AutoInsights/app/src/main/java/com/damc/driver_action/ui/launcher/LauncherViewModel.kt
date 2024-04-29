package com.damc.driver_action.ui.launcher

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.PreferenceRepository
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.domain.models.Users
import com.damc.driver_action.ui.BaseViewModel
import com.damc.driver_action.utils.Utils
import com.damc.driver_action.utils.Utils.Companion.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

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
    ): Boolean = withContext(Dispatchers.IO) { // Move to background thread
        var user: Users? = null
        if (biometrics) {
            user = database.userLoginBio(username)
        } else {
            user = database.userLogin(username, password)
        }

        if (user != null) {
            application.setLoginUser(user)

            val actionData = ActionData(
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

            database.insertAction(actionData)
            application.setActionData(actionData)

            val newTrip = Trip(
                userId = user.userId,
                date = Date()
            )
            database.insertTrip(newTrip)
            application.setTrip(newTrip)

            val newTripMetrics = TripMetrics(
                userId = user.userId,
                averageSpeed = 0.0,
                maxSpeed = 0.0,
                tripDistance = 0.0,
                tripDuration = 0.0,
                speedingInstances = 0,
                hardAccelerationInstances = 0,
                hardBrakingInstances = 0
            )
            database.insertTripMetrics(newTripMetrics)
            application.setTripMetrics(newTripMetrics)
        }

        return@withContext user != null
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