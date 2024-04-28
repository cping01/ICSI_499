package com.damc.driver_action.LocationServices
import android.content.Context
import com.damc.driver_action.data.local.room.DatabaseClient
import kotlin.math.*

import kotlin.Pair

import java.util.*
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.data.local.room.OnDataBaseActions

class GPSProcessor() {



    data class Region(val name: String, val center: GPSPoint, val radius: Double)

    class Geo(private val lat: Double, private val lon: Double) {
        companion object {
            const val earthRadiusKm: Double = 6372.8
        }

        fun haversine(destination: Geo): Double {
            val dLat = Math.toRadians(destination.lat - this.lat)
            val dLon = Math.toRadians(destination.lon - this.lon)
            val originLat = Math.toRadians(this.lat)
            val destinationLat = Math.toRadians(destination.lat)
            val a = sin(dLat / 2).pow(2.toDouble()) +
                    sin(dLon / 2).pow(2.toDouble()) *
                    cos(originLat) * cos(destinationLat)
            val c = 2 * asin(sqrt(a))
            return earthRadiusKm * c * 1000;  // return distance in meters
        }
    }

    private fun haversine(point1: GPSPoint, point2: GPSPoint): Double {
        val geo1 = Geo(point1.latitude, point1.longitude)
        val geo2 = Geo(point2.latitude, point2.longitude)
        return geo1.haversine(geo2)  // Distance in kilometers
    }



    fun projectTrajectoryPrivacy(points: List<GPSPoint>): List<GPSPoint> {
        // Scaling
        val scalingFactor = 1e6


        // Differences
        val differences = mutableListOf<GPSPoint>()
        for (i in 1 until points.size) {
            val p1 = points[i - 1]
            val p2 = points[i]
            val timeDifference = p2.timestamp - p1.timestamp
            if (timeDifference > 0) {
                differences.add(
                    GPSPoint(
                        (p2.latitude * scalingFactor - p1.latitude * scalingFactor) / scalingFactor,
                        (p2.longitude * scalingFactor - p1.longitude * scalingFactor) / scalingFactor,
                        timeDifference
                    )
                )
            }
        }

        // Return the differences
        return differences
    }

    private fun inferRegionFromGPS(points: List<GPSPoint>): Region {
        require(points.isNotEmpty()) { "Need at least one point to infer a region" }

        // Bounding Box Initialization
        var minLat = points[0].latitude
        var maxLat = points[0].latitude
        var minLon = points[0].longitude
        var maxLon = points[0].longitude

        for (point in points) {
            minLat = minOf(minLat, point.latitude)
            maxLat = maxOf(maxLat, point.latitude)
            minLon = minOf(minLon, point.longitude)
            maxLon = maxOf(maxLon, point.longitude)
        }

        val center = GPSPoint((minLat + maxLat) / 2, (minLon + maxLon) / 2, points[0].timestamp)
        val initialRadius = calculateRadius(points, center) // Calculate the initial radius based on points
        // Initialize the Region
        var region = Region("Inferred Region", center, initialRadius)
        return region
    }

    private fun calculateRadius(points: List<GPSPoint>, center: GPSPoint): Double {
        var maxDistance = 0.0
        for (point in points) {
            val distance = haversine(center, point)
            maxDistance = maxOf(maxDistance, distance)
        }
        return maxDistance
    }


    // Helper Function to calculate center (replace with your logic)
    private fun calculateCenter(points: List<GPSPoint>): GPSPoint {
        // can implement averaging or a more sophisticated centroid calculation here
        val sumLat = points.sumOf { it.latitude }
        val sumLon = points.sumOf { it.longitude }
        val avgLat = sumLat / points.size
        val avgLon = sumLon / points.size
        return GPSPoint(avgLat, avgLon, points[0].timestamp) // Use timestamp from any point
    }

