package com.aarokoinsaari.accessibilitymap.intent

import org.osmdroid.util.GeoPoint

sealed class MapIntent {
    data class LoadMarkers(val zoomLevel: Double, val center: GeoPoint) : MapIntent()
    data object ClearMarkers : MapIntent()
}
