/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class Place(
    @PrimaryKey val id: Long,
    val name: String,
    val type: String,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>?,
    val accessibility: AccessibilityInfo?
)

data class AccessibilityInfo(
    val wheelchairAccess: WheelchairAccessStatus?,
    val entry: EntryAccessibilityStatus?,
    val hasAccessibleToilet: Boolean?,
    val hasElevator: Boolean?,
    val additionalInfo: String?
)
