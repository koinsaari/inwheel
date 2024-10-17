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
import com.aarokoinsaari.accessibilitymap.state.ErrorState
import com.aarokoinsaari.accessibilitymap.state.MapState
import com.aarokoinsaari.accessibilitymap.utils.PlaceCategory
import com.aarokoinsaari.accessibilitymap.view.model.PlaceClusterItem
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(FlowPreview::class)
class MapViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    private val moveIntents = MutableSharedFlow<MapIntent.Move>(extraBufferCapacity = 64)
    private val apiCallFlow =
        MutableSharedFlow<Pair<LatLngBounds, LatLngBounds>>(extraBufferCapacity = 64)
    val state: StateFlow<MapState> = _state

    init {
        viewModelScope.launch {
            placeRepository.placesFlow.collect { places ->
                _state.value = _state.value.copy(
                    markers = places,
                    clusterItems = places.map { PlaceClusterItem(it, zIndex = null) }
                )
            }
        }

        viewModelScope.launch {
            moveIntents.collect { intent ->
                handleMove(intent)
            }
        }

        viewModelScope.launch {
            apiCallFlow
                .debounce(DEBOUNCE_VALUE)
                .collect { (currentBounds, expandedBounds) ->
                    fetchAndSetPlaces(currentBounds, expandedBounds)
                }
        }

        viewModelScope.launch {
            _state
                .map { it.searchQuery }
                .debounce(DEBOUNCE_VALUE)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        applySearchFilter(query)
                    }
                }
        }
    }

    fun handleIntent(intent: MapIntent) {
        viewModelScope.launch {
            when (intent) {
                is MapIntent.Move -> moveIntents.emit(intent)
                is MapIntent.MapClick -> handleMapClick(intent.item)
                is MapIntent.ToggleFilter -> handleToggleFilter(intent.category)
                is MapIntent.Search -> applySearchFilter(intent.query)
                is MapIntent.SelectPlace -> {
                    _state.value = _state.value.copy(
                        selectedClusterItem = PlaceClusterItem(intent.place, 1f)
                    )
                }

                is MapIntent.UpdateQuery -> {
                    _state.value = _state.value.copy(
                        searchQuery = intent.query
                    )
                }
            }
        }
    }

    private suspend fun fetchAndSetPlaces(
        currentBounds: LatLngBounds,
        expandedBounds: LatLngBounds
    ) {
        _state.value = _state.value.copy(isLoading = true)
        placeRepository.getPlacesWithinBounds(currentBounds, expandedBounds)
            .distinctUntilChanged()
            .catch { e ->
                _state.value = _state.value.copy(isLoading = false)
                when (e) {
                    is UnknownHostException, is ConnectException -> {
                        _state.value = _state.value.copy(errorState = ErrorState.NoInternet)
                    }

                    is SocketTimeoutException -> {
                        _state.value = _state.value.copy(errorState = ErrorState.Timeout)
                    }

                    is HttpException -> {
                        _state.value = _state.value.copy(
                            errorState = ErrorState.ApiError(
                                e.code(),
                                e.message()
                            )
                        )
                    }

                    else -> {
                        _state.value = _state.value.copy(errorState = ErrorState.Unknown(e))
                        Log.e("MapViewModel", "Unknown fetching places", e)
                    }
                }
            }
            .collect { newPlaces ->
                val allPlaces = (_state.value.markers + newPlaces).distinctBy { it.id }
                val combinedClusterItems = (_state.value.clusterItems + newPlaces.map {
                    PlaceClusterItem(it, 1f)
                }).distinctBy { it.placeData.id }

                _state.value = _state.value.copy(
                    markers = allPlaces,
                    clusterItems = combinedClusterItems,
                    isLoading = false,
                    errorState = ErrorState.None
                )
                applyFilters()
            }
    }

    private fun handleMove(intent: MapIntent.Move) {
        Log.d("MapViewModel", "Update view intent: $intent")
        _state.value = _state.value.copy(
            zoomLevel = intent.zoomLevel,
            center = intent.center,
            currentBounds = intent.bounds
        )

        if (intent.zoomLevel < ZOOM_THRESHOLD) {
            handleClearMarkers()
            return
        }

        val currentState = _state.value
        if (currentState.snapshotBounds == null ||
            centerIsOutOfBounds(intent.center, currentState.snapshotBounds)
        ) {
            val expandedBounds = calculateExpandedBounds(intent.bounds)
            _state.value = _state.value.copy(
                currentBounds = intent.bounds,
                snapshotBounds = expandedBounds
            )
            apiCallFlow.tryEmit(intent.bounds to expandedBounds)
        }
        Log.d("MapViewModel", "MapState after move: ${_state.value}")
    }

    private fun handleMapClick(item: PlaceClusterItem?) {
        if (item == _state.value.selectedClusterItem) {
            _state.value = _state.value.copy(
                selectedClusterItem = null
            )
        } else {
            _state.value = _state.value.copy(
                selectedClusterItem = item
            )
        }
    }

    private fun handleToggleFilter(category: PlaceCategory) {
        val updatedCategories = if (_state.value.selectedCategories.contains(category)) {
            _state.value.selectedCategories - category
        } else {
            _state.value.selectedCategories + category
        }
        _state.value = _state.value.copy(
            selectedCategories = updatedCategories
        )
        applyFilters()
    }

    private fun handleClearMarkers() {
        if (_state.value.clusterItems.isNotEmpty()) {
            _state.value = _state.value.copy(
                markers = emptyList(),
                clusterItems = emptyList(),
                currentBounds = null,
                snapshotBounds = null,
                selectedClusterItem = null
            )
            Log.d("MapViewModel", "Clear markers action, current state: ${_state.value}")
        }
    }

    private fun applyFilters() {
        val filteredMarkers = if (_state.value.selectedCategories.isEmpty()) {
            _state.value.markers
        } else {
            _state.value.markers.filter { _state.value.selectedCategories.contains(it.category) }
        }
        _state.value = _state.value.copy(
            clusterItems = filteredMarkers.map {
                PlaceClusterItem(it, 1f)
            }
        )
    }

    private fun applySearchFilter(query: String) {
        val allPlaces = _state.value.markers
        val filtered = if (query.isBlank()) {
            emptyList()
        } else {
            allPlaces.filter { place ->
                place.name.contains(query, ignoreCase = true)
                place.name != place.category.defaultName // Excludes places without name (toilets, parking spots, etc)
            }
        }
        _state.value = _state.value.copy(filteredPlaces = filtered)
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

    private fun centerIsOutOfBounds(center: LatLng, bounds: LatLngBounds): Boolean =
        !bounds.contains(center)

    companion object {
        private const val ZOOM_THRESHOLD = 13
        private const val EXPAND_FACTOR = 3.0
        private const val DEBOUNCE_VALUE = 250L
    }
}
