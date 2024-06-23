package com.aarokoinsaari.accessibilitymap.repository

import com.aarokoinsaari.accessibilitymap.database.ElementDao
import com.aarokoinsaari.accessibilitymap.model.BoundingBox
import com.aarokoinsaari.accessibilitymap.model.Element
import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import com.aarokoinsaari.accessibilitymap.network.OverpassQueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlaceRepository(
    private val apiService: OverpassApiService,
    private val elementDao: ElementDao
) {
    fun getPlaces(bbox: BoundingBox, categories: List<String>): Flow<List<Element>> = flow {
        val cachedElements = elementDao.getElements(
            bbox.minLat,
            bbox.minLon,
            bbox.maxLat,
            bbox.maxLon
        )
        if (cachedElements.isNotEmpty()) {
            emit(cachedElements)
        } else {
            val bboxStr = "${bbox.minLat},${bbox.minLon},${bbox.maxLat},${bbox.maxLon}"
            val query = OverpassQueryBuilder.buildQuery(bboxStr, categories)
            val response = apiService.getPlaces(query)
            elementDao.insertAll(response.elements)
            emit(response.elements)
        }
    }.flowOn(Dispatchers.IO)
}
