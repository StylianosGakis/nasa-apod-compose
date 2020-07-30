package com.stylianosgakis.composenasapotd.util

sealed class LoadingStatus {
    object Loading : LoadingStatus()
    object Idle : LoadingStatus()

    fun isLoading() = this is Loading
}

sealed class NetworkState<T> {
    class Loading<T> : NetworkState<T>()
    data class Success<T>(val data: T) : NetworkState<T>()
    data class Error<T>(val message: String) : NetworkState<T>()

    companion object {
        fun <T> loading() = Loading<T>()

        fun <T> success(data: T) = Success(data)

        fun <T> error(message: String) = Error<T>(message)
    }
}

/**
 * Force exhaustive when statements
 */
val <T> T.exhaustive: T
    get() = this