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
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceDetailProperty
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

                is PlaceDetailIntent.UpdateAccessibilityDetail -> {
                    updatePlaceAccessibilityDetail(
                        intent.place,
                        intent.detailProperty,
                        intent.status
                    )
                }

                is PlaceDetailIntent.OpenAccessibilityUpdateDialog -> {
                    openGeneralAccessibilityUpdateDialog()
                }

                is PlaceDetailIntent.CloseAccessibilityUpdateDialog -> {
                    closeGeneralAccessibilityUpdateDialog()
                }

                is PlaceDetailIntent.UpdateAccessibilityDetailCustom -> {
                    updatePlaceAccessibilityDetail(
                        intent.place,
                        intent.detailProperty,
                        intent.value
                    )
                }

                is PlaceDetailIntent.UpdateAccessibilityDetailBoolean -> {
                    updatePlaceAccessibilityDetail(
                        intent.place,
                        intent.detailProperty,
                        intent.value
                    )
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun updatePlaceGeneralAccessibility(place: Place, status: AccessibilityStatus) {
        if (place.generalAccessibility != status) {
            viewModelScope.launch {
                try {
                    Log.d("PlaceDetailsViewModel", "Updating place general accessibility")
                    repository.updatePlaceGeneralAccessibility(place, status)
                    _state.update {
                        it.copy(
                            place = place.copy(generalAccessibility = status),
                            showUpdateDialog = false
                        )
                    }
                } catch (e: Exception) {
                    Log.e("PlaceDetailsViewModel", "Error updating place general accessibility", e)
                }
            }
        } else {
            openGeneralAccessibilityUpdateDialog()
        }
    }

    private fun updatePlaceAccessibilityDetail(
        place: Place,
        detail: PlaceDetailProperty,
        newValue: Any?,
    ) {
        viewModelScope.launch {
            Log.d("PlaceDetailsViewModel", "Updating place accessibility detail")
            try {
                repository.updatePlaceAccessibilityDetail(
                    place = place,
                    detail = detail,
                    newValue = newValue
                )
                _state.update {
                    it.copy(
                        place = place.update(detail, newValue)
                    )
                }
                Log.d("PlaceDetailsViewModel", "Updated place accessibility detail: $place")
            } catch (e: Exception) {
                Log.e("PlaceDetailsViewModel", "Error updating place accessibility detail", e)
            }
        }
    }

    private fun openGeneralAccessibilityUpdateDialog() {
        _state.update {
            it.copy(
                showUpdateDialog = true
            )
        }
        Log.d(
            "PlaceDetailsViewModel",
            "Show dialog: ${_state.value.showUpdateDialog}"
        )
    }

    private fun closeGeneralAccessibilityUpdateDialog() {
        _state.update {
            it.copy(
                showUpdateDialog = false
            )
        }
        Log.d(
            "PlaceDetailsViewModel",
            "Show dialog: ${_state.value.showUpdateDialog}"
        )
    }

    fun Place.update(detail: PlaceDetailProperty, newValue: Any?): Place {
        return when (detail) {
            PlaceDetailProperty.STEP_COUNT -> copy(stepCount = newValue as? Int)
            PlaceDetailProperty.STEP_HEIGHT -> copy(stepHeight = newValue as? AccessibilityStatus)
            PlaceDetailProperty.DOOR_WIDTH -> copy(doorWidth = newValue as? AccessibilityStatus)
            PlaceDetailProperty.RAMP -> copy(ramp = newValue as? AccessibilityStatus)
            PlaceDetailProperty.LIFT -> copy(lift = newValue as? AccessibilityStatus)
            PlaceDetailProperty.DOOR_TYPE -> copy(type = newValue as? String)
            PlaceDetailProperty.ENTRANCE_ADDITIONAL_INFO -> copy(entranceAdditionalInfo = newValue as? String)
            PlaceDetailProperty.ROOM_MANEUVER -> copy(roomManeuver = newValue as? AccessibilityStatus)
            PlaceDetailProperty.GRAB_RAILS -> copy(grabRails = newValue as? AccessibilityStatus)
            PlaceDetailProperty.TOILET_SEAT -> copy(toiletSeat = newValue as? AccessibilityStatus)
            PlaceDetailProperty.EMERGENCY_ALARM -> copy(emergencyAlarm = newValue as? AccessibilityStatus)
            PlaceDetailProperty.SINK -> copy(sink = newValue as? AccessibilityStatus)
            PlaceDetailProperty.EURO_KEY -> copy(euroKey = newValue as? Boolean)
            PlaceDetailProperty.ACCESSIBLE_VIA -> copy(accessibleVia = newValue as? String)
            PlaceDetailProperty.RESTROOM_ADDITIONAL_INFO -> copy(restroomAdditionalInfo = newValue as? String)
        }
    }
}
