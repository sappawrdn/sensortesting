package com.example.datasensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private val attitudeReading = FloatArray(3)
    private val gravityReading = FloatArray(3)
    private val gyroscopeReading = FloatArray(3)
    private val accelerometerReading = FloatArray(3)

    private val handler = Handler(Looper.getMainLooper())
    private val saveInterval = 20L

    private val checkHandler = Handler(Looper.getMainLooper())
    private val checkInterval = 1000L // 1 detik

    // Counter untuk jumlah data yang disimpan
    private var dataCounter = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensordatabase = SensorRoomDatabase.getDatabase(this)

        registerSensors()
        startSavingDataPeriodically()
        startCheckingData()


    }

    private fun startSavingDataPeriodically() {
        val saveDataRunnable = object : Runnable {
            override fun run() {
                saveSensorData()
                handler.postDelayed(this, saveInterval)
            }
        }
        handler.post(saveDataRunnable)
    }

    private fun saveSensorData() {
        val sensorData = SensorData().apply {
            attituderoll = attitudeReading[0].toString()
            attitudepitch = attitudeReading[1].toString()
            attitudeazimuth = attitudeReading[2].toString()
            gravityx = gravityReading[0].toString()
            gravityy = gravityReading[1].toString()
            gravityz = gravityReading[2].toString()
            rotationratex = gyroscopeReading[0].toString()
            rotationratey = gyroscopeReading[1].toString()
            rotationratez = gyroscopeReading[2].toString()
            useraccelerationx = accelerometerReading[0].toString()
            useraccelarationy = accelerometerReading[1].toString()
            useraccelerationz = accelerometerReading[2].toString()
        }

        Log.d("SensorData", "Prepared SensorData object: $sensorData")
        lifecycleScope.launch(Dispatchers.IO) {
            sensordatabase.sensorDao().insert(sensorData)
            dataCounter++
        }
        updateUI(sensorData)
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

    private fun startCheckingData() {
        val checkDataRunnable = object : Runnable {
            override fun run() {
                Log.d("DataCounter", "Number of rows inserted in the last second: $dataCounter")
                dataCounter = 0 // Reset counter
                checkHandler.postDelayed(this, checkInterval)
            }
        }
        checkHandler.post(checkDataRunnable)
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

            if(event == null){
                return
            }

            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR){
                System.arraycopy(event.values, 0, attitudeReading, 0, attitudeReading.size)
            }else if (event.sensor.type == Sensor.TYPE_GRAVITY){
                System.arraycopy(event.values, 0, gravityReading, 0, gravityReading.size)
            }else if (event.sensor.type == Sensor.TYPE_GYROSCOPE){
                System.arraycopy(event.values, 0, gyroscopeReading, 0, gyroscopeReading.size)
            }else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER){
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
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