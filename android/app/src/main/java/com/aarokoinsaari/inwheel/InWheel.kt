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

package com.aarokoinsaari.inwheel

import android.app.Application
import android.util.Log
import com.aarokoinsaari.inwheel.data.remote.config.ConfigProvider
import com.aarokoinsaari.inwheel.di.appModule
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import android.content.pm.PackageManager

class InWheel : Application() {
    private val tag = "InWheel"
    lateinit var configProvider: ConfigProvider
        private set

    override fun onCreate() {
        super.onCreate()
        setupConfig()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@InWheel)
            modules(listOf(appModule))
        }
    }

    private fun setupConfig() {
        // Setup Firebase Remote Config
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600) // 1h
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf())
        
        configProvider = ConfigProvider(remoteConfig)

        try { // Set Maps API key immediately using cached values
            val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            applicationInfo.metaData.putString("com.google.android.geo.API_KEY", configProvider.getMapsApiKey())
        } catch (e: Exception) {
            Log.e(tag, "Failed to set initial Maps API key: ${e.message}")
        }
        
        // Fetch latest config values from server
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(tag, "Config params fetched and activated")
                    try {
                        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                        applicationInfo.metaData.putString("com.google.android.geo.API_KEY", configProvider.getMapsApiKey())
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to update Maps API key after fetch: ${e.message}")
                    }
                } else {
                    Log.e(tag, "Config fetch failed: ${task.exception?.message}")
                }
            }
    }
}
