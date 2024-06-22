package com.aarokoinsaari.accessibilitymap.network

import com.aarokoinsaari.accessibilitymap.model.OverpassResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApiService {
    @GET("interpreter")
    suspend fun getPlaces(@Query("data") data: String): OverpassResponse
}
