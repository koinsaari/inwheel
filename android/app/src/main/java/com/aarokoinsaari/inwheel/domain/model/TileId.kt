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

package com.aarokoinsaari.inwheel.domain.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Represents a geographic tile in a grid system for efficient data fetching and caching.
 *
 * The map is divided into tiles of approximately 1km × 1km (at equator) to allow
 * fetching and managing map data in manageable chunks rather than all at once.
 */
data class TileId(val latIndex: Int, val lonIndex: Int) {

    override fun toString(): String = "$latIndex:$lonIndex"

    /**
     * Converts this tile ID to its geographic bounds.
     */
    fun toBounds(): LatLngBounds {
        val southLat = latIndex * TILE_SIZE_DEGREES
        val westLon = lonIndex * TILE_SIZE_DEGREES
        val northLat = southLat + TILE_SIZE_DEGREES
        val eastLon = westLon + TILE_SIZE_DEGREES

        return LatLngBounds(
            LatLng(southLat, westLon),  // southwest
            LatLng(northLat, eastLon)   // northeast
        )
    }

    companion object {
        private const val TILE_SIZE_DEGREES = 0.01 // Roughly 1km at equator

        /**
         * Creates a TileId from a geographic coordinate.
         */
        fun fromLatLng(latLng: LatLng, tileSize: Double = TILE_SIZE_DEGREES): TileId {
            val latIndex = (latLng.latitude / tileSize).toInt()
            val lonIndex = (latLng.longitude / tileSize).toInt()
            return TileId(latIndex, lonIndex)
        }
    }
}
