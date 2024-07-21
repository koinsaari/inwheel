package com.aarokoinsaari.accessibilitymap.intent

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

sealed class MapIntent {
    data class Move(
        val center: GeoPoint,
        val zoomLevel: Double,
        val bbox: BoundingBox
    ) : MapIntent()

    data class LoadMarkers(
        val zoomLevel: Double,
        val center: GeoPoint
    ) : MapIntent()

    data object ClearMarkers : MapIntent()
}
