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

package com.aarokoinsaari.inwheel.di

import androidx.room.Room
import com.aarokoinsaari.inwheel.InWheel
import com.aarokoinsaari.inwheel.data.local.AppDatabase
import com.aarokoinsaari.inwheel.data.local.Converters
import com.aarokoinsaari.inwheel.data.local.PlacesDao
import com.aarokoinsaari.inwheel.data.remote.config.ConfigProvider
import com.aarokoinsaari.inwheel.data.remote.supabase.SupabaseApiService
import com.aarokoinsaari.inwheel.data.repository.PlaceRepository
import com.aarokoinsaari.inwheel.viewmodel.MapViewModel
import com.aarokoinsaari.inwheel.viewmodel.PlaceDetailsViewModel
import com.aarokoinsaari.inwheel.viewmodel.SharedViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    single { (androidContext().applicationContext as InWheel).configProvider }

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
            .fallbackToDestructiveMigration(false).build()
    }

    single {
        val configProvider = get<ConfigProvider>()
        createSupabaseClient(
            supabaseUrl = configProvider.getSupabaseUrl(),
            supabaseKey = configProvider.getSupabaseKey()
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

    single { Converters() }
    single<PlacesDao> { get<AppDatabase>().placesDao() }
    single { PlaceRepository(get(), get()) }
    single { SupabaseApiService(get(), get()) }
    single { SharedViewModel() }
    factoryOf(::MapViewModel)
    factoryOf(::PlaceDetailsViewModel)
}
