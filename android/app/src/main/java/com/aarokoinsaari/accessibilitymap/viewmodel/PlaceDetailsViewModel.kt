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

package com.aarokoinsaari.accessibilitymap.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarokoinsaari.accessibilitymap.data.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.domain.intent.PlaceDetailIntent
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.state.PlaceDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaceDetailsViewModel(private val repository: PlaceRepository) : ViewModel() {
    private val _state = MutableStateFlow(PlaceDetailState())
    val state: StateFlow<PlaceDetailState> = _state.asStateFlow()

    fun setPlace(place: Place) {
        _state.update {
            it.copy(
                place = place
            )
        }
    }

    fun handleIntent(intent: PlaceDetailIntent) {
        viewModelScope.launch {
            when (intent) {
                is PlaceDetailIntent.UpdateGeneralAccessibility -> {
                    updatePlaceGeneralAccessibility(intent.place, intent.status)
                }

                is PlaceDetailIntent.OpenGeneralAccessibilityUpdateDialog -> {
                    openGeneralAccessibilityUpdateDialog()
                }

                is PlaceDetailIntent.CloseGeneralAccessibilityUpdateDialog -> {
                    closeGeneralAccessibilityUpdateDialog()
                }
            }

        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun updatePlaceGeneralAccessibility(place: Place, status: AccessibilityStatus) {
        if (place.accessibility.general?.accessibilityStatus == null ||
            place.accessibility.general.accessibilityStatus == AccessibilityStatus.UNKNOWN
        ) {
            viewModelScope.launch {
                try {
                    Log.d("PlaceDetailsViewModel", "Updating place general accessibility")
                    repository.updatePlaceGeneralAccessibility(place)
                } catch (e: Exception) {
                    Log.e("PlaceDetailsViewModel", "Error updating place general accessibility", e)
                }
            }
        } else if (place.accessibility.general.accessibilityStatus == status) {
            openGeneralAccessibilityUpdateDialog()
        }
    }

    private fun openGeneralAccessibilityUpdateDialog() {
        _state.update {
            it.copy(
                showGeneralAccessibilityUpdateDialog = true
            )
        }
        Log.d("PlaceDetailsViewModel", "Show dialog: ${_state.value.showGeneralAccessibilityUpdateDialog}")
    }

    private fun closeGeneralAccessibilityUpdateDialog() {
        _state.update {
            it.copy(
                showGeneralAccessibilityUpdateDialog = false
            )
        }
        Log.d("PlaceDetailsViewModel", "Show dialog: ${_state.value.showGeneralAccessibilityUpdateDialog}")
    }
}
