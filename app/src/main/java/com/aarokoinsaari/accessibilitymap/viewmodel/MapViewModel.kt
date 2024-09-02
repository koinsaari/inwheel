/*
 * Copyright (c) 2024 Aaro Koinsaari
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

package com.aarokoinsaari.accessibilitymap.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.model.PlaceClusterItem
import com.aarokoinsaari.accessibilitymap.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.state.MapState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class MapViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    private val moveIntents = MutableSharedFlow<MapIntent.Move>(extraBufferCapacity = 64)
    val state: StateFlow<MapState> = _state

    init {
        viewModelScope.launch {
            moveIntents
                .debounce(DEBOUNCE_VALUE)
                .collect { intent ->
                    handleMove(intent)
                }
        }
    }

    fun handleIntent(intent: MapIntent) {
        viewModelScope.launch {
            when (intent) {
                is MapIntent.Move -> moveIntents.emit(intent)
                is MapIntent.MarkerClick -> handleMarkerClick(intent)
            }
        }
    }


    private suspend fun handleMove(intent: MapIntent.Move) {
        if (intent.zoomLevel < ZOOM_THRESHOLD) {
            handleClearMarkers()
        } else {
            Log.d("MapViewModel", "Update view intent: $intent")
            _state.value = _state.value.copy(
                zoomLevel = intent.zoomLevel,
                center = intent.center,
                currentBounds = intent.bounds
            )
            val currentState = _state.value
            val newBounds = intent.bounds

            if (currentState.snapshotBounds == null ||
                centerIsOutOfBounds(intent.center, currentState.snapshotBounds)
            ) {
                val expandedBounds = calculateExpandedBounds(newBounds)
                _state.value = currentState.copy(snapshotBounds = newBounds)
                _state.value = currentState.copy(currentBounds = expandedBounds)
                loadClusterItems(expandedBounds)
            }
        }
        Log.d("MapViewModel", "MapState after move: ${_state.value}")
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadClusterItems(bounds: LatLngBounds) {
        _state.value = _state.value.copy(isLoading = true)
        try {
            placeRepository.getPlaces(bounds)
                .distinctUntilChanged()
                .collect { places ->
                    val clusterItems = places.map { PlaceClusterItem(it, zIndex = 1f) }
                    _state.value = _state.value.copy(
                        clusterItems = clusterItems,
                        isLoading = false
                    )
                    Log.d("MapViewModel", "MapState after cluster item load: ${_state.value}")
                }
        } catch (e: Exception) {
            // TODO: Handle error
            Log.e("MapViewModel", "Error loading cluster items", e)
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private suspend fun handleMarkerClick(intent: MapIntent.MarkerClick) {
        TODO("Not yet implemented")
    }

    private fun handleClearMarkers() {
        if (_state.value.markers.isNotEmpty()) {
            _state.value = _state.value.copy(
                markers = emptyList(),
                currentBounds = null,
                snapshotBounds = null
            )
            Log.d("MapViewModel", "Clear markers action, current state: ${_state.value}")
        }
    }

    private fun calculateExpandedBounds(bounds: LatLngBounds): LatLngBounds {
        val latBuffer =
            (bounds.northeast.latitude - bounds.southwest.latitude) * (EXPAND_FACTOR - 1) / 2
        val lonBuffer =
            (bounds.northeast.longitude - bounds.southwest.longitude) * (EXPAND_FACTOR - 1) / 2
        return LatLngBounds(
            LatLng(
                bounds.southwest.latitude - latBuffer,
                bounds.southwest.longitude - lonBuffer
            ),
            LatLng(
                bounds.northeast.latitude + latBuffer,
                bounds.northeast.longitude + lonBuffer
            )
        )
    }

    private fun centerIsOutOfBounds(center: LatLng, bounds: LatLngBounds?): Boolean {
        if (bounds == null) return true
        return center.latitude < bounds.southwest.latitude ||
                center.latitude > bounds.northeast.latitude ||
                center.longitude < bounds.southwest.longitude ||
                center.longitude > bounds.northeast.longitude
    }

    companion object {
        private const val ZOOM_THRESHOLD = 14.0
        private const val EXPAND_FACTOR = 3.0
        private const val DEBOUNCE_VALUE = 300L
    }
}
