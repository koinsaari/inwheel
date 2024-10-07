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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarokoinsaari.accessibilitymap.intent.PlaceListIntent
import com.aarokoinsaari.accessibilitymap.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.state.PlaceListState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Suppress("UnusedPrivateProperty")
@OptIn(FlowPreview::class)
class PlaceListViewModel(private val repository: PlaceRepository) : ViewModel() {
    private val _state = MutableStateFlow(PlaceListState())
    val state: StateFlow<PlaceListState> = _state

    init {
        viewModelScope.launch {
            _state
                .map { it.searchQuery }
                .debounce(DEBOUNCE_VALUE)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        performSearch(query)
                    } else {
                        _state.value = _state.value.copy(
                            filteredPlaces = emptyList()
                        )
                    }
                }
        }
    }

    fun handleIntent(intent: PlaceListIntent) {
        viewModelScope.launch {
            when (intent) {
                is PlaceListIntent.UpdateQuery -> {
                    _state.value = _state.value.copy(
                        searchQuery = intent.text
                    )
                }

                is PlaceListIntent.Search -> performSearch(intent.query)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun performSearch(query: String) {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        try {
            val places = repository.searchPlacesByName(
                query = query,
                userLat = _state.value.userLocation?.latitude ?: 0.0,
                userLon = _state.value.userLocation?.longitude ?: 0.0
            )
            _state.value = _state.value.copy(
                filteredPlaces = places,
                isLoading = false
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                filteredPlaces = emptyList(),
                isLoading = false,
                errorMessage = e.message ?: "An error occurred" // TODO
            )
        }
    }

    companion object {
        private const val DEBOUNCE_VALUE = 200L
    }
}
