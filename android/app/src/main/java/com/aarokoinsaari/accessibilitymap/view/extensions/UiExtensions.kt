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
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.NOT_ACCESSIBLE

internal fun AccessibilityStatus?.getAccessibilityStatusMarkerBgDrawableRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.drawable.bg_marker_green
        PARTIALLY_ACCESSIBLE -> R.drawable.bg_marker_yellow
        NOT_ACCESSIBLE -> R.drawable.bg_marker_red
        else -> R.drawable.bg_marker_grey
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
