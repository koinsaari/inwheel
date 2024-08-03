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
package com.aarokoinsaari.accessibilitymap.ui.screens

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.network.CategoryConfig
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
            val existingMarkers = mapView.overlays
                .filterIsInstance<Marker>()
                .associateBy { it.title }

            state.markers.forEach { mapMarker ->
                val marker = existingMarkers[mapMarker.name] ?: Marker(mapView).apply {
                    title = mapMarker.name
                    mapView.overlays.add(this)
                }
                marker.position = GeoPoint(mapMarker.lat, mapMarker.lon)
                marker.icon = getMarkerIcon(context, mapMarker.type)
            }
        }
    )
}

private fun getMarkerIcon(context: Context, type: String): Drawable? {
    Log.d("MapScreen", "Marker type: $type")
    val iconResId = CategoryConfig.allCategories[type] ?: CategoryConfig.allCategories["default"]!!
    return ContextCompat.getDrawable(context, iconResId)
}
