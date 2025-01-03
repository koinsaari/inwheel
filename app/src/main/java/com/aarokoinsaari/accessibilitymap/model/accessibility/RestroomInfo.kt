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
import com.aarokoinsaari.accessibilitymap.utils.extensions.toAccessibilityStatus

/**
 * Represents a restroom's accessibility details.
 *
 * - `doorWidth` - Is door wide enough for accessibility. The width should be at least 75 cm when fully open.
 * - `roomSpaciousness` - Represents whether the room is spacious enough for a wheelchair. The
 *                        restroom should allow a wheelchair turning radius of at least 150 cm to be fully accessible.
 * - `grabRails` - Rails that are present near the toilet. Rails should be on both sides and steady
 *                 enough to support the user's weight.
 * - `toiletSeat` - The seat height of the toilet. The seat height should be between 45–50 cm
 *                  from the ground to allow for easy transfers.
 * - `sink` - Accessibility of the sink. The sink should be reachable from a seated position (about 80 cm).
 * - `hasEmergencyAlarm` - Emergency alarm system for calling help in emergencies (usually a pull cord
 *                         or string placed near the toilet and close to the floor so users can easily
 *                         reach it even if they have fallen).
 * - `euroKey` - Specifies if the restroom requires a EuroKey for access. This is additional
 *               information and does not directly affect accessibility.
 * - `additionalInfo` - A field for extra notes or specific details about the restroom's
 *                      accessibility features. Does not affect accessibility.
 *
 * Combined, these factors determine whether the restroom is fully or partially accessible,
 * inaccessible, or unknown due to missing information (we should prefer labelling as `UNKNOWN`
 * rather than risk mislabeling the whole restroom as `FULLY_ACCESSIBLE` or `NOT_ACCESSIBLE`).
 *
 * **NOTE:** These values are based on the UN design manuals. See
 *           [here](https://www.un.org/esa/socdev/enable/designm/AD2-10.htm).
 */
data class RestroomInfo(
    val doorWidth: Boolean? = null,
    val roomSpaciousness: AccessibilityStatus? = AccessibilityStatus.UNKNOWN,
    val grabRails: AccessibilityStatus? = AccessibilityStatus.UNKNOWN,
    val toiletSeat: AccessibilityStatus? = AccessibilityStatus.UNKNOWN,
    val sink: AccessibilityStatus? = AccessibilityStatus.UNKNOWN,
    val hasEmergencyAlarm: Boolean? = null,
    val euroKey: Boolean? = null,
    val additionalInfo: String? = null
) {

    /**
     * Determines the overall accessibility status by checking:
     * 1) Door width
     * 2) Room spaciousness
     * 3) Detailed features (grab rails, sink, toilet seat, emergency alarm)
     *
     * Each step can cause the final result to be NOT_ACCESSIBLE, LIMITED_ACCESSIBILITY, UNKNOWN,
     * or (if everything is optimal) FULLY_ACCESSIBLE.
     */
    fun determineAccessibilityStatus(): AccessibilityStatus {
        val doorStatus = doorWidth.toAccessibilityStatus()
        if (doorStatus != AccessibilityStatus.FULLY_ACCESSIBLE) return doorStatus

        val roomStatus = roomSpaciousness.orUnknown()
        if (roomSpaciousness != AccessibilityStatus.FULLY_ACCESSIBLE) return roomStatus

        val detailsStatus = evaluateDetailedStatuses(
            listOf(
                grabRails,
                sink,
                toiletSeat,
                hasEmergencyAlarm.toAccessibilityStatus()
            )
        )
        return detailsStatus
    }

    /**
     * Evaluates the overall accessibility status of a restroom based on the statuses.
     * If all statuses are `UNKNOWN`, it returns `UNKNOWN` to avoid misrepresentation. The toilet seat has
     * higher priority: if it's `NOT_ACCESSIBLE`, the entire restroom is flagged as `NOT_ACCESSIBLE`.
     * Otherwise, the restroom is `LIMITED_ACCESSIBILITY` if any feature is limited or inaccessible,
     * and `FULLY_ACCESSIBLE` only if all features are fully accessible. The reason for flagging as
     * `LIMITED_ACCESSIBILITY`, even if other features (excluding toilet seat) are inaccessible, is
     * that we do not want to mark the entire restroom as `NOT_ACCESSIBLE` if it is missing some,
     * not as critical features, which in this case are sink, emergency alarm and grab rails
     * (although each important).
     *
     * @param statuses A list of feature statuses (`AccessibilityStatus?`).
     * @param toiletSeat The accessibility status of the toilet seat.
     * @return The combined restroom accessibility status.
     */
    private fun evaluateDetailedStatuses(statuses: List<AccessibilityStatus?>): AccessibilityStatus =
        when {
            statuses.all { it == AccessibilityStatus.UNKNOWN } -> AccessibilityStatus.UNKNOWN
            toiletSeat == AccessibilityStatus.NOT_ACCESSIBLE -> AccessibilityStatus.NOT_ACCESSIBLE
            statuses.any {
                it == AccessibilityStatus.LIMITED_ACCESSIBILITY ||
                        it == AccessibilityStatus.NOT_ACCESSIBLE ||
                        it == AccessibilityStatus.UNKNOWN
            } -> AccessibilityStatus.LIMITED_ACCESSIBILITY

            else -> AccessibilityStatus.FULLY_ACCESSIBLE
        }
}
