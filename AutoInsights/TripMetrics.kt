
@Entity
data class TripMetrics(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripID: Int, 
    val averageSpeed: Double?,  // Can be null if not calculated
    val topSpeed: Double?,      // Can be null if not calculated
    val tripLength: Long, 
    val tripDistance: Double, 
    val speedingInstances: Int,
    val hardBrakingInstances: Int = 0,
    val rapidAccelerationInstances: Int = 0
) 
