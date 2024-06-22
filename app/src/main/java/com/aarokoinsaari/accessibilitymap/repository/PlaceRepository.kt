package com.aarokoinsaari.accessibilitymap.repository

import com.aarokoinsaari.accessibilitymap.model.Element
import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PlaceRepository(private val apiService: OverpassApiService) {
    fun getPlaces(query: String): Flow<List<Element>> = flow {
        val response = apiService.getPlaces(query)
        emit(response.elements)
    }.flowOn(Dispatchers.IO)
}
