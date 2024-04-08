package com.damc.driver_action.domain.models



import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity (tableName = "trip")
 data class DBTrip(
   @ColumnInfo(name = "tripID")
    @PrimaryKey(autoGenerate = true) val tripID: Int = 0,
      @ColumnInfo(name = "user_id")
    val userID: Int?,
   @ColumnInfo(name = "startTime")
    val startTime: Date,
   @ColumnInfo(name = "endTime")
    val endTime: Date?,
   @ColumnInfo(name ="autodetect")
    val autoDetected: Boolean
)
