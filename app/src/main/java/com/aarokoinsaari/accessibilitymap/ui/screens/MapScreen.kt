/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.state.MapState
import com.aarokoinsaari.accessibilitymap.ui.handlers.MapListener
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Suppress("MagicNumber")
@Composable
fun MapScreen(
    stateFlow: StateFlow<MapState>,
    onEvent: (MapIntent) -> Unit
) {
    val state by stateFlow.collectAsState()
    val context = LocalContext.current

    Configuration.getInstance().load(
        context, context.getSharedPreferences(
            "osmdroid", Context.MODE_PRIVATE
        )
    )

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(
                    org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
                )
                controller.setCenter(GeoPoint(60.192059, 24.945831)) // TODO
                controller.setZoom(9.5)
                addMapListener(
                    MapListener(onEvent = onEvent)
                )
            }
        },
        update = { mapView ->
            mapView.overlays.clear()
            state.markers.forEach { mapMarker ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(mapMarker.lat, mapMarker.lon)
                    title = mapMarker.name
                }
                mapView.overlays.add(marker)
            }
        }
    )
}
