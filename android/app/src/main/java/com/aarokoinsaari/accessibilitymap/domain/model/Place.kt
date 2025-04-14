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

package com.aarokoinsaari.accessibilitymap.domain.model

import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.view.models.PlaceClusterItem
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "places")
data class Place(
    @PrimaryKey val id: String,
    val name: String,
    val category: PlaceCategory,
    val lat: Double,
    val lon: Double,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val website: String? = null,
    val generalAccessibility: AccessibilityStatus?,
    val indoorAccessibility: AccessibilityStatus? = null,
    val generalAdditionalInfo: String? = null,
    val entranceAccessibility: AccessibilityStatus? = null,
    val stepCount: Int? = null,
    val stepHeight: AccessibilityStatus? = null,
    val ramp: AccessibilityStatus? = null,
    val lift: AccessibilityStatus? = null,
    val width: AccessibilityStatus? = null,
    val type: String? = null,
    val entranceAdditionalInfo: String? = null,
    val restroomAccessibility: AccessibilityStatus? = null,
    val doorWidth: AccessibilityStatus? = null,
    val roomManeuver: AccessibilityStatus? = null,
    val grabRails: AccessibilityStatus? = null,
    val toiletSeat: AccessibilityStatus? = null,
    val emergencyAlarm: AccessibilityStatus? = null,
    val sink: AccessibilityStatus? = null,
    val euroKey: Boolean? = null,
    val accessibleVia: String? = null,
    val restroomAdditionalInfo: String? = null,
)

enum class ValueType {
    ACCESSIBILITY_STATUS, INT, BOOLEAN, STRING
}

enum class PlaceDetailProperty(
    @StringRes val labelRes: Int,
    val dbColumnRoom: String,
    val dbColumnApi: String,
    val valueType: ValueType
) {
    STEP_COUNT(R.string.step_count, "stepCount", "step_count", ValueType.INT),
    STEP_HEIGHT(R.string.step_height, "stepHeight", "step_height", ValueType.ACCESSIBILITY_STATUS),
    DOOR_WIDTH(R.string.door_width, "doorWidth", "door_width", ValueType.ACCESSIBILITY_STATUS),
    RAMP(R.string.ramp, "ramp", "ramp", ValueType.ACCESSIBILITY_STATUS),
    LIFT(R.string.lift, "lift", "lift", ValueType.ACCESSIBILITY_STATUS),
    DOOR_TYPE(R.string.doorType, "type", "type", ValueType.STRING),
    ENTRANCE_ADDITIONAL_INFO(R.string.additional_info, "entranceAdditionalInfo", "additional_info", ValueType.STRING),
    ROOM_MANEUVER(R.string.room_maneuver, "roomManeuver", "room_maneuver", ValueType.ACCESSIBILITY_STATUS),
    GRAB_RAILS(R.string.grab_rails, "grabRails", "grab_rails", ValueType.ACCESSIBILITY_STATUS),
    TOILET_SEAT(R.string.toilet_seat, "toiletSeat", "toilet_seat", ValueType.ACCESSIBILITY_STATUS),
    EMERGENCY_ALARM(R.string.emergency_alarm, "emergencyAlarm", "emergency_alarm", ValueType.ACCESSIBILITY_STATUS),
    SINK(R.string.sink, "sink", "sink", ValueType.ACCESSIBILITY_STATUS),
    EURO_KEY(R.string.euro_key, "euroKey", "euro_key", ValueType.BOOLEAN),
    ACCESSIBLE_VIA(R.string.accessible_via, "accessibleVia", "accessible_via", ValueType.STRING),
    RESTROOM_ADDITIONAL_INFO(R.string.additional_info, "restroomAdditionalInfo", "additional_info", ValueType.STRING)
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
