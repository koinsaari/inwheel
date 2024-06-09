package com.aarokoinsaari.accessibilitymap.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Suppress("MagicNumber")
@Composable
fun MapScreen() {
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
                controller.setCenter(GeoPoint(60.192059, 24.945831))
                controller.setZoom(9.5)
            }
        }
    )
}

// Previews
@Preview
@Composable
fun MapScreen_Preview() {
    MapScreen()
}
