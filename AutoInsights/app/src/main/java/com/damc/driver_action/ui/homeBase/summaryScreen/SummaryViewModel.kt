package com.damc.driver_action.ui.homeBase.summaryScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.adapter.SummaryAdapter
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.ui.BaseViewModel
import kotlinx.coroutines.launch

class SummaryViewModel(val localRepostories: LocalRepostories) : BaseViewModel() {

    var actionData = MutableLiveData<List<ActionData>>()
    lateinit var adapter: SummaryAdapter


    fun getActionData(userID: Int) {
        viewModelScope.launch {
            actionData.postValue(localRepostories.getUserActions(userID))
        }
    }
}