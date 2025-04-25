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

package com.aarokoinsaari.inwheel.view.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus

/**
 * Returns the appropriate content description string resource based on accessibility status.
 */
@StringRes
fun AccessibilityStatus?.getAccessibilityStatusContentDescStringRes(): Int =
    when (this) {
        AccessibilityStatus.FULLY_ACCESSIBLE -> R.string.content_desc_fully_accessible
        AccessibilityStatus.PARTIALLY_ACCESSIBLE -> R.string.content_desc_partially_accessible
        AccessibilityStatus.NOT_ACCESSIBLE -> R.string.content_desc_not_accessible
        else -> R.string.content_desc_unknown_accessibility
    }

/**
 * Returns the appropriate drawable resource based on accessibility status.
 */
@DrawableRes
fun AccessibilityStatus?.getAccessibilityStatusDrawableRes(): Int =
    when (this) {
        AccessibilityStatus.FULLY_ACCESSIBLE -> R.drawable.accessibility_status_green
        AccessibilityStatus.PARTIALLY_ACCESSIBLE -> R.drawable.accessibility_status_yellow
        AccessibilityStatus.NOT_ACCESSIBLE -> R.drawable.accessibility_status_red
        else -> R.drawable.accessibility_status_grey
    }
