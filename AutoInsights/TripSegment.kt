@Entity
data class TripSegment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripID: Int, 
    val distance: Double,   
    val duration: Long  // assuming duration in seconds (hence 'Long')
) 