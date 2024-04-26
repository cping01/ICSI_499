package com.damc.driver_action.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Trip", foreignKeys = [ForeignKey(entity = Users::class,
parentColumns = ["user_id"],
childColumns = ["user_id"],
onDelete = ForeignKey.CASCADE)])
data class Trip(
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trip_id")
    val id: Int = 0,
    @ColumnInfo(name = "Date")
    val date: Date = Date()
)