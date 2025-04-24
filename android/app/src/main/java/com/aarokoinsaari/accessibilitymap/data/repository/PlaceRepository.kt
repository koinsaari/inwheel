/*
 * Copyright (c) 2024–2025 Aaro Koinsaari
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

package com.aarokoinsaari.accessibilitymap.data.repository

import android.util.Log
import com.aarokoinsaari.accessibilitymap.data.local.PlacesDao
import com.aarokoinsaari.accessibilitymap.data.remote.supabase.SupabaseApiService
import com.aarokoinsaari.accessibilitymap.data.remote.supabase.toDomain
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceDetailProperty
import com.aarokoinsaari.accessibilitymap.domain.model.TileId
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

class PlaceRepository(
    private val api: SupabaseApiService,
    private val dao: PlacesDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = job + dispatcher

    private val loadedTiles = ConcurrentHashMap<TileId, Long>()
    private val requestSemaphore = Semaphore(MAX_CONCURRENT_REQUESTS)
    private val job = SupervisorJob()

    init {
        startPeriodicCleanup()
    }

    fun cleanup() {
        job.cancel()
    }

    suspend fun updatePlaceGeneralAccessibility(place: Place, newStatus: AccessibilityStatus?) =
        withContext(Dispatchers.IO) {
            dao.updatePlaceGeneralAccessibility(place.id, newStatus?.name)
            Log.d(
                "PlaceRepository",
                "Updated place ${place.id} general accessibility to $newStatus"
            )
            api.updatePlaceGeneralAccessibility(place.id, newStatus?.name)
        }

    suspend fun updatePlaceDetailProperty(
        place: Place,
        property: PlaceDetailProperty,
        newValue: Any?,
    ) {
        withContext(Dispatchers.IO) {
            val processedValue = when {
                newValue is String && property == PlaceDetailProperty.ADDITIONAL_INFO ->
                    newValue.sanitize()
                else -> newValue
            }
            
            when (processedValue) {
                is AccessibilityStatus ->
                    dao.updatePlaceAccessibilityDetailString(
                        place.id,
                        property.dbColumnRoom,
                        processedValue.name
                    )

                is Boolean ->
                    dao.updatePlaceAccessibilityDetailBoolean(
                        place.id,
                        property.dbColumnRoom,
                        processedValue
                    )

                is String ->
                    dao.updatePlaceAccessibilityDetailString(
                        place.id,
                        property.dbColumnRoom,
                        processedValue
                    )

                else -> {
                    Log.e("PlaceRepository", "Unsupported type: ${processedValue?.javaClass?.name}")
                }
            }

            api.updatePlaceAccessibilityDetail(
                placeId = place.id,
                property = property,
                newValue = processedValue
            )

            Log.d(
                "PlaceRepository",
                "Updated place ${place.id} accessibility detail ${property.dbColumnRoom}"
            )
        }
    }

    /**
     * Observes places within the given bounds and returns as a Flow.
     */
    fun observePlacesWithinBounds(bounds: LatLngBounds, limit: Int = 5000): Flow<List<Place>> =
        dao.getPlacesWithinBounds(
            southLat = bounds.southwest.latitude,
            northLat = bounds.northeast.latitude,
            westLon = bounds.southwest.longitude,
            eastLon = bounds.northeast.longitude,
            limit = limit
        )

    /**
     * Prefetches tiles around to the current visible region for smoother panning.
     * Uses lower priority (delayed) fetching to avoid blocking visible tile loading.
     */
    fun prefetchNearbyTiles(currentBounds: LatLngBounds) {
        launch {
            val visibleTiles = getTilesForBounds(currentBounds)
            val adjacentTiles = getNearbyTiles(visibleTiles) - visibleTiles.toSet()

            // Filter for tiles not already loaded or expired
            val tilesToPrefetch = adjacentTiles.filter { tileId ->
                val lastFetchTime = loadedTiles[tileId]
                lastFetchTime == null || System.currentTimeMillis() - lastFetchTime > CACHE_EXPIRY_MS
            }

            if (tilesToPrefetch.isNotEmpty()) {
                Log.d("PlaceRepository", "Prefetching ${tilesToPrefetch.size} adjacent tiles")

                tilesToPrefetch.forEach { tileId ->
                    launch(Dispatchers.IO) {
                        delay(500) // Lower priority by delaying
                        try {
                            requestSemaphore.withPermit {
                                fetchTile(tileId)
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "PlaceRepository",
                                "Prefetch failed for tile $tileId: ${e.message}"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Fetches places for the currently visible map region, skipping tiles that are already cached.
     */
    suspend fun fetchPlacesForVisibleRegion(bounds: LatLngBounds) = withContext(dispatcher) {
        val allTiles = getTilesForBounds(bounds)
        val tilesToFetch = allTiles.filter { tileId ->
            val lastFetchTime = loadedTiles[tileId]
            lastFetchTime == null || System.currentTimeMillis() - lastFetchTime > CACHE_EXPIRY_MS
        }

        Log.d("PlaceRepository", "Fetching ${tilesToFetch.size}/${allTiles.size} tiles for visible region")

        try {
            tilesToFetch.map { tileId ->
                async {
                    try {
                        requestSemaphore.withPermit {
                            fetchTile(tileId)
                        }
                    } catch (e: Exception) {
                        when (e) {
                            is CancellationException -> {
                                Log.d("PlaceRepository", "Cancelled fetch for tile $tileId")
                            }
                            else -> {
                                Log.e("PlaceRepository", "Failed to fetch tile $tileId: ${e.message}")
                                // Still mark the tile as partially fetched to avoid repeated failures
                                loadedTiles[tileId] = System.currentTimeMillis() - (CACHE_EXPIRY_MS * 3 / 4)
                            }
                        }
                    }
                }
            }.awaitAll()
        } catch (e: CancellationException) {
            Log.d("PlaceRepository", "Fetch operation was cancelled")
            throw e
        }

        Log.d("PlaceRepository", "Completed fetching ${tilesToFetch.size} tiles")
    }

    /**
     * Converts map bounds to a collection of tile IDs that cover the visible region.
     *
     * @param bounds The geographic bounds of the visible map area
     * @return List of TileIds that fully cover the specified bounds
     */
    private fun getTilesForBounds(bounds: LatLngBounds): List<TileId> {
        // Convert bounds to tile indices
        val swTile = TileId.fromLatLng(bounds.southwest)
        val neTile = TileId.fromLatLng(bounds.northeast)

        // Generate all tile IDs in the rectangle
        val tileIds = mutableListOf<TileId>()
        for (lat in swTile.latIndex..neTile.latIndex) {
            for (lon in swTile.lonIndex..neTile.lonIndex) {
                tileIds.add(TileId(lat, lon))
            }
        }

        Log.d("PlaceRepository", "Region divided into ${tileIds.size} tiles")
        return tileIds
    }

    /**
     * Finds all neighboring tiles surrounding the visible map area.
     *
     * This function identifies the "border" of tiles that surround
     * the currently visible map tiles. These border tiles are used for
     * prefetching data to ensure smooth map panning.
     *
     * @param tiles List of visible tile IDs currently on screen
     * @return Set of border tile IDs surrounding the visible area
     */
    private fun getNearbyTiles(tiles: List<TileId>): Set<TileId> {
        if (tiles.isEmpty()) return emptySet()

        val visibleTiles = tiles.toSet()
        val borderTiles = mutableSetOf<TileId>()

        // Directions: N, NE, E, SE, S, SW, W, NW
        val directions = listOf(
            0 to 1, 1 to 1, 1 to 0, 1 to -1,
            0 to -1, -1 to -1, -1 to 0, -1 to 1
        )

        // For each visible tile, check its 8 neighboring directions
        for (tile in visibleTiles) {
            for ((latOffset, lonOffset) in directions) {
                val neighborTile = TileId(
                    latIndex = tile.latIndex + latOffset,
                    lonIndex = tile.lonIndex + lonOffset
                )

                // Only add if not already in visible set
                if (neighborTile !in visibleTiles) {
                    borderTiles.add(neighborTile)
                }
            }
        }

        return borderTiles
    }

    /**
     * Removes tiles from the in-memory cache that exceed the expiration threshold.
     */
    private fun cleanupExpiredTiles() {
        val now = System.currentTimeMillis()
        val expiredTiles = loadedTiles.entries
            .filter { (_, timestamp) -> now - timestamp > CACHE_EXPIRY_MS }
            .map { it.key }

        expiredTiles.forEach { loadedTiles.remove(it) }

        if (expiredTiles.isNotEmpty()) {
            Log.d("PlaceRepository", "Memory cleanup: removed ${expiredTiles.size} expired tiles")
        }
    }

    /**
     * Begins periodic background cleanup of expired tiles and database entries.
     */
    private fun startPeriodicCleanup() {
        launch {
            while (isActive) {
                delay((CACHE_EXPIRY_MS / 4).toLong())
                cleanupExpiredTiles()
                performDatabaseCleanup()
            }
        }
    }

    /**
     * Fetches a single tile with some retry logic.
     */
    private suspend fun fetchTile(tileId: TileId) {
        var retryCount = 0
        val maxRetries = 3

        while (retryCount < maxRetries) {
            try {
                val tileBounds = tileId.toBounds()
                Log.d("PlaceRepository", "Fetching tile $tileId")

                val places = api.fetchPlacesInBBox(
                    westLon = tileBounds.southwest.longitude,
                    southLat = tileBounds.southwest.latitude,
                    eastLon = tileBounds.northeast.longitude,
                    northLat = tileBounds.northeast.latitude
                ).map { it.toDomain() }

                // Store in database and update cache timestamp
                dao.insertPlacesInTile(tileId.toString(), places)
                loadedTiles[tileId] = System.currentTimeMillis()

                Log.d("PlaceRepository", "Tile $tileId fetched ${places.size} places")
                return
            } catch (e: CancellationException) {
                Log.d("PlaceRepository", "Fetch for tile $tileId was cancelled")
                throw e
            } catch (e: IOException) {
                retryCount++
                Log.e("PlaceRepository", "Network error on tile $tileId (retry $retryCount/$maxRetries): ${e.message}")
                delay(1000L * retryCount)
            } catch (e: Exception) {
                Log.e("PlaceRepository", "Failed to fetch tile $tileId: ${e.javaClass.simpleName}: ${e.message}")
                loadedTiles[tileId] = System.currentTimeMillis() - (CACHE_EXPIRY_MS * 3 / 4)
                return
            }
        }

        loadedTiles[tileId] = System.currentTimeMillis() - (CACHE_EXPIRY_MS / 2)
        Log.e("PlaceRepository", "Abandoned tile $tileId after $maxRetries failed attempts")
    }

    /**
     * Cleans up old place entries from the database and preserves active tiles.
     */
    private suspend fun performDatabaseCleanup() {
        val now = System.currentTimeMillis()
        val cutoffTime = now - CACHE_EXPIRY_MS * 2
        val activeTileIds = loadedTiles.keys.map { it.toString() }

        val deletedCount = dao.cleanupOldPlaces(cutoffTime, activeTileIds)
        if (deletedCount > 0) {
            Log.d("PlaceRepository", "Database cleanup: removed $deletedCount old places")
        }
    }

    private fun String.sanitize(): String =
        this.replace(Regex("\\p{Cc}&&[^\r\n\t]"), "") // Keep newlines and tabs
            .trim()
            .take(1000)

    companion object {
        private const val CACHE_EXPIRY_MS = 1000 * 60 * 60 // 1 hour
        private const val MAX_CONCURRENT_REQUESTS = 8
    }
}
