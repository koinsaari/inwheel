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

package com.aarokoinsaari.accessibilitymap.repository

import android.util.Log
import com.aarokoinsaari.accessibilitymap.database.PlacesDao
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import com.aarokoinsaari.accessibilitymap.network.OverpassQueryBuilder
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlaceRepository(
    private val apiService: OverpassApiService,
    private val placesDao: PlacesDao
) {
    @Suppress("TooGenericExceptionCaught")
    fun getPlaces(bounds: LatLngBounds, snapshotBounds: LatLngBounds): Flow<List<Place>> = flow {
        val cachedPlaces = placesDao.getPlaces(
            snapshotBounds.southwest.latitude,
            snapshotBounds.southwest.longitude,
            snapshotBounds.northeast.latitude,
            snapshotBounds.northeast.longitude
        )
        emit(cachedPlaces)
        Log.d("Repository", "Using cached places: $cachedPlaces")

        if (!dataCoversBounds(cachedPlaces, bounds)) {
            try {
                val boundStr = "${bounds.southwest.latitude}," +
                        "${bounds.southwest.longitude}," +
                        "${bounds.northeast.latitude}," +
                        "${bounds.northeast.longitude}"
                val query = OverpassQueryBuilder.buildQuery(boundStr)
                Log.d("Repository", "Query: $query")
                val response = apiService.getMarkers(query)
                val apiPlaces = response.elements.mapNotNull {
                    ApiDataConverter.convertMapMarkersToPlace(it)
                }

                if (apiPlaces.isNotEmpty()) {
                    placesDao.insertAll(apiPlaces)
                    Log.d("Repository", "Inserted places into database: $apiPlaces")
                    emit(apiPlaces)
                }
            } catch (e: Exception) {
                Log.e("Repository", "Failed to fetch or save places", e)
            }
        }
    }.flowOn(Dispatchers.IO)

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
