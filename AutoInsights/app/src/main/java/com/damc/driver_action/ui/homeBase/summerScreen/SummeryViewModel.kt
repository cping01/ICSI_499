package com.damc.driver_action.ui.homeBase.summerScreen

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.adapter.SummeryAdapter
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.ui.BaseViewModel
import kotlinx.coroutines.launch

class SummeryViewModel(val localRepostories: LocalRepostories) : BaseViewModel() {

    var actionData = MutableLiveData<List<ActionData>>()
    lateinit var adapter: SummeryAdapter


    fun getActionData(userID: Int) {
        viewModelScope.launch {
            actionData.postValue(localRepostories.getUserActions(userID))
        }
    }
}