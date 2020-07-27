package com.stylianosgakis.composenasapotd.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import com.stylianosgakis.composenasapotd.database.NasaPhotoDao
import com.stylianosgakis.composenasapotd.model.NasaDate
import com.stylianosgakis.composenasapotd.model.NasaPhoto
import com.stylianosgakis.composenasapotd.network.NasaApiService
import com.stylianosgakis.composenasapotd.util.NetworkState
import timber.log.Timber

@ExperimentalCoroutinesApi
class NasaRepository(
    private val nasaApiService: NasaApiService,
    private val nasaPhotoDao: NasaPhotoDao
) {

    suspend fun downloadPhotoOfToday() = makeApiCall {
        nasaApiService.getPhotoOfToday()
    }

    suspend fun downloadPhotoOfDay(nasaDate: NasaDate) = makeApiCall {
        nasaApiService.getPhotoOfDate(nasaDate.formattedString())
    }

    suspend fun downloadPhotosSinceDate(startDate: NasaDate) = makeApiCall {
        Timber.d("Repository: called getPhotosSince")
        nasaApiService.getPhotosSinceDate(startDate.formattedString())
    }

    suspend fun downloadPhotosBetweenDates(startDate: NasaDate, endDate: NasaDate) = makeApiCall {
        nasaApiService.getPhotosBetweenDates(startDate.formattedString(), endDate.formattedString())
    }

    @Suppress("RemoveExplicitTypeArguments")
    suspend fun fetchPhotosFromDatabase() = flow<NetworkState<List<NasaPhoto>>> {
        emit(NetworkState.loading())
        val allPhotos = nasaPhotoDao.getAllPhotos()
        emit(NetworkState.success(allPhotos))
    }.flowOn(Dispatchers.IO)

    @Suppress("RemoveExplicitTypeArguments")
    suspend fun makeApiCall(
        apiMethod: suspend () -> Response<List<NasaPhoto>>
    ) = flow<NetworkState<List<NasaPhoto>>> {
        Timber.d("Making api call")
        try {
            emit(NetworkState.loading())
            val apiResponse: Response<List<NasaPhoto>> = apiMethod()
            val responseBody = apiResponse.body()
            Timber.d("ApiResponse: ${responseBody}")
            if (apiResponse.isSuccessful) {
                if (responseBody != null) {
                    nasaPhotoDao.insertNasaPhotos(responseBody)
                } else {
                    Timber.e("Response was successful, but photo was null!")
                    Timber.e("Response: $apiResponse\nPhoto: $responseBody")
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