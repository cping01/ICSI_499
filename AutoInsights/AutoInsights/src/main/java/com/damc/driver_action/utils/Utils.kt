package com.damc.driver_action.utils

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Utils {
    companion object{

        fun validateEmail(email: String): Boolean {
            val emailRegex = Regex("^\\S+@\\S+\\.\\S+\$")
            return emailRegex.matches(email)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getCurrentDateAsString(): String {
            val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDate = LocalDate.now()
            return currentDate.format(dateFormat)
        }

        fun showToast(message: String, context: Context) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}