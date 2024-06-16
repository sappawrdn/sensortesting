package com.example.datasensor.helper

import com.example.datasensor.database.SensorData

object CsvHelper {

    fun toCsv(sensorDataList: List<SensorData>): String {
        val csvBuilder = StringBuilder()
        // Adding header
        csvBuilder.append("id,Attitude Roll,Attitude Pitch,Attitude Azimuth,Gravity X,Gravity Y,Gravity Z,Rotation Rate X,Rotation Rate Y,Rotation Rate Z,User Acceleration X,User Acceleration Y,User Acceleration Z\n")
        // Adding rows
        for (data in sensorDataList) {
            csvBuilder.append(
                "${data.id},${data.attituderoll},${data.attitudepitch},${data.attitudeazimuth},${data.gravityx},${data.gravityy},${data.gravityz},${data.rotationratex},${data.rotationratey},${data.rotationratez},${data.useraccelerationx},${data.useraccelarationy},${data.useraccelerationz}\n")
        }
        return csvBuilder.toString()
    }
}