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
import com.aarokoinsaari.accessibilitymap.intent.PlaceListIntent
import com.aarokoinsaari.accessibilitymap.data.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.state.PlaceListState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class PlaceListViewModel(private val repository: PlaceRepository) : ViewModel() {
    private val _state = MutableStateFlow(PlaceListState())
    val state: StateFlow<PlaceListState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllPlaces().collect { places ->
                _state.value = _state.value.copy(places = places)
            }
        }

        viewModelScope.launch {
            _state
                .map { it.searchQuery }
                .debounce(DEBOUNCE_VALUE)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        applyFilter(query)
                    }
                }
        }
    }

    fun handleIntent(intent: PlaceListIntent) {
        when (intent) {
            is PlaceListIntent.UpdateQuery -> {
                _state.value = _state.value.copy(
                    searchQuery = intent.text
                )
                Log.d("PlaceListViewModel", "New query: ${intent.text}")
            }

            is PlaceListIntent.Search -> applyFilter(intent.query)
            is PlaceListIntent.SelectPlace -> {
                _state.value = _state.value.copy(selectedPlace = intent.place)
                Log.d("PlaceListViewModel", "Selected place: ${intent.place}")
                // TODO: Navigate to place details view
            }
        }
    }

    private fun applyFilter(query: String) {
        val allPlaces = _state.value.places
        Log.d("PlaceListViewModel", "All places: $allPlaces")
        val filtered = if (query.isBlank()) {
            emptyList()
        } else {
            allPlaces.filter { place ->
                // Excludes places without name (toilets, parking spots, etc)
                place.name.contains(query, ignoreCase = true) &&
                        place.name != place.category.defaultName
            }
        }
        _state.value = _state.value.copy(filteredPlaces = filtered)
        Log.d("PlaceListViewModel", "Filtered places: ${_state.value.filteredPlaces}")
    }

    companion object {
        private const val DEBOUNCE_VALUE = 200L
    }
}
