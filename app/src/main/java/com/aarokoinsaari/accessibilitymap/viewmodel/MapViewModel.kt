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
import com.aarokoinsaari.accessibilitymap.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.state.MapState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

@OptIn(FlowPreview::class)
class MapViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    private val moveIntents = MutableSharedFlow<MapIntent.Move>()
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
            moveIntents.emit(intent as MapIntent.Move)
        }
    }

    private suspend fun handleMove(intent: MapIntent.Move) {
        if (intent.zoomLevel < ZOOM_THRESHOLD) {
            handleClearMarkers()
        } else {
            Log.d("MapViewModel", "Update view intent: $intent")
            _state.value = _state.value.copy(zoomLevel = intent.zoomLevel)
            val currentState = _state.value
            val newBbox = intent.bbox

            if (currentState.snapshotBbox == null ||
                centerIsOutOfBBox(intent.center, currentState.snapshotBbox)
            ) {
                val expandedBbox = calculateExpandedBBox(newBbox)
                _state.value = currentState.copy(snapshotBbox = newBbox)
                _state.value = currentState.copy(currentBbox = expandedBbox)
                loadMarkers(expandedBbox)
            }
        }
        Log.d("MapViewModel", "MapState after move: ${_state.value}")
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun loadMarkers(bbox: BoundingBox) {
        _state.value = _state.value.copy(isLoading = true)
        try {
            placeRepository.getPlaces(bbox)
                .distinctUntilChanged()
                .collect { places ->
                    _state.value = _state.value.copy(
                        markers = places,
                        isLoading = false
                    )
                    Log.d("MapViewModel", "MapState after marker load: ${_state.value}")
                }
        } catch (e: Exception) {
            // TODO: Handle error
            Log.e("MapViewModel", "Error loading markers", e)
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun handleClearMarkers() {
        if (_state.value.markers.isNotEmpty()) {
            _state.value = _state.value.copy(
                markers = emptyList(),
                currentBbox = null,
                snapshotBbox = null
            )
            Log.d("MapViewModel", "Clear markers action, current state: ${_state.value}")
        }
    }

    private fun calculateExpandedBBox(bbox: BoundingBox): BoundingBox {
        val latBuffer = (bbox.latNorth - bbox.latSouth) * (EXPAND_FACTOR - 1) / 2
        val lonBuffer = (bbox.lonEast - bbox.lonWest) * (EXPAND_FACTOR - 1) / 2
        return BoundingBox(
            bbox.latNorth + latBuffer,
            bbox.lonEast + lonBuffer,
            bbox.latSouth - latBuffer,
            bbox.lonWest - lonBuffer
        )
    }

    private fun centerIsOutOfBBox(center: GeoPoint, bbox: BoundingBox): Boolean =
        center.latitude < bbox.latSouth || center.latitude > bbox.latNorth ||
                center.longitude < bbox.lonWest || center.longitude > bbox.lonEast

    companion object {
        private const val ZOOM_THRESHOLD = 16.0
        private const val EXPAND_FACTOR = 3.0
        private const val DEBOUNCE_VALUE = 200L
    }
}
