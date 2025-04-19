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
    val entranceAccessibility: AccessibilityStatus? = null,
    val additionalInfo: String? = null,
    val stepCount: AccessibilityStatus? = null,
    val stepHeight: AccessibilityStatus? = null,
    val ramp: AccessibilityStatus? = null,
    val lift: AccessibilityStatus? = null,
    val width: AccessibilityStatus? = null,
    val type: String? = null,
    val restroomAccessibility: AccessibilityStatus? = null,
    val doorWidth: AccessibilityStatus? = null,
    val roomManeuver: AccessibilityStatus? = null,
    val grabRails: AccessibilityStatus? = null,
    val toiletSeat: AccessibilityStatus? = null,
    val emergencyAlarm: AccessibilityStatus? = null,
    val sink: AccessibilityStatus? = null,
    val euroKey: Boolean? = null,
)

enum class PlaceDetailProperty(
    @StringRes val labelRes: Int,
    @StringRes val dialogTitleRes: Int,
    val dbColumnRoom: String,
    val dbColumnApi: String,
) {
    GENERAL_ACCESSIBILITY(
        labelRes = R.string.general_accessibility,
        dialogTitleRes = R.string.general_accessibility_dialog_title,
        dbColumnRoom = "generalAccessibility",
        dbColumnApi = "general_accessibility"
    ),
    INDOOR_ACCESSIBILITY(
        labelRes = R.string.indoor_accessibility,
        dialogTitleRes = R.string.indoor_accessibility_dialog_title,
        dbColumnRoom = "indoorAccessibility",
        dbColumnApi = "indoor_accessibility"
    ),
    ENTRANCE_ACCESSIBILITY(
        labelRes = R.string.entrance_accessibility,
        dialogTitleRes = R.string.entrance_accessibility_dialog_title,
        dbColumnRoom = "entranceAccessibility",
        dbColumnApi = "entrance_accessibility"
    ),
    RESTROOM_ACCESSIBILITY(
        labelRes = R.string.restroom_accessibility,
        dialogTitleRes = R.string.restroom_accessibility_dialog_title,
        dbColumnRoom = "restroomAccessibility",
        dbColumnApi = "restroom_accessibility"
    ),
    ADDITIONAL_INFO(
        labelRes = R.string.additional_info,
        dialogTitleRes = R.string.additional_info_dialog_title,
        dbColumnRoom = "additionalInfo",
        dbColumnApi = "additional_info"
    ),
    STEP_COUNT(
        labelRes = R.string.step_count,
        dialogTitleRes = R.string.step_count_dialog_title,
        dbColumnRoom = "stepCount",
        dbColumnApi = "step_count"
    ),
    STEP_HEIGHT(
        labelRes = R.string.step_height,
        dialogTitleRes = R.string.step_height_dialog_title,
        dbColumnRoom = "stepHeight",
        dbColumnApi = "step_height"
    ),
    RAMP(
        labelRes = R.string.ramp,
        dialogTitleRes = R.string.ramp_dialog_title,
        dbColumnRoom = "ramp",
        dbColumnApi = "ramp"
    ),
    LIFT(
        labelRes = R.string.lift,
        dialogTitleRes = R.string.lift_dialog_title,
        dbColumnRoom = "lift",
        dbColumnApi = "lift"
    ),
    ENTRANCE_WIDTH( // Note this is entrance width related to entrance table
        labelRes = R.string.entrance_width,
        dialogTitleRes = R.string.entrance_width_dialog_title,
        dbColumnRoom = "width",
        dbColumnApi = "width"
    ),
    DOOR_TYPE(
        labelRes = R.string.doorType,
        dialogTitleRes = R.string.door_type_dialog_title,
        dbColumnRoom = "type",
        dbColumnApi = "type"
    ),
    DOOR_WIDTH( // Note this is restroom door width related to restroom table
        labelRes = R.string.door_width,
        dialogTitleRes = R.string.door_width_dialog_title,
        dbColumnRoom = "doorWidth",
        dbColumnApi = "door_width"
    ),
    ROOM_MANEUVER(
        labelRes = R.string.room_maneuver,
        dialogTitleRes = R.string.room_maneuver_dialog_title,
        dbColumnRoom = "roomManeuver",
        dbColumnApi = "room_maneuver"
    ),
    GRAB_RAILS(
        labelRes = R.string.grab_rails,
        dialogTitleRes = R.string.grab_rails_dialog_title,
        dbColumnRoom = "grabRails",
        dbColumnApi = "grab_rails"
    ),
    TOILET_SEAT(
        labelRes = R.string.toilet_seat,
        dialogTitleRes = R.string.toilet_seat_dialog_title,
        dbColumnRoom = "toiletSeat",
        dbColumnApi = "toilet_seat"
    ),
    EMERGENCY_ALARM(
        labelRes = R.string.emergency_alarm,
        dialogTitleRes = R.string.emergency_alarm_dialog_title,
        dbColumnRoom = "emergencyAlarm",
        dbColumnApi = "emergency_alarm"
    ),
    SINK(
        labelRes = R.string.sink,
        dialogTitleRes = R.string.sink_dialog_title,
        dbColumnRoom = "sink",
        dbColumnApi = "sink"
    ),
    EURO_KEY(
        labelRes = R.string.euro_key,
        dialogTitleRes = R.string.euro_key_dialog_title,
        dbColumnRoom = "euroKey",
        dbColumnApi = "euro_key"
    ),
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
    val name: String,
)
