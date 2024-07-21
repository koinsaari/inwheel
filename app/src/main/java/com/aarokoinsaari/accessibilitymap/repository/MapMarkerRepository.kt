package com.aarokoinsaari.accessibilitymap.repository

import android.util.Log
import com.aarokoinsaari.accessibilitymap.database.MapMarkerDao
import com.aarokoinsaari.accessibilitymap.model.MapMarker
import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import com.aarokoinsaari.accessibilitymap.network.OverpassQueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.osmdroid.util.BoundingBox

class MapMarkerRepository(
    private val apiService: OverpassApiService,
    private val mapMarkerDao: MapMarkerDao
) {
    @Suppress("TooGenericExceptionCaught")
    fun getMarkers(bbox: BoundingBox, categories: List<String>): Flow<List<MapMarker>> = flow {
        val cachedMarkers = mapMarkerDao.getMarkers(
            bbox.latSouth,
            bbox.lonWest,
            bbox.latNorth,
            bbox.lonEast
        )
        if (cachedMarkers.isNotEmpty()) {
            emit(cachedMarkers)
            Log.d("Repository", "Using cached markers: $cachedMarkers")
        } else {
            val bboxStr = "${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast}"
            val query = OverpassQueryBuilder.buildQuery(bboxStr, categories)
            Log.d("Repository", "Query: $query")
            try {
                val response = apiService.getMarkers(query)
                Log.d("Repository", "Response: $response")
                val nonNullMarkers = response.elements.mapNotNull {
                    if (it.tags != null) {
                        MapMarker(
                            id = it.id,
                            type = it.type,
                            name = it.tags["name"],
                            lat = it.lat,
                            lon = it.lon,
                        )
                    } else null
                }
                if (nonNullMarkers.isNotEmpty()) {
                    mapMarkerDao.insertAll(nonNullMarkers)
                    Log.d("Repository", "Inserted markers into database: $nonNullMarkers")
                    emit(nonNullMarkers)
                } else {
                    Log.d("Repository", "No markers found from API")
                }
            } catch (e: Exception) {
                Log.e("Repository", "Failed to fetch or save markers", e)
            }
        }
    }.flowOn(Dispatchers.IO)
}
