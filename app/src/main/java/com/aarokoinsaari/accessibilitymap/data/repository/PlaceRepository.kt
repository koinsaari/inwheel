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

package com.aarokoinsaari.accessibilitymap.data.repository

import android.util.Log
import com.aarokoinsaari.accessibilitymap.data.local.PlacesDao
import com.aarokoinsaari.accessibilitymap.data.local.PlacesFtsDao
import com.aarokoinsaari.accessibilitymap.data.mapper.ApiDataConverter
import com.aarokoinsaari.accessibilitymap.data.remote.OverpassApiService
import com.aarokoinsaari.accessibilitymap.data.remote.OverpassQueryBuilder
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.PlaceFts
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaceRepository(
    private val apiService: OverpassApiService,
    private val placesDao: PlacesDao,
    private val placesFtsDao: PlacesFtsDao
) {
    fun observeAllPlaces(): Flow<List<Place>> = placesDao.getAllPlacesFlow()

    fun observePlacesWithinBounds(bounds: LatLngBounds): Flow<List<Place>> =
        placesDao.getPlacesFlowWithinBounds(
            southLat = bounds.southwest.latitude,
            northLat = bounds.northeast.latitude,
            westLon = bounds.southwest.longitude,
            eastLon = bounds.northeast.longitude
        )

    suspend fun getPlaces(
        bounds: LatLngBounds,
        snapshotBounds: LatLngBounds
    ): List<Place> {
        val cachedPlaces = withContext(Dispatchers.IO) {
            placesDao.getPlacesWithinBounds(
                southLat = snapshotBounds.southwest.latitude,
                northLat = snapshotBounds.northeast.latitude,
                westLon = snapshotBounds.southwest.longitude,
                eastLon = snapshotBounds.northeast.longitude
            )
        }
        Log.d("PlaceRepository", "Cached places: ${cachedPlaces.size}")

        // Return the cached places immediately and check if need to fetch from API
        if (!dataCoversBounds(cachedPlaces, bounds)) {
            fetchPlacesFromApiAsync(bounds)
        }
        return cachedPlaces
    }

    @Suppress("TooGenericExceptionCaught")
    private fun fetchPlacesFromApiAsync(bounds: LatLngBounds) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newPlaces = fetchPlacesFromApi(bounds)
                Log.d("PlaceRepository", "Fetched ${newPlaces.size} places from API")
                if (newPlaces.isNotEmpty()) {
                    placesDao.insertPlaces(newPlaces)
                    placesFtsDao.insertPlaces(
                        newPlaces.map {
                            PlaceFts(
                                rowId = it.id,
                                name = it.name
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("PlaceRepository", "Error fetching places from API", e)
                // TODO
            }
        }
    }

    private suspend fun fetchPlacesFromApi(bounds: LatLngBounds): List<Place> {
        val boundStr = "${bounds.southwest.latitude}," +
                "${bounds.southwest.longitude}," +
                "${bounds.northeast.latitude}," +
                "${bounds.northeast.longitude}"
        val query = OverpassQueryBuilder.buildQuery(boundStr)
        Log.d("Repository", "Query: $query")
        val response = apiService.getMarkers(query)
        return response.elements.mapNotNull {
            ApiDataConverter.convertMapMarkersToPlace(it)
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