    fun processGPSPoints(points: List<GPSPoint>): List<GPSPoint> {
        var lastInferenceTime = System.currentTimeMillis()
        val recentPoints = mutableListOf<GPSPoint>()
        val reInferenceInterval = 5 * 60 * 1000
        var currentRegion: Region = inferRegionFromGPS(points) // Initial region inference
        val projectedTrajectory = mutableListOf<GPSPoint>()
        for (i in 0 until points.size - 1) {
            val point = points[i]
            val nextPoint = points[i + 1]
            val currentTime = point.timestamp

            if (currentTime - lastInferenceTime >= reInferenceInterval) {
                // Recalculate the region
                currentRegion = inferRegionFromGPS(recentPoints)

                // Reset for the next interval
                lastInferenceTime = currentTime
                recentPoints.clear()
            }

            recentPoints.add(point)

            // Privacy-Preserving Projection
            val trajectoryDifferences =
                projectTrajectoryPrivacy(listOf(point, nextPoint)) // Use the current region

            // Add the region center as the initial point
            projectedTrajectory.add(
                GPSPoint(
                    currentRegion.center.latitude,
                    currentRegion.center.longitude,
                    point.timestamp
                )
            )

            for (diff in trajectoryDifferences) {
                val newLat = projectedTrajectory.last().latitude + diff.latitude
                val newLon = projectedTrajectory.last().longitude + diff.longitude
                projectedTrajectory.add(
                    GPSPoint(
                        newLat,
                        newLon,
                        projectedTrajectory.last().timestamp + diff.timestamp
                    )
                ) // Use the timestamp from the difference
            }
        }
        return projectedTrajectory
    }


    fun CalculateHardAccelerationInstances(segments: List<Pair<Double, Long>>): Int {
        var hardAccelerationInstances = 0

        for (i in 0 until segments.size - 1) {
            val segment = segments[i]
            val nextSegment = segments[i + 1]
            val distance = segment.first
            val timeDifference = segment.second
            val nextTimeDifference = nextSegment.second

            if (timeDifference > 0 && nextTimeDifference > 0) {
                val speed = (distance / timeDifference) * 3600
                val nextSpeed = (nextSegment.first / nextTimeDifference) * 3600

                if (nextSpeed - speed > 12) {
                    hardAccelerationInstances++
                }
            }
        }

        return hardAccelerationInstances
    }

    fun CalculateHardBrakingInstances(segments: List<Pair<Double, Long>>): Int {
        var hardBrakingInstances = 0

        for (i in 0 until segments.size - 1) {
            val segment = segments[i]
            val nextSegment = segments[i + 1]
            val distance = segment.first
            val timeDifference = segment.second
            val nextTimeDifference = nextSegment.second

            if (timeDifference > 0 && nextTimeDifference > 0) {
                val speed = (distance / timeDifference) * 3600
                val nextSpeed = (nextSegment.first / nextTimeDifference) * 3600

                if (speed - nextSpeed > 12) {
                    hardBrakingInstances++
                }
            }
        }

        return hardBrakingInstances
    }

    fun CalculateSegment(projectedTrajectory: List<GPSPoint>): List<Pair<Double, Long>> {
        val segments = mutableListOf<Pair<Double, Long>>()

        for (i in 0 until projectedTrajectory.size - 1) {
            val point1 = projectedTrajectory[i]
            val point2 = projectedTrajectory[i + 1]

            val distance = haversine(point1, point2) // Distance in meters
            val timeDifference = point2.timestamp - point1.timestamp

            if (timeDifference > 0) {
                segments.add(Pair(distance, timeDifference))
            }
        }

        return segments
    }


    fun CalculateTripDistance(segments: List<Pair<Double, Long>>): Double {
        var totalDistance = 0.0

        for (segment in segments) {

            val distance = segment.first
            totalDistance += distance
        }

        return totalDistance / 1000.0 // Convert meters to kilometers
    }

