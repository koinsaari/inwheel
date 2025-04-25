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

package com.aarokoinsaari.inwheel.data.remote.supabase

import android.util.Log
import com.aarokoinsaari.inwheel.data.remote.config.ConfigProvider
import com.aarokoinsaari.inwheel.domain.model.PlaceDetailProperty
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.util.concurrent.CancellationException

class SupabaseApiService(
    private val httpClient: HttpClient,
    configProvider: ConfigProvider
) {
    private val supabaseUrl = configProvider.getSupabaseUrl()
    private val supabaseKey = configProvider.getSupabaseKey()
    
    suspend fun fetchPlacesInBBox(
        westLon: Double,
        southLat: Double,
        eastLon: Double,
        northLat: Double,
    ): List<PlaceDto> {
        val url = "$supabaseUrl/rest/v1/rpc/places_in_bbox"

        return try {
            val response: HttpResponse = httpClient.post(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "min_lon" to westLon,
                        "min_lat" to southLat,
                        "max_lon" to eastLon,
                        "max_lat" to northLat
                    )
                )
            }
            Log.d("SupabaseApiService", "Response status: ${response.status}")

            if (response.status.isSuccess()) {
                response.body()
            } else {
                Log.e("SupabaseApiService", "Error response: ${response.status}")
                emptyList()
            }
        } catch (e: CancellationException) {
            Log.d("SupabaseApiService", "Request was cancelled $e")
            emptyList()
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error fetching places: $e")
            emptyList()
        }
    }

    /**
     * Updates the general accessibility status for a place and marks it as user-modified.
     *
     * Makes two separate API calls: one to update the property value and another to set
     * the user_modified flag. This approach works around Kotlin serialization limitations
     * that prevent mixing different value types (like String and Boolean) in a single map.
     *
     * TODO: Consider implementing a database function that handles both updates in a single transaction.
     *
     * @param placeId The unique identifier of the place to update
     * @param status The new accessibility status to set (one of the AccessibilityStatus values)
     */
    suspend fun updatePlaceGeneralAccessibility(placeId: String, status: String?) {
        val url = "$supabaseUrl/rest/v1/general_accessibility?place_id=eq.$placeId"
        
        Log.d("SupabaseApiService", "Status: $status")
        try {
            val response1: HttpResponse = httpClient.patch(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(mapOf("accessibility" to status))
            }
            
            val response2: HttpResponse = httpClient.patch(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(mapOf("user_modified" to true))
            }
            
            Log.d("SupabaseApiService", "Update response status: ${response1.status}, ${response2.status}")
            if (!response1.status.isSuccess() || !response2.status.isSuccess()) {
                Log.e("SupabaseApiService", "Error updating place: ${response1.status}, ${response2.status}")
            }
        } catch (e: CancellationException) {
            Log.d("SupabaseApiService", "Request was cancelled $e")
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error updating place: $e")
        }
    }

    /**
     * Updates a specific accessibility property for a place and marks it as user-modified.
     *
     * Makes two separate API calls: one to update the property value and another to set
     * the user_modified flag. This way we work around Kotlin serialization limitations
     * that prevent mixing different value types (like String and Boolean) in a single map.
     *
     * TODO: Consider implementing a database function that handles both updates in a single transaction.
     *
     * @param placeId The ID of the place to update
     * @param property The accessibility property to update
     * @param newValue The new value for the property
     */
    suspend fun updatePlaceAccessibilityDetail(
        placeId: String,
        property: PlaceDetailProperty,
        newValue: Any?,
    ) {
        val (table, column) = mapPropertyToTableAndColumn(property)

        val url = "$supabaseUrl/rest/v1/$table?place_id=eq.$placeId"
        try {
            val response1: HttpResponse = httpClient.patch(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(mapOf(column to newValue))
            }
            
            val response2: HttpResponse = httpClient.patch(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(mapOf("user_modified" to true))
            }
            
            Log.d("SupabaseApiService", "Update response status: ${response1.status}, ${response2.status}")
            if (!response1.status.isSuccess() || !response2.status.isSuccess()) {
                Log.e("SupabaseApiService", "Error updating place: ${response1.status}, ${response2.status}")
            }
        } catch (e: CancellationException) {
            Log.d("SupabaseApiService", "Request was cancelled $e")
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error updating place: $e")
        }
    }

    /**
     * Searches for places by name globally.
     */
    suspend fun searchPlaces(query: String): List<SearchResultDto> {
        val endpoint = "$supabaseUrl/rest/v1/places"
        return try {
            val response = httpClient.get(endpoint) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                parameter("select",
                    "id,name,category,lat,lon,region," +
                            "contact(address)," +
                            "general_accessibility(accessibility)"
                )
                parameter("name", "ilike.*$query*")
                parameter("limit", "50")
            }
            
            if (response.status.isSuccess()) {
                response.body<List<SearchResultDto>>()
            } else {
                Log.d("SupabaseApiService", "Global search response status: ${response.status}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Search error: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Maps a PlaceDetailProperty to its corresponding database table and column.
     */
    private fun mapPropertyToTableAndColumn(property: PlaceDetailProperty): Pair<String, String> {
        return when (property) {
            // General accessibility table
            PlaceDetailProperty.GENERAL_ACCESSIBILITY -> "general_accessibility" to "accessibility"
            PlaceDetailProperty.INDOOR_ACCESSIBILITY -> "general_accessibility" to "indoor_accessibility"
            PlaceDetailProperty.ADDITIONAL_INFO -> "general_accessibility" to "additional_info"

            // Entrance accessibility table
            PlaceDetailProperty.ENTRANCE_ACCESSIBILITY -> "entrance_accessibility" to "accessibility"
            PlaceDetailProperty.STEP_COUNT -> "entrance_accessibility" to "step_count"
            PlaceDetailProperty.STEP_HEIGHT -> "entrance_accessibility" to "step_height"
            PlaceDetailProperty.RAMP -> "entrance_accessibility" to "ramp"
            PlaceDetailProperty.LIFT -> "entrance_accessibility" to "lift"
            PlaceDetailProperty.DOOR_TYPE -> "entrance_accessibility" to "door_type"
            PlaceDetailProperty.ENTRANCE_WIDTH -> "entrance_accessibility" to "entrance_width"

            // Restroom accessibility table
            PlaceDetailProperty.RESTROOM_ACCESSIBILITY -> "restroom_accessibility" to "accessibility"
            PlaceDetailProperty.DOOR_WIDTH -> "restroom_accessibility" to "door_width"
            PlaceDetailProperty.ROOM_MANEUVER -> "restroom_accessibility" to "room_maneuver"
            PlaceDetailProperty.GRAB_RAILS -> "restroom_accessibility" to "grab_rails"
            PlaceDetailProperty.SINK -> "restroom_accessibility" to "sink"
            PlaceDetailProperty.TOILET_SEAT -> "restroom_accessibility" to "toilet_seat"
            PlaceDetailProperty.EMERGENCY_ALARM -> "restroom_accessibility" to "emergency_alarm"
            PlaceDetailProperty.EURO_KEY -> "restroom_accessibility" to "euro_key"
        }
    }
}
