package com.damc.driver_action.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "trip_metrics", foreignKeys = [ForeignKey(entity = Trip::class,
    parentColumns = ["trip_id"],
    childColumns = ["trip_id"],
    onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = Users::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE)
])


data class TripMetrics(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_id")
    var tripId: Int = 0,
    @ColumnInfo(name = "maxSpeed")
    val maxSpeed: Double, // kph
    @ColumnInfo(name = "averageSpeed")
    var averageSpeed: Double, // kph
    @ColumnInfo(name = "tripDuration")
    val tripDuration: Double, // minutes
    @ColumnInfo(name = "tripDistance")
    val tripDistance: Double, // km
    @ColumnInfo(name = "speedingInstances")
    val speedingInstances: Int,
    @ColumnInfo(name = "hardAccelerationInstances")
    val hardAccelerationInstances: Int,
    @ColumnInfo(name = "hardBrakingInstances")
    val hardBrakingInstances: Int
)