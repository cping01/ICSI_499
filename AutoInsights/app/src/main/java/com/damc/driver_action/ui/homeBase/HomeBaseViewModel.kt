package com.damc.driver_action.ui.homeBase

import com.damc.driver_action.ui.BaseViewModel

class HomeBaseViewModel : BaseViewModel() {

    fun logOut() {
        navigateBack()
    }

}