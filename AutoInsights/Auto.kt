private const val RAPID_ACCELERATION_THRESHOLD = 7.0 // 7 mph threshold
private const val SPEEDING_THRESHOLD = 75.0 // mph 
private const val SPEEDING_DURATION = 20 * 1000 // 20 seconds in milliseconds 

data class GPSPoint(val latitude: Double, val longitude: Double, val timestamp: Long)

data class TripSegment(val startPoint: GPSPoint, 
                       val endPoint: GPSPoint, 
                       val distance: Double,   // In meters
                       val duration: Long)     // In seconds 


 data class Region(
     val name: String, // could give the region a name
     val center: GPSPoint, 
     val radius: Double // Radius in meters 
                    )

fun main() {
    val gpsPoints = listOf(/* Your list of GPSPoint objects */)
    
    val tripSegments = calculateSegmentData(gpsPoints)  // Region is handled internally
    val speedingInstances = detectSpeedingInstances(tripSegments)

    println("Number of speeding instances: $speedingInstances")

    // Store or process the tripSegments as needed
}

fun haversine(point1: GPSPoint, point2: GPSPoint): Double {
    val lat1 = Math.toRadians(point1.latitude)
    val lat2 = Math.toRadians(point2.latitude)
    val dlat = Math.toRadians(point2.latitude - point1.latitude)
    val dlon = Math.toRadians(point2.longitude - point1.longitude)

    val a = Math.pow(Math.sin(dlat / 2), 2.0) + 
            Math.cos(lat1) * Math.cos(lat2) * 
            Math.pow(Math.sin(dlon / 2), 2.0)

    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)) 

    val earthRadius = 6371.0 // Earth radius in kilometers
    return earthRadius * c * 1000 // Convert to meters
}

fun calculateSegmentData(points: List<GPSPoint>, tripID: Int): List<TripSegment> { // No longer need the region as input
    require(points.size >= 10) { "Need at least 10 GPS points for accurate calculations" }

    val initialPoints = points.subList(0, minOf(points.size, 10))  // Take the first 10 points
    val inferredRegion = inferRegionFromGPS(initialPoints)

    val segments = mutableListOf<TripSegment>()
    var currentPoint = inferredRegion.center 

    var previousSpeed = 0.0 // Initialize previous speed in meters per second
    var rapidAccelerationCount = 0
    var hardBrakingCount = 0 

        // Speeding tracking
        var speedingInstanceCount = 0
        var speedingStartTime: Long? = null 

    for ((index, point) in points.withIndex()) {
        if (index == 0) continue // Skip the first point

        // Reconstruct approximate point
        val projectedPoint = GPSPoint(currentPoint.latitude + point.latitude,
                                      currentPoint.longitude + point.longitude,
                                      point.timestamp)

        // Calculate distance using Haversine
        val distance = haversine(currentPoint, projectedPoint)

        // Calculate duration
        val duration = point.timestamp - currentPoint.timestamp 

        val currentSpeed = distance / duration
        val speeding = metersPerSecondToMilesPerHour(currentSpeed)

         // Check for rapid acceleration (increase of 7 mph or more in 1 second)
         val speedDiff = metersPerSecondToMilesPerHour(currentSpeed) - metersPerSecondToMilesPerHour(previousSpeed)
         if (speedDiff >= RAPID_ACCELERATION_THRESHOLD) {
             rapidAccelerationCount++
         }

         // Hard Braking Check
        val speedDiff = metersPerSecondToMilesPerHour(currentSpeed) - metersPerSecondToMilesPerHour(previousSpeed)
        if (speedDiff <= -RAPID_ACCELERATION_THRESHOLD) { // Check for decrease 
            hardBrakingCount++
        }

           // Speeding check
           if (speeding > SPEEDING_THRESHOLD) {
            if (speedingStartTime == null) {  
                speedingStartTime = point.timestamp // Start tracking
            } else if (point.timestamp - speedingStartTime!! >= SPEEDING_DURATION) { 
                speedingInstanceCount++ 
                speedingStartTime = null // Reset 
            }
        } else {
            speedingStartTime = null // Reset if speed drops below the threshold
        }      

 
         previousSpeed = currentSpeed

        val segment = TripSegment(currentPoint, projectedPoint, tripID, distance, duration)
        
        tripDao.insertTripSegment(segment)
        
        segments.add(segment)
        currentPoint = projectedPoint
    }

     // Update trip metrics with rapidAccelerationCount
    tripDao.updateRapidAccelerationInstances(tripID, rapidAccelerationCount)
    tripDao.updateHardBrakingInstances(tripID, hardBrakingCount)
    return segments
}