    fun calculateMetrics(segments: List<Pair<Double, Long>>): TripSummary {
        var totalSpeed = 0.0
        var maxSpeed = 0.0
        var speedingInstances = 0
        var lastSpeed = 0.0
        var speedingTime = 0L

        for ((distance, timeDifference) in segments) {
            val speed = (distance / timeDifference) * 3600

            if (speed > maxSpeed) {
                maxSpeed = speed
            }

            totalSpeed += speed

            if (speed > 121) {
                if (lastSpeed > 121) {
                    speedingTime += timeDifference
                } else {
                    speedingTime = timeDifference
                }

                if (speedingTime >= 20 * 1000) {
                    speedingInstances++
                    speedingTime -= 20 * 1000
                }
            }

            lastSpeed = speed
        }

        val averageSpeed = if (segments.isNotEmpty()) totalSpeed / segments.size else 0.0
        val tripDistance = CalculateTripDistance(segments) / 1.60934 // Convert kilometers to miles
        val tripDuration = CalculateTripDuration(segments)
        val hardAccelerationInstances = CalculateHardAccelerationInstances(segments)
        val hardBrakingInstances = CalculateHardBrakingInstances(segments)

        return TripSummary(
            maxSpeed = maxSpeed / 1.60934, // Convert kilometers per hour to miles per hour
            averageSpeed = averageSpeed / 1.60934, // Convert kilometers per hour to miles per hour
            tripDuration = tripDuration,
            tripDistance = tripDistance,
            speedingInstances = speedingInstances,
            hardAccelerationInstances = hardAccelerationInstances,
            hardBrakingInstances = hardBrakingInstances
        )
    }


    fun CalculateTripDuration(segments: List<Pair<Double, Long>>): Double {
        val sumOfTimeDifferences = segments.sumOf { it.second }
        val tripDuration = sumOfTimeDifferences / 60000.0 // Convert milliseconds to minutes
        return tripDuration
    }


    data class TripSummary(
        val maxSpeed: Double, // kph
        val averageSpeed: Double, // kph
        val tripDuration: Double, // minutes
        val tripDistance: Double, // km
        val speedingInstances: Int,
        val hardAccelerationInstances: Int,
        val hardBrakingInstances: Int
    )

    fun tripSummaryToTripMetrics(userId: Int,tripSummary: TripSummary): TripMetrics {
        return TripMetrics(
            // Fill in the properties
            userId = userId,
            maxSpeed = tripSummary.maxSpeed,
            averageSpeed = tripSummary.averageSpeed,
            tripDuration = tripSummary.tripDuration,
            tripDistance = tripSummary.tripDistance,
            speedingInstances = tripSummary.speedingInstances,
            hardAccelerationInstances = tripSummary.hardAccelerationInstances,
            hardBrakingInstances = tripSummary.hardBrakingInstances

        )
    }

    suspend fun calculateAllMetrics(context: Context, points: List<GPSPoint>, tripId: Int, userId: Int): TripSummary {
        val differences = projectTrajectoryPrivacy(points)
        val projectedTrajectory = processGPSPoints(differences)
        val segments = CalculateSegment(projectedTrajectory)
        val metrics = calculateMetrics(segments)
        val tripMetrics = tripSummaryToTripMetrics(userId ,metrics)
        val databaseClient = DatabaseClient(context)
        val appDatabase = databaseClient.getAppDatabase()
        val onDataBaseActions = appDatabase?.OnDataBaseActions()

        // Retrieve the current TripMetrics for the trip
        val currentTripMetricsLiveData = onDataBaseActions?.getTripMetrics(tripId, userId)
        val currentTripMetrics = currentTripMetricsLiveData?.value?.firstOrNull()

        // Calculate the new average speed
        val totalDistance: Double? = currentTripMetrics?.tripDistance?.plus(metrics.tripDistance)
        val totalDuration: Double? = currentTripMetrics?.tripDuration?.plus(metrics.tripDuration)
        val newAverageSpeed = if (totalDuration != null && totalDistance != null) {
            totalDistance / totalDuration
        } else {
            // Handle the case where totalDuration or totalDistance is null
            0.0
        }

        // Update the average speed in the new TripMetrics
        tripMetrics.averageSpeed = (newAverageSpeed ?: tripMetrics.averageSpeed)

        // Update the TripMetrics in the database
        onDataBaseActions?.updateTripMetrics(    maxSpeed = tripMetrics.maxSpeed,
            averageSpeed = tripMetrics.averageSpeed,
            tripDuration = tripMetrics.tripDuration,
            tripDistance = tripMetrics.tripDistance,
            speedingInstances = tripMetrics.speedingInstances,
            hardAccelerationInstances = tripMetrics.hardAccelerationInstances,
            hardBrakingInstances = tripMetrics.hardBrakingInstances, tripId)

        return metrics
    }


}

