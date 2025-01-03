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

package com.aarokoinsaari.accessibilitymap.data.mapper.overpass

import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.model.accessibility.ParkingInfo.ParkingType

/**
 * Parses multiple Overpass tags into an [AccessibilityStatus].
 * Tries each key in order and returns the first non-null, valid status.
 *
 * @param keys The list of keys to parse.
 * @return The corresponding AccessibilityStatus, or UNKNOWN if none are valid.
 */
internal fun Map<String, String>.parseOsmAccessibilityStatus(vararg keys: String): AccessibilityStatus =
    keys.mapNotNull { this[it]?.trim()?.lowercase()?.parseOsmAccessibilityStatus() }
        .firstOrNull() ?: AccessibilityStatus.UNKNOWN

/**
 * Parses the value of an Overpass tag (e.g., elevator=yes) into an [AccessibilityStatus].
 */
internal fun String?.parseOsmAccessibilityStatus(): AccessibilityStatus = when {
    this == null -> AccessibilityStatus.UNKNOWN
    this.trim().lowercase() == "wheelchair" -> AccessibilityStatus.FULLY_ACCESSIBLE
    this.trim().lowercase() == "yes" -> AccessibilityStatus.FULLY_ACCESSIBLE
    this.trim().lowercase() == "limited" -> AccessibilityStatus.LIMITED_ACCESSIBILITY
    this.trim().lowercase() == "no" -> AccessibilityStatus.NOT_ACCESSIBLE
    this.trim().lowercase() == "unknown" -> AccessibilityStatus.UNKNOWN
    else -> AccessibilityStatus.UNKNOWN
}

/**
 * Parses a string to a double representing meters based on Overpass data.
 */
internal fun String.parseOsmMeters(): Double? =
    this.trim()
        .replace("[^\\d.,]".toRegex(), "")  // Removes all non-numeric characters
        .replace(",", ".")
        .toDoubleOrNull()?.let { value ->
            when {
                this.contains("cm", ignoreCase = true) -> value / 100
                this.contains("m", ignoreCase = true) -> value
                value > 10 -> value / 100  // Unlikely that entrance width is over 10m so assume as cm
                else -> value
            }
        }

internal fun String.parseOsmParkingType(): ParkingType? =
    when (this.trim().lowercase()) {
        "surface" -> ParkingType.SURFACE
        "underground" -> ParkingType.UNDERGROUND
        "multi-storey", "multi_storey", "multistorey" -> ParkingType.MULTI_STOREY
        "rooftop" -> ParkingType.ROOFTOP
        else -> null
    }

internal fun String.parseOsmAutomaticDoorBoolean(): Boolean? =
    when (this.trim().lowercase()) {
        "yes", "button", "motion", "continuous", "slowdown_button" -> true
        "no" -> false
        else -> null
    }

internal fun Double?.parseOsmDoorWidthAccessibilityStatus(): AccessibilityStatus = when {
    this == null -> AccessibilityStatus.UNKNOWN
    this >= 0.85 -> AccessibilityStatus.FULLY_ACCESSIBLE
    this >= 0.80 -> AccessibilityStatus.LIMITED_ACCESSIBILITY
    else -> AccessibilityStatus.NOT_ACCESSIBLE
}

internal fun Double?.parseOsmDoorWidthAccessibilityBoolean(): Boolean? = when {
    this == null -> null
    this >= 0.80 -> true
    else -> false
}
