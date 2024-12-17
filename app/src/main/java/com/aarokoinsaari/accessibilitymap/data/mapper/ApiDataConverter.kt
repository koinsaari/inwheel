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

import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.ElevatorInfo
import com.aarokoinsaari.accessibilitymap.model.EntranceInfo
import com.aarokoinsaari.accessibilitymap.model.FloorInfo
import com.aarokoinsaari.accessibilitymap.model.ParkingInfo
import com.aarokoinsaari.accessibilitymap.model.ParkingInfo.ParkingType
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.RestroomInfo
import com.aarokoinsaari.accessibilitymap.data.remote.ApiMapMarker
import com.aarokoinsaari.accessibilitymap.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.model.PlaceCategory.Companion.mapApiTagToCategory
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

    private fun parseAccessibilityInfo(tags: Map<String, String>): AccessibilityInfo =
        AccessibilityInfo(
            entranceInfo = parseEntranceInfo(tags),
            restroomInfo = parseRestroomInfo(tags),
            parkingInfo = parseParkingInfo(tags),
            floorInfo = parseFloorInfo(tags),
            additionalInfo = tags["wheelchair:description"] ?: tags["description"]
        )

    private fun parseEntranceInfo(tags: Map<String, String>): EntranceInfo? =
        EntranceInfo(
            hasRamp = tags["ramp"]?.parseAccessibility() == true || tags["ramp:wheelchair"]?.trim()
                ?.lowercase() == "yes",
            notTooSteepEntrance = tags["kerb"]?.trim()?.lowercase() in setOf(
                "wheelchair",
                "lowered",
                "flush",
                "no"
            ),
            stepCount = tags["entrance:step_count"]?.trim()?.toIntOrNull(),
            isDoorWide = tags["entrance:width"]?.toMetersOrNull()?.let { it >= 0.9 },
            hasAutomaticDoor = tags["automatic_door"]?.parseAccessibility(),
            additionalInfo = tags["entrance:description"]
        )

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

    private fun parseFloorInfo(tags: Map<String, String>): FloorInfo? {
        val hasElevator = tags["elevator"]?.trim()?.lowercase() == "yes" ||
                tags.keys.any { it.startsWith("elevator:") }

        return FloorInfo(
            level = tags["building:levels"]?.toIntOrNull(),
            hasElevator = hasElevator,
            elevatorInfo = if (hasElevator == true) parseElevatorInfo(tags) else null,
            additionalInfo = tags["building:description"]
        )
    }

    private fun parseElevatorInfo(tags: Map<String, String>): ElevatorInfo? =
        ElevatorInfo(  // The nulls are not available in OSM
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
