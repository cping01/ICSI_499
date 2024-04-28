@Dao
interface TripDao {

    @Insert
    suspend fun insertTripSegment(segment: TripSegment) 

    @Insert
    suspend fun insertTripMetrics(metrics: TripMetrics) 

    // Add other queries as needed 

    @Transaction // Important for data consistency  
    suspend fun calculateAndUpdateTotalDistance(tripID: Int) {
        val totalDistance = calculateTotalDistance(tripID)
        updateTripDistance(tripID, totalDistance) 
    }

    @Query("SELECT SUM(distance) FROM trip_segments WHERE tripID = :tripID")
    suspend fun calculateTotalDistance(tripID: Int): Double 

    @Query("UPDATE trip_metrics SET tripDistance = :distance WHERE tripID = :tripID")
    suspend fun updateTripDistance(tripID: Int, distance: Double)

    @Transaction 
    suspend fun calculateAndUpdateTripLength(tripID: Int) {
        val tripLength = calculateTripLength(tripID)
        updateTripLength(tripID, tripLength) 
    }

    @Query("SELECT SUM(duration) / 60 FROM trip_segments WHERE tripID = :tripID") // Calculate minutes
    suspend fun calculateTripLength(tripID: Int): Long

    @Query("UPDATE trip_metrics SET tripLength = :length WHERE tripID = :tripID")
    suspend fun updateTripLength(tripID: Int, length: Long)


    @Transaction
    suspend fun calculateAndUpdateTopSpeed(tripID: Int) {
        val topSpeed = calculateTopSpeed(tripID)
        updateTopSpeed(tripID, topSpeed) 
    }

    @Query("SELECT MAX(distance * 2.23694 / duration) FROM trip_segments WHERE tripID = :tripID") // Calculation in MPH
    suspend fun calculateTopSpeed(tripID: Int): Double?

    @Query("UPDATE trip_metrics SET topSpeed = :speed WHERE tripID = :tripID")
    suspend fun updateTopSpeed(tripID: Int, speed: Double?) 

    @Transaction 
    suspend fun calculateAndUpdateAverageSpeed(tripID: Int) {
        val averageSpeed = calculateAverageSpeed(tripID)
        updateAverageSpeed(tripID, averageSpeed) 
    }

    @Query("SELECT SUM(distance * 2.23694 / duration) / SUM(duration)  FROM trip_segments WHERE tripID = :tripID") // Calculation in MPH
    suspend fun calculateAverageSpeed(tripID: Int): Double?

    @Query("UPDATE trip_metrics SET averageSpeed = :speed WHERE tripID = :tripID")
    suspend fun updateAverageSpeed(tripID: Int, speed: Double?) 

    @Query("UPDATE trip_metrics SET rapidAccelerationInstances = :count WHERE tripID = :tripID")
    suspend fun updateRapidAccelerationInstances(tripID: Int, count: Int) 

    @Query("UPDATE trip_metrics SET hardBrakingInstances = :count WHERE tripID = :tripID")
    suspend fun updateHardBrakingInstances(tripID: Int, count: Int) 

}




