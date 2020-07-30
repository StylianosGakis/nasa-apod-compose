package com.stylianosgakis.composenasapotd

import android.app.Application
import com.stylianosgakis.composenasapotd.di.appModule
import com.stylianosgakis.composenasapotd.di.mainModule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        startKoin {
            androidContext(this@MyApp)
            modules(
                listOf(
                    appModule,
                    mainModule
                )
            )
        }
    }
}