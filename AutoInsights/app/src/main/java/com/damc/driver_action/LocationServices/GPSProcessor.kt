package com.damc.driver_action.LocationServices
import android.content.Context
import com.damc.driver_action.data.local.room.DatabaseClient
import kotlin.math.*

import kotlin.Pair

import java.util.*
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.data.local.room.OnDataBaseActions

class GPSProcessor(private val points: List<GPSPoint>) {



    data class Region(val name: String, val center: GPSPoint, val radius: Double)

    class Geo(private val lat: Double, private val lon: Double) {
        companion object {
            const val earthRadiusKm: Double = 6372.8
        }

        fun haversine(destination: Geo): Double {
            val dLat = Math.toRadians(destination.lat - this.lat);
            val dLon = Math.toRadians(destination.lon - this.lon);
            val originLat = Math.toRadians(this.lat);
            val destinationLat = Math.toRadians(destination.lat);
            val a = Math.pow(Math.sin(dLat / 2), 2.toDouble()) +
                    Math.pow(Math.sin(dLon / 2), 2.toDouble()) *
                    Math.cos(originLat) * Math.cos(destinationLat);
            val c = 2 * Math.asin(Math.sqrt(a));
            return earthRadiusKm * c * 1000;  // return distance in meters
        }
    }

    private fun haversine(point1: GPSPoint, point2: GPSPoint): Double {
        val geo1 = Geo(point1.latitude, point1.longitude)
        val geo2 = Geo(point2.latitude, point2.longitude)
        return geo1.haversine(geo2)  // Distance in kilometers
    }


