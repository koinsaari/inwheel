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

package com.aarokoinsaari.accessibilitymap.model.accessibility

import kotlinx.serialization.Serializable

@Serializable
sealed class AccessibilityInfo {

    @Serializable
    data class GeneralAccessibility(
        val accessibilityStatus: AccessibilityStatus?,
        val indoorAccessibility: AccessibilityStatus?,
        val entrance: EntranceAccessibility?,
        val restroom: RestroomAccessibility?,
        val additionalInfo: String?
    ) : AccessibilityInfo() {

        @Serializable
        data class EntranceAccessibility(
            val accessibilityStatus: AccessibilityStatus?,
            val steps: StepsAccessibility?,
            val door: DoorAccessibility?,
            val additionalInfo: String?
        ) {
            @Serializable
            data class StepsAccessibility(
                val stepCount: Int?,
                val stepHeight: AccessibilityStatus?,
                val ramp: AccessibilityStatus?,
                val lift: AccessibilityStatus?
            )

            @Serializable
            data class DoorAccessibility(
                val doorWidth: AccessibilityStatus?,
                val doorType: String?
            )
        }

        @Serializable
        data class RestroomAccessibility(
            val accessibility: AccessibilityStatus?,
            val doorWidth: AccessibilityStatus?,
            val roomManeuver: AccessibilityStatus?,
            val grabRails: AccessibilityStatus?,
            val toiletSeat: AccessibilityStatus?,
            val emergencyAlarm: AccessibilityStatus?,
            val sink: AccessibilityStatus?,
            val euroKey: Boolean?,
            val accessibleVia: String?,
            val additionalInfo: String?
        )
    }

    @Serializable
    data class ToiletAccessibility(
        val accessibilityStatus: AccessibilityStatus?,
        val doorWidth: AccessibilityStatus?,
        val grabRails: AccessibilityStatus?,
        val toiletSeat: AccessibilityStatus?,
        val emergencyAlarm: AccessibilityStatus?,
        val sink: AccessibilityStatus?,
        val euroKey: Boolean?,
        val additionalInfo: String?
    ): AccessibilityInfo()

    @Serializable
    data class ParkingAccessibility(
        val accessibilityStatus: AccessibilityStatus?,
        val accessibleSpotCount: Int?,
        val surface: String?,
        val parkingType: String?,
        val hasElevator: Boolean?,
        val additionalInfo: String?
    ) : AccessibilityInfo()
}

val AccessibilityInfo?.accessibilityStatus: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.accessibilityStatus
        is AccessibilityInfo.ToiletAccessibility -> this.accessibilityStatus
        is AccessibilityInfo.ParkingAccessibility -> this.accessibilityStatus
        else -> null
    }

val AccessibilityInfo?.indoorAccessibility: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.indoorAccessibility
        else -> null
    }

val AccessibilityInfo?.stepsCount: Int?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.entrance?.steps?.stepCount
        else -> null
    }

val AccessibilityInfo?.stepHeight: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.entrance?.steps?.stepHeight
        else -> null
    }

val AccessibilityInfo?.ramp: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.entrance?.steps?.ramp
        else -> null
    }

val AccessibilityInfo?.lift: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.entrance?.steps?.lift
        else -> null
    }

val AccessibilityInfo?.doorWidth: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.entrance?.door?.doorWidth
        else -> null
    }

val AccessibilityInfo?.doorType: String?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.entrance?.door?.doorType
        else -> null
    }

val AccessibilityInfo?.restroomDoorWidth: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.restroom?.doorWidth
        is AccessibilityInfo.ToiletAccessibility -> this.doorWidth
        else -> null
    }

val AccessibilityInfo?.roomManeuver: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.restroom?.roomManeuver
        else -> null
    }

val AccessibilityInfo?.grabRails: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.restroom?.grabRails
        is AccessibilityInfo.ToiletAccessibility -> this.grabRails
        else -> null
    }

val AccessibilityInfo?.toiletSeat: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.restroom?.toiletSeat
        is AccessibilityInfo.ToiletAccessibility -> this.toiletSeat
        else -> null
    }

val AccessibilityInfo?.emergencyAlarm: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.restroom?.emergencyAlarm
        is AccessibilityInfo.ToiletAccessibility -> this.emergencyAlarm
        else -> null
    }

val AccessibilityInfo?.sink: AccessibilityStatus?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.restroom?.sink
        is AccessibilityInfo.ToiletAccessibility -> this.sink
        else -> null
    }

val AccessibilityInfo?.euroKey: Boolean?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.restroom?.euroKey
        is AccessibilityInfo.ToiletAccessibility -> this.euroKey
        else -> null
    }

val AccessibilityInfo?.accessibleVia: String?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.restroom?.accessibleVia
        else -> null
    }

val AccessibilityInfo?.parkingSpotCount: Int?
    get() = when (this) {
        is AccessibilityInfo.ParkingAccessibility -> this.accessibleSpotCount
        else -> null
    }

val AccessibilityInfo?.parkingSurface: String?
    get() = when (this) {
        is AccessibilityInfo.ParkingAccessibility -> this.surface
        else -> null
    }

val AccessibilityInfo?.parkingType: String?
    get() = when (this) {
        is AccessibilityInfo.ParkingAccessibility -> this.parkingType
        else -> null
    }

val AccessibilityInfo?.parkingElevator: Boolean?
    get() = when (this) {
        is AccessibilityInfo.ParkingAccessibility -> this.hasElevator
        else -> null
    }

val AccessibilityInfo?.additionalInfo: String?
    get() = when (this) {
        is AccessibilityInfo.GeneralAccessibility -> this.additionalInfo
        is AccessibilityInfo.ToiletAccessibility -> this.additionalInfo
        is AccessibilityInfo.ParkingAccessibility -> this.additionalInfo
        else -> null
    }
