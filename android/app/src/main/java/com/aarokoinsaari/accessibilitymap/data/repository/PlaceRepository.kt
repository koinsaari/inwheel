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
import androidx.annotation.VisibleForTesting
import com.aarokoinsaari.accessibilitymap.data.local.PlacesDao
import com.aarokoinsaari.accessibilitymap.data.remote.supabase.SupabaseApiService
import com.aarokoinsaari.accessibilitymap.data.remote.supabase.toDomain
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceDetailProperty
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaceRepository(
    private val api: SupabaseApiService,
    private val dao: PlacesDao,
//    private val ftsDao: PlacesFtsDao,
) {
    fun observePlacesWithinBounds(bounds: LatLngBounds, limit: Int = 5000): Flow<List<Place>> =
        dao.getPlacesFlowWithinBounds(
            southLat = bounds.southwest.latitude,
            northLat = bounds.northeast.latitude,
            westLon = bounds.southwest.longitude,
            eastLon = bounds.northeast.longitude,
            limit = limit
        )

    suspend fun fetchAndStorePlaces(
        bounds: LatLngBounds,
        existingIds: Set<String> = emptySet(),
    ) = withContext(Dispatchers.IO) {
        val subBounds = splitBoundsIntoFour(bounds = bounds)
        val startTime = System.currentTimeMillis()

        coroutineScope {
            subBounds.forEach { sub ->
                launch {
                    val newPlaces = api.fetchPlacesInBBox(
                        westLon = sub.southwest.longitude,
                        southLat = sub.southwest.latitude,
                        eastLon = sub.northeast.longitude,
                        northLat = sub.northeast.latitude
                    )
                        .filterNot { it.id in existingIds }
                        .map { it.toDomain() }
                        .distinctBy { it.id }

                    if (newPlaces.isNotEmpty()) {
                        dao.insertPlaces(newPlaces)
                        Log.d(
                            "PlaceRepository",
                            "Fetched and inserted ${newPlaces.size} places to Room"
                        )
                    }
                }
            }
        }
        Log.d(
            "PlaceRepository",
            "Total time to fetch and store places: ${System.currentTimeMillis() - startTime} ms"
        )
//            val ftsPlaces = newPlaces
//                .filterNot { it.category.name.lowercase() in setOf("toilets", "parking", "unknown") }
//                .map { PlaceFts(rowId = it.id, name = it.name) }
//            ftsDao.insertPlaces(ftsPlaces)
//            Log.d("PlaceRepository", "Inserted ${ftsPlaces.size} places into FTS")
    }

    suspend fun updatePlaceGeneralAccessibility(place: Place, newStatus: AccessibilityStatus) =
        withContext(Dispatchers.IO) {
            dao.updatePlaceGeneralAccessibility(
                place.id,
                newStatus.name
            )
            Log.d(
                "PlaceRepository",
                "Updated place ${place.id} general accessibility to $newStatus"
            )
            api.updatePlaceGeneralAccessibility(
                place.id,
                newStatus.name
            )
            Log.d(
                "PlaceRepository",
                "Updated place ${place.id} general accessibility to $newStatus on Supabase"
            )
        }

    suspend fun updatePlaceAccessibilityDetail(
        place: Place,
        property: PlaceDetailProperty,
        newValue: Any?
    ) {
        withContext(Dispatchers.IO) {
            when (newValue) {
                is AccessibilityStatus ->
                    dao.updatePlaceAccessibilityDetailString(place.id, property.dbColumnRoom, newValue.name)
                is Boolean ->
                    dao.updatePlaceAccessibilityDetailBoolean(place.id, property.dbColumnRoom, newValue)
                is Int ->
                    dao.updatePlaceAccessibilityDetailInt(place.id, property.dbColumnRoom, newValue)
                is String ->
                    dao.updatePlaceAccessibilityDetailString(place.id, property.dbColumnRoom, newValue)
                else -> {
                    Log.e("PlaceRepository", "Unsupported type: ${newValue?.javaClass?.name}")
                }
            }
            Log.d(
                "PlaceRepository",
                "Updated place ${place.id} accessibility detail ${property.dbColumnRoom} to $newValue"
            )
            api.updatePlaceAccessibilityDetail(
                placeId = place.id,
                property = property,
                newValue = newValue
            )
            Log.d(
                "PlaceRepository",
                "Updated place ${place.id} accessibility detail ${property.dbColumnApi} to $newValue on db"
            )
        }
    }

    /*
     * Left this here for testing/illustration purposes to demonstrate the difference
     * in efficiency between sequential and asynchronous fetch/store operations.
     * To test this, remove the annotation and replace the call to `fetchAndStorePlaces`
     * in the `handleMove` method at `MapViewModel` with this synchronous version.
     */
    @VisibleForTesting
    suspend fun fetchAndStorePlacesSequential(
        bounds: LatLngBounds,
        existingIds: Set<String> = emptySet(),
    ) {
        val subBounds = splitBoundsIntoFour(bounds = bounds)
        val startTime = System.currentTimeMillis()

        for (sub in subBounds) {
            val newPlaces = api.fetchPlacesInBBox(
                westLon = sub.southwest.longitude,
                southLat = sub.southwest.latitude,
                eastLon = sub.northeast.longitude,
                northLat = sub.northeast.latitude
            )
                .filterNot { it.id in existingIds }
                .map { it.toDomain() }
                .distinctBy { it.id }

            if (newPlaces.isNotEmpty()) {
                dao.insertPlaces(newPlaces)
                Log.d("PlaceRepository", "Fetched and inserted ${newPlaces.size} places to Room")
            }
        }
        Log.d(
            "PlaceRepository",
            "Total time to fetch and store places: ${System.currentTimeMillis() - startTime} ms"
        )
    }

    private fun splitBoundsIntoFour(bounds: LatLngBounds): List<LatLngBounds> {
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
