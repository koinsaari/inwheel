/*
 * Copyright (c) 2024–2025 Aaro Koinsaari
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
import com.aarokoinsaari.accessibilitymap.BuildConfig
import com.aarokoinsaari.accessibilitymap.data.local.AppDatabase
import com.aarokoinsaari.accessibilitymap.data.local.Converters
import com.aarokoinsaari.accessibilitymap.data.local.PlacesDao
import com.aarokoinsaari.accessibilitymap.data.local.PlacesFtsDao
import com.aarokoinsaari.accessibilitymap.data.remote.supabase.SupabaseApiService
import com.aarokoinsaari.accessibilitymap.data.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.PlaceDetailsViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.SharedViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("MagicNumber")
val appModule = module {
//    single {
//        Room.databaseBuilder(
//            androidContext(),
//            AppDatabase::class.java,
//            "app_database"
//        )
//            .addTypeConverter(get<Converters>())
//            .fallbackToDestructiveMigration().build()
//    }
    single {
        Room.inMemoryDatabaseBuilder(
            androidContext(),
            AppDatabase::class.java,
        )
            .addTypeConverter(get<Converters>())
            .fallbackToDestructiveMigration().build()
    }

    single {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest)
        }
    }
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }
    single {
        Converters()
    }
    single<PlacesDao> {
        get<AppDatabase>().placesDao()
    }
    single<PlacesFtsDao> {
        get<AppDatabase>().placesFtsDao()
    }
    single {
        PlaceRepository(get(), get())
    }
    single {
        SupabaseApiService(get())
    }
    single {
        SharedViewModel()
    }
    viewModel {
        MapViewModel(get(), get())
    }
    viewModel { (place: Place) ->
        PlaceDetailsViewModel(place)
    }
}
