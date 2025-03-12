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

package com.aarokoinsaari.accessibilitymap.data.remote.supabase

import android.util.Log
import com.aarokoinsaari.accessibilitymap.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.util.concurrent.CancellationException

@Suppress("TooGenericExceptionCaught")
class SupabaseApiService(
    private val httpClient: HttpClient,
) {
    suspend fun fetchPlacesInBBox(
        westLon: Double,
        southLat: Double,
        eastLon: Double,
        northLat: Double,
    ): List<PlaceDto> {
        val url = "${BuildConfig.SUPABASE_URL}/rest/v1/rpc/places_in_bbox"

        return try {
            val response: HttpResponse = httpClient.post(url) {
                header("apikey", BuildConfig.SUPABASE_KEY)
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
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

    suspend fun updatePlaceGeneralAccessibility(placeId: String, status: String?) {
        val url = "${BuildConfig.SUPABASE_URL}/rest/v1/general_accessibility?place_id=eq.$placeId"
        try {
            val response: HttpResponse = httpClient.patch(url) {
                header("apikey", BuildConfig.SUPABASE_KEY)
                header("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                contentType(ContentType.Application.Json)
                setBody(mapOf("accessibility" to status))
            }
            Log.d("SupabaseApiService", "Update response status: ${response.status}")
            if (!response.status.isSuccess()) {
                Log.e("SupabaseApiService", "Error updating place: ${response.status}")
            }
        } catch (e: CancellationException) {
            Log.d("SupabaseApiService", "Request was cancelled $e")
        } catch (e: Exception) {
            Log.e("SupabaseApiService", "Error updating place: $e")
        }
    }
}
