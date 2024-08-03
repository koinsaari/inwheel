/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.repository

import android.util.Log
import com.aarokoinsaari.accessibilitymap.database.PlacesDao
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import com.aarokoinsaari.accessibilitymap.network.OverpassQueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.osmdroid.util.BoundingBox

class PlaceRepository(
    private val apiService: OverpassApiService,
    private val placesDao: PlacesDao
) {
    @Suppress("TooGenericExceptionCaught")
    fun getPlaces(bbox: BoundingBox): Flow<List<Place>> = flow {
        val cachedPlaces = placesDao.getPlaces(
            bbox.latSouth,
            bbox.lonWest,
            bbox.latNorth,
            bbox.lonEast
        )
        if (cachedPlaces.isNotEmpty()) {
            emit(cachedPlaces)
            Log.d("Repository", "Using cached places: $cachedPlaces")
        } else {
            val bboxStr = "${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast}"
            val query = OverpassQueryBuilder.buildQuery(bboxStr)
            Log.d("Repository", "Query: $query")
            try {
                val response = apiService.getMarkers(query)
                Log.d("Repository", "Response: $response")
                val places = response.elements.mapNotNull {
                    ApiDataConverter.convertMapMarkersToPlace(it)
                }
                if (places.isNotEmpty()) {
                    placesDao.insertAll(places)
                    Log.d("Repository", "Inserted places into database: $places")
                    emit(places)
                } else {
                    Log.d("Repository", "No places found from API")
                }
            } catch (e: Exception) {
                Log.e("Repository", "Failed to fetch or save places", e)
            }
        }
    }.flowOn(Dispatchers.IO)
}
