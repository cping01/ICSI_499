package com.damc.driver_action.ui.homeBase.settings

import androidx.lifecycle.viewModelScope
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.PreferenceRepository
import com.damc.driver_action.domain.models.Users
import com.damc.driver_action.ui.BaseViewModel
import kotlinx.coroutines.launch

class SettingsViewModel(
    val localRepostories: LocalRepostories,
    val preferenceRepository: PreferenceRepository
) : BaseViewModel() {

    lateinit var users: Users
    fun upDateUser(users: Users) {
        viewModelScope.launch {
            localRepostories.upDateUserData(users)
        }
    }
}