package com.damc.driver_action.ui.homeBase.summaryScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.adapter.SummaryAdapter
import com.damc.driver_action.data.local.LocalRepositoryImpl
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.ui.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SummaryViewModel(val localRepostories: LocalRepostories) : BaseViewModel() {

    val tripMetricsData: LiveData<List<TripMetrics>> = MutableLiveData()
    val tripData = MutableLiveData<List<Trip>>()
    
    var actionData = MutableLiveData<List<ActionData>>()
    lateinit var adapter: SummaryAdapter


    fun getActionData(userID: Int) {
        viewModelScope.launch(Dispatchers.IO) { // Dispatches the coroutine to a background thread
            val actions = localRepostories.getUserActions(userID)
            // Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                actionData.postValue(actions)
            }
        }
    }

    fun getTripData(tripId: Int, userId: Int) {
        viewModelScope.launch(Dispatchers.IO) { // Dispatches the coroutine to a background thread
            val trips = localRepostories.getTrips(tripId, userId)
            // Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                (tripData as MutableLiveData).postValue(trips)
            }
        }
    }

    fun getTripMetricsData(tripId: Int, userId: Int) {
        viewModelScope.launch(Dispatchers.IO) { // Dispatches the coroutine to a background thread
            val tripMetrics = localRepostories.getTripMetrics(tripId, userId)
            // Switch back to the main thread to update the UI
            withContext(Dispatchers.Main) {
                (tripMetricsData as MutableLiveData).postValue(tripMetrics)
            }
        }
    }
}