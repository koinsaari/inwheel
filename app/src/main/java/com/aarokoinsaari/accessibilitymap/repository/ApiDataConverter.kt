/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.repository

import android.util.Log
import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.EntryAccessibilityStatus
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus
import com.aarokoinsaari.accessibilitymap.network.ApiMapMarkers

object ApiDataConverter {
    fun convertMapMarkersToPlace(apiMarkers: ApiMapMarkers): Place? {
        apiMarkers.tags?.let { tags ->
            val name = tags["name"]
            val type = tags["amenity"]
            if (name != null && type != null) {
                return Place(
                    id = apiMarkers.id,
                    name = name,
                    type = type,
                    lat = apiMarkers.lat,
                    lon = apiMarkers.lon,
                    tags = tags,
                    accessibility = parseAccessibilityInfo(tags)
                )
            }
        }
        return null
    }

    private fun parseAccessibilityInfo(tags: Map<String, String>): AccessibilityInfo =
        AccessibilityInfo(
            wheelchairAccess = parseWheelchairAccessStatus(tags["wheelchair"]),
            entry = parseEntryStatus(tags["entry"]),
            hasAccessibleToilet = tags["toilet:wheelchair"] == "yes",
            hasElevator = tags["elevator"] == "yes",
            additionalInfo = tags["note"]
        )

    private fun parseWheelchairAccessStatus(status: String?): WheelchairAccessStatus =
        when (status?.lowercase()) {
            "yes", "designated" -> WheelchairAccessStatus.FULLY_ACCESSIBLE
            "limited" -> WheelchairAccessStatus.LIMITED_ACCESSIBILITY
            "no" -> WheelchairAccessStatus.NOT_ACCESSIBLE
            else -> WheelchairAccessStatus.UNKNOWN
        }

    private fun parseEntryStatus(status: String?): EntryAccessibilityStatus =
        try {
            if (status != null) {
                EntryAccessibilityStatus.valueOf(
                    status.replace(" ", "_").uppercase()
                )
            } else {
                EntryAccessibilityStatus.UNKNOWN
            }
        } catch (e: IllegalArgumentException) {
            Log.e("ApiDataConverter", "Invalid entry status: $status", e)
            EntryAccessibilityStatus.UNKNOWN
        }
}
