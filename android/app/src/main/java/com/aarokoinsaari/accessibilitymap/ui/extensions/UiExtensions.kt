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

package com.aarokoinsaari.accessibilitymap.ui.extensions

import androidx.compose.ui.graphics.Color
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus.LIMITED_ACCESSIBILITY
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus.NOT_ACCESSIBLE

internal fun AccessibilityStatus?.getStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.yes
        LIMITED_ACCESSIBILITY -> R.string.limited
        NOT_ACCESSIBLE -> R.string.no
        else -> R.string.unknown
    }

internal fun AccessibilityStatus?.getFullStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.wheelchair_access_fully_accessible
        LIMITED_ACCESSIBILITY -> R.string.wheelchair_access_limited_accessibility
        NOT_ACCESSIBLE -> R.string.wheelchair_access_not_accessible
        else -> R.string.wheelchair_access_unknown
    }

internal fun AccessibilityStatus?.getEmojiStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.emoji_checkmark
        LIMITED_ACCESSIBILITY -> R.string.emoji_warning
        NOT_ACCESSIBLE -> R.string.emoji_cross
        else -> R.string.emoji_question
    }

internal fun Boolean?.getStringRes(): Int =
    when (this) {
        true -> R.string.accessibility_status_yes
        false -> R.string.accessibility_status_no
        null -> R.string.accessibility_status_unknown
    }

internal fun Boolean?.getEmojiStringRes(): Int =
    when (this) {
        true -> R.string.emoji_checkmark
        false -> R.string.emoji_cross
        null -> R.string.emoji_question
    }

// TODO: Use MaterialTheme
internal fun AccessibilityStatus?.getAccessibilityStatusColor(): Color =
    when (this) {
        AccessibilityStatus.FULLY_ACCESSIBLE -> Color.Green
        AccessibilityStatus.LIMITED_ACCESSIBILITY -> Color.Yellow
        AccessibilityStatus.NOT_ACCESSIBLE -> Color.Red
        AccessibilityStatus.UNKNOWN -> Color.Gray
        null -> Color.Gray
    }

// TODO
internal fun String.getIconResId(): Int =
    when (this) {
        // Amenity
        "restaurant" -> R.drawable.ic_restaurant
        "cafe" -> R.drawable.ic_cafe
        "pharmacy" -> R.drawable.ic_pharmacy
        "hospital" -> R.drawable.ic_hospital
        "parking" -> R.drawable.ic_parking_area
        "toilets" -> R.drawable.ic_wc
        "supermarket" -> R.drawable.ic_grocery_store

        else -> R.drawable.ic_default_marker
    }