    private fun calculateHeading(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dlon = Math.toRadians(lon2 - lon1)
        val latHead1 = Math.toRadians(lat1)
        val latHead2 = Math.toRadians(lat2)

        val y = sin(dlon) * cos(latHead2)
        val x = cos(latHead1) * sin(latHead2) - sin(latHead1) * cos(latHead2) * cos(dlon)
        return Math.toDegrees(atan2(y, x))
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
        val initialRadius =
            calculateRadius(points, center) // Calculate the initial radius based on points
        //val initialRadius = 300 // Set a default initial radius
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

    private fun calculateRegion(
        currentRegion: Region,
        newPoint: GPSPoint,
        recentPoints: List<GPSPoint>
    ): Region {
        // Distance from new point to region center
        val distance = haversine(currentRegion.center, newPoint)

        // Simple Expansion Logic
        if (distance > currentRegion.radius * (1 + 0.2)) {
            // Point is outside (with buffer), expand radius
            val newRadius = distance * 1.1 // Expand by 10%
            return Region(currentRegion.name, currentRegion.center, newRadius)
        } else {
            // Optionally recalculate the center based on recent points
            val newCenter = calculateCenter(recentPoints + newPoint)
            return Region(currentRegion.name, newCenter, currentRegion.radius)
        }
    }

    // Helper Function to calculate center (replace with your logic)
    private fun calculateCenter(points: List<GPSPoint>): GPSPoint {
        // You can implement averaging or a more sophisticated centroid calculation here
        val sumLat = points.sumOf { it.latitude }
        val sumLon = points.sumOf { it.longitude }
        val avgLat = sumLat / points.size
        val avgLon = sumLon / points.size
        return GPSPoint(avgLat, avgLon, points[0].timestamp) // Use timestamp from any point
    }

    fun processGPSPoints( points: List<GPSPoint>): List<GPSPoint> {
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


//    fun ProcessTrajectory(fileName: String, numRowsToSkip: Int): ArrayList<String> {
//        val lines = ArrayList<String>()
//        val file = File(fileName)
//        var count = 0
//
//        file.forEachLine { line ->
//            if (count >= numRowsToSkip) {
//                val Data = line.split(",")
//                val timestamp = Data[4] + " " + Data[5]
//                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//                val date =  dateFormat.parse(timestamp)
//                val GPSPoint = GPSPoint(Data[0].toDouble(), Data[1].toDouble(), date)
//                lines.add(line)
//            }
//            count++
//        }

//        return lines
//    }

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

    fun tripSummaryToTripMetrics(tripSummary: GPSProcessor.TripSummary): TripMetrics {
        return TripMetrics(
            // Fill in the properties here. For example:
            maxSpeed = tripSummary.maxSpeed,
            averageSpeed = tripSummary.averageSpeed,
            tripDuration = tripSummary.tripDuration,
            tripDistance = tripSummary.tripDistance,
            speedingInstances = tripSummary.speedingInstances,
            hardAccelerationInstances = tripSummary.hardAccelerationInstances,
            hardBrakingInstances = tripSummary.hardBrakingInstances
            // Add the rest of the properties here
        )
    }

    suspend fun calculateAllMetrics(context: Context, points: List<GPSPoint>, tripId: Int): TripSummary {
        val differences = projectTrajectoryPrivacy(points)
        val projectedTrajectory = processGPSPoints(differences)
        val segments = CalculateSegment(projectedTrajectory)
        val metrics = calculateMetrics(segments)
        val tripMetrics = tripSummaryToTripMetrics(metrics)
        val databaseClient = DatabaseClient(context)
        val appDatabase = databaseClient.getAppDatabase()
        val onDataBaseActions = appDatabase?.OnDataBaseActions()

        // Retrieve the current TripMetrics for the trip
        val currentTripMetrics = onDataBaseActions?.getTripMetrics(tripId)

        // Calculate the new average speed
        val totalDistance = currentTripMetrics?.tripDistance?.plus(metrics.tripDistance)
        val totalDuration = currentTripMetrics?.tripDuration?.plus(metrics.tripDuration)
        val newAverageSpeed = totalDistance?.div(totalDuration ?: 1)

        // Update the average speed in the new TripMetrics
        tripMetrics.averageSpeed = newAverageSpeed ?: tripMetrics.averageSpeed

        // Update the TripMetrics in the database
        onDataBaseActions?.updateTripMetrics(tripMetrics, tripId)

        return metrics
    }

//    fun main() {
//        val points = listOf(
//            // pull list of gps data
//        )
//        val processor = com.damc.driver_action.LocationServices.GPSProcessor(points)
//        val metrics = processor.calculateAllMetrics()
//        // store 'metrics' in database
//    }


//    fun main() {
//        val processedtrajectory = ProcessTrajectory("/Users/ajn/Desktop/20120712091004.plt", 6)
//        for (i in 0 until processedtrajectory.size) {
//            println(processedtrajectory[i])
//            if (i == 5) {
//                break
//            }
//        }
//    }


// fun main() {

//     val points = listOf(
//         GPSPoint(49.9931740, 30.2978897, 1668624500000),
//         GPSPoint(49.9905254, 30.2917099, 1668624530000),
//         GPSPoint(49.9863314, 30.2841568, 1668624565000),
//         GPSPoint(49.9803709, 30.2748871, 1668624600000),
//         GPSPoint(49.9726432, 30.2608109, 1668624650000),
//         GPSPoint(49.9611597, 30.2402115, 1668624700000),
//         GPSPoint(49.9545334, 30.2288818, 1668624730000),
//         GPSPoint(49.9461388, 30.2175522, 1668624790000),
//         GPSPoint(49.9406152, 30.2106857, 1668624830000),
//         GPSPoint(49.9406152, 30.2089691, 1668624840000),
//         GPSPoint(49.9399523, 30.2082825, 1668624850000),
//         GPSPoint(49.9399523, 30.2048492, 1668624870000),
//         GPSPoint(49.9406152, 30.2021027, 1668624890000),
//         GPSPoint(49.9408361, 30.1996994, 1668624900000),
//         GPSPoint(49.9412780, 30.1972961, 1668624915000),
//         GPSPoint(49.9408361, 30.1952362, 1668624930000),
//         GPSPoint(49.9397314, 30.1928329, 1668624950000),
//         GPSPoint(49.9390685, 30.1890564, 1668624980000),
//         GPSPoint(49.9384056, 30.1852798, 1668625010000),
//         GPSPoint(49.9364168, 30.1852798, 1668625050000),
//         GPSPoint(49.9348700, 30.1859665, 1668625080000),
//         GPSPoint(49.9313341, 30.1883698, 1668625120000),
//         GPSPoint(49.9249247, 30.1883698, 1668625180000),
//         GPSPoint(49.9205040, 30.1907730, 1668625220000),
//         GPSPoint(49.9129878, 30.1869965, 1668625280000),
//         GPSPoint(49.9074604, 30.1849365, 1668625310000),
//         GPSPoint(49.8979518, 30.1804733, 1668625360000),
//         GPSPoint(49.8884413, 30.1766968, 1668625400000),
//         GPSPoint(49.8756103, 30.1691437, 1668625480000),
//         GPSPoint(49.8596774, 30.1636505, 1668625560000),
//         GPSPoint(49.8512662, 30.1588440, 1668625610000),
//         GPSPoint(49.8393110, 30.1595306, 1668625670000),
//         GPSPoint(49.8313392, 30.1684570, 1668625720000),
//         GPSPoint(49.8229231, 30.1794434, 1668625790000),
//         GPSPoint(49.8100747, 30.1897430, 1668625870000)
//     //     GPSPoint(49.8012117, 30.1993561, 1668625930000),
//     //     GPSPoint(49.7927904, 30.2014160, 1668625960000),
//     //     GPSPoint(49.7856976, 30.2000427, 1668625990000),
//     //     GPSPoint(49.7768301, 30.2000427, 1668626040000),
//     //     GPSPoint(49.7675176, 30.2000427, 1668626090000),
//     //     GPSPoint(49.7599775, 30.2000427, 1668626130000),
//     //     GPSPoint(49.7488871, 30.1993561, 1668626180000),
//     //     GPSPoint(49.7382379, 30.1979828, 1668626230000),
//     //     GPSPoint(49.7311371, 30.1966095, 1668626270000),
//     //     GPSPoint(49.7013913, 30.1972961, 1668626400000),
//     //       GPSPoint(49.6694054, 30.1890564, 1668626500000),
//     // GPSPoint(49.6587388, 30.1904297, 1668626540000),
//     // GPSPoint(49.6511818, 30.1883698, 1668626580000),
//     // GPSPoint(49.6409558, 30.1842499, 1668626640000),
//     // GPSPoint(49.6333961, 30.1835632, 1668626670000),
//     // GPSPoint(49.6249456, 30.1808167, 1668626710000),
//     // GPSPoint(49.6187180, 30.1808167, 1668626740000),
//     // GPSPoint(49.6129345, 30.1794434, 1668626770000),
//     // GPSPoint(49.6071504, 30.1787567, 1668626800000),
//     // GPSPoint(49.5942447, 30.1753235, 1668626860000),
//     // GPSPoint(49.5817809, 30.1746368, 1668626910000),
//     // GPSPoint(49.5728761, 30.1746368, 1668626950000),
//     // GPSPoint(49.5648605, 30.1725769, 1668627000000),
//     // GPSPoint(49.5555073, 30.1725769, 1668627060000)
// )


//     // Region Inference
//     val inferredRegion = inferRegionFromGPS(points)


//     // Print the inferred region
//     println("Inferred Region: $inferredRegion")


//     val projectedTrajectory = processGPSPoints(points)
//     // Calculate segments from the projected trajectory
//     val segments = CalculateSegment(projectedTrajectory)

//     // Print the segment information (outside of any loop)
//     for (segment in segments) {
//         println("Distance: ${segment.first} meters, Time Difference: ${segment.second} milliseconds")
//     }


//     // Analysis using the projected trajectory
//     for (i in 0 until points.size - 1) {
//         val point1 = points[i]
//         val point2 = points[i + 1]

//         // Original trajectory analysis
//         val distance = haversine(point1, point2)
//         val timeDifference = point2.timestamp - point1.timestamp
//         val originalHeading = calculateHeading(point1.latitude, point1.longitude, point2.latitude, point2.longitude)

//         println("Original Trajectory Segment:")
//         println("Point 1: $point1")
//         println("Point 2: $point2")
//         println("Distance: $distance meters")
//         println("Time Difference: $timeDifference milliseconds")
//         println("Heading: $originalHeading degrees\n")
//     }

//     for (i in 0 until projectedTrajectory.size - 1) {
//         val projectedPoint1 = projectedTrajectory[i]
//         val projectedPoint2 = projectedTrajectory[i + 1]

//         // Calculate segments from the projected trajectory
//         val projectedSegments = CalculateSegment(listOf(projectedPoint1, projectedPoint2))

//         for (segment in projectedSegments) {
//             val projectedDistance = segment.first
//             val projectedTimeDifference = segment.second

//             // Skip segments with near-zero time differences
//             if (projectedTimeDifference > 1000) {
//                 val projectedHeading = calculateHeading(projectedPoint1.latitude, projectedPoint1.longitude, projectedPoint2.latitude, projectedPoint2.longitude)

//                 println("Projected Trajectory Segment:")
//                 println("Point 1: $projectedPoint1")
//                 println("Point 2: $projectedPoint2")
//                 println("Distance: $projectedDistance meters")
//                 println("Time Difference: $projectedTimeDifference milliseconds")
//                 println("Heading: $projectedHeading degrees\n")
//             }
//         }
//     }

//      val tripSummary = calculateMetrics(segments)

// // Print the trip summary
// println("Trip Summary:")
// println("Max Speed: ${tripSummary.maxSpeed} mph")
// println("Average Speed: ${tripSummary.averageSpeed} mph")
// println("Trip Duration: ${tripSummary.tripDuration} minutes")
// println("Trip Distance: ${tripSummary.tripDistance} miles")
// println("Speeding Instances: ${tripSummary.speedingInstances}")
// println("Hard Acceleration Instances: ${tripSummary.hardAccelerationInstances}")
// println("Hard Braking Instances: ${tripSummary.hardBrakingInstances}")


// }

}

