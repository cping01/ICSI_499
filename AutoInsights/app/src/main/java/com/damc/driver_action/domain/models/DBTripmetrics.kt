package com.damc.driver_action.domain.models
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
    (tableName = "trip_metrics")
data class DBTripmetrics (
    @ColumnInfo(name = "metric_id")
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "tripID")
    val tripID: Int,
    @ColumnInfo(name = "averageSpeed")
    val averageSpeed: Double?,  // Can be null if not calculated
    @ColumnInfo(name = "topSpeed")
    val topSpeed: Double?,      // Can be null if not calculated
    @ColumnInfo(name = "tripLength")
    val tripLength: Long,
    @ColumnInfo(name = "tripDistance")
    val tripDistance: Double,
    @ColumnInfo(name = "speedingInstances")
    val speedingInstances: Int,
    @ColumnInfo(name = "hardBrakingInstances")
    val hardBrakingInstances: Int = 0,
    @ColumnInfo(name = "rapidAccelerationInstances")
    val rapidAccelerationInstances: Int = 0
)



