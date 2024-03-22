package com.damc.driver_action.ui.homeBase.home

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.accelerationHelper.Accelerometer
import com.damc.driver_action.common.Constants.FAST_ACCELARATION
import com.damc.driver_action.common.Constants.GOOD_ACCELARATION
import com.damc.driver_action.common.Constants.MEDIUM_ACCELARATION
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.ui.BaseViewModel
import kotlinx.coroutines.launch

class HomeScreenViewModel(
    val accelerometer: Accelerometer,
    val localRepostories: LocalRepostories
) :
    BaseViewModel() {

    var acceleration = MutableLiveData<Float>()
    var velocity = MutableLiveData<Float>()
    var lastSecondAcceleration = 0.0f

    lateinit var actionData: ActionData
    var topSpeed: Float = 0.0f

    lateinit var hardStopCount: MutableLiveData<Int>
    var goodStopCount = 0
    var mediumStopCount = 0
    lateinit var fastAccelerartionCount: MutableLiveData<Int>
    var goodAccelerationCount = 0
    var mediumAccelerationCount = 0

    var isStartRide = false


    fun checkFastAccOrHardStop() {
        val timer = object : CountDownTimer(1000, 10) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
//                if ((acceleration.value?.minus(lastSecondAcceleration))!! > FAST_ACCELARATION) {
//                    fastAccelerartionCount.postValue(fastAccelerartionCount.value?.plus(1))
//                    actionData.fastAcceleration = fastAccelerartionCount.value!!
//                }
//
//                if (lastSecondAcceleration.minus(acceleration.value!!)!! > FAST_ACCELARATION) {
//                    hardStopCount.postValue(hardStopCount.value?.plus(1))
//                    actionData.hardStopCount = hardStopCount.value!!
//                }

                if(isStartRide){
                    when {
                        (acceleration.value?.minus(lastSecondAcceleration))!! in GOOD_ACCELARATION..MEDIUM_ACCELARATION -> {
                            actionData.goodAcceleration = goodAccelerationCount.plus(1)
                        }

                        (acceleration.value?.minus(lastSecondAcceleration))!! in MEDIUM_ACCELARATION..FAST_ACCELARATION -> {
                            actionData.mediumAcceleration = mediumAccelerationCount.plus(1)
                        }

                        (acceleration.value?.minus(lastSecondAcceleration))!! > FAST_ACCELARATION -> {
                            fastAccelerartionCount.postValue(fastAccelerartionCount.value?.plus(1))
                            actionData.fastAcceleration = fastAccelerartionCount.value!!
                        }

                        lastSecondAcceleration.minus(acceleration.value!!) in GOOD_ACCELARATION..MEDIUM_ACCELARATION -> {
                            actionData.goodStopCount = goodStopCount.plus(1)
                        }

                        lastSecondAcceleration.minus(acceleration.value!!) in MEDIUM_ACCELARATION..FAST_ACCELARATION -> {
                            actionData.mediumStopCount = mediumStopCount.plus(1)
                        }

                        lastSecondAcceleration.minus(acceleration.value!!) > FAST_ACCELARATION -> {
                            hardStopCount.postValue(hardStopCount.value?.plus(1))
                            actionData.hardStopCount = hardStopCount.value!!
                        }

                    }

                    lastSecondAcceleration = acceleration.value!!
                }
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
//        navigate(HomeScreenDirections.homeToSummery())
    }
}