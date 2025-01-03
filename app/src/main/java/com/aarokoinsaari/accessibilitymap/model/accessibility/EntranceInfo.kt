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

package com.aarokoinsaari.accessibilitymap.model.accessibility

import com.aarokoinsaari.accessibilitymap.utils.extensions.orUnknown

/**
 * Represents an entrance's accessibility details, including steps (stairs, elevator, ramp)
 * and door properties (width, automation). The logic here checks each aspect and decides
 * whether the entrance is fully, partially, or not accessible.
 */
data class EntranceInfo(
    val stepsInfo: EntranceSteps? = null,
    val doorInfo: EntranceDoor? = null,
    val additionalInfo: String? = null
) {

    /**
     * Combines the results of step and door checks.
     * If one part fails the entire entrance is considered not accessible or unknown.
     */
    fun determineAccessibilityStatus(): AccessibilityStatus =
        combineStepAndDoorStatus(
            stepsInfo?.determineAccessibilityStatus(),
            doorInfo?.determineAccessibilityStatus()
        )

    /**
     * If either part is NOT_ACCESSIBLE, final result is NOT_ACCESSIBLE.
     * If either is UNKNOWN, final result is UNKNOWN.
     * Otherwise, we take the more restrictive one (e.g., FULLY vs. LIMITED => LIMITED).
     */
    private fun combineStepAndDoorStatus(
        stepStatus: AccessibilityStatus?,
        doorStatus: AccessibilityStatus?
    ): AccessibilityStatus = when {
        (stepStatus == null || stepStatus == AccessibilityStatus.UNKNOWN) ||
                (doorStatus == null || doorStatus == AccessibilityStatus.UNKNOWN) ->
            AccessibilityStatus.UNKNOWN

        stepStatus == AccessibilityStatus.NOT_ACCESSIBLE ||
                doorStatus == AccessibilityStatus.NOT_ACCESSIBLE ->
            AccessibilityStatus.NOT_ACCESSIBLE

        else -> if (stepStatus.severity > doorStatus.severity) stepStatus else doorStatus
    }
}

/**
 * Represents door-related accessibility details.
 *
 * `doorOpening` - Indicates the accessibility of the door based on its width. `FULLY_ACCESSIBLE`
 *                 means the door is at least 85 cm wide (recommendation for minimum width in
 *                 Finland is used). `LIMITED_ACCESSIBILITY` means door is between 80 cm (about 32 inches, ADA
 *                 recommendation) and 85 cm wide. This width may accommodate some wheelchair users
 *                 but could pose challenges in tight spaces or for larger wheelchairs or mobility devices.
 *                 `NOT_ACCESSIBLE` means the door is less than 80 cm wide, which does not meet most
 *                 accessibility standards and is typically too narrow for wheelchair users.
 *
 * `automaticDoor` - Indicates whether the door is automatic.
 *
 * **NOTE:** These values are based on the [UN design manuals](https://www.un.org/esa/socdev/enable/designm/AD2-01.htm)
 *           and The Finnish Association of People with Physical Disabilities [recommendations](https://www.invalidiliitto.fi/esteettomyys/ulkoalue/kulkuvayla)
 */
data class EntranceDoor(
    val doorOpening: AccessibilityStatus? = null,
    val automaticDoor: Boolean? = null
) {
    /**
     * Checks door width and automation:
     * - Narrow => not accessible
     * - Wide & automatic => fully accessible
     * - Wide & not automatic => limited
     */
    fun determineAccessibilityStatus(): AccessibilityStatus {
        val widthStatus = doorOpening ?: AccessibilityStatus.UNKNOWN

        return when (widthStatus) {
            AccessibilityStatus.NOT_ACCESSIBLE -> AccessibilityStatus.NOT_ACCESSIBLE
            AccessibilityStatus.LIMITED_ACCESSIBILITY -> AccessibilityStatus.LIMITED_ACCESSIBILITY
            AccessibilityStatus.UNKNOWN -> AccessibilityStatus.UNKNOWN
            else -> {
                when (automaticDoor) {
                    true -> AccessibilityStatus.FULLY_ACCESSIBLE
                    false -> AccessibilityStatus.LIMITED_ACCESSIBILITY
                    null -> AccessibilityStatus.UNKNOWN
                }
            }
        }
    }
}

