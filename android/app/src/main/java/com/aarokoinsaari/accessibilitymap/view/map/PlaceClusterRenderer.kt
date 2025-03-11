/*
 * Copyright (c) 2025 Aaro Koinsaari
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

package com.aarokoinsaari.accessibilitymap.view.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.aarokoinsaari.accessibilitymap.view.extensions.getAccessibilityStatusMarkerBgDrawableRes
import com.aarokoinsaari.accessibilitymap.view.models.PlaceClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class PlaceClusterRenderer(
    private val context: Context,
    private val clusterItemSize: Int,
    map: GoogleMap,
    clusterManager: ClusterManager<PlaceClusterItem>,
) : DefaultClusterRenderer<PlaceClusterItem>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: PlaceClusterItem, markerOptions: MarkerOptions) {
        val bitmap = createMarkerBitmap(
            context = context,
            backgroundRes = item.placeData.accessibility.general?.accessibilityStatus
                .getAccessibilityStatusMarkerBgDrawableRes(),
            iconRes = item.placeData.category.iconRes,
            size = clusterItemSize
        )
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
    }
}

private fun createMarkerBitmap(
    context: Context,
    @DrawableRes backgroundRes: Int,
    @DrawableRes iconRes: Int,
    size: Int = 72,
    iconPadding: Int = 16,
): Bitmap {
    val iconSize = size - iconPadding * 2

    return createBitmap(size, size).apply {
        val canvas = Canvas(this)

        ContextCompat.getDrawable(context, backgroundRes)?.apply {
            setBounds(0, 0, size, size)
            draw(canvas)
        }

        ContextCompat.getDrawable(context, iconRes)?.apply {
            setBounds(iconPadding, iconPadding, iconPadding + iconSize, iconPadding + iconSize)
            draw(canvas)
        }
    }
}
