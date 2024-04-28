package com.damc.driver_action.velocityHelper

import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.os.Bundle




interface IBaseGpsListener : LocationListener, GpsStatus.Listener {

    fun onLocationChangedI(location: Location?)

    fun onProviderDisabledI(provider: String?)

    fun onProviderEnabledI(provider: String?)

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?)

    override fun onGpsStatusChanged(event: Int)
}