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
package com.aarokoinsaari.accessibilitymap.view.handlers

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
