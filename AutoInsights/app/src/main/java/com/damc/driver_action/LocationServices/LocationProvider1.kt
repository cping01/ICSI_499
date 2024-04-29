import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.damc.driver_action.LocationServices.GPSPoint
import com.damc.driver_action.LocationServices.GPSProcessor
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.data.local.room.DatabaseClient
import com.damc.driver_action.domain.models.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class LocationProvider1(private val context: Context, private val gpsProcessor: GPSProcessor, private val userId: Int) {
    private var currentTripId: Int = 0
    private var isTripStarted = false
    suspend fun startNewTrip() {
        val application = context.applicationContext as AssignmentApplication
        val currentTrip = application.getTrip()
        val currentTripMetrics = application.getTripMetrics()
        val databaseClient = DatabaseClient(context)
        val appDatabase = databaseClient.getAppDatabase()
        val onDataBaseActions = appDatabase?.OnDataBaseActions()
        onDataBaseActions?.insertTrip(Trip(userId = userId ,date = Date())) // Set the date and time of the trip
        currentTripId = onDataBaseActions?.getLatestTrip(userId)?.id  ?: 0
    }

    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val gpsPoints = mutableListOf<GPSPoint>()

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val currentTimeStamp = System.currentTimeMillis()
            val gpsPoint = GPSPoint(location.latitude, location.longitude, currentTimeStamp)
            gpsPoints.add(gpsPoint)
            val SOME_THRESHOLD = 1000
            if (gpsPoints.size >= SOME_THRESHOLD) {
                GlobalScope.launch {
                    if (!isTripStarted) {
                        withContext(Dispatchers.IO) {
                            startNewTrip() // Start a new trip before calculating metrics
                        }
                        isTripStarted = true
                    }
                    gpsProcessor.calculateAllMetrics(context, gpsPoints.toList(), currentTripId.toInt(), userId)
                    gpsPoints.clear()
                }
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0f, locationListener)
        }
    }

    fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
        isTripStarted = false
    }
}