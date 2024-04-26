package com.damc.driver_action.activityTrackingHelper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.damc.driver_action.common.Constants
import com.google.android.gms.location.ActivityTransitionResult
import io.karn.notify.Notify
import java.text.SimpleDateFormat
import java.util.*

open class ActivityTransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let {
                result.transitionEvents.forEach { event ->
                    // Info about activity
                    val info =
                        "Transition: " + ActivityTransitionsUtil.toActivityString(event.activityType) +
                                " (" + ActivityTransitionsUtil.toTransitionType(event.transitionType) + ")" + " " +
                                SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
                    // notification details
                    Notify
                        .with(context)
                        .content {
                            title = "Activity Detected"
                            text = "State: ${
                                ActivityTransitionsUtil.toActivityString(
                                    event.activityType
                                )
                            } "
                        }
                        .show(id = Constants.ACTIVITY_TRANSITION_NOTIFICATION_ID)
                    Toast.makeText(context, info, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
