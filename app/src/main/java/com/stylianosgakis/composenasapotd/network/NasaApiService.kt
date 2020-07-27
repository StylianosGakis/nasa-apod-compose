package com.stylianosgakis.composenasapotd.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import com.stylianosgakis.composenasapotd.model.NasaPhoto

interface NasaApiService {
    @GET(".")
    suspend fun getPhotoOfToday(): Response<List<NasaPhoto>>

    @GET(".")
    suspend fun getPhotoOfDate(
        @Query("date") date: String
    ): Response<List<NasaPhoto>>

    @GET(".")
    suspend fun getPhotosSinceDate(
        @Query("start_date") startDate: String
    ): Response<List<NasaPhoto>>

    @GET(".")
    suspend fun getPhotosBetweenDates(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Response<List<NasaPhoto>>
}