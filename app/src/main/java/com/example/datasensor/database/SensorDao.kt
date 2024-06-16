package com.example.datasensor.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SensorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(sensorData: SensorData)

    @Query("SELECT * FROM SensorData")
    fun getAllSensorData(): List<SensorData>

}

