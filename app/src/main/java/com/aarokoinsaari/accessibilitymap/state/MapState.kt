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

package com.aarokoinsaari.accessibilitymap.state

import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.view.model.PlaceClusterItem
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

data class MapState(
    val markers: List<Place> = emptyList(), // TODO: No use currently
    val clusterItems: List<PlaceClusterItem> = emptyList(),
    val zoomLevel: Float? = 10f,
    val center: LatLng? = null,
    val currentBounds: LatLngBounds? = null,
    val snapshotBounds: LatLngBounds? = null,
    val isLoading: Boolean = false,
    val selectedClusterItem: PlaceClusterItem? = null,
) {
    override fun toString(): String =
        """
            MapState(
                markers=${markers.size},
                clusterItems=${clusterItems.size},
                zoomLevel=$zoomLevel,
                center=$center,
                currentBoundingBox=$currentBounds,
                snapshotBoundingBox=$snapshotBounds,
                isLoading=$isLoading,
                selectedClusterItem=$selectedClusterItem
            )
        """.trimIndent()
}
