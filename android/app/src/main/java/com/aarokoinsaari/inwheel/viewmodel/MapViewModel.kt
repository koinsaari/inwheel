/*
 * Copyright (c) 2024–2025 Aaro Koinsaari
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

package com.aarokoinsaari.inwheel.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarokoinsaari.inwheel.data.repository.PlaceRepository
import com.aarokoinsaari.inwheel.domain.intent.MapIntent
import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.domain.model.PlaceCategory
import com.aarokoinsaari.inwheel.domain.model.toClusterItem
import com.aarokoinsaari.inwheel.domain.state.MapState
import com.aarokoinsaari.inwheel.view.models.PlaceClusterItem
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MapViewModel(
    private val repository: PlaceRepository,
    private val sharedViewModel: SharedViewModel,
) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private val cachedClusterItems = mutableSetOf<PlaceClusterItem>()
    private var moveJob: Job? = null

    private var cachedPlaces: Set<Place> = emptySet()

    init {
        // Handle search query changes
        viewModelScope.launch {
            _state.map { it.searchQuery }
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        applySearch(query)
                    }
                }
        }

        // Apply category filters
        viewModelScope.launch {
            _state.map { it.selectedCategories }
                .distinctUntilChanged()
                .collect { categories ->
                    if (categories.isEmpty()) {
                        _state.update { it.copy(clusterItems = cachedClusterItems.toList()) }
                        return@collect
                    }

                    val filteredItems = cachedClusterItems.filter {
                        categories.contains(it.placeData.category.rawValue)
                    }

                    Log.d(
                        "MapViewModel",
                        "Applied ${categories.size} filters: ${filteredItems.size}/${cachedClusterItems.size} places shown"
                    )
                    _state.update {
                        it.copy(
                            selectedCategories = categories,
                            clusterItems = filteredItems
                        )
                    }
                }
        }

        // Observe Room changes within bounds
        viewModelScope.launch {
            _state.map { it.snapshotBounds }
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { bounds ->
                    repository.observePlacesWithinBounds(bounds)
                }
                .collect { places ->
                    Log.d("MapViewModel", "Updating UI with ${places.size} places")

                    // Update both caches before UI update
                    cachedPlaces = places.toSet()
                    val clusterItems = places.map { it.toClusterItem() }
                    cachedClusterItems.clear()
                    cachedClusterItems.addAll(clusterItems)

                    _state.update {
                        it.copy(
                            clusterItems = if (it.selectedCategories.isEmpty()) {
                                clusterItems
                            } else {
                                clusterItems.filter { item ->
                                    it.selectedCategories.contains(item.placeData.category.rawValue)
                                }
                            },
                            isLoading = false
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
        Log.d("MapViewModel", "Cleared ViewModel and repository resources")
    }

    fun handleIntent(intent: MapIntent) {
        viewModelScope.launch {
            when (intent) {
                is MapIntent.MoveMap -> handleMove(intent)
                is MapIntent.ToggleFilter -> handleToggleFilter(intent.category)
                is MapIntent.SearchPlace -> applySearch(intent.query)

                is MapIntent.UpdateQuery -> {
                    _state.update { it.copy(searchQuery = intent.query) }
                }

                is MapIntent.SelectPlace -> {
                    _state.update { it.copy(selectedPlace = intent.place) }
                    sharedViewModel.selectPlace(intent.place)
                }

                is MapIntent.LocationPermissionGranted -> {
                    _state.update { it.copy(locationPermissionGranted = intent.granted) }
                }

                is MapIntent.UpdateUserLocation -> {
                    _state.update { it.copy(userLocation = intent.latLng) }
                }
            }
        }
    }

    private fun handleMove(intent: MapIntent.MoveMap) {
        if (intent.zoomLevel >= ZOOM_THRESHOLD) {
            val currentBounds = _state.value.snapshotBounds
            if (currentBounds == null || !currentBounds.contains(intent.center)) {
                _state.update { it.copy(isLoading = true) }
            }
        }

        moveJob?.cancel()
        moveJob = viewModelScope.launch {
            delay(if (_state.value.snapshotBounds == null) INITIAL_DEBOUNCE_VALUE else DEBOUNCE_VALUE)

            if (intent.zoomLevel < ZOOM_THRESHOLD) {
                Log.d("MapViewModel", "Skipping fetch, zoom level too low")
                handleClearMarkers()
                return@launch
            }

            val smallSnapshotBounds = _state.value.snapshotBounds
            if (smallSnapshotBounds == null || !smallSnapshotBounds.contains(intent.center)) {
                Log.d("MapViewModel", "Center moved outside snapshot bounds, fetching new data")

                try {
                    val newSmallSnapshotBounds = intent.bounds
                    val expandedBounds =
                        calculateExpandedBounds(newSmallSnapshotBounds, intent.zoomLevel)

                    // First fetch visible region, then prefetch nearby tiles for smoother panning
                    repository.fetchPlacesForVisibleRegion(expandedBounds)
                    repository.prefetchNearbyTiles(expandedBounds) // This is lower priority background fetch

                    _state.update {
                        it.copy(
                            snapshotBounds = newSmallSnapshotBounds,
                            expandedSnapshotBounds = expandedBounds
                        )
                    }
                    Log.d("MapViewModel", "State after move: ${_state.value}")
                    
                    // Add a minimum loading time to give feedback to the user
                    delay(MINIMUM_LOADING_DISPLAY_TIME)
                } catch (e: Exception) {
                    Log.e("MapViewModel", "Error fetching places: ${e.message}")
                    delay(MINIMUM_LOADING_DISPLAY_TIME)
                } finally {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    // Calculate expanded bounds with zoom-based buffer factor
    private fun calculateExpandedBounds(
        bounds: LatLngBounds,
        zoomLevel: Float,
    ): LatLngBounds {
        val zoomFactor = when {
            zoomLevel < 12f -> 1.0f
            zoomLevel in 12f..14f -> 1.5f
            else -> 2.0f
        }

        val latDiff = bounds.northeast.latitude - bounds.southwest.latitude
        val lonDiff = bounds.northeast.longitude - bounds.southwest.longitude

        val expandedSouthwest = LatLng(
            bounds.southwest.latitude - latDiff * (zoomFactor - 1) / 2,
            bounds.southwest.longitude - lonDiff * (zoomFactor - 1) / 2
        )
        val expandedNortheast = LatLng(
            bounds.northeast.latitude + latDiff * (zoomFactor - 1) / 2,
            bounds.northeast.longitude + lonDiff * (zoomFactor - 1) / 2
        )

        return LatLngBounds(expandedSouthwest, expandedNortheast)
    }

    private fun applySearch(query: String) {
        val filtered = if (query.isBlank()) {
            emptyList()
        } else {
            cachedPlaces.filter { place ->
                place.name.contains(query, ignoreCase = true) &&
                        place.name != place.category.rawValue
            }
        }
        Log.d("MapViewModel", "Found ${filtered.size} places matching query")
        _state.update { it.copy(filteredPlaces = filtered) }
    }

    private fun handleToggleFilter(category: PlaceCategory) {
        val currentState = _state.value
        val updatedCategories =
            if (currentState.selectedCategories.contains(category.rawValue)) {
                currentState.selectedCategories - category.rawValue
            } else {
                currentState.selectedCategories + category.rawValue
            }
        _state.update {
            it.copy(selectedCategories = updatedCategories)
        }
    }

    private fun handleClearMarkers() {
        if (_state.value.clusterItems.isNotEmpty()) {
            _state.update {
                it.copy(
                    clusterItems = emptyList(),
                    snapshotBounds = null,
                    expandedSnapshotBounds = null,
                    selectedClusterItem = null,
                    isLoading = false
                )
            }
            Log.d("MapViewModel", "Cleared markers, reset bounds for next zoom-in")
        }
    }

    companion object {
        private const val DEBOUNCE_VALUE = 200L
        private const val INITIAL_DEBOUNCE_VALUE = 500L
        private const val ZOOM_THRESHOLD = 12
        private const val MINIMUM_LOADING_DISPLAY_TIME = 800L
    }
}
