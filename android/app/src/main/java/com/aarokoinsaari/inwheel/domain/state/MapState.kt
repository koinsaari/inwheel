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

package com.aarokoinsaari.inwheel.domain.state

import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.view.models.PlaceClusterItem
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

data class MapState(
    val center: LatLng? = null,
    val zoomLevel: Float? = 10f,
    val snapshotBounds: LatLngBounds? = null, // Small bounds for triggering fetching
    val expandedSnapshotBounds: LatLngBounds? = null, // Expanded bounds for actual fetched area
    val clusterItems: List<PlaceClusterItem> = emptyList(), // Places on UI
    val selectedClusterItem: PlaceClusterItem? = null,
    val selectedCategories: Set<String> = emptySet(),
    val selectedPlace: Place? = null, // Used for place details
    val filteredPlaces: List<Place> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val locationPermissionGranted: Boolean = false,
    val errorState: ErrorState = ErrorState.None,
    val userLocation: LatLng? = null
) {
    override fun toString(): String =
        """
            MapState(
                center=$center,
                zoomLevel=$zoomLevel,
                snapshotBounds=$snapshotBounds,
                expandedSnapshotBounds=$expandedSnapshotBounds,
                clusterItems=${clusterItems.size},
                selectedClusterItem=$selectedClusterItem,
                selectedCategories=$selectedCategories,
                selectedPlace=$selectedPlace,
                filteredPlaces=${filteredPlaces.size},
                searchQuery='$searchQuery',
                isLoading=$isLoading,
                isSearching=$isSearching,
                locationPermissionGranted=$locationPermissionGranted,
                errorState=$errorState
                userLocation=$userLocation
            )
        """.trimIndent()
}
