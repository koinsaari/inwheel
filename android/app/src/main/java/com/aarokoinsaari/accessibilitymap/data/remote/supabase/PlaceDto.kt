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

package com.aarokoinsaari.accessibilitymap.data.remote.supabase

import android.util.Log
import androidx.room.PrimaryKey
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    @PrimaryKey val id: String,
    @SerialName("osm_id") val osmId: Long,
    val name: String,
    val category: String,
    val lat: Double,
    val lon: Double,
    @SerialName("last_osm_update") val lastOsmUpdate: String?,
    @SerialName("last_user_update") val lastUserUpdate: String?,
    @SerialName("created_at") val createdAt: String,
    val contact: ContactDto?,
    val generalAccessibility: GeneralAccessibilityDto?,
    val entranceAccessibility: EntranceAccessibilityDto?,
    val restroomAccessibility: RestroomAccessibilityDto?,
)

@Serializable
data class ContactDto(
    val phone: String? = null,
    val website: String? = null,
    val email: String? = null,
    val address: String? = null,
)

@Serializable
data class GeneralAccessibilityDto(
    val accessibility: String?,
    @SerialName("indoor_accessibility") val indoorAccessibility: String?,
    @SerialName("additional_info") val additionalInfo: String?,
    @SerialName("user_modified") val userModified: Boolean? = false,
)

@Serializable
data class EntranceAccessibilityDto(
    val accessibility: String?,
    @SerialName("step_count") val stepCount: Int?,
    @SerialName("step_height") val stepHeight: String?,
    val ramp: String?,
    val lift: String?,
    val width: String?,
    val type: String?,
    @SerialName("additional_info") val additionalInfo: String?,
    @SerialName("user_modified") val userModified: Boolean? = false,
)

@Serializable
data class RestroomAccessibilityDto(
    val accessibility: String?,
    @SerialName("door_width") val doorWidth: String?,
    @SerialName("room_maneuver") val roomManeuver: String?,
    @SerialName("grab_rails") val grabRails: String?,
    val sink: String?,
    @SerialName("toilet_seat") val toiletSeat: String?,
    @SerialName("emergency_alarm") val emergencyAlarm: String?,
    @SerialName("accessible_via") val accessibleVia: String?,
    @SerialName("euro_key") val euroKey: Boolean?,
    @SerialName("additional_info") val additionalInfo: String?,
    @SerialName("user_modified") val userModified: Boolean? = false,
)

fun PlaceDto.toDomain(): Place =
    Place(
        id = id,
        name = name,
        category = PlaceCategory.valueOf(category.uppercase()),
        lat = lat,
        lon = lon,
        email = contact?.email,
        phone = contact?.phone,
        address = contact?.address,
        website = contact?.website,
        generalAccessibility = parseStatus(generalAccessibility?.accessibility),
        indoorAccessibility = parseStatus(generalAccessibility?.indoorAccessibility),
        generalAdditionalInfo = generalAccessibility?.additionalInfo,
        entranceAccessibility = parseStatus(entranceAccessibility?.accessibility),
        stepCount = entranceAccessibility?.stepCount,
        stepHeight = parseStatus(entranceAccessibility?.stepHeight),
        ramp = parseStatus(entranceAccessibility?.ramp),
        lift = parseStatus(entranceAccessibility?.lift),
        width = parseStatus(entranceAccessibility?.width),
        type = entranceAccessibility?.type,
        entranceAdditionalInfo = entranceAccessibility?.additionalInfo,
        restroomAccessibility = parseStatus(restroomAccessibility?.accessibility),
        doorWidth = parseStatus(restroomAccessibility?.doorWidth),
        roomManeuver = parseStatus(restroomAccessibility?.roomManeuver),
        grabRails = parseStatus(restroomAccessibility?.grabRails),
        toiletSeat = parseStatus(restroomAccessibility?.toiletSeat),
        emergencyAlarm = parseStatus(restroomAccessibility?.emergencyAlarm),
        sink = parseStatus(restroomAccessibility?.sink),
        euroKey = restroomAccessibility?.euroKey,
        accessibleVia = restroomAccessibility?.accessibleVia,
        restroomAdditionalInfo = restroomAccessibility?.additionalInfo
    )

private fun parseStatus(value: String?): AccessibilityStatus {
    if (value.isNullOrBlank()) {
        return AccessibilityStatus.UNKNOWN
    }
    return try {
        AccessibilityStatus.valueOf(value.uppercase())
    } catch (e: IllegalArgumentException) {
        Log.e("parseStatus", "Unknown accessibility status: $value with error: $e")
        AccessibilityStatus.UNKNOWN
    }
}
