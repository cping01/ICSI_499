@Database(entities = [TripSegment::class, Trip::class, TripMetrics::class], version = 1) 
abstract class AppDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile 
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) { 
            override fun migrate(database: SupportSQLiteDatabase) {
                // Execute your table creation queries here
                database.execSQL(CREATE TABLE trip_segments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tripID INTEGER NOT NULL, 
                    distance DOUBLE NOT NULL,   
                    duration INTEGER NOT NULL, 
                    FOREIGN KEY (tripID) REFERENCES trips(tripID)
                );) 
                database.execSQL(CREATE TABLE trips (
                    tripID INTEGER PRIMARY KEY AUTOINCREMENT,
                    userID INTEGER, -- If you associate trips with users
                    startTime DATETIME NOT NULL,  
                    endTime DATETIME, -- Can be NULL initially
                    autoDetected BOOLEAN, -- Optional - if the trip was detected automatically
                ); ) 

                database.execSQL(CREATE TABLE trip_metrics (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tripID INTEGER NOT NULL, 
                    averageSpeed DOUBLE,   
                    topSpeed DOUBLE,
                    tripLength INTEGER,  -- Total trip duration in seconds
                    tripDistance DOUBLE, 
                    speedingInstances INTEGER,
                    hardBrakingInstances INTEGER,
                    rapidAccelerationInstances INTEGER,  -- Add this new column
                    FOREIGN KEY (tripID) REFERENCES trips(tripID)
                );) 
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder( 
                    context.applicationContext,
                    AppDatabase::class.java,
                    "AutoInsightsDB" 
                )
                // Optionally add migrations or pre-population here
                .build() 
                INSTANCE = instance
                instance
            }
        }
    }
}