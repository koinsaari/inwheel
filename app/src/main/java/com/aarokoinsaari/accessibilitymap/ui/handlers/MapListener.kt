/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.ui.handlers

import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint

class MapListener(
    private val onEvent: (MapIntent) -> Unit
) : MapListener {

    override fun onZoom(event: ZoomEvent?): Boolean {
        event?.let {
            onEvent(
                MapIntent.Move(
                    center = it.source.mapCenter as GeoPoint,
                    zoomLevel = it.zoomLevel,
                    bbox = it.source.boundingBox
                )
            )
        }
        return true
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        event?.let {
            onEvent(
                MapIntent.Move(
                    center = it.source.mapCenter as GeoPoint,
                    zoomLevel = it.source.zoomLevelDouble,
                    bbox = it.source.boundingBox
                )
            )
        }
        return true
    }
}
