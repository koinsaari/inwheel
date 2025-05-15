/*
 * Copyright (c) 2025 Aaro Koinsaari
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

package com.aarokoinsaari.inwheel.data.remote.supabase

import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus
import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.domain.model.PlaceCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceSearchResultDto(
    val id: String,
    val name: String,
    val category: String,
    val lat: Double,
    val lon: Double,
    val region: String? = null,
    @SerialName("contact")
    val contact: ContactDto? = null,
    @SerialName("general_accessibility")
    val accessibility: GeneralAccessibilityDto? = null
) {
    val address: String?
        get() = contact?.address

    val generalAccessibility: AccessibilityStatus?
        get() = accessibility
            ?.accessibility
            ?.let { AccessibilityStatus.valueOf(it.uppercase()) }
}

fun PlaceSearchResultDto.toDomain(): Place =
    Place(
        id = id,
        name = name,
        category = PlaceCategory.valueOf(category.uppercase()),
        lat = lat,
        lon = lon,
        address = address,
        region = region,
        generalAccessibility = generalAccessibility
    )
