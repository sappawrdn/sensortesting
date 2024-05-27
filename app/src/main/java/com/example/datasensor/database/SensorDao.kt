package com.example.datasensor.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface SensorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(sensorData: SensorData)

}