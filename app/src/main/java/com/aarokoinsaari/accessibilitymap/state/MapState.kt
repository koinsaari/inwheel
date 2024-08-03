/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.state

import com.aarokoinsaari.accessibilitymap.model.Place
import org.osmdroid.util.BoundingBox

data class MapState(
    val markers: List<Place> = emptyList(),
    val zoomLevel: Double? = null,
    val currentBbox: BoundingBox? = null,
    val snapshotBbox: BoundingBox? = null,
    val isLoading: Boolean = false
) {
    override fun toString(): String {
        return """
            MapState(
                markers=${markers.size},
                zoomLevel=$zoomLevel,
                isLoading=$isLoading,
                currentBoundingBox=$currentBbox
            )
        """.trimIndent()
    }
}
