package com.aarokoinsaari.accessibilitymap.repository

import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.network.OverpassApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class PlaceRepository(private val apiService: OverpassApiService) {
    fun getPlaces(query: String): Flow<List<Place>> {
        return apiService
            .getPlaces(query)
            .flowOn(Dispatchers.IO)
    }
}
