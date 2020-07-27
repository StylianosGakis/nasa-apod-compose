package com.stylianosgakis.composenasapotd.ui.main

import com.stylianosgakis.composenasapotd.model.NasaDate
import com.stylianosgakis.composenasapotd.model.NasaPhoto
import com.stylianosgakis.composenasapotd.util.LoadingStatus

sealed class MainStateEvent {
    object NoEvent : MainStateEvent()
    object FetchDatabasePhotos : MainStateEvent()
    object DownloadPhotoOfToday : MainStateEvent()
    data class DownloadPhotoOfDay(val date: NasaDate) : MainStateEvent()
    data class DownloadPhotosSinceDate(val startDate: NasaDate) : MainStateEvent()
    data class DownloadPhotosBetweenDates(val startDate: NasaDate, val endDate: NasaDate) : MainStateEvent()
}

sealed class MainViewEffect {
    object NoEffect : MainViewEffect()
    data class ShowToast(val message: String) : MainViewEffect()
}

data class MainViewState(
    val loadingStatus: LoadingStatus = LoadingStatus.Idle,
    val listOfPhotos: List<NasaPhoto> = listOf()
)