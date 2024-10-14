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

import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus
import com.aarokoinsaari.accessibilitymap.network.ApiMapMarker
import com.aarokoinsaari.accessibilitymap.utils.PlaceCategory.Companion.mapApiTagToCategory

object ApiDataConverter {
    fun convertMapMarkersToPlace(apiMarker: ApiMapMarker): Place? {
        val tags = apiMarker.tags ?: return null
        val amenity = tags["amenity"] ?: "default"
        val category = mapApiTagToCategory(amenity)
        val name = tags["name"] ?: category.defaultName

        return Place(
            id = apiMarker.id,
            name = name,
            category = category,
            lat = apiMarker.lat,
            lon = apiMarker.lon,
            tags = tags,
            accessibility = parseAccessibilityInfo(tags)
        )
    }

    private fun parseAccessibilityInfo(tags: Map<String, String>): AccessibilityInfo =
        AccessibilityInfo(
            wheelchairAccess = parseWheelchairAccessStatus(tags["wheelchair"]),
            hasAccessibleToilet = parseBooleanAccessibilityStatus(tags["toilet:wheelchair"]),
            hasElevator = parseBooleanAccessibilityStatus(tags["elevator"]),
            additionalInfo = tags["wheelchair:description"] ?: tags["note"]
        )

    private fun parseWheelchairAccessStatus(status: String?): WheelchairAccessStatus =
        when (status?.lowercase()) {
            "yes", "designated" -> WheelchairAccessStatus.FULLY_ACCESSIBLE
            "limited" -> WheelchairAccessStatus.LIMITED_ACCESSIBILITY
            "no" -> WheelchairAccessStatus.NOT_ACCESSIBLE
            else -> WheelchairAccessStatus.UNKNOWN
        }

    private fun parseBooleanAccessibilityStatus(status: String?): Boolean? =
        when (status?.lowercase()) {
            "yes" -> true
            "no" -> false
            else -> null
        }
}
