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

package com.aarokoinsaari.accessibilitymap.data.repository

import android.util.Log
import com.aarokoinsaari.accessibilitymap.data.local.PlacesDao
import com.aarokoinsaari.accessibilitymap.data.remote.supabase.SupabaseApiService
import com.aarokoinsaari.accessibilitymap.data.remote.supabase.toDomain
import com.aarokoinsaari.accessibilitymap.model.Place
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.flow.Flow

class PlaceRepository(
    private val supabaseApiService: SupabaseApiService,
    private val placesDao: PlacesDao,
//    private val placesFtsDao: PlacesFtsDao
) {
    fun observePlacesWithinBounds(bounds: LatLngBounds): Flow<List<Place>> =
        placesDao.getPlacesFlowWithinBounds(
            southLat = bounds.southwest.latitude,
            northLat = bounds.northeast.latitude,
            westLon = bounds.southwest.longitude,
            eastLon = bounds.northeast.longitude
        )

    suspend fun loadPlacesWithinBounds(bounds: LatLngBounds, limit: Int = 50) {
        val newPlaces = supabaseApiService.fetchPlacesWithGeom(
            westLon = bounds.southwest.longitude,
            southLat = bounds.southwest.latitude,
            eastLon = bounds.northeast.longitude,
            northLat = bounds.northeast.latitude,
            limit = limit
        ).map { it.toDomain() }
        Log.d("PlaceRepository", "Loaded ${newPlaces.size} places")

        if (newPlaces.isNotEmpty()) {
            placesDao.insertPlaces(newPlaces)
            Log.d("PlaceRepository", "Inserted ${newPlaces.size} places into database")
        }
    }

    private fun dataCoversBounds(cachedPlaces: List<Place>, bounds: LatLngBounds): Boolean {
        if (cachedPlaces.isEmpty()) return false

        val minLat = cachedPlaces.minOf { it.lat }
        val maxLat = cachedPlaces.maxOf { it.lat }
        val minLon = cachedPlaces.minOf { it.lon }
        val maxLon = cachedPlaces.maxOf { it.lon }

        return minLat <= bounds.southwest.latitude &&
                maxLat >= bounds.northeast.latitude &&
                minLon <= bounds.southwest.longitude &&
                maxLon >= bounds.northeast.longitude
    }
}
