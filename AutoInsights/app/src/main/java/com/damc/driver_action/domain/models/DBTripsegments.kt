package com.damc.driver_action.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "trip_segments")
data class DBTripsegments (
    @ColumnInfo(name = "segmentID")
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "tripID")
        val tripID: Int,
    @ColumnInfo(name = "distance")
        val distance: Double,
    @ColumnInfo(name = "duration")
        val duration: Long  // assuming duration in seconds (hence 'Long')
    )