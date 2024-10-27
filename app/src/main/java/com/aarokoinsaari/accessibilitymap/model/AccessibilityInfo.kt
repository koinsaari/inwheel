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

data class AccessibilityInfo(
    val entranceInfo: EntranceInfo? = null,
    val restroomInfo: RestroomInfo? = null,
    val parkingInfo: ParkingInfo? = null,
    val floorInfo: FloorInfo? = null,
    val additionalInfo: String? = null
) {
    /**
     * This determines the general accessibility of a place. Note that the entrance has the most
     * weight evaluating the accessibility because if a person in a wheelchair can't even enter
     * the place, the rest of the details (like restrooms or elevators) don't really matter.
     * See more details about determining the accessibility status in AccessibilityStatus class.
     */
    fun determineGeneralAccessibilityStatus(): AccessibilityStatus {
        val entranceStatus = entranceInfo?.determineAccessibilityStatus()
            ?: AccessibilityStatus.UNKNOWN
        if (entranceStatus != AccessibilityStatus.FULLY_ACCESSIBLE) return entranceStatus

        val statuses = listOf(
            restroomInfo?.determineAccessibilityStatus(),
            floorInfo?.determineAccessibilityStatus(),
            parkingInfo?.determineAccessibilityStatus()
        )

        return when {
            statuses.any {
                it == AccessibilityStatus.NOT_ACCESSIBLE ||
                        it == AccessibilityStatus.LIMITED_ACCESSIBILITY
            } ->
                AccessibilityStatus.LIMITED_ACCESSIBILITY

            statuses.any { it == AccessibilityStatus.UNKNOWN } ->
                AccessibilityStatus.UNKNOWN

            else -> AccessibilityStatus.FULLY_ACCESSIBLE
        }
    }
}

/**
 * TODO:
 * - get rid of suppressed warnings, write better code
 * - improve the evaluations in general. These should be tested after with some unit tests because
 *   there are so many different cases
 */

data class EntranceInfo(
    val hasRamp: Boolean? = null,
    val notTooSteepEntrance: Boolean? = null,
    val stepCount: Int? = null,
    val isDoorWide: Boolean? = null, // Over 90 cm which is based on ADA recommendations (36 inches)
    val hasAutomaticDoor: Boolean? = null,
    val additionalInfo: String? = null
) {
    // TODO: what if automatic door?
    @Suppress("CyclomaticComplexMethod")
    fun determineAccessibilityStatus(): AccessibilityStatus =
        when {
            isDoorWide == false ->
                AccessibilityStatus.NOT_ACCESSIBLE

            stepCount == 0 && isDoorWide == true ->
                AccessibilityStatus.FULLY_ACCESSIBLE

            isDoorWide == null ->
                AccessibilityStatus.UNKNOWN

            (stepCount == null || hasRamp == null) && isDoorWide == true ->
                AccessibilityStatus.UNKNOWN

            hasRamp == true && isDoorWide == true ->
                AccessibilityStatus.FULLY_ACCESSIBLE

            hasRamp == false && stepCount != null && stepCount > 1 ->
                AccessibilityStatus.NOT_ACCESSIBLE

            hasRamp == false && stepCount in 0..1 && isDoorWide == true ->
                AccessibilityStatus.LIMITED_ACCESSIBILITY

            hasRamp == false && stepCount == 0 &&
                    notTooSteepEntrance == true &&
                    isDoorWide == true -> AccessibilityStatus.FULLY_ACCESSIBLE

            else -> AccessibilityStatus.UNKNOWN
        }
}

data class RestroomInfo(
    val hasGrabRails: Boolean? = null,
    val isDoorWideEnough: Boolean? = null,
    val isLargeEnough: Boolean? = null,
    val hasEmergencyAlarm: Boolean? = null,
    val euroKey: Boolean? = null,
    val additionalInfo: String? = null
) {
    fun determineAccessibilityStatus(): AccessibilityStatus =
        when {
            isDoorWideEnough == null && hasGrabRails == null -> AccessibilityStatus.UNKNOWN
            isDoorWideEnough == false || hasGrabRails == false -> AccessibilityStatus.NOT_ACCESSIBLE
            isDoorWideEnough == true && hasGrabRails == true -> AccessibilityStatus.FULLY_ACCESSIBLE
            else -> AccessibilityStatus.LIMITED_ACCESSIBILITY
        }
}

