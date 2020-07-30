package com.stylianosgakis.composenasapotd.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stylianosgakis.composenasapotd.model.NasaDate
import com.stylianosgakis.composenasapotd.model.NasaPhoto
import com.stylianosgakis.composenasapotd.repository.NasaRepository
import com.stylianosgakis.composenasapotd.util.LoadingStatus
import com.stylianosgakis.composenasapotd.util.NetworkState
import com.stylianosgakis.composenasapotd.util.exhaustive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class MainViewModel(
    private val nasaRepository: NasaRepository,
) : ViewModel() {
    // region State variables
    private val mutableMainStateEvent = MutableStateFlow<MainStateEvent>(MainStateEvent.NoEvent)
    private val mainStateEvent: StateFlow<MainStateEvent> = mutableMainStateEvent

    private val mutableMainViewState = MutableStateFlow(MainViewState())
    val mainViewState: StateFlow<MainViewState> = mutableMainViewState

    private val mutableMainViewEffect = MutableStateFlow<MainViewEffect>(MainViewEffect.NoEffect)
    val mainViewEffect: StateFlow<MainViewEffect> = mutableMainViewEffect

    init {
        mainStateEvent.onEach {
            Timber.d("Processing event: $it")
            processEvent(it)
        }.launchIn(viewModelScope)
    }
    // endregion

    // region Public methods
    fun startEvent(event: MainStateEvent) {
        Timber.d("Starting state event: $event")
        mutableMainStateEvent.value = event
    }
    // endregion

    // region Event handling
    private suspend fun processEvent(stateEvent: MainStateEvent) =
        viewModelScope.launch(Dispatchers.Default) {
            when (stateEvent) {
                MainStateEvent.NoEvent -> {
                    return@launch
                }
                MainStateEvent.FetchDatabasePhotos -> {
                    fetchPhotosFromDatabase()
                }
                MainStateEvent.DownloadPhotoOfToday -> {
                    downloadPhotoOfToday()
                }
                is MainStateEvent.DownloadPhotoOfDay -> {
                    downloadPhotoOfDay(stateEvent.date)
                }
                is MainStateEvent.DownloadPhotosSinceDate -> {
                    Timber.d("Is getPhotosSinceDate")
                    downloadPhotoSinceDate(stateEvent.startDate)
                }
                is MainStateEvent.DownloadPhotosBetweenDates -> {
                    downloadPhotoOfDateRange(stateEvent.startDate, stateEvent.endDate)
                }
            }.exhaustive
            startEvent(MainStateEvent.NoEvent)
        }

    private suspend fun fetchPhotosFromDatabase() {
        nasaRepository.fetchPhotosFromDatabase().collect { networkState ->
            handleRepositoryResponse(networkState) { nasaPhotos ->
                updateListOfNasaPhotos(nasaPhotos)
            }
        }
    }

    private suspend fun downloadPhotoOfToday() {
        nasaRepository.downloadPhotoOfToday().collect { networkState ->
            handleRepositoryResponse(networkState) { nasaPhotos ->
                updateListOfNasaPhotos(nasaPhotos)
            }
        }
    }

    private suspend fun downloadPhotoOfDay(nasaDate: NasaDate) {
        nasaRepository.downloadPhotoOfDay(nasaDate).collect { networkState ->
            handleRepositoryResponse(networkState) { nasaPhotos ->
                updateListOfNasaPhotos(nasaPhotos)
            }
        }
    }

    private suspend fun downloadPhotoSinceDate(startDate: NasaDate) {
        Timber.d("getPhotosSinceDate with date: $startDate")
        nasaRepository.downloadPhotosSinceDate(startDate).collect { networkState ->
            handleRepositoryResponse(networkState) { nasaPhotos ->
                updateListOfNasaPhotos(nasaPhotos)
            }
        }
    }

    private suspend fun downloadPhotoOfDateRange(
        startDate: NasaDate,
        endDate: NasaDate,
    ) {
        nasaRepository.downloadPhotosBetweenDates(startDate, endDate)
            .collect { networkState ->
                handleRepositoryResponse(networkState) { nasaPhotos ->
                    updateListOfNasaPhotos(nasaPhotos)
                }
            }
    }
    // endregion

    // region ViewState updating
    private fun updateLoadingState(loading: Boolean) {
        val loadingStatus = if (loading) LoadingStatus.Loading else LoadingStatus.Idle
        mutableMainViewState.value = mutableMainViewState.value.copy(loadingStatus = loadingStatus)
    }

    private fun loadingError(errorMessage: String) =
        updateViewEffect(MainViewEffect.ShowToast(errorMessage))

    private fun updateViewEffect(viewEffect: MainViewEffect) {
        mutableMainViewEffect.value = viewEffect
    }

    private fun updateListOfNasaPhotos(nasaPhoto: NasaPhoto) =
        updateListOfNasaPhotos(listOf(nasaPhoto))

    private fun updateListOfNasaPhotos(nasaPhotos: List<NasaPhoto>) {
        val currentState = mutableMainViewState.value
        val currentPhotos = currentState.listOfPhotos
        mutableMainViewState.value = currentState.copy(
            listOfPhotos = (currentPhotos union nasaPhotos).filter { it.isImage() }.toList()
        )
    }

    private fun <T> handleRepositoryResponse(
        networkState: NetworkState<T>,
        successMethod: (T) -> Unit,
    ) {
        Timber.d("HandleRepositoryResponse: $networkState")
        updateLoadingState(networkState is NetworkState.Loading)
        when (networkState) {
            is NetworkState.Success -> successMethod(networkState.data)
            is NetworkState.Error -> loadingError(networkState.message)
        }
    }
    // endregion
}