package com.damc.driver_action.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Trip")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_id")
    val id: Int = 0,
    @ColumnInfo(name = "Date")
    val date: Date = Date()
)