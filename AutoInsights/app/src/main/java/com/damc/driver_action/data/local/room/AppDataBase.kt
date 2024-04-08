package com.damc.driver_action.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Users

@Database(
    entities = [Users::class, ActionData::class, DBTripsegments::class, DBTrip::class, DBTripmetrics::class],
    version = 3,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun OnDataBaseActions(): OnDataBaseActions
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
             override fun migrate(db: SupportSQLiteDatabase) {
                // Execute  table creation queries 
                 db.execSQL(
                         "CREATE TABLE trip_segments( id INTEGER PRIMARY KEY AUTOINCREMENT, tripID INTEGER NOT NULL, distance DOUBLE NOT NULL, duration INTEGER NOT NULL, FOREIGN KEY (tripID) REFERENCES trips(tripID));"
                         )

                db.execSQL("CREATE TABLE trips (tripID INTEGER PRIMARY KEY AUTOINCREMENT, userID INTEGER, startTime DATETIME NOT NULL, endTime DATETIME,  autoDetected BOOLEAN);" )

                        db.execSQL("CREATE TABLE trip_metrics ( id INTEGER PRIMARY KEY AUTOINCREMENT, tripID INTEGER NOT NULL, averageSpeed DOUBLE, topSpeed DOUBLE, tripLength INTEGER, tripDistance DOUBLE, speedingInstances INTEGER, hardBrakingInstances INTEGER, rapidAccelerationInstances INTEGER,   FOREIGN KEY (tripID) REFERENCES trips(tripID));")

            }
        }

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java, //  database class here
                    "AutoInsightsDB"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
