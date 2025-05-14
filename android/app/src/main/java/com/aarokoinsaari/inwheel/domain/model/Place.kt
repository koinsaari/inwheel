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

package com.aarokoinsaari.inwheel.domain.model

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.view.models.PlaceClusterItem
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "places",
    indices = [
        Index(value = ["lat", "lon"]),
        Index(value = ["tileId"]),
        Index(value = ["region"]),
        Index(value = ["category"]),
        Index(value = ["lastVisited"])
    ]
)
data class Place(
    @PrimaryKey val id: String,
    val name: String,
    val category: PlaceCategory,
    val lat: Double,
    val lon: Double,
    val region: String? = null,
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
    val entranceWidth: AccessibilityStatus? = null,
    val doorType: String? = null,
    val restroomAccessibility: AccessibilityStatus? = null,
    val doorWidth: AccessibilityStatus? = null,
    val roomManeuver: AccessibilityStatus? = null,
    val grabRails: AccessibilityStatus? = null,
    val toiletSeat: AccessibilityStatus? = null,
    val emergencyAlarm: AccessibilityStatus? = null,
    val sink: AccessibilityStatus? = null,
    val euroKey: Boolean? = null,
    val tileId: String? = null,
    val fetchTimestamp: Long = System.currentTimeMillis(),
    val lastVisited: Long = System.currentTimeMillis(),
    val userModified: Boolean = false,
)

enum class PlaceDetailProperty(
    @StringRes val labelRes: Int,
    @StringRes val dialogTitleRes: Int,
    @StringRes val successMessageRes: Int,
    val dbColumnRoom: String,
    val dbColumnApi: String,
) {
    GENERAL_ACCESSIBILITY(
        labelRes = R.string.general_accessibility,
        dialogTitleRes = R.string.general_accessibility_dialog_title,
        successMessageRes = R.string.success_accessibility_updated,
        dbColumnRoom = "generalAccessibility",
        dbColumnApi = "general_accessibility"
    ),
    INDOOR_ACCESSIBILITY(
        labelRes = R.string.indoor_accessibility,
        dialogTitleRes = R.string.indoor_accessibility_dialog_title,
        successMessageRes = R.string.success_indoor_accessibility_updated,
        dbColumnRoom = "indoorAccessibility",
        dbColumnApi = "indoor_accessibility"
    ),
    ENTRANCE_ACCESSIBILITY(
        labelRes = R.string.entrance_accessibility,
        dialogTitleRes = R.string.entrance_accessibility_dialog_title,
        successMessageRes = R.string.success_entrance_accessibility_updated,
        dbColumnRoom = "entranceAccessibility",
        dbColumnApi = "entrance_accessibility"
    ),
    RESTROOM_ACCESSIBILITY(
        labelRes = R.string.restroom_accessibility,
        dialogTitleRes = R.string.restroom_accessibility_dialog_title,
        successMessageRes = R.string.success_restroom_accessibility_updated,
        dbColumnRoom = "restroomAccessibility",
        dbColumnApi = "restroom_accessibility"
    ),
    ADDITIONAL_INFO(
        labelRes = R.string.additional_info,
        dialogTitleRes = R.string.additional_info_dialog_title,
        successMessageRes = R.string.success_additional_info_updated,
        dbColumnRoom = "additionalInfo",
        dbColumnApi = "additional_info"
    ),
    STEP_COUNT(
        labelRes = R.string.step_count,
        dialogTitleRes = R.string.step_count_dialog_title,
        successMessageRes = R.string.success_step_count_updated,
        dbColumnRoom = "stepCount",
        dbColumnApi = "step_count"
    ),
    STEP_HEIGHT(
        labelRes = R.string.step_height,
        dialogTitleRes = R.string.step_height_dialog_title,
        successMessageRes = R.string.success_step_height_updated,
        dbColumnRoom = "stepHeight",
        dbColumnApi = "step_height"
    ),
    RAMP(
        labelRes = R.string.ramp,
        dialogTitleRes = R.string.ramp_dialog_title,
        successMessageRes = R.string.success_ramp_updated,
        dbColumnRoom = "ramp",
        dbColumnApi = "ramp"
    ),
    LIFT(
        labelRes = R.string.lift,
        dialogTitleRes = R.string.lift_dialog_title,
        successMessageRes = R.string.success_lift_updated,
        dbColumnRoom = "lift",
        dbColumnApi = "lift"
    ),
    ENTRANCE_WIDTH( // Note this is entrance width related to entrance table
        labelRes = R.string.entrance_width,
        dialogTitleRes = R.string.entrance_width_dialog_title,
        successMessageRes = R.string.success_entrance_width_updated,
        dbColumnRoom = "entranceWidth",
        dbColumnApi = "entrance_width"
    ),
    DOOR_TYPE(
        labelRes = R.string.doorType,
        dialogTitleRes = R.string.door_type_dialog_title,
        successMessageRes = R.string.success_door_type_updated,
        dbColumnRoom = "doorType",
        dbColumnApi = "door_type"
    ),
    DOOR_WIDTH( // Note this is restroom door width related to restroom table
        labelRes = R.string.door_width,
        dialogTitleRes = R.string.door_width_dialog_title,
        successMessageRes = R.string.success_door_width_updated,
        dbColumnRoom = "doorWidth",
        dbColumnApi = "door_width"
    ),
    ROOM_MANEUVER(
        labelRes = R.string.room_maneuver,
        dialogTitleRes = R.string.room_maneuver_dialog_title,
        successMessageRes = R.string.success_room_maneuver_updated,
        dbColumnRoom = "roomManeuver",
        dbColumnApi = "room_maneuver"
    ),
    GRAB_RAILS(
        labelRes = R.string.grab_rails,
        dialogTitleRes = R.string.grab_rails_dialog_title,
        successMessageRes = R.string.success_grab_rails_updated,
        dbColumnRoom = "grabRails",
        dbColumnApi = "grab_rails"
    ),
    TOILET_SEAT(
        labelRes = R.string.toilet_seat,
        dialogTitleRes = R.string.toilet_seat_dialog_title,
        successMessageRes = R.string.success_toilet_seat_updated,
        dbColumnRoom = "toiletSeat",
        dbColumnApi = "toilet_seat"
    ),
    EMERGENCY_ALARM(
        labelRes = R.string.emergency_alarm,
        dialogTitleRes = R.string.emergency_alarm_dialog_title,
        successMessageRes = R.string.success_emergency_alarm_updated,
        dbColumnRoom = "emergencyAlarm",
        dbColumnApi = "emergency_alarm"
    ),
    SINK(
        labelRes = R.string.sink,
        dialogTitleRes = R.string.sink_dialog_title,
        successMessageRes = R.string.success_sink_updated,
        dbColumnRoom = "sink",
        dbColumnApi = "sink"
    ),
    EURO_KEY(
        labelRes = R.string.euro_key,
        dialogTitleRes = R.string.euro_key_dialog_title,
        successMessageRes = R.string.success_euro_key_updated,
        dbColumnRoom = "euroKey",
        dbColumnApi = "euro_key"
    ),
}

fun Place.toClusterItem(zIndex: Float? = null): PlaceClusterItem =
    PlaceClusterItem(
        place = this,
        zIndex = zIndex
    )
