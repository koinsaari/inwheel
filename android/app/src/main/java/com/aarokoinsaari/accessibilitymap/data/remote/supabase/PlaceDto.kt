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
import com.aarokoinsaari.accessibilitymap.domain.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo.GeneralAccessibility
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo.ParkingAccessibility
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo.ToiletAccessibility
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class PlaceDto(
    @PrimaryKey val id: String,
    @SerialName("osm_id") val osmId: Long,
    val name: String,
    val category: String,
    val lat: Double,
    val lon: Double,
    val contact: Map<String, String?>?,
    @SerialName("accessibility_osm") val accessibilityOsm: Map<String, JsonElement>?,
    @SerialName("accessibility_user") val accessibilityUser: Map<String, JsonElement>?,
    @SerialName("last_osm_update") val lastOsmUpdate: String?,
    @SerialName("last_user_update") val lastUserUpdate: String?,
    @SerialName("created_at") val createdAt: String,
)

fun PlaceDto.toDomain(): Place =
    Place(
        id = this.id,
        name = this.name,
        category = PlaceCategory.fromRawValue(this.category),
        lat = this.lat,
        lon = this.lon,
        contact = this.contact?.let {
            ContactInfo(
                email = it["email"],
                phone = it["phone"],
                address = it["address"],
                website = it["website"]
            )
        } ?: ContactInfo(),
        accessibility = parseAccessibility(
            osm = this.accessibilityOsm,
            user = this.accessibilityUser,
            category = this.category
        )
    )

// Tries to cast a JsonElement to a JsonObject
private val JsonElement.jsonObjectOrNull: JsonObject?
    get() = this as? JsonObject

private fun parseAccessibility(
    osm: Map<String, JsonElement>?,
    user: Map<String, JsonElement>?,
    category: String?,
): AccessibilityInfo {

    fun getMergedJson(key: String): JsonElement? =
        user?.get(key) ?: osm?.get(key)

    fun getMergedString(key: String): String? =
        getMergedJson(key)?.jsonPrimitive?.contentOrNull

    fun getMergedBoolean(key: String): Boolean? =
        getMergedJson(key)?.jsonPrimitive?.booleanOrNull

    fun getMergedInt(key: String): Int? =
        getMergedJson(key)?.jsonPrimitive?.intOrNull

    return when (category) {
        "toilets" -> {
            ToiletAccessibility(
                accessibilityStatus = parseStatus(getMergedString("accessibility_status")),
                doorWidth = parseStatus(getMergedString("door_width")),
                grabRails = parseStatus(getMergedString("grab_rails")),
                toiletSeat = parseStatus(getMergedString("toilet_seat")),
                emergencyAlarm = parseStatus(getMergedString("emergency_alarm")),
                sink = parseStatus(getMergedString("sink")),
                euroKey = getMergedBoolean("euro_key") == true,
                additionalInfo = getMergedString("additional_info")
            )
        }

        "parking" -> {
            ParkingAccessibility(
                accessibilityStatus = parseStatus(getMergedString("accessibility_status")),
                accessibleSpotCount = getMergedInt("accessible_spot_count"),
                surface = getMergedString("surface"),
                parkingType = getMergedString("parking_type"),
                hasElevator = getMergedBoolean("has_elevator"),
                additionalInfo = getMergedString("additional_info")
            )
        }

        else -> {
            val mergedEntrance = mergeJsonObjects(
                osm?.get("entrance")?.jsonObjectOrNull,
                user?.get("entrance")?.jsonObjectOrNull
            )
            val mergedRestroom = mergeJsonObjects(
                osm?.get("restroom")?.jsonObjectOrNull,
                user?.get("restroom")?.jsonObjectOrNull
            )

            GeneralAccessibility(
                accessibilityStatus = parseStatus(getMergedString("accessibility_status")),
                indoorAccessibility = parseStatus(getMergedString("indoor_accessibility")),
                entrance = parseEntrance(mergedEntrance),
                restroom = parseRestroom(mergedRestroom),
                additionalInfo = getMergedString("additional_info")
            )
        }
    }
}

private fun parseStatus(value: String?): AccessibilityStatus =
    if (value.isNullOrBlank()) {
        AccessibilityStatus.UNKNOWN
    } else {
        try {
            AccessibilityStatus.valueOf(value.uppercase())
        } catch (e: IllegalArgumentException) {
            Log.e("parseStatus", "Invalid enum value: $value", e)
            AccessibilityStatus.UNKNOWN
        }
    }

// Merges two JsonObjects, where the second JsonObject (user changes) overrides any matching
// keys in the first (osm changes)
private fun mergeJsonObjects(a: JsonObject?, b: JsonObject?): JsonObject? {
    if (a == null && b == null) return null
    val merged = a?.toMutableMap() ?: mutableMapOf()
    b?.forEach { (k, v) -> merged[k] = v }
    return JsonObject(merged)
}

private fun parseEntrance(json: JsonObject?): GeneralAccessibility.EntranceAccessibility? {
    if (json == null) return null
    val stepsObj = json["steps"]?.jsonObjectOrNull
    val doorObj = json["door"]?.jsonObjectOrNull

    return GeneralAccessibility.EntranceAccessibility(
        accessibilityStatus = parseStatus(json["accessibility_status"]?.jsonPrimitive?.contentOrNull),
        steps = stepsObj?.let {
            GeneralAccessibility.EntranceAccessibility.StepsAccessibility(
                stepCount = it["step_count"]?.jsonPrimitive?.intOrNull,
                stepHeight = parseStatus(it["step_height"]?.jsonPrimitive?.contentOrNull),
                ramp = parseStatus(it["ramp"]?.jsonPrimitive?.contentOrNull),
                lift = parseStatus(it["lift"]?.jsonPrimitive?.contentOrNull)
            )
        },
        door = doorObj?.let { door ->
            GeneralAccessibility.EntranceAccessibility.DoorAccessibility(
                doorWidth = parseStatus(door["width"]?.jsonPrimitive?.contentOrNull),
                doorType = door["type"]?.jsonPrimitive?.contentOrNull
            )
        },
        additionalInfo = json["additional_info"]?.jsonPrimitive?.contentOrNull
    )
}

private fun parseRestroom(json: JsonObject?): GeneralAccessibility.RestroomAccessibility? {
    if (json == null) return null
    return GeneralAccessibility.RestroomAccessibility(
        accessibility = parseStatus(json["accessibility_status"]?.jsonPrimitive?.contentOrNull),
        doorWidth = parseStatus(json["door_width"]?.jsonPrimitive?.contentOrNull),
        roomManeuver = parseStatus(json["room_manuever"]?.jsonPrimitive?.contentOrNull),
        grabRails = parseStatus(json["grab_rails"]?.jsonPrimitive?.contentOrNull),
        toiletSeat = parseStatus(json["toilet_seat"]?.jsonPrimitive?.contentOrNull),
        emergencyAlarm = parseStatus(json["emergency_alarm"]?.jsonPrimitive?.contentOrNull),
        sink = parseStatus(json["sink"]?.jsonPrimitive?.contentOrNull),
        euroKey = json["euro_key"]?.jsonPrimitive?.booleanOrNull == true,
        accessibleVia = json["accessible_via"]?.jsonPrimitive?.contentOrNull,
        additionalInfo = json["additional_info"]?.jsonPrimitive?.contentOrNull
    )
}
