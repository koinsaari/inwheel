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

package com.aarokoinsaari.accessibilitymap.view.screens

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.network.CategoryConfig
import com.aarokoinsaari.accessibilitymap.state.MapState
import com.aarokoinsaari.accessibilitymap.view.handlers.MapListener
import com.aarokoinsaari.accessibilitymap.utils.Utils.drawableToBitmap
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapScreen(
    stateFlow: StateFlow<MapState>,
    onEvent: (MapIntent) -> Unit = { }
) {
    val state by stateFlow.collectAsState()
    val context = LocalContext.current
    val clusterer = remember { RadiusMarkerClusterer(context) }
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(
                org.osmdroid.views.CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT
            )
            controller.setCenter(GeoPoint(46.462, 6.841))
            controller.setZoom(9.5)
        }
    }

    Configuration.getInstance().load(
        context, context.getSharedPreferences(
            "osmdroid", Context.MODE_PRIVATE
        )
    )

    AndroidView(
        factory = { mapView },
        update = { map ->
            map.overlays.clear()
            map.overlays.add(clusterer)
            map.addMapListener(MapListener(onEvent))

            clusterer.apply {
                setIcon(drawableToBitmap(context, R.drawable.ic_clusterer))
                items.clear()
                state.markers.forEach { mapMarker ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(mapMarker.lat, mapMarker.lon)
                        title = mapMarker.name
                        icon = getMarkerIcon(context, mapMarker.type)
                    }
                    add(marker)
                }
                map.invalidate()
            }
        }
    )
}

private fun getMarkerIcon(context: Context, type: String): Drawable? {
    Log.d("MapScreen", "Marker type: $type")
    val iconResId = CategoryConfig.allCategories[type] ?: CategoryConfig.allCategories["default"]!!
    return ContextCompat.getDrawable(context, iconResId)
}
