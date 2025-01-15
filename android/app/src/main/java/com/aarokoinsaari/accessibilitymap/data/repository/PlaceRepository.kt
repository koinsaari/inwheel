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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class PlaceRepository(
    private val api: SupabaseApiService,
    private val dao: PlacesDao,
//    private val ftsDao: PlacesFtsDao,
) {
    fun observePlacesWithinBounds(bounds: LatLngBounds): Flow<List<Place>> =
        dao.getPlacesFlowWithinBounds(
            southLat = bounds.southwest.latitude,
            northLat = bounds.northeast.latitude,
            westLon = bounds.southwest.longitude,
            eastLon = bounds.northeast.longitude
        )

    suspend fun fetchAndStorePlaces(
        bounds: LatLngBounds,
        existingIds: Set<String> = emptySet(),
    ) {
        delay(300)
        val subBounds = splitBoundsIntoFour(bounds = bounds)
        val apiPlaces = coroutineScope {
            subBounds.map { sub ->
                async {
                    api.fetchPlacesInBBox(
                        westLon = sub.southwest.longitude,
                        southLat = sub.southwest.latitude,
                        eastLon = sub.northeast.longitude,
                        northLat = sub.northeast.latitude
                    )
                }
            }
            .awaitAll().flatten()
        }

        if (apiPlaces.isNotEmpty()) {
            val newPlaces = apiPlaces
                .filterNot { it.id in existingIds }
                .map { it.toDomain() }
                .distinctBy { it.id }
            dao.insertPlaces(newPlaces)
            Log.d("PlaceRepository", "Inserted ${newPlaces.size} new places into Room")

//            val ftsPlaces = newPlaces
//                .filterNot { it.category.name.lowercase() in setOf("toilets", "parking", "unknown") }
//                .map { PlaceFts(rowId = it.id, name = it.name) }
//            ftsDao.insertPlaces(ftsPlaces)
//            Log.d("PlaceRepository", "Inserted ${ftsPlaces.size} places into FTS")
        }
    }

    private fun splitBoundsIntoFour(bounds: LatLngBounds, ): List<LatLngBounds> {
        val midLat = (bounds.southwest.latitude + bounds.northeast.latitude) / 2
        val midLon = (bounds.southwest.longitude + bounds.northeast.longitude) / 2

        val sw = LatLng(bounds.southwest.latitude, bounds.southwest.longitude)
        val ne = LatLng(bounds.northeast.latitude, bounds.northeast.longitude)
        val midSW = LatLng(midLat, midLon)

        return listOf(
            LatLngBounds(
                sw,
                LatLng(midLat, midLon)  // (southwest)
            ),
            LatLngBounds(
                LatLng(bounds.southwest.latitude, midLon),
                LatLng(midLat, bounds.northeast.longitude) // (southeast)
            ),
            LatLngBounds(
                LatLng(midLat, bounds.southwest.longitude),
                LatLng(bounds.northeast.latitude, midLon)   // (northwest)
            ),
            LatLngBounds(
                midSW,
                ne  // (northeast)
            )
        )
    }
}
