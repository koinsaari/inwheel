/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.di

import androidx.room.Room
import com.aarokoinsaari.accessibilitymap.database.AppDatabase
import com.aarokoinsaari.accessibilitymap.database.MapMarkerDao
import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import com.aarokoinsaari.accessibilitymap.repository.MapMarkerRepository
import com.aarokoinsaari.accessibilitymap.viewmodel.MainViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Suppress("MagicNumber")
val appModule = module {
    single {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApiService::class.java)
    }
    single {
        Room.inMemoryDatabaseBuilder(
            androidContext(),
            AppDatabase::class.java
        ).build()
    }
    single<MapMarkerDao> {
        get<AppDatabase>().mapMarkerDao()
    }
    single {
        MapMarkerRepository(get(), get())
    }
    viewModel {
        MainViewModel()
    }
    viewModel {
        MapViewModel(get())
    }
}
