package com.damc.driver_action.TripManager

import android.content.Context

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.damc.driver_action.data.local.model.TripDataclasses
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult



class TripManager (
    private val context: Context,
    private val fusedLocationProviderClient: FusedLocationProviderClient,

) {

    private val gpsPointsLock = Any() // Object for synchronization
    private val gpsPoints: MutableList<TripDataclasses.GPSPoint> = mutableListOf()

    private var isTripActive: Boolean = false
    private lateinit var locationCallback: LocationCallback // We'll define this soon


    fun startTrip() {
        if (checkLocationPermissions()) {
            isTripActive = true
            requestLocationUpdates()
        } else {
            // Permission not granted
            Toast.makeText(
                context,
                "Location permission is required to start the trip",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun stopTrip() {
        isTripActive = false
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    // Inside your TripManager class:
    private fun checkLocationPermissions(): Boolean { // No context parameter needed here
        if (ActivityCompat.checkSelfPermission(
                context, // Use the stored context
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,  // And here
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted yet, request it
            ActivityCompat.requestPermissions(
                context as Activity,  // The current context should be an Activity
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return false
        } else {
            return true
        }
    }


    // Companion object for the request code
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1  // Or any arbitrary number
    }

    private val locationCallback1 = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return // Handle potential null location results
            for (location in locationResult.locations) {
                val gpsPoint = TripDataclasses.GPSPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = location.time // Or System.currentTimeMillis()
                )
                synchronized(gpsPointsLock) {
                    gpsPoints.add(gpsPoint)
                }
            }

        }
    }


    private fun requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted. Handle this, potentially with:
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return // Exit the method if permission wasn't granted
        }

        // Permission already granted, proceed:
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback1,
            Looper.getMainLooper()
        )
    }
    fun getGpsPoints(): MutableList<TripDataclasses.GPSPoint> {
        synchronized(gpsPointsLock) {
            return gpsPoints // Return a copy
        }
    }

    }



