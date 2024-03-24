@Entity
data class Trip(
    @PrimaryKey(autoGenerate = true) val tripID: Int = 0,
    val userID: Int?, 
    val startTime: Date,  
    val endTime: Date?, 
    val autoDetected: Boolean 
) 
