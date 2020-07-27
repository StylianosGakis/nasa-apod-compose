package com.stylianosgakis.composenasapotd.util

import android.content.Context
import android.view.View
import android.widget.Toast

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

fun Context.showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

fun Context.showLongToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

/**
 * Force exhaustive when statements
 */
val <T> T.exhaustive: T
    get() = this

inline fun <T : View> T.invisibleIf(predicate: () -> Boolean): T {
    this.visibility = if (predicate()) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
    return this
}

inline fun <T : View> T.goneIf(predicate: () -> Boolean): T {
    this.visibility = if (predicate()) {
        View.GONE
    } else {
        View.VISIBLE
    }
    return this
}