package com.damc.driver_action.data.local.room
import androidx.room.TypeConverters
import androidx.room.Database
import androidx.room.RoomDatabase
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.Users

@Database(
    entities = [Users::class, ActionData::class, TripMetrics::class, Trip::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {
    abstract fun OnDataBaseActions(): OnDataBaseActions
}