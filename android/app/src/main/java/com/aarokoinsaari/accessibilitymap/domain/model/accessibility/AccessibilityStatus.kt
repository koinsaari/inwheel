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

package com.aarokoinsaari.accessibilitymap.domain.model.accessibility

/**
 * This enum represents the accessibility level of a place (or a feature of it), mostly from a
 * wheelchair user's perspective. To standardize the evaluation of accessibility, it is categorized
 * into three main levels, which are defined as follows:
 *
 * - FULLY_ACCESSIBLE:
 *   A person in a wheelchair can independently access and use the place and all of its facilities
 *   without assistance. This means, for example, that there is an accessible entrance without steps
 *   (or with a ramp not too steep), doors wide enough for a wheelchair, accessible restrooms, and
 *   elevators if there are multiple floors.
 *
 * - LIMITED_ACCESSIBILITY:
 *   A person in a wheelchair might require assistance to access certain parts of the place or use
 *   certain facilities. For example, there may be one or more steps at the entrance without a ramp,
 *   narrow doorways, or restrooms that are not fully accessible. Even if it is not ideal, the place
 *   can still be used with some limitations or with the help of an assistant by wheelchair user.
 *
 * - NOT_ACCESSIBLE:
 *   A person in a wheelchair cannot access the place at all. This means that the place has
 *   significant barriers that prevent access, like multiple stairs at the entrance with no ramp,
 *   very narrow doorways, no accessible restrooms, or lack of an elevator when needed to access
 *   other floors. Even with assistance, a person in a wheelchair would not be able to access or
 *   use the place effectively. Note that even if the place has other accessible features, like
 *   restrooms or elevators, it will still be considered NOT_ACCESSIBLE if the only way in involves,
 *   for example, 30 steps without a ramp or any other solution. A wheelchair user wouldn’t be able
 *   to enter the place in the first place, even with assistance, so that makes those other
 *   accessible features irrelevant.
 *
 * - UNKNOWN:
 *   The accessibility status of the place is unknown due to insufficient data or missing
 *   information. Without adequate details about the place's accessibility features, we cannot
 *   determine its suitability for wheelchair users. Unless absolutely sure, UNKNOWN should be
 *   preferred instead of flagging place without adequate data as NOT_ACCESSIBLE.
 *
 * Note: This is not an official definition of accessibility standards. It is a simplified
 * categorization used in the app to help indicate how accessible a place may be for wheelchair
 * users. For official regulations and detailed guidelines on accessibility, please refer to
 * official sources, like the ADA etc.
 */
enum class AccessibilityStatus {
    FULLY_ACCESSIBLE,
    LIMITED_ACCESSIBILITY,
    NOT_ACCESSIBLE,
    UNKNOWN;
}
