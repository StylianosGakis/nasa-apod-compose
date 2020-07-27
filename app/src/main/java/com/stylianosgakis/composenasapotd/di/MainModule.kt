package com.stylianosgakis.composenasapotd.di

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.stylianosgakis.composenasapotd.repository.NasaRepository
import com.stylianosgakis.composenasapotd.ui.main.MainViewModel

@InternalCoroutinesApi
@ExperimentalCoroutinesApi

@Suppress("RemoveExplicitTypeArguments")
val mainModule = module {
    viewModel<MainViewModel> { MainViewModel(get()) }
    single<NasaRepository> { NasaRepository(get(), get()) }
}