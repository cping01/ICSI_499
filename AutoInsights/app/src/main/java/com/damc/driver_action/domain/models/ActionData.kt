package com.damc.driver_action.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "action_data")
data class ActionData(
    @ColumnInfo("user_id")
    var userId: Int,
    @ColumnInfo("date")
    var date: String,
    @ColumnInfo("hard_stop_count")
    var hardStopCount: Int,
    @ColumnInfo("medium_stop_count")
    var mediumStopCount: Int,
    @ColumnInfo("good_stop_count")
    var goodStopCount: Int,
    @ColumnInfo("fast_acceleration_count")
    var fastAcceleration: Int,
    @ColumnInfo("medium_acceleration")
    var mediumAcceleration: Int,
    @ColumnInfo("good_acceleration")
    var goodAcceleration: Int,
    @ColumnInfo("highest_speed")
    var highestSpeed: Float
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "action_id")
    var actionId: Int = 0
}