data class ParkingInfo(
    val hasAccessibleSpots: Boolean? = null,
    val spotCount: Int? = null,
    val parkingType: ParkingType? = null,
    val hasSmoothSurface: Boolean? = null,
    val hasElevator: Boolean? = null,
    val elevatorInfo: ElevatorInfo? = null,
    val additionalInfo: String? = null
) {
    enum class ParkingType {
        SURFACE,
        UNDERGROUND,
        MULTI_STOREY,
        ROOFTOP
    }

    @Suppress("CyclomaticComplexMethod")
    fun determineAccessibilityStatus(): AccessibilityStatus =
        when {
            (hasAccessibleSpots == null && spotCount == null) || hasSmoothSurface == null ->
                AccessibilityStatus.UNKNOWN

            hasAccessibleSpots == false || spotCount == 0 -> AccessibilityStatus.NOT_ACCESSIBLE
            hasSmoothSurface == false -> AccessibilityStatus.LIMITED_ACCESSIBILITY
            parkingType != ParkingType.SURFACE -> {
                when (hasElevator) {
                    true -> {
                        val elevatorStatus = elevatorInfo?.determineAccessibilityStatus()
                        when (elevatorStatus) {
                            AccessibilityStatus.FULLY_ACCESSIBLE ->
                                AccessibilityStatus.FULLY_ACCESSIBLE

                            AccessibilityStatus.LIMITED_ACCESSIBILITY ->
                                AccessibilityStatus.LIMITED_ACCESSIBILITY

                            AccessibilityStatus.NOT_ACCESSIBLE ->
                                AccessibilityStatus.LIMITED_ACCESSIBILITY

                            else -> AccessibilityStatus.UNKNOWN
                        }
                    }

                    false -> AccessibilityStatus.NOT_ACCESSIBLE
                    null -> AccessibilityStatus.UNKNOWN
                }
            }

            hasAccessibleSpots == true || (spotCount != null && spotCount > 0) ->
                AccessibilityStatus.FULLY_ACCESSIBLE

            else -> AccessibilityStatus.UNKNOWN
        }
}

data class FloorInfo(
    val level: Int? = null,
    val hasElevator: Boolean? = null,
    val elevatorInfo: ElevatorInfo? = null,
    val additionalInfo: String? = null
) {
    fun determineAccessibilityStatus(): AccessibilityStatus =
        when {
            level == 0 -> AccessibilityStatus.FULLY_ACCESSIBLE
            level == null -> AccessibilityStatus.UNKNOWN
            hasElevator == false -> AccessibilityStatus.NOT_ACCESSIBLE
            hasElevator == true && elevatorInfo != null ->
                elevatorInfo.determineAccessibilityStatus()

            hasElevator == true && elevatorInfo == null -> AccessibilityStatus.UNKNOWN
            else -> AccessibilityStatus.UNKNOWN
        }
}

data class ElevatorInfo(
    val isAvailable: Boolean? = null,
    val isSpaciousEnough: Boolean? = null,
    val hasBrailleButtons: Boolean? = null,
    val hasAudioAnnouncements: Boolean? = null,
    val additionalInfo: String? = null
) {
    fun determineAccessibilityStatus(): AccessibilityStatus =
        when {
            isAvailable == null -> AccessibilityStatus.UNKNOWN
            isAvailable == false -> AccessibilityStatus.NOT_ACCESSIBLE
            isSpaciousEnough == false -> AccessibilityStatus.LIMITED_ACCESSIBILITY
            hasBrailleButtons == false || hasAudioAnnouncements == false ->
                AccessibilityStatus.LIMITED_ACCESSIBILITY

            hasBrailleButtons == null || hasAudioAnnouncements == null ->
                AccessibilityStatus.UNKNOWN

            isSpaciousEnough == true && hasBrailleButtons == true &&
                    hasAudioAnnouncements == true -> AccessibilityStatus.FULLY_ACCESSIBLE

            else -> AccessibilityStatus.UNKNOWN
        }
}
