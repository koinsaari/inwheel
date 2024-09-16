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

package com.aarokoinsaari.accessibilitymap.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

@Entity(tableName = "places")
data class Place(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>?,
    val accessibility: AccessibilityInfo
)

data class PlaceClusterItem(
    private val place: Place,
    private val zIndex: Float?
) : ClusterItem {

    val placeData: Place
        get() = place

    override fun getPosition(): LatLng = LatLng(place.lat, place.lon)
    override fun getTitle(): String = place.name
    override fun getSnippet(): String = place.type
    override fun getZIndex(): Float? = null
}

data class AccessibilityInfo(
    val wheelchairAccess: WheelchairAccessStatus,
    val entry: EntryAccessibilityStatus,
    val hasAccessibleToilet: Boolean?,
    val hasElevator: Boolean?,
    val additionalInfo: String?
)
