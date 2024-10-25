/*
 * Copyright (c) 2024 Aaro Koinsaari
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aarokoinsaari.accessibilitymap.di

import androidx.room.Room
import com.aarokoinsaari.accessibilitymap.database.AppDatabase
import com.aarokoinsaari.accessibilitymap.database.PlacesDao
import com.aarokoinsaari.accessibilitymap.database.PlacesFtsDao
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import com.aarokoinsaari.accessibilitymap.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.PlaceDetailsViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.PlaceListViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.SharedPlaceViewModel
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
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }
//    single {
//        Room.inMemoryDatabaseBuilder(
//            androidContext(),
//            AppDatabase::class.java,
//        ).fallbackToDestructiveMigration().build()
//    }
    single<PlacesDao> {
        get<AppDatabase>().placesDao()
    }
    single {
        PlaceRepository(get(), get(), get())
    }
    single<PlacesFtsDao> {
        get<AppDatabase>().placesFtsDao()
    }
    viewModel {
        MapViewModel(get())
    }
    viewModel {
        PlaceListViewModel(get())
    }
    viewModel { (place: Place) ->
        PlaceDetailsViewModel(place)
    }
    viewModel {
        SharedPlaceViewModel()
    }
}
