package com.damc.driver_action.ui.homeBase.summaryScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.adapter.SummaryAdapter
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.ui.BaseViewModel
import kotlinx.coroutines.launch

class SummaryViewModel(val localRepostories: LocalRepostories) : BaseViewModel() {

    val tripMetricsData = MutableLiveData<List<TripMetrics>>()
    val tripData = MutableLiveData<List<Trip>>()
    
    var actionData = MutableLiveData<List<ActionData>>()
    lateinit var adapter: SummaryAdapter


    fun getActionData(userID: Int) {
        viewModelScope.launch {
            actionData.postValue(localRepostories.getUserActions(userID))
        }
    }

    fun getTripData(tripId: Int ,userId: Int) {
        viewModelScope.launch {
            localRepostories.getTrips(tripId ,userId).observeForever { trips ->
                tripData.postValue(trips)
            }
        }
    }

    fun getTripMetricsData(tripId: Int,userId: Int) {
        viewModelScope.launch {
            localRepostories.getTripMetrics(tripId ,userId).observeForever { tripMetrics ->
                tripMetricsData.postValue(tripMetrics)
            }
        }
    }
}