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
import java.math.BigDecimal
import kotlin.math.abs

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
        checkSensorAvailability(Sensor.TYPE_ROTATION_VECTOR)
        checkSensorAvailability(Sensor.TYPE_GRAVITY)
        checkSensorAvailability(Sensor.TYPE_GYROSCOPE)
        checkSensorAvailability(Sensor.TYPE_LINEAR_ACCELERATION)
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
            attitudepitch = convertToPlainString(attitudeReading[0])
            attituderoll = convertToPlainString(attitudeReading[1])
            attitudeazimuth = convertToPlainString(attitudeReading[2])
            gravityx = convertToPlainString(gravityReading[0])
            gravityy = convertToPlainString(gravityReading[1])
            gravityz = convertToPlainString(gravityReading[2])
            rotationratex = convertToPlainString(gyroscopeReading[0])
            rotationratey = convertToPlainString(gyroscopeReading[1])
            rotationratez = convertToPlainString(gyroscopeReading[2])
            useraccelerationx = convertToPlainString(accelerometerReading[0])
            useraccelarationy = convertToPlainString(accelerometerReading[1])
            useraccelerationz = convertToPlainString(accelerometerReading[2])
        }

        Log.d("SensorData", "Prepared SensorData object: $sensorData")
        lifecycleScope.launch(Dispatchers.IO) {
            sensordatabase.sensorDao().insert(sensorData)
            dataCounter++
        }
        updateUI(sensorData)
    }

    private fun convertToPlainString(value: Float): String {
        return BigDecimal(value.toString()).toPlainString()
    }

    private fun registerSensors() {
        val sensors = listOf(
            Sensor.TYPE_GAME_ROTATION_VECTOR,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_LINEAR_ACCELERATION
        )

        sensors.forEach { sensorType ->
            sensorManager.getDefaultSensor(sensorType)?.also { sensor ->
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }
    }

    private fun checkSensorAvailability(sensorType: Int) {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor != null) {
            Log.d("SensorAvailability", "Sensor with type $sensorType is available on this device.")
        } else {
            Log.d("SensorAvailability", "Sensor with type $sensorType is not available on this device.")
        }
    }

    fun getEulerAngles(rotationVector: FloatArray?): FloatArray? {
        if (rotationVector == null || rotationVector.size < 4) {
            return null // Handle invalid input
        }

        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector)

        val w = rotationMatrix[0]
        val x = rotationMatrix[3]
        val y = rotationMatrix[6]
        val z = rotationMatrix[1]

        val sinB = 2.0f * (w * z + x * y)
        val cosB = 1.0f - 2.0f * (x * x + y * y)

        val pitch: Float
        val roll: Float
        val yaw: Float


            // Standard calculation for most orientations
            pitch = Math.atan2(y.toDouble(), cosB.toDouble()).toFloat()
            roll = Math.atan2(sinB.toDouble(), x.toDouble()).toFloat()
            yaw = Math.atan2(z.toDouble(), w.toDouble()).toFloat()

        return floatArrayOf(pitch, roll, yaw)
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

            if (event.sensor.type == Sensor.TYPE_GAME_ROTATION_VECTOR){
                /*System.arraycopy(event.values, 0, attitudeReading, 0, attitudeReading.size)*/
                val eulerAngles = getEulerAngles(event.values)
                if (eulerAngles != null) {
                    attitudeReading[0] = eulerAngles[0] // Check for null on specific index
                    attitudeReading[1] = eulerAngles[1]
                    attitudeReading[2] = eulerAngles[2]
                }

            }else if (event.sensor.type == Sensor.TYPE_GRAVITY){
                System.arraycopy(event.values, 0, gravityReading, 0, gravityReading.size)
            }else if (event.sensor.type == Sensor.TYPE_GYROSCOPE){
                System.arraycopy(event.values, 0, gyroscopeReading, 0, gyroscopeReading.size)
            }else if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
                accelerometerReading[0] = event.values[0] / -9.81f
                accelerometerReading[1] = event.values[1] / -9.81f
                accelerometerReading[2] = event.values[2] / -9.81f
            }

    }

    private fun updateUI(sensorData: SensorData) {
        runOnUiThread {
            binding.valueAttituderoll.text = sensorData.attituderoll.toString()
            binding.valueAttitudepitch.text = sensorData.attitudepitch.toString()
            binding.valueAttitudeazimuth.text = sensorData.attitudeazimuth.toString()
            binding.valueGravityx.text = sensorData.gravityx.toString()
            binding.valueGravityy.text = sensorData.gravityy.toString()
            binding.valueGravityz.text = sensorData.gravityz.toString()
            binding.valueRotaionratex.text = sensorData.rotationratex.toString()
            binding.valueRotationratey.text = sensorData.rotationratey.toString()
            binding.valueRotationratez.text = sensorData.rotationratez.toString()
            binding.valueUseraccx.text = sensorData.useraccelerationx.toString()
            binding.valueUseraccy.text = sensorData.useraccelarationy.toString()
            binding.valueUseraccz.text = sensorData.useraccelerationz.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d("sensor Accuracy", "Sensor Accuracy changed: $accuracy")
    }
}