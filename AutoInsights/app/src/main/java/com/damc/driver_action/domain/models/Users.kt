package com.damc.driver_action.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class Users(
    @ColumnInfo(name = "username")
    var username: String,
    @ColumnInfo(name = "password")
    var password: String,
    @ColumnInfo(name = "biometrics_enabled")
    var biometricsEnabled: Boolean,
    @ColumnInfo(name = "auoto_tracking_enabled")
    var autoTrackingEnabled: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    var userId: Int = 0
}
