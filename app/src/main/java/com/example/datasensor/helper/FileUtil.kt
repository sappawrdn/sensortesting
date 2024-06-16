package com.example.datasensor.helper

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.io.IOException

object FileUtil {
    @Throws(IOException::class)
    fun saveCsvToInternalStorage(context: Context, csvData: String): String {
        val fileName = "sensor_data.csv"
        val file = File(context.filesDir, fileName)
        val fileWriter = FileWriter(file)
        fileWriter.write(csvData)
        fileWriter.close()
        return file.absolutePath
    }
}