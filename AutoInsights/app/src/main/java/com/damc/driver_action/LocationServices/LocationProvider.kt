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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocationProvider(private val context: Context, private val gpsProcessor: GPSProcessor) {

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
                    gpsProcessor.processGPSPoints(gpsPoints.toList())
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
    }
}