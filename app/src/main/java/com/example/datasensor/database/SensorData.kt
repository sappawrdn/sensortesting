package com.example.datasensor.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity
data class SensorData(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "Attitude Roll")
    var attituderoll: String? = null,

    @ColumnInfo(name = "Attitude Pitch")
    var attitudepitch: String? = null,

    @ColumnInfo(name = "Attitude Azimuth")
    var attitudeazimuth: String? = null,

    @ColumnInfo(name = "Gravity X")
    var gravityx: String? = null,

    @ColumnInfo(name = "Gravity Y")
    var gravityy: String? = null,

    @ColumnInfo(name = "Gravity Z")
    var gravityz:String? = null,

    @ColumnInfo(name = "Rotation Rate X")
    var rotationratex: String? = null,

    @ColumnInfo(name = "Rotation Rate Y")
    var rotationratey: String? = null,

    @ColumnInfo(name = "Rotation Rate Z")
    var rotationratez: String? = null,

    @ColumnInfo(name = "User Acceleration X")
    var useraccelerationx: String? = null,

    @ColumnInfo(name = "User Acceleration Z")
    var useraccelerationz: String? = null,

    @ColumnInfo(name = "User Acceleration Y")
    var useraccelarationy: String? = null,

    )