fun inferRegionFromGPS(points: List<GPSPoint>,  reInferenceInterval: Long = 5 * 60 * 1000,
maxRecentPoints: Int = 10): Region {
    require(points.isNotEmpty()) { "Need at least one point to infer a region" }

    // Initial inference
    var region = Region("Inferred Region", points[0], 0.0) // Start with a region at the first point

  
    val recentPoints = mutableListOf<GPSPoint>() 

    var lastInferenceTime = points[0].timestamp // Start with the first point's timestamp

    for (point in points) {
        val currentTime = point.timestamp
        if (currentTime - lastInferenceTime >= reInferenceInterval) { 
            region = calculateRegion(region, point, recentPoints) // Pass recentPoints here
            lastInferenceTime = currentTime 

            recentPoints.add(point) // Keep track of the point
            if (recentPoints.size > maxRecentPoints) {
                recentPoints.removeFirst() // Enforce maximum limit
            }
        }
    } 
    return region 
}

fun calculateRegion(currentRegion: Region, newPoint: GPSPoint): Region {
    val distance = haversine(currentRegion.center, newPoint)
    if (distance <= currentRegion.radius * (1 + bufferPercentage)) { 
        return currentRegion // Point is within the region (with buffer)
    } else {
          // Recalculate center 
          val newCenterLat = (currentRegion.center.latitude + newPoint.latitude) / 2
          val newCenterLong = (currentRegion.center.longitude + newPoint.longitude) / 2
          val newCenter = GPSPoint(newCenterLat, newCenterLong, 0L)
      
          //Recalculate Radius
          var maxDistanceFromCenter = 0.0 
          for (point in recentPoints + newPoint) {  // Consider all recent points and the new point
              val dist = haversine(currentRegion.center, point)
              maxDistanceFromCenter = maxOf(maxDistanceFromCenter, dist)
          }

          val bufferPercentage = 0.2 
          val newRadius = maxDistanceFromCenter * (1 + bufferPercentage) 
  
          return Region("Inferred Region", newCenter, newRadius)
    }
}


private var currentTripID: Int? = null 

fun processTripData(gpsPoints: List<GPSPoint>) { // Or whatever your function is called
    if (shouldStartNewTrip()) { // Placeholder
        currentTripID = createNewTripRecord() 
    }

    if (currentTripID != null) {
        val tripSegments = calculateSegmentData(gpsPoints, currentTripID!!) 
        // logic to process tripSegments for real time data
    }
}

fun shouldStartNewTrip(): Boolean {
    //Implement trip start detection (ex: user presses "Start Trip")
    return true // Placeholder 
}

fun createNewTripRecord(): Int {
    val newTrip = Trip(userID = ..., startTime = Date(), autoDetected = false) // needs to be cleaned up to be done more automatically
    val tripID = tripDao.insertTrip(newTrip) 
    return tripID.toInt() 
}

// Conversion function (assuming meters to kilometers and seconds to hours)
fun metersPerSecondToMilesPerHour(speed: Double): Double {
    val metersToKm = 1 / 1000.0
    val secondsToHours = 1 / 3600.0
    return speed * metersToKm * secondsToHours * 0.621371
}


fun endTrip() {
    // 1. Get the current tripID
    currentTripID?.let { tripID ->
       tripDao.calculateAndUpdateTotalDistance(tripID) 
       tripDao.calculateAndUpdateTripLength(tripID)
       tripDao.calculateAndUpdateTopSpeed(tripID)
       tripDao.calculateAndUpdateAverageSpeed(tripID)
    }
    currentTripID = null // Reset the current trip
}
