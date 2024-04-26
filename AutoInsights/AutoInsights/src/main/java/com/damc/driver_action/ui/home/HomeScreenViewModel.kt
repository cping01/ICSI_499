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
) : BaseViewModel() {

    var acceleration = MutableLiveData<Float>()
    var velocity = MutableLiveData<Float>()
    var latitude = MutableLiveData<Double>() // LiveData for latitude
    var longitude = MutableLiveData<Double>() // LiveData for longitude
    var lastSecondAcceleration = 0.0f

    lateinit var actionData: ActionData
    var topSpeed: Float = 0.0f

    lateinit var hardStopCount: MutableLiveData<Int>
    lateinit var fastAccelerationCount: MutableLiveData<Int>

    init {
        // Initialize LiveData
        latitude.value = 0.0
        longitude.value = 0.0
    }

    fun checkFastAccOrHardStop() {
        val timer = object : CountDownTimer(1000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                // Do nothing on tick
            }

            override fun onFinish() {
                acceleration.value?.let { currentAcceleration ->
                    // Check for fast acceleration
                    if ((currentAcceleration - lastSecondAcceleration) > FAST_OR_HARD_ACCELARATION) {
                        fastAccelerationCount.postValue((fastAccelerationCount.value ?: 0) + 1)
                        actionData.fastAcceleration = fastAccelerationCount.value ?: 0
                    }
                    // Check for hard stop
                    if ((lastSecondAcceleration - currentAcceleration) > FAST_OR_HARD_ACCELARATION) {
                        hardStopCount.postValue((hardStopCount.value ?: 0) + 1)
                        actionData.hardStopCount = hardStopCount.value ?: 0
                    }
                    // Update last second acceleration
                    lastSecondAcceleration = currentAcceleration
                }
            }
        }

        timer.start()

        // Update user data after each cycle
        updateUserData(actionData)
    }

    fun updateUserData(actionData: ActionData) {
        viewModelScope.launch {
            localRepostories.updateAction(actionData)
        }
    }

    fun updateLocationData(latitude: Double, longitude: Double) {
        // Update latitude and longitude LiveData
        this.latitude.postValue(latitude)
        this.longitude.postValue(longitude)
    }

    fun goToSummary() {
        navigate(HomeScreenDirections.homeToSummary())
    }
}
