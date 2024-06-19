package com.example.datasensor.Api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CsvApiService {
    @Multipart
    @POST("api/v1/storage/upload-csv/")
    suspend fun uploadFile(@Part file: MultipartBody.Part): CsvResponse
}