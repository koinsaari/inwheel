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

package com.aarokoinsaari.accessibilitymap.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarokoinsaari.accessibilitymap.data.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.domain.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.domain.model.toClusterItem
import com.aarokoinsaari.accessibilitymap.domain.state.MapState
import com.aarokoinsaari.accessibilitymap.view.models.PlaceClusterItem
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

    private var fetchJob: Job? = null
    private var moveJob: Job? = null
    private var cachedPlaces: Set<Place> = emptySet()
    private val cachedClusterItems = mutableSetOf<PlaceClusterItem>()

    init {
        // Handle search query changes
        viewModelScope.launch {
            _state.map { it.searchQuery }
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        applySearchFilter(query)
                    }
                }
        }

        // updates cluster items based on category selections
        viewModelScope.launch {
            _state.map { it.selectedCategories }
                .distinctUntilChanged()
                .collect { categories ->
                    if (categories.isEmpty()) {
                        _state.update { it.copy(clusterItems = cachedClusterItems.toList()) }
                        Log.d(
                            "MapViewModel",
                            "cluster items: ${_state.value.clusterItems.size}, " +
                                    "filters: ${_state.value.selectedCategories}"
                        )
                        return@collect
                    }
                    val filteredItems = cachedClusterItems.filter {
                        categories.contains(it.placeData.category.rawValue)
                    }
                    _state.update {
                        it.copy(
                            selectedCategories = categories,
                            clusterItems = filteredItems
                        )
                    }
                    Log.d(
                        "MapViewModel",
                        "cluster items: ${_state.value.clusterItems.size}, " +
                                "filters: ${_state.value.selectedCategories}"
                    )
                }
        }

        // updates cluster items based on smaller snapshot bounds changes fetching
        // directly from Room
        viewModelScope.launch {
            _state.map { it.smallSnapshotBounds }
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { bounds ->
                    repository.observePlacesWithinBounds(
                        bounds,
//                        MAX_CLUSTER_ITEMS
                    )
                }
                .map { places -> places.map { it.toClusterItem() } }
                .collect { clusterItems ->
                    _state.update {
                        it.copy(
                            clusterItems = clusterItems,
                            isLoading = false
                        )
                    }
                    cachedPlaces = clusterItems.map { it.placeData }.toSet()
                    cachedClusterItems.addAll(clusterItems)
                    Log.d("MapViewModel", "Updated cluster items: ${clusterItems.size}")
                }
        }
    }

    fun handleIntent(intent: MapIntent) {
        viewModelScope.launch {
            when (intent) {
                is MapIntent.MoveMap -> handleMove(intent)
                is MapIntent.ClickMap -> handleMapClick(intent.item, intent.position)
                is MapIntent.ToggleFilter -> handleToggleFilter(intent.category)
                is MapIntent.SearchPlace -> applySearchFilter(intent.query)

                is MapIntent.UpdateQuery -> {
                    _state.update { it.copy(searchQuery = intent.query) }
                }

                is MapIntent.SelectPlace -> {
                    _state.update { it.copy(selectedPlace = intent.place) }
                    sharedViewModel.selectPlace(intent.place)
                }
            }
        }
    }

    private fun handleMove(intent: MapIntent.MoveMap) {
        moveJob?.cancel()
        moveJob = viewModelScope.launch {
            delay(DEBOUNCE_VALUE)
            if (intent.zoomLevel < ZOOM_THRESHOLD_LARGE) {
                handleClearMarkers()
                return@launch
            }

            val currentLargeBounds = _state.value.largeSnapshotBounds
            if ((currentLargeBounds == null || !currentLargeBounds.contains(intent.center)) &&
                intent.zoomLevel >= ZOOM_THRESHOLD_LARGE
            ) {
                fetchJob?.cancel()
                fetchJob = viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    val newLargeBounds =
                        calculateExpandedBounds(intent.bounds, intent.zoomLevel, true)
                    repository.fetchAndStorePlaces(
                        newLargeBounds,
                        cachedPlaces.map { it.id }.toSet()
                    )
                    _state.update {
                        it.copy(
                            zoomLevel = intent.zoomLevel,
                            center = intent.center,
                            largeSnapshotBounds = newLargeBounds,
                            selectedClusterItem = if (it.selectedClusterItem != null &&
                                !intent.bounds.contains(it.selectedClusterItem.position)
                            ) {
                                sharedViewModel.clearSelectedPlace()
                                null // closes the info window when out of view
                            } else {
                                it.selectedClusterItem
                            },
                            isLoading = false
                        )
                    }
                    cachedClusterItems.addAll(_state.value.clusterItems)
                }
            }

            val currentSmallBounds = _state.value.smallSnapshotBounds
            if (currentSmallBounds == null || !currentSmallBounds.contains(intent.center) &&
                intent.zoomLevel >= ZOOM_THRESHOLD_SMALL
            ) {
                val newSmallBounds = calculateExpandedBounds(intent.bounds, intent.zoomLevel, false)
                _state.update {
                    it.copy(
                        zoomLevel = intent.zoomLevel,
                        center = intent.center,
                        smallSnapshotBounds = newSmallBounds,
                        selectedClusterItem = if (it.selectedClusterItem != null &&
                            !intent.bounds.contains(it.selectedClusterItem.position)
                        ) {
                            sharedViewModel.clearSelectedPlace()
                            null // closes the info window when out of view
                        } else {
                            it.selectedClusterItem
                        }
                    )
                }
            }
            return@launch
        }
    }

    private fun calculateExpandedBounds(
        bounds: LatLngBounds,
        zoomLevel: Float,
        isLargeBounds: Boolean,
    ): LatLngBounds {
        val zoomFactor = if (isLargeBounds) {
            when {
                zoomLevel < 12f -> 1.0f
                zoomLevel in 12f..14f -> 1.5f
                else -> 2.0f
            }
        } else {
            when {
                zoomLevel < 15f -> 1.0f
                zoomLevel in 15f..17f -> 1.5f
                else -> 2.0f
            }
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

    private fun handleMapClick(item: PlaceClusterItem?, position: LatLng?) {
        _state.update {
            it.copy(
                selectedClusterItem = item,
                center = if (position != null) position else return
            )
        }
        Log.d("MapViewModel", "Selected cluster item: ${_state.value.selectedClusterItem}")
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
        Log.d("MapViewModel", "Updated categories: $updatedCategories")
    }

    private fun handleClearMarkers() {
        if (_state.value.clusterItems.isNotEmpty()) {
            _state.update {
                it.copy(
                    clusterItems = emptyList(),
                    currentBounds = null,
                    selectedClusterItem = null,
                    isLoading = false
                )
            }
            Log.d("MapViewModel", "Clear markers action, current state: ${_state.value}")
        }
    }

    private fun applySearchFilter(query: String) {
        val filtered = if (query.isBlank()) {
            emptyList()
        } else {
            cachedPlaces.filter { place ->
                // Excludes places without name (toilets, parking spots, etc)
                place.name.contains(query, ignoreCase = true) &&
                        place.name != place.category.rawValue
            }
        }
        _state.update { it.copy(filteredPlaces = filtered) }
        Log.d("MapViewModel", "Filtered places count: ${_state.value.filteredPlaces.size}")
    }

    companion object {
        private const val MAX_CLUSTER_ITEMS = 500
        private const val ZOOM_THRESHOLD_LARGE = 12
        private const val ZOOM_THRESHOLD_SMALL = 14
        private const val DEBOUNCE_VALUE = 200L
    }
}
