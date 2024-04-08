package com.damc.driver_action.data.local.location

import com.damc.driver_action.TripManager.TripManager
import com.damc.driver_action.data.local.DAO.TripDao




import com.damc.driver_action.data.local.model.TripDataclasses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject


import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class GPSProcess {

    private val RAPID_ACCELERATION_THRESHOLD = 7.0 // 7 mph threshold
    private val SPEEDING_THRESHOLD = 75.0 // mph
    private val SPEEDING_DURATION = 20 * 1000 // 20 seconds in milliseconds

    private val tripManager: TripManager by inject(TripManager::class.java)



    private fun haversine(point1: TripDataclasses.GPSPoint, point2: TripDataclasses.GPSPoint): Double {
        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)
        val dlat = Math.toRadians(point2.latitude - point1.latitude)
        val dlon = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(dlat / 2).pow(2.0) +
                cos(lat1) * cos(lat2) *
                sin(dlon / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val earthRadius = 6371.0 // Earth radius in kilometers
        return earthRadius * c * 1000 // Convert to meters
    }

    fun calculateSegmentData( tripID: Int, tripDao: TripDao): List<TripDataclasses.TripSegment> { // No longer need the region as input
        val points = tripManager.getGpsPoints()
        require(points.size >= 10) { "Need at least 10 GPS points for accurate calculations" }

        val initialPoints = points.subList(0, minOf(points.size, 10))  // Take the first 10 points
        val inferredRegion = inferRegionFromGPS(initialPoints)

        val segments = mutableListOf<TripDataclasses.TripSegment>()
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
            val projectedPoint = TripDataclasses.GPSPoint(currentPoint.latitude + point.latitude,
                currentPoint.longitude + point.longitude,
                point.timestamp)

            // Calculate distance using Haversine
            val distance = haversine(currentPoint, projectedPoint)

            // Calculate duration
            val duration : Long = point.timestamp - currentPoint.timestamp

            val currentSpeed = distance/duration
            val speeding = metersPerSecondToMilesPerHour(currentSpeed)

            // Check for rapid acceleration (increase of 7 mph or more in 1 second)
            val speedDiff1 = metersPerSecondToMilesPerHour(currentSpeed) - metersPerSecondToMilesPerHour(previousSpeed)
            if (speedDiff1 >= RAPID_ACCELERATION_THRESHOLD) {
                rapidAccelerationCount++
            }

            // Hard Braking Check
            val speedDiff2 = metersPerSecondToMilesPerHour(currentSpeed) - metersPerSecondToMilesPerHour(previousSpeed)
            if (speedDiff2 <= -RAPID_ACCELERATION_THRESHOLD) { // Check for decrease
                hardBrakingCount++
            }

            // Speeding check
            if (speeding > SPEEDING_THRESHOLD) {
                if (speedingStartTime == null) {
                    speedingStartTime = point.timestamp // Start tracking
                } else if (point.timestamp - speedingStartTime >= SPEEDING_DURATION) {
                    speedingInstanceCount++
                    speedingStartTime = null // Reset
                }
            } else {
                speedingStartTime = null // Reset if speed drops below the threshold
            }


            previousSpeed = currentSpeed

            val segment = TripDataclasses.TripSegment(currentPoint, projectedPoint,
                tripID.toDouble(), distance, duration)

            CoroutineScope(Dispatchers.IO).launch {
            tripDao.insertTripSegment(segment) }

            segments.add(segment)
            currentPoint = projectedPoint
        }

        // Update trip metrics with rapidAccelerationCount
        CoroutineScope(Dispatchers.IO).launch {
            tripDao.updateRapidAccelerationInstances(tripID, rapidAccelerationCount)
        }
        CoroutineScope(Dispatchers.IO).launch {
            tripDao.updateHardBrakingInstances(tripID, hardBrakingCount)
        }
        return segments
    }

    private fun inferRegionFromGPS(points: List<TripDataclasses.GPSPoint>, reInferenceInterval: Long = 5 * 60 * 1000,
                                   maxRecentPoints: Int = 10): TripDataclasses.Region {
        require(points.isNotEmpty()) { "Need at least one point to infer a region" }

        // Initial inference
        var region = TripDataclasses.Region("Inferred Region", points[0], 0.0) // Start with a region at the first point


        val recentPoints = mutableListOf<TripDataclasses.GPSPoint>()

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

    private fun calculateRegion(currentRegion: TripDataclasses.Region, newPoint: TripDataclasses.GPSPoint, recentPoints: List<TripDataclasses.GPSPoint> ): TripDataclasses.Region {
        val distance = haversine(currentRegion.center, newPoint)
        if (distance <= currentRegion.radius * (1 + 0.2)) {
            return currentRegion // Point is within the region (with buffer)
        } else {
            // Recalculate center
            val newCenterLat = (currentRegion.center.latitude + newPoint.latitude) / 2
            val newCenterLong = (currentRegion.center.longitude + newPoint.longitude) / 2
            val newCenter = TripDataclasses.GPSPoint(newCenterLat, newCenterLong, 0L)

            //Recalculate Radius
            var maxDistanceFromCenter = 0.0
            for (point in recentPoints + newPoint) {  // Consider all recent points and the new point
                val dist = haversine(currentRegion.center, point)
                maxDistanceFromCenter = maxOf(maxDistanceFromCenter, dist)
            }

            val bufferPercentage = 0.2
            val newRadius = maxDistanceFromCenter * (1 + bufferPercentage)

            return TripDataclasses.Region("Inferred Region", newCenter, newRadius)
        }
    }

    // Conversion function (assuming meters to kilometers and seconds to hours)
    private fun metersPerSecondToMilesPerHour(speed: Double): Double {
        val metersToKm = 1 / 1000.0
        val secondsToHours = 1 / 3600.0
        return speed * metersToKm * secondsToHours * 0.621371
    }

}