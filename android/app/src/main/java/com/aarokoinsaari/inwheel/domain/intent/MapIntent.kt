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

package com.aarokoinsaari.inwheel.domain.intent

import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.domain.model.PlaceCategory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

sealed class MapIntent {
    data class MoveMap(
        val center: LatLng,
        val zoomLevel: Float,
        val bounds: LatLngBounds
    ) : MapIntent()
    data class UpdateQuery(val query: String) : MapIntent()
    data class SearchPlace(val query: String) : MapIntent()
    data class SelectPlace(val place: Place) : MapIntent()
    data class ToggleFilter(val category: PlaceCategory) : MapIntent()
    data class LocationPermissionGranted(val granted: Boolean) : MapIntent()
    data class UpdateUserLocation(val latLng: LatLng) : MapIntent()
}
