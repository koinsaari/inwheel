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
import com.aarokoinsaari.accessibilitymap.data.local.AppDatabase
import com.aarokoinsaari.accessibilitymap.data.local.PlacesDao
import com.aarokoinsaari.accessibilitymap.data.local.PlacesFtsDao
import com.aarokoinsaari.accessibilitymap.data.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.PlaceDetailsViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("MagicNumber")
val appModule = module {
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

    single {
        createSupabaseClient(
            supabaseUrl = "",
            supabaseKey = ""
        ) {
            install(Postgrest)
        }
    }
    single<PlacesDao> {
        get<AppDatabase>().placesDao()
    }
    single {
        PlaceRepository(get(), get())
    }
    single<PlacesFtsDao> {
        get<AppDatabase>().placesFtsDao()
    }
    viewModel {
        MapViewModel(get())
    }
    viewModel { (place: Place) ->
        PlaceDetailsViewModel(place)
    }
}
