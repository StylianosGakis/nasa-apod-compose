package com.stylianosgakis.composenasapotd.repository

import com.stylianosgakis.composenasapotd.database.NasaPhotoDao
import com.stylianosgakis.composenasapotd.model.NasaDate
import com.stylianosgakis.composenasapotd.model.NasaPhoto
import com.stylianosgakis.composenasapotd.network.NasaApiService
import com.stylianosgakis.composenasapotd.util.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import timber.log.Timber

@ExperimentalCoroutinesApi
class NasaRepository(
    private val nasaApiService: NasaApiService,
    private val nasaPhotoDao: NasaPhotoDao
) {

    suspend fun downloadPhotoOfToday() = handleNasaPhotosNetworkCallAndCache(
        networkCall = { nasaApiService.getPhotoOfToday() },
        databaseInsert = { nasaPhotoDao.insertNasaPhoto(it) },
    )

    suspend fun downloadPhotoOfDay(nasaDate: NasaDate) = handleNasaPhotosNetworkCallAndCache(
        networkCall = { nasaApiService.getPhotoOfDate(nasaDate.formattedString()) },
        databaseInsert = { nasaPhotoDao.insertNasaPhoto(it) },
    )

    suspend fun downloadPhotosSinceDate(startDate: NasaDate) = handleNasaPhotosNetworkCallAndCache(
        networkCall = { nasaApiService.getPhotosSinceDate(startDate.formattedString()) },
        databaseInsert = { nasaPhotoDao.insertNasaPhotos(it) },
    )

    suspend fun downloadPhotosBetweenDates(startDate: NasaDate, endDate: NasaDate) =
        handleNasaPhotosNetworkCallAndCache(
            networkCall = {
                nasaApiService.getPhotosBetweenDates(
                    startDate.formattedString(),
                    endDate.formattedString()
                )
            },
            databaseInsert = { nasaPhotoDao.insertNasaPhotos(it) },
        )

    @Suppress("RemoveExplicitTypeArguments")
    suspend fun fetchPhotosFromDatabase() = flow<NetworkState<List<NasaPhoto>>> {
        emit(NetworkState.loading())
        val allPhotos = nasaPhotoDao.getAllPhotos()
        emit(NetworkState.success(allPhotos))
    }.flowOn(Dispatchers.IO)

    @Suppress("RemoveExplicitTypeArguments")
    private suspend fun <T> handleNasaPhotosNetworkCallAndCache(
        networkCall: suspend () -> Response<T>,
        databaseInsert: suspend (T) -> Unit,
    ) = flow<NetworkState<List<NasaPhoto>>> {
        try {
            emit(NetworkState.loading())
            val apiResponse: Response<T> = networkCall()
            val responseBody = apiResponse.body()
            //Timber.d("ApiResponse: $responseBody")
            if (apiResponse.isSuccessful) {
                if (responseBody != null) {
                    databaseInsert(responseBody)
                } else {
                    emit(NetworkState.error("There was no photo in the response"))
                }
            } else {
                emit(NetworkState.error(apiResponse.message()))
            }
        } catch (exception: Exception) {
            Timber.e("Exception when getting photo of today of type: ${exception.javaClass}")
            emit(NetworkState.error(exception.localizedMessage ?: "Error"))
        }
        emit(NetworkState.success(nasaPhotoDao.getAllPhotos()))
    }.flowOn(Dispatchers.IO)
}