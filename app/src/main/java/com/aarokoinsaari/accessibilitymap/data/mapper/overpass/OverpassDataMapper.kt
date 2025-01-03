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

package com.aarokoinsaari.accessibilitymap.data.mapper.overpass

import com.aarokoinsaari.accessibilitymap.data.remote.overpass.OverpassElement
import com.aarokoinsaari.accessibilitymap.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.PlaceCategory.Companion.mapOverpassTagToCategory
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.model.accessibility.ElevatorInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceDoor
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceSteps
import com.aarokoinsaari.accessibilitymap.model.accessibility.MiscellaneousInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.ParkingInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.RestroomInfo
import kotlin.text.lowercase

object OverpassDataMapper {
    fun convertElementToPlace(element: OverpassElement): Place? {
        val tags = element.tags ?: return null
        val amenity = tags["amenity"] ?: "default"
        val category = mapOverpassTagToCategory(amenity)

        return Place(
            id = element.id,
            name = tags["name"] ?: category.defaultName,
            category = category,
            lat = element.lat,
            lon = element.lon,
            tags = tags,
            accessibility = parseAccessibilityInfo(tags),
            address = tags["addr:street"],
            contactInfo = parseContactInfo(tags)
        )
    }

    private fun parseAccessibilityInfo(tags: Map<String, String>): AccessibilityInfo =
        AccessibilityInfo(
            entranceInfo = parseEntranceInfo(tags),
            restroomInfo = parseRestroomInfo(tags),
            parkingInfo = parseParkingInfo(tags),
            miscInfo = parseMiscInfo(tags),
            additionalInfo = tags["wheelchair:description"] ?: tags["description"]
        )

    private fun parseContactInfo(map: Map<String, String>): ContactInfo? =
        ContactInfo(
            email = map["contact:email"],
            phone = map["contact:phone"],
            website = map["contact:website"]
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
        val ramp = tags.parseOsmAccessibilityStatus("ramp", "ramp:wheelchair", "wheelchair:ramp")
        val elevator = tags.parseOsmAccessibilityStatus("entrance:elevator", "wheelchair:elevator")

        if (stepCount == null && ramp == AccessibilityStatus.UNKNOWN && elevator == AccessibilityStatus.UNKNOWN) {
            return null
        }

        return EntranceSteps(
            hasStairs = hasStairs,
            stepCount = stepCount,
            ramp = ramp,
            elevator = elevator
        )
    }

    private fun parseEntranceDoor(tags: Map<String, String>): EntranceDoor? {
        val doorOpening =
            tags["entrance:width"]?.parseOsmMeters().parseOsmDoorWidthAccessibilityStatus()
        val automaticDoor = tags["automatic_door"]?.parseOsmAutomaticDoorBoolean()
            ?: tags["entrance:automatic_door"]?.parseOsmAutomaticDoorBoolean()

        return EntranceDoor(
            doorOpening = doorOpening,
            automaticDoor = automaticDoor
        )
    }

    private fun parseRestroomInfo(tags: Map<String, String>): RestroomInfo? =
        RestroomInfo(
            grabRails = tags["toilets:wheelchair:grab_rails"]?.parseOsmAccessibilityStatus(),
            doorWidth = tags["toilets:wheelchair:door_width"]
                ?.parseOsmMeters()
                .parseOsmDoorWidthAccessibilityBoolean(),
            roomSpaciousness = null, // TODO Not available in OSM
            hasEmergencyAlarm = null, // TODO Does not seem to be available in OSM
            euroKey = tags["centralkey"]?.trim()?.lowercase() == "eurokey",
            additionalInfo = tags["toilets:description"]
        )

    private fun parseParkingInfo(tags: Map<String, String>): ParkingInfo? {
        // Sometimes only details are given and not "elevator"="yes"
        val hasElevator = tags["elevator"]?.trim()?.lowercase() == "yes" ||
                tags.keys.any { it.startsWith("elevator:") }

        return ParkingInfo(
            spotCount = tags["capacity:disabled"]?.trim()?.toIntOrNull(),
            hasAccessibleSpots = tags["capacity:disabled"]?.trim()?.toIntOrNull()
                ?.let { it >= 1 } == true ||
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
            parkingType = tags["parking"]?.parseOsmParkingType(),
            hasElevator = hasElevator,
            elevatorInfo = if (hasElevator == true) parseElevatorInfo(tags) else null,
            additionalInfo = tags["parking:description"]
        )
    }

    private fun parseMiscInfo(tags: Map<String, String>): MiscellaneousInfo? {
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
        val width = tags["elevator:width"]?.parseOsmMeters()
        val depth = tags["elevator:depth"]?.parseOsmMeters()

        if (width == null || depth == null) return null
        return width >= 1.1 && depth >= 1.3 // See: https://www.kone.co.uk/tools-downloads/codes-and-standards/en81-70-compliant-solutions
    }
}
