/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.state.MapState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

class MapViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state

    fun handleIntent(intent: MapIntent) {
        viewModelScope.launch {
            when (intent) {
                is MapIntent.Move -> handleMove(intent)
                is MapIntent.LoadMarkers -> {}
                is MapIntent.ClearMarkers -> handleClearMarkers()
            }
        }
    }

    private suspend fun handleMove(intent: MapIntent.Move) {
        Log.d("MapViewModel", "Update view intent: $intent")
        val currentState = _state.value

        if (intent.zoomLevel < ZOOM_THRESHOLD) {
            _state.value = currentState.copy(markers = emptyList())
        } else if (intent.zoomLevel >= ZOOM_THRESHOLD) {
            val snapshotBbox = currentState.snapshotBbox
            val newBbox = intent.bbox

            if (snapshotBbox == null || centerIsOutOfBoundingBox(intent.center, snapshotBbox)) {
                _state.value = currentState.copy(snapshotBbox = newBbox)
                val expandedBbox = calculateExpandedBBox(newBbox)
                loadMarkers(expandedBbox)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught", "ForbiddenComment", "MagicNumber")
    @OptIn(FlowPreview::class)
    private suspend fun loadMarkers(bbox: BoundingBox) {
        _state.value = _state.value.copy(isLoading = true)
        try {
            placeRepository.getPlaces(bbox)
                .debounce(500)
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
        Log.d("MapViewModel", "Clear markers action")
        _state.value = _state.value.copy(markers = emptyList(), currentBbox = null)
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

    private fun centerIsOutOfBoundingBox(center: GeoPoint, bbox: BoundingBox): Boolean {
        return center.latitude < bbox.latSouth || center.latitude > bbox.latNorth ||
                center.longitude < bbox.lonWest || center.longitude > bbox.lonEast
    }

    companion object {
        private const val ZOOM_THRESHOLD = 17.5
        private const val EXPAND_FACTOR = 3.0
    }
}