/**
 * Represents step-related accessibility details.
 *
 * - `hasStairs` - Indicates whether the entrance has stairs.
 * - `stepCount` - Number of steps at the entrance. More than one step is typically a significant
 *                 barrier and should lead directly to `NOT_ACCESSIBLE` status. One step, if it is max
 *                 7 cm high, can be considered `LIMITED_ACCESSIBILITY`.
 *
 * - `hasRamp` - Whether there is a ramp as an alternative to stairs.
 *               `FULLY_ACCESSIBLE` means the ramp is easy to navigate without assistance (slope is < 1:20).
 *               `LIMITED_ACCESSIBILITY` means the ramp is manageable for some users, but not ideal
 *               and might require assistance (slope is > 1:20 <= 1:10)
 *               `NOT_ACCESSIBLE` means steep ramp where the slope is too steep for most wheelchair
 *               users (> 1:10).
 *
 * - `hasElevator` - Whether there is an elevator at the entrance as an alternative to stairs.
 *                   `FULLY_ACCESSIBLE` should be used if there is an elevator big enough to accommodate
 *                   a wheelchair, and that it works properly.
 *                   `LIMITED_ACCESSIBILITY` should be used if there is an elevator but it is not either
 *                   big enough to accommodate a wheelchair user, or not working properly.
 *                   `NOT_ACCESSIBILITY` should be used when there is no elevator at all, or it is
 *                   completely inaccessible for a person in a wheelchair.
 *
 * **NOTE:** These values are based on the UN design manuals. See
 *           [here](https://www.un.org/esa/socdev/enable/designm/AD2-01.htm).
 */
data class EntranceSteps(
    val hasStairs: Boolean? = null,
    val stepCount: Int? = null,
    val ramp: AccessibilityStatus? = AccessibilityStatus.UNKNOWN,
    val elevator: AccessibilityStatus? = AccessibilityStatus.UNKNOWN,
) {
    /**
     * Checks how the entrance handles stairs, ramps, and elevators:
     * - No stairs or a functioning elevator => fully/limited accessible (depending on the elevator accessibility)
     * - Ramp can downgrade or upgrade depending on accessibility status (referencing to ramp steepness)
     * - Multiple steps (no ramp/elevator) => not accessible
     * - One step => limited (though could refine further if we want to check step height in the future)
     */
    fun determineAccessibilityStatus(): AccessibilityStatus {
        val elevatorStatus = elevator.orUnknown()
        val rampStatus = ramp.orUnknown()

        return when {
            hasStairs == false -> AccessibilityStatus.FULLY_ACCESSIBLE
            elevatorStatus == AccessibilityStatus.FULLY_ACCESSIBLE -> AccessibilityStatus.FULLY_ACCESSIBLE
            elevatorStatus == AccessibilityStatus.LIMITED_ACCESSIBILITY -> AccessibilityStatus.LIMITED_ACCESSIBILITY
            elevatorStatus == AccessibilityStatus.UNKNOWN -> AccessibilityStatus.UNKNOWN
            rampStatus == AccessibilityStatus.FULLY_ACCESSIBLE -> AccessibilityStatus.FULLY_ACCESSIBLE
            rampStatus == AccessibilityStatus.LIMITED_ACCESSIBILITY -> AccessibilityStatus.LIMITED_ACCESSIBILITY
            rampStatus == AccessibilityStatus.UNKNOWN -> AccessibilityStatus.UNKNOWN
            else -> stepCount.toStepCountStatus()
        }
    }

    private fun Int?.toStepCountStatus(): AccessibilityStatus = when {
        this == null -> AccessibilityStatus.UNKNOWN
        this > 1 -> AccessibilityStatus.NOT_ACCESSIBLE
        this == 1 -> AccessibilityStatus.LIMITED_ACCESSIBILITY
        this == 0 -> AccessibilityStatus.FULLY_ACCESSIBLE
        else -> AccessibilityStatus.UNKNOWN
    }
}
