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

package com.aarokoinsaari.accessibilitymap.view.extensions

import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.NOT_ACCESSIBLE

internal fun AccessibilityStatus?.getStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.yes
        PARTIALLY_ACCESSIBLE -> R.string.limited
        NOT_ACCESSIBLE -> R.string.no
        else -> R.string.unknown
    }

internal fun AccessibilityStatus?.getFullStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.wheelchair_access_fully_accessible
        PARTIALLY_ACCESSIBLE -> R.string.wheelchair_access_limited_accessibility
        NOT_ACCESSIBLE -> R.string.wheelchair_access_not_accessible
        else -> R.string.wheelchair_access_unknown
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

internal fun AccessibilityStatus?.getAccessibilityStatusMarkerBgDrawableRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.drawable.bg_marker_green
        PARTIALLY_ACCESSIBLE -> R.drawable.bg_marker_yellow
        NOT_ACCESSIBLE -> R.drawable.bg_marker_red
        else -> R.drawable.bg_marker_grey
    }

internal fun AccessibilityStatus?.getAccessibilityStatusIconRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.drawable.ic_accessible_general
        PARTIALLY_ACCESSIBLE -> R.drawable.ic_accessible_limited
        NOT_ACCESSIBLE -> R.drawable.ic_not_accessible
        else -> R.drawable.ic_accessible_general
    }

internal fun AccessibilityStatus?.getAccessibilityStatusContentDescStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.content_desc_fully_accessible
        PARTIALLY_ACCESSIBLE -> R.string.content_desc_partially_accessible
        NOT_ACCESSIBLE -> R.string.content_desc_not_accessible
        else -> R.string.content_desc_unknown_accessibility
    }

internal fun AccessibilityStatus?.getAccessibilityStatusDrawableRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.drawable.accessibility_status_green
        PARTIALLY_ACCESSIBLE -> R.drawable.accessibility_status_yellow
        NOT_ACCESSIBLE -> R.drawable.accessibility_status_red
        else -> R.drawable.accessibility_status_grey
    }
