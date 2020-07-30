package com.stylianosgakis.composenasapotd.di

import android.content.Context
import androidx.room.Room
import coil.ImageLoader
import com.stylianosgakis.composenasapotd.BuildConfig
import com.stylianosgakis.composenasapotd.database.AppDatabase
import com.stylianosgakis.composenasapotd.database.NasaPhotoDao
import com.stylianosgakis.composenasapotd.network.NasaApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

@InternalCoroutinesApi
@ExperimentalCoroutinesApi
@Suppress("RemoveExplicitTypeArguments")
val appModule = module {
    factory<HttpLoggingInterceptor> { provideLoggingInterceptor() }
    factory<Interceptor> { provideNasaAuthInterceptor(BuildConfig.API_KEY) }
    factory<OkHttpClient> { provideOkHttpClient(get(), get()) }
    single<Retrofit> { provideRetrofit(BuildConfig.BASE_URL, get()) }
    single<NasaApiService> { provideNasaApi(get()) }

    single<ImageLoader> { provideCoilImageLoader(get()) }

    single<AppDatabase> { provideRoomDatabase(get()) }
    single<NasaPhotoDao> { provideNasaPhotoDao(get()) }
}

fun provideCoilImageLoader(applicationContext: Context): ImageLoader {
    return ImageLoader.Builder(applicationContext)
        //.placeholder(R.drawable.loading_animation)
        .build()
}

fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }
}

fun provideNasaAuthInterceptor(apiKey: String): Interceptor {
    return object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            val newUrl = originalUrl
                .newBuilder()
                .addQueryParameter("api_key", apiKey)
                .build()
            val newRequest = originalRequest
                .newBuilder()
                .url(newUrl)
                .build()
            return chain.proceed(newRequest)
        }
    }
}

fun provideOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor, nasaAuthInterceptor: Interceptor
): OkHttpClient {
    return OkHttpClient()
        .newBuilder()
        .addInterceptor(nasaAuthInterceptor)
        //.addInterceptor(loggingInterceptor)
        .build()
}

fun provideRetrofit(baseUrl: String, httpClient: OkHttpClient): Retrofit {
    return Retrofit
        .Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun provideNasaApi(retrofit: Retrofit): NasaApiService {
    return retrofit.create()
}

fun provideRoomDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.name)
        .fallbackToDestructiveMigration()
        .build()
}

fun provideNasaPhotoDao(appDatabase: AppDatabase): NasaPhotoDao {
    return appDatabase.nasaPhotoDao()
}