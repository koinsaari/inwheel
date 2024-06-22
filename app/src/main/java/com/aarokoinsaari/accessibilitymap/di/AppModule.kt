package com.aarokoinsaari.accessibilitymap.di

import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import com.aarokoinsaari.accessibilitymap.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.viewmodel.MainViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OverpassApiService::class.java)
    }
    single {
        PlaceRepository(get())
    }
    viewModel {
        MainViewModel()
    }
    viewModel {
        MapViewModel(get())
    }
}
