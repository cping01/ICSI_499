package com.damc.driver_action.ui.home

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.accelerationHelper.Accelerometer
import com.damc.driver_action.accelerationHelper.Gyroscope
import com.damc.driver_action.common.Constants.FAST_OR_HARD_ACCELARATION
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.ui.BaseViewModel
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    val accelerometer: Accelerometer,
    val gyroscope: Gyroscope,
    val localRepostories: LocalRepostories
) :
    BaseViewModel() {

    var acceleration = MutableLiveData<Float>()
    var velocity = MutableLiveData<Float>()
    var lastSecondAcceleration = 0.0f

    lateinit var actionData: ActionData
    var topSpeed: Float = 0.0f

    lateinit var hardStopCount: MutableLiveData<Int>
    lateinit var fastAccelerartionCount: MutableLiveData<Int>

    fun checkFastAccOrHardStop() {
        val timer = object : CountDownTimer(1000, 10) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                if ((acceleration.value?.minus(lastSecondAcceleration))!! > FAST_OR_HARD_ACCELARATION) {
                    fastAccelerartionCount.postValue(fastAccelerartionCount.value?.plus(1))
                    actionData.fastAcceleration = fastAccelerartionCount.value!!
                }

                if (lastSecondAcceleration.minus(acceleration.value!!)!! > FAST_OR_HARD_ACCELARATION) {
                    hardStopCount.postValue(hardStopCount.value?.plus(1))
                    actionData.hardStopCount = hardStopCount.value!!
                }

                lastSecondAcceleration = acceleration.value!!
            }
        }

        timer.start()

        updateUserData(actionData)

    }

    fun updateUserData(actionData: ActionData) {
        viewModelScope.launch {
            localRepostories.updateAction(actionData)
        }
    }

    fun goToSummery() {
        navigate(HomeScreenDirections.homeToSummery())
    }
}