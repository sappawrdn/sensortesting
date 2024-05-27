package com.example.datasensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.datasensor.database.SensorData
import com.example.datasensor.database.SensorRoomDatabase
import com.example.datasensor.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var sensordatabase: SensorRoomDatabase
    private var previousSensorData: SensorData? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensordatabase = SensorRoomDatabase.getDatabase(this)

        registerSensors()


    }

    private fun registerSensors() {
        val sensors = listOf(
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_ACCELEROMETER
        )

        sensors.forEach { sensorType ->
            sensorManager.getDefaultSensor(sensorType)?.also { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerSensors()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { e ->
            Log.d("SensorValue", "Received sensor values: ${e.values.contentToString()}")
            var sensorData = SensorData()

            previousSensorData?.let {
                sensorData = it.copy()
            }

            when (e.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    sensorData.attituderoll =
                        e.values.getOrNull(0)?.toString() ?: sensorData.attituderoll
                    sensorData.attitudepitch =
                        e.values.getOrNull(1)?.toString() ?: sensorData.attitudepitch
                    sensorData.attitudeazimuth =
                        e.values.getOrNull(2)?.toString() ?: sensorData.attitudeazimuth
                }

                Sensor.TYPE_GRAVITY -> {
                    sensorData.gravityx = e.values.getOrNull(0)?.toString() ?: sensorData.gravityx
                    sensorData.gravityy = e.values.getOrNull(1)?.toString() ?: sensorData.gravityy
                    sensorData.gravityz = e.values.getOrNull(2)?.toString() ?: sensorData.gravityz
                }

                Sensor.TYPE_GYROSCOPE -> {
                    sensorData.rotationratex =
                        e.values.getOrNull(0)?.toString() ?: sensorData.rotationratex
                    sensorData.rotationratey =
                        e.values.getOrNull(1)?.toString() ?: sensorData.rotationratey
                    sensorData.rotationratez =
                        e.values.getOrNull(2)?.toString() ?: sensorData.rotationratez
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    sensorData.useraccelerationx =
                        e.values.getOrNull(0)?.toString() ?: sensorData.useraccelerationx
                    sensorData.useraccelarationy =
                        e.values.getOrNull(1)?.toString() ?: sensorData.useraccelarationy
                    sensorData.useraccelerationz =
                        e.values.getOrNull(2)?.toString() ?: sensorData.useraccelerationz
                }
            }
            Log.d("SensorData", "Prepared SensorData object: $sensorData")
            lifecycleScope.launch(Dispatchers.IO) {
                sensordatabase.sensorDao().insert(sensorData)
            }
            updateUI(sensorData)
        }

    }

    private fun updateUI(sensorData: SensorData) {
        runOnUiThread {
            binding.valueAttituderoll.text = sensorData.attituderoll
            binding.valueAttitudepitch.text = sensorData.attitudepitch
            binding.valueAttitudeazimuth.text = sensorData.attitudeazimuth
            binding.valueGravityx.text = sensorData.gravityx
            binding.valueGravityy.text = sensorData.gravityy
            binding.valueGravityz.text = sensorData.gravityz
            binding.valueRotaionratex.text = sensorData.rotationratex
            binding.valueRotationratey.text = sensorData.rotationratey
            binding.valueRotationratez.text = sensorData.rotationratez
            binding.valueUseraccx.text = sensorData.useraccelerationx
            binding.valueUseraccy.text = sensorData.useraccelarationy
            binding.valueUseraccz.text = sensorData.useraccelerationz
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("sensor Accuracy", "Sensor Accuracy changed: $accuracy")
    }
}