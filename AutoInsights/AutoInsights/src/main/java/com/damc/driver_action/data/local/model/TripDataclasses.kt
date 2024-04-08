package com.damc.driver_action.data.local.model

class TripDataclasses {

    data class GPSPoint(val latitude: Double, val longitude: Double, val timestamp: Long)

    data class TripSegment(val startPoint: GPSPoint,
                           val endPoint: GPSPoint,
                           val tripID: Double,
                           val distance: Double,   // In meters
                           val duration: Long)     // In seconds


    data class Region(
        val name: String, // could give the region a name
        val center: GPSPoint,
        val radius: Double // Radius in meters
    )


}