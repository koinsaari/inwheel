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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.ui.models.PlaceClusterItem

@Entity(tableName = "places")
data class Place(
    @PrimaryKey val id: Long,
    val name: String,
    val category: PlaceCategory,
    val lat: Double,
    val lon: Double,
    val address: String?,
    val tags: Map<String, String>?,
    val accessibility: AccessibilityInfo?,
    val contactInfo: ContactInfo?
) {
    /**
     * Note: This function temporarily handles the determination of the general accessibility status
     * by parsing the "wheelchair" tag from the Overpass API. Only this tag is used to determine
     * the accessibility of a place if there are no other information. Until a better solution is
     * implemented, we use this to determine the general accessibility.
     **/
    fun determineAccessibilityStatus(): AccessibilityStatus =
        when (tags?.get("wheelchair")?.lowercase()) {
            "yes", "designated" -> AccessibilityStatus.FULLY_ACCESSIBLE
            "limited" -> AccessibilityStatus.LIMITED_ACCESSIBILITY
            "no" -> AccessibilityStatus.NOT_ACCESSIBLE
            else -> AccessibilityStatus.UNKNOWN
        }
}

fun Place.toClusterItem(zIndex: Float? = null): PlaceClusterItem =
    PlaceClusterItem(
        place = this,
        zIndex = zIndex
    )

@Entity(tableName = "places_fts")
@Fts4(contentEntity = Place::class)
data class PlaceFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Long,
    val name: String
)

data class ContactInfo(
    val email: String?,
    val phone: String?,
    val website: String?
)
