package com.aarokoinsaari.accessibilitymap.ui.extensions

import androidx.compose.ui.graphics.Color
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.model.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.model.AccessibilityStatus.LIMITED_ACCESSIBILITY
import com.aarokoinsaari.accessibilitymap.model.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.model.AccessibilityStatus.UNKNOWN

fun AccessibilityStatus?.getAccessibilityStatusShortStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.yes
        LIMITED_ACCESSIBILITY -> R.string.limited
        NOT_ACCESSIBLE -> R.string.no
        else -> R.string.unknown
    }

fun AccessibilityStatus?.getAccessibilityStatusStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.wheelchair_access_fully_accessible
        LIMITED_ACCESSIBILITY -> R.string.wheelchair_access_limited_accessibility
        NOT_ACCESSIBLE -> R.string.wheelchair_access_not_accessible
        else -> R.string.wheelchair_access_unknown
    }

fun AccessibilityStatus.getAccessibilityColor(): Color =
    when (this) { // TODO: Change to MaterialTheme
        FULLY_ACCESSIBLE -> Color.Green
        LIMITED_ACCESSIBILITY -> Color.Yellow
        NOT_ACCESSIBLE -> Color.Red
        UNKNOWN -> Color.Gray
    }

fun Boolean?.getAccessibilityStatusBoolean(): Int =
    when (this) {
        true -> R.string.accessibility_status_yes
        false -> R.string.accessibility_status_no
        null -> R.string.accessibility_status_unknown
    }
