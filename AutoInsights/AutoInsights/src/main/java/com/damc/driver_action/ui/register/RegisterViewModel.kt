package com.damc.driver_action.ui.register

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.damc.driver_action.domain.LocalRepostories
import com.damc.driver_action.domain.models.Users
import com.damc.driver_action.ui.BaseViewModel
import com.damc.driver_action.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterViewModel(val dataBase: LocalRepostories) : BaseViewModel() {

    lateinit var users: Users
    fun registerToLogin() {
        CoroutineScope(Dispatchers.Main).launch { navigateBack() }
    }

    fun addUerToDb(users: Users) {
        viewModelScope.launch { dataBase.insertUser(users) }
    }

    suspend fun checkUsernameInDb(username: String): Int {
        return dataBase.isUsernameInDb(username)
    }

    suspend fun validateFields(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        context: Context
    ) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Fields cannot be empty", context)
        } else if (!Utils.validateEmail(email)) {
            showToast("Invalid email", context)
        } else if (password != confirmPassword) {
            showToast("Password & Confirm Password should be match", context)
        } else if (checkUsernameInDb(username) > 0) {
            showToast("Username already taken", context)
        } else {
            users = Users(username, password)
            addUerToDb(users)// can use hashing for store password securely
            showToast("Successfully Registered", context)
            registerToLogin()
        }
    }

    fun showToast(message: String, context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }

    }


}