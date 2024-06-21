package com.aarokoinsaari.accessibilitymap.network

import com.aarokoinsaari.accessibilitymap.model.Place
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApiService {
    @GET("interpreter")
    fun getPlaces(@Query("data") data: String): Flow<List<Place>>
}
