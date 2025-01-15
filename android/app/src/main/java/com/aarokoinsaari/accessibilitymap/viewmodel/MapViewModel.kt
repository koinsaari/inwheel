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
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.model.toClusterItem
import com.aarokoinsaari.accessibilitymap.state.MapState
import com.aarokoinsaari.accessibilitymap.ui.models.PlaceClusterItem
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class MapViewModel(
    private val repository: PlaceRepository,
    private val sharedViewModel: SharedViewModel
) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private var fetchJob: Job? = null

    init {
        // Handle search query changes
        viewModelScope.launch {
            _state.map { it.searchQuery }
                .debounce(DEBOUNCE_VALUE)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        applySearchFilter(query)
                    }
                }
        }

        // updates cluster items reactively based on changes to currentBounds
        viewModelScope.launch {
            _state.map { it.currentBounds }
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { bounds -> repository.observePlacesWithinBounds(bounds) }
                .map { places -> places.take(MAX_CLUSTER_ITEMS).map { it.toClusterItem() } }
                .collect { clusterItems ->
                    _state.update {
                        it.copy(
                            clusterItems = clusterItems,
                            allClusterItems = (it.allClusterItems + clusterItems)
                                .distinctBy { it.placeData.id },
                            isLoading = false
                        )
                    }
                    Log.d("MapViewModel", "Updated cluster items: ${clusterItems.size}")
                }
        }
    }

    fun handleIntent(intent: MapIntent) {
        viewModelScope.launch {
            when (intent) {
                is MapIntent.MoveMap -> handleMove(intent)
                is MapIntent.ClickMap -> handleMapClick(intent.item)
                is MapIntent.ToggleFilter -> handleToggleFilter(intent.category)
                is MapIntent.SearchPlace -> applySearchFilter(intent.query)
                is MapIntent.ClickClusterItem -> {
                    _state.update {
                        it.copy(
                            selectedClusterItem = PlaceClusterItem(
                                intent.place,
                                1f
                            )
                        )
                    }
                }

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
        _state.update {
            it.copy(
                zoomLevel = intent.zoomLevel,
                center = intent.center,
                currentBounds = intent.bounds,
                isLoading = true,
                selectedClusterItem = if (it.selectedClusterItem != null &&
                    !intent.bounds.contains(it.selectedClusterItem.position)) {
                    sharedViewModel.clearSelectedPlace()
                    null // closes the info window when out of view
                } else {
                    it.selectedClusterItem
                }
            )
        }
        Log.d("MapViewModel", "MapState before observe: ${_state.value}")

        if (intent.zoomLevel < ZOOM_THRESHOLD) {
            handleClearMarkers()
            return
        }

        val currentSnapshotBounds = _state.value.snapshotBounds
        if (currentSnapshotBounds == null || !currentSnapshotBounds.contains(intent.center)) {
            fetchJob?.cancel()
            fetchJob = viewModelScope.launch {
                _state.update { it.copy(snapshotBounds = intent.bounds) }
                val expandedBounds = calculateExpandedBounds(intent.bounds)
                val existingIds = _state.value.allClusterItems.map { it.placeData.id }.toSet()
                repository.fetchAndStorePlaces(expandedBounds, existingIds)
            }
        }
    }

    private fun handleMapClick(item: PlaceClusterItem?) {
        _state.update {
            it.copy(
                selectedClusterItem = if (item == it.selectedClusterItem) {
                    sharedViewModel.clearSelectedPlace()
                    null
                } else {
                    sharedViewModel.selectPlace(item?.placeData)
                    item
                }
            )
        }
    }

    private fun handleToggleFilter(category: PlaceCategory) {
        Log.d("MapViewModel", "Toggled category: $category")
        _state.update { currentState ->
            val updatedCategories =
                if (currentState.selectedCategories.contains(category.name.lowercase())) {
                    currentState.selectedCategories - category.name.lowercase()
                } else {
                    currentState.selectedCategories + category.name.lowercase()
                }
            Log.d("MapViewModel", "Updated categories: $updatedCategories")
            currentState.copy(selectedCategories = updatedCategories)
        }
        applyFilters()
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

    private fun applyFilters() {
        val selectedCategories = _state.value.selectedCategories
        val allClusterItems = _state.value.allClusterItems
        val filteredClusterItems = if (selectedCategories.isEmpty()) {
            allClusterItems
        } else {
            allClusterItems.filter {
                selectedCategories.contains(it.placeData.category.name.lowercase())
            }
        }
        _state.update { it.copy(clusterItems = filteredClusterItems) }
        Log.d("MapViewModel", "Filtered markers: ${_state.value.clusterItems}")
    }

    private fun applySearchFilter(query: String) {
        val allPlaces = _state.value.clusterItems
        val filtered = if (query.isBlank()) {
            emptyList()
        } else {
            allPlaces.filter { place ->
                // Excludes places without name (toilets, parking spots, etc)
                place.placeData.name.contains(query, ignoreCase = true) &&
                        place.placeData.name != place.placeData.category.name.lowercase()
            }
        }
        _state.update { it.copy(filteredPlaces = filtered.map { it.placeData }) }
        Log.d("MapViewModel", "Filtered places: ${_state.value.filteredPlaces}")
    }

    private fun calculateExpandedBounds(bounds: LatLngBounds): LatLngBounds {
        val latDiff = bounds.northeast.latitude - bounds.southwest.latitude
        val lonDiff = bounds.northeast.longitude - bounds.southwest.longitude

        val expandedSouthwest = LatLng(
            bounds.southwest.latitude - latDiff * (EXPAND_FACTOR - 1) / 2,
            bounds.southwest.longitude - lonDiff * (EXPAND_FACTOR - 1) / 2
        )
        val expandedNortheast = LatLng(
            bounds.northeast.latitude + latDiff * (EXPAND_FACTOR - 1) / 2,
            bounds.northeast.longitude + lonDiff * (EXPAND_FACTOR - 1) / 2
        )

        return LatLngBounds(expandedSouthwest, expandedNortheast)
    }

    companion object {
        private const val MAX_CLUSTER_ITEMS = 500
        private const val ZOOM_THRESHOLD = 12
        private const val EXPAND_FACTOR = 1.5
        private const val DEBOUNCE_VALUE = 400L
    }
}
