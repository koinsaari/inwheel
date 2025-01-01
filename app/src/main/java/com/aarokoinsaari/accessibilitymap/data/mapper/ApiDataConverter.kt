/*
 * Copyright (c) 2024 Aaro Koinsaari
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

package com.aarokoinsaari.accessibilitymap.data.mapper

import com.aarokoinsaari.accessibilitymap.data.remote.ApiMapMarker
import com.aarokoinsaari.accessibilitymap.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.PlaceCategory.Companion.mapApiTagToCategory
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.ElevatorInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceDoor
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceSteps
import com.aarokoinsaari.accessibilitymap.model.accessibility.MiscellaneousInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.ParkingInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.ParkingInfo.ParkingType
import com.aarokoinsaari.accessibilitymap.model.accessibility.RestroomInfo
import kotlin.text.lowercase

@Suppress("TooManyFunctions")
object ApiDataConverter {
    fun convertMapMarkersToPlace(apiMarker: ApiMapMarker): Place? {
        val tags = apiMarker.tags ?: return null
        val amenity = tags["amenity"] ?: "default"
        val category = mapApiTagToCategory(amenity)

        return Place(
            id = apiMarker.id,
            name = tags["name"] ?: category.defaultName,
            category = category,
            lat = apiMarker.lat,
            lon = apiMarker.lon,
            tags = tags,
            accessibility = parseAccessibilityInfo(tags),
            address = tags["addr:street"],
            contactInfo = parseContactInfo(tags)
        )
    }

    private fun parseContactInfo(map: Map<String, String>): ContactInfo? =
        ContactInfo(
            email = map["contact:email"],
            phone = map["contact:phone"],
            website = map["contact:website"]
        )

    // High level accessibility info
    private fun parseAccessibilityInfo(tags: Map<String, String>): AccessibilityInfo =
        AccessibilityInfo(
            entranceInfo = parseEntranceInfo(tags),
            restroomInfo = parseRestroomInfo(tags),
            parkingInfo = parseParkingInfo(tags),
            miscInfo = parseFloorInfo(tags),
            additionalInfo = tags["wheelchair:description"] ?: tags["description"]
        )

    private fun parseEntranceInfo(tags: Map<String, String>): EntranceInfo? {
        val steps = parseEntranceSteps(tags)
        val door = parseEntranceDoor(tags)
        val additional = tags["entrance:description"]

        if (steps == null && door == null && additional.isNullOrEmpty()) {
            return null
        }
        return EntranceInfo(
            stepsInfo = steps,
            doorInfo = door,
            additionalInfo = additional
        )
    }

    private fun parseEntranceSteps(tags: Map<String, String>): EntranceSteps? {
        val stepCount = tags["entrance:step_count"]?.trim()?.toIntOrNull()
        val hasStairs = stepCount?.let { it > 0 }
        val hasRamp = (tags["ramp"]?.parseAccessibility() == true ||
                tags["ramp:wheelchair"]?.trim()?.lowercase() == "yes")
        val rampSteepness = null // TODO
        val hasElevator = (tags["entrance:elevator"]?.parseAccessibility() == true ||
                tags["wheelchair:elevator"]?.parseAccessibility() == true)

        if (stepCount == null && !hasRamp && !hasElevator) {
            return null
        }

        return EntranceSteps(
            hasStairs = hasStairs,
            stepCount = stepCount,
            hasRamp = hasRamp,
            rampSteepness = rampSteepness,
            hasElevator = hasElevator
        )
    }

    private fun parseEntranceDoor(tags: Map<String, String>): EntranceDoor? {
        val isDoorWideEnough = tags["entrance:width"]
            ?.toMetersOrNull()
            ?.let { it >= 0.9 }
        val isDoorAutomatic = tags["automatic_door"]?.parseAccessibility()

        if (isDoorWideEnough == null && isDoorAutomatic == null) {
            return null
        }

        return EntranceDoor(
            isDoorWideEnough = isDoorWideEnough,
            isDoorAutomatic = isDoorAutomatic
        )
    }

    private fun parseRestroomInfo(tags: Map<String, String>): RestroomInfo? =
        RestroomInfo(
            hasGrabRails = tags["toilets:wheelchair:grab_rails"]?.parseAccessibility(),
            isDoorWideEnough = tags["toilets:wheelchair:door_width"]?.toMetersOrNull()
                ?.let { it >= 0.9 },
            isLargeEnough = tags["toilets:wheelchair:turning_circle"]?.trim()?.lowercase() == "yes"
                    || tags["wheelchair:turning_circle"]?.trim()?.lowercase() == "yes",
            hasEmergencyAlarm = null, // Does not seem to be available in OSM
            euroKey = tags["centralkey"]?.trim()?.lowercase() == "eurokey",
            additionalInfo = tags["toilets:description"]
        )

    private fun parseParkingInfo(tags: Map<String, String>): ParkingInfo? {
        // Sometimes only details are given and not "elevator"="yes"
        val hasElevator = tags["elevator"]?.trim()?.lowercase() == "yes" ||
                tags.keys.any { it.startsWith("elevator:") }

        return ParkingInfo(
            spotCount = tags["capacity:disabled"]?.trim()?.toIntOrNull(),
            hasAccessibleSpots = tags["capacity:disabled"]?.parseAccessibility() == true ||
                    tags["parking_space"]?.trim()?.lowercase() == "disabled",
            hasSmoothSurface = tags["surface"]?.trim()?.lowercase() in setOf(
                "asphalt",
                "concrete",
                "paved",
                "paving_stones",
                "concrete:plates",
                "concrete:lanes",
                "bricks",
                "wood",
                "metal"
            ),
            parkingType = tags["parking"]?.toParkingType(),
            hasElevator = hasElevator,
            elevatorInfo = if (hasElevator == true) parseElevatorInfo(tags) else null,
            additionalInfo = tags["parking:description"]
        )
    }

    private fun parseFloorInfo(tags: Map<String, String>): MiscellaneousInfo? {
        val hasElevator = tags["elevator"]?.trim()?.lowercase() == "yes" ||
                tags.keys.any { it.startsWith("elevator:") }

        return MiscellaneousInfo(
            level = tags["building:levels"]?.toIntOrNull(),
            hasElevator = hasElevator,
            elevatorInfo = if (hasElevator == true) parseElevatorInfo(tags) else null,
            additionalInfo = tags["building:description"]
        )
    }

    private fun parseElevatorInfo(tags: Map<String, String>): ElevatorInfo? =
        ElevatorInfo(  // nulls are not available in OSM
            isAvailable = null,
            isSpaciousEnough = checkElevatorWidthAndDepth(tags),
            hasBrailleButtons = null,
            hasAudioAnnouncements = null,
            additionalInfo = tags["elevator:description"]
        )

    private fun checkElevatorWidthAndDepth(tags: Map<String, String>): Boolean? {
        val width = tags["elevator:width"]?.toMetersOrNull()
        val depth = tags["elevator:depth"]?.toMetersOrNull()

        if (width == null || depth == null) return null
        return width >= 1.1 && depth >= 1.3 // See: https://www.kone.co.uk/tools-downloads/codes-and-standards/en81-70-compliant-solutions
    }

    private fun String.toMetersOrNull(): Double? =
        this.trim()
            .replace("[^\\d.,]".toRegex(), "")  // Removes all not-numeric characters
            .replace(",", ".")
            .toDoubleOrNull()?.let { value ->
                when {
                    this.contains("cm", ignoreCase = true) -> value / 100
                    this.contains("m", ignoreCase = true) -> value
                    value > 10 -> value / 100  // Unlikely that entrance width is over 10m so assume as cm
                    else -> value
                }
            }

    private fun String.toParkingType(): ParkingType? =
        when (this.trim().lowercase()) {
            "surface" -> ParkingType.SURFACE
            "underground" -> ParkingType.UNDERGROUND
            "multi-storey", "multi_storey", "multistorey" -> ParkingType.MULTI_STOREY
            "rooftop" -> ParkingType.ROOFTOP
            else -> null
        }

    private fun String.parseAccessibility(): Boolean =
        this.trim().lowercase() == "yes" || this.lowercase() == "wheelchair" ||
                this.toIntOrNull()?.let { it > 0 } == true
}
