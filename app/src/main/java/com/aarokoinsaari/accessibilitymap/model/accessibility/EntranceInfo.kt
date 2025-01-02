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
     * If one part fails (like too many steps or a narrow door), the entire entrance
     * is considered not accessible or unknown.
     */
    fun determineAccessibilityStatus(): AccessibilityStatus {
        val stepsStatus = determineStepsAccessibilityStatus(stepsInfo)
        val doorStatus = determineDoorAccessibilityStatus(doorInfo)
        return combineStepAndDoorStatus(stepsStatus, doorStatus)
    }

    /**
     * Checks how the entrance handles stairs, ramps, and elevators:
     * - No stairs or an elevator => fully accessible from the steps perspective
     * - Ramp can downgrade or upgrade depending on steepness
     * - Multiple steps (no ramp/elevator) => not accessible
     * - One step => limited (though could refine further if we want to check step height in the future)
     */
    @Suppress("CyclomaticComplexMethod")
    private fun determineStepsAccessibilityStatus(steps: EntranceSteps?): AccessibilityStatus =
        when {
            steps == null ->
                AccessibilityStatus.UNKNOWN

            steps.hasStairs == false ->
                AccessibilityStatus.FULLY_ACCESSIBLE

            steps.hasStairs == true && steps.hasElevator == true ->
                AccessibilityStatus.FULLY_ACCESSIBLE

            steps.hasStairs == true && steps.hasElevator == false && steps.hasRamp == true ->
                when (steps.rampSteepness) {
                    RampSteepness.STEEP -> AccessibilityStatus.FULLY_ACCESSIBLE
                    RampSteepness.MEDIUM -> AccessibilityStatus.LIMITED_ACCESSIBILITY
                    RampSteepness.SHALLOW -> AccessibilityStatus.NOT_ACCESSIBLE
                    null -> AccessibilityStatus.UNKNOWN
                }

            steps.hasStairs == true && steps.hasElevator == false && steps.hasRamp == false -> {
                val count = steps.stepCount
                when {
                    count == null -> AccessibilityStatus.UNKNOWN
                    count > 1 -> AccessibilityStatus.NOT_ACCESSIBLE
                    count == 0 -> AccessibilityStatus.FULLY_ACCESSIBLE
                    count == 1 -> AccessibilityStatus.LIMITED_ACCESSIBILITY
                    else -> AccessibilityStatus.UNKNOWN
                }
            }

            else ->
                AccessibilityStatus.UNKNOWN
        }

    /**
     * Checks door width and automation:
     * - Narrow => not accessible
     * - Wide & automatic => fully accessible
     * - Wide & not automatic => limited
     */
    private fun determineDoorAccessibilityStatus(door: EntranceDoor?): AccessibilityStatus =
        when {
            door == null ->
                AccessibilityStatus.UNKNOWN

            door.isDoorWideEnough == false ->
                AccessibilityStatus.NOT_ACCESSIBLE

            door.isDoorWideEnough == true && door.isDoorAutomatic == true ->
                AccessibilityStatus.FULLY_ACCESSIBLE

            door.isDoorWideEnough == true && door.isDoorAutomatic == false ->
                AccessibilityStatus.LIMITED_ACCESSIBILITY

            else ->
                AccessibilityStatus.UNKNOWN
        }

    /**
     * If either part is NOT_ACCESSIBLE, final result is NOT_ACCESSIBLE.
     * If either is UNKNOWN, final result is UNKNOWN.
     * Otherwise, we take the more restrictive one (e.g., FULLY vs. LIMITED => LIMITED).
     */
    private fun combineStepAndDoorStatus(
        stepStatus: AccessibilityStatus,
        doorStatus: AccessibilityStatus
    ): AccessibilityStatus = when {
        stepStatus == AccessibilityStatus.NOT_ACCESSIBLE ||
                doorStatus == AccessibilityStatus.NOT_ACCESSIBLE ->
            AccessibilityStatus.NOT_ACCESSIBLE

        stepStatus == AccessibilityStatus.UNKNOWN ||
                doorStatus == AccessibilityStatus.UNKNOWN ->
            AccessibilityStatus.UNKNOWN

        else ->
            if (stepStatus.ordinal > doorStatus.ordinal) stepStatus else doorStatus
    }
}

/**
 * Represents door-related accessibility details.
 * - `isDoorWideEnough` - Whether the door is at least 90 cm wide. This is typically considered the
 *                       minimum for wheelchair access for example in Europe.
 * - `isDoorAutomatic` - Whether the door is automatic, which can impact ease of access.
 */
data class EntranceDoor(
    val isDoorWideEnough: Boolean? = null, // >= 90cm
    val isDoorAutomatic: Boolean? = null
)

/**
 * Represents step-related accessibility details.
 * - `hasStairs` - Indicates whether the entrance has stairs.
 * - `stepCount` - Number of steps at the entrance. More than one step is typically a significant
 *                barrier and should lead directly to NOT_ACCESSIBLE status.
 * - `hasRamp` - Whether there is a ramp as an alternative to stairs.
 * - `rampSteepness` - Describes the steepness of the ramp, which affects accessibility. See [RampSteepness].
 * - `hasElevator` - Indicates whether there is an elevator at the entrance as an alternative to stairs.
 */
data class EntranceSteps(
    val hasStairs: Boolean? = null,
    val stepCount: Int? = null,
    val hasRamp: Boolean? = null,
    val rampSteepness: RampSteepness? = null,
    val hasElevator: Boolean? = null,
)


/**
 * Represents the steepness of a ramp, defined by the slope ratio of rise (height) to run (length).
 * These ratios are used to determine the usability of ramps:
 * - `1:10` means that for every 10 units of horizontal length, the ramp rises by 1 unit.
 * - `1:20` means that for every 20 units of horizontal length, the ramp rises by 1 unit.
 *
 * - `STEEP` - A steep ramp (> 1:10). This slope is too steep for most wheelchair users
 *            without assistance and should be considered NOT_ACCESSIBLE.
 * - `MEDIUM` - A moderately steep ramp (<= 1:10 > 1:20). It is manageable for some users,
 *             but not ideal and might require assistance. Should be considered LIMITED_ACCESSIBILITY.
 * - `SHALLOW` - A shallow ramp (<= 1:20). This slope is easy to navigate and meets accessibility
 *              standards, so it should be considered FULLY_ACCESSIBLE.
 *
 * **NOTE:** These values are based on the UN design manuals. See
 * [here](https://www.un.org/esa/socdev/enable/designm/AD2-01.htm).
 */
enum class RampSteepness {
    STEEP,
    MEDIUM,
    SHALLOW
}
