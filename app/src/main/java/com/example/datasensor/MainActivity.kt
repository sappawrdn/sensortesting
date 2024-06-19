package com.example.datasensor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.datasensor.Api.CsvApiConfig
import com.example.datasensor.database.SensorData
import com.example.datasensor.database.SensorRoomDatabase
import com.example.datasensor.databinding.ActivityMainBinding
import com.example.datasensor.helper.CsvHelper
import com.example.datasensor.helper.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
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

//    private val chooseDir = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            result.data?.data?.also { uri ->
//                exportDataToCsv(uri)
//            }
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensordatabase = SensorRoomDatabase.getDatabase(this)

        registerSensors()
        startSavingDataPeriodically()
        startCheckingData()
        checkSensorAvailability(Sensor.TYPE_GAME_ROTATION_VECTOR)
        checkSensorAvailability(Sensor.TYPE_GRAVITY)
        checkSensorAvailability(Sensor.TYPE_GYROSCOPE)
        checkSensorAvailability(Sensor.TYPE_LINEAR_ACCELERATION)

//        binding.exportButton.setOnClickListener {
//            chooseStorageDirectory()
//        }
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
                System.arraycopy(event.values, 0, attitudeReading, 0, attitudeReading.size)
            }else if (event.sensor.type == Sensor.TYPE_GRAVITY){
                System.arraycopy(event.values, 0, gravityReading, 0, gravityReading.size)
            }else if (event.sensor.type == Sensor.TYPE_GYROSCOPE){
                System.arraycopy(event.values, 0, gyroscopeReading, 0, gyroscopeReading.size)
            }else if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION){
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
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

//    private fun chooseStorageDirectory() {
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
//            flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
//                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
//                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
//        }
//        chooseDir.launch(intent)
//    }

//    private fun exportDataToCsv(uri: Uri) {
//        lifecycleScope.launch{
//            withContext(Dispatchers.IO) {
//                val sensorDataList: List<SensorData> = sensordatabase.sensorDao().getAllSensorData()
//                val csvData = CsvHelper.toCsv(sensorDataList)
//                val contentResolver = applicationContext.contentResolver
//                val documentFile = DocumentFile.fromTreeUri(applicationContext, uri)
//                val file = documentFile?.createFile("text/csv", "sensor_data.csv")
//                try {
//                    file?.let {
//                        contentResolver.openOutputStream(it.uri)?.use { outputStream ->
//                            outputStream.write(csvData.toByteArray())
//                            Log.d("FileSave", "File saved to: ${it.uri}")
//                        }
//
//                        val tempFile = File(applicationContext.cacheDir, "sensor_data.csv")
//                        tempFile.writeText(csvData)
//                        Log.d("ExportData", "Temporary file created at: ${tempFile.absolutePath}")
//                        uploadCsvToApi(tempFile)
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    Log.d("FileSave", "Failed to save file")
//                }
//            }
//        }
//    }
//
//    private fun uploadCsvToApi(file: File) {
//        lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
//                try {
//                    val requestFile = file.asRequestBody("text/csv".toMediaTypeOrNull())
//                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
//                    val response = CsvApiConfig.getApiService().uploadFile(body)
//                    Log.d("UploadCsvToApi", "Response: ${response.message}")
//                    withContext(Dispatchers.Main) {
//                        withContext(Dispatchers.Main) {
//                            Toast.makeText(this@MainActivity, response.message, Toast.LENGTH_LONG)
//                                .show()
//                        }
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    Log.d("UploadCsvToApi", "File upload failed: ${e.message}")
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@MainActivity, "File upload failed", Toast.LENGTH_LONG)
//                            .show()
//                    }
//                }
//            }
//        }
//    }
}