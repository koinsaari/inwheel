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
            it.copy(place = place)
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

                is PlaceDetailIntent.UpdateAccessibilityDetailString -> {
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

                is PlaceDetailIntent.OpenDialog -> {
                    _state.update {
                        it.copy(activeDialog = intent.property)
                    }
                }

                is PlaceDetailIntent.CloseDialog -> {
                    _state.update {
                        it.copy(activeDialog = null)
                    }
                }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun updatePlaceGeneralAccessibility(place: Place, status: AccessibilityStatus) {
        viewModelScope.launch {
            try {
                // if the status is the same as the current one, we reset it to null
                val newStatus =
                    if (place.generalAccessibility == status) null
                    else status
                repository.updatePlaceGeneralAccessibility(place, newStatus)
                _state.update {
                    it.copy(
                        place = place.copy(generalAccessibility = newStatus),
                        activeDialog = null
                    )
                }
                Log.d(
                    "PlaceDetailsViewModel",
                    "Updated place general accessibility to: $newStatus"
                )
            } catch (e: Exception) {
                Log.e("PlaceDetailsViewModel", "Error updating place general accessibility", e)
            }
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
                val currentValue = getCurrentValue(place, detail)
                val valueToUpdate = if (currentValue == newValue) null else newValue
                
                repository.updatePlaceAccessibilityDetail(
                    place = place,
                    property = detail,
                    newValue = valueToUpdate
                )
                _state.update {
                    it.copy(
                        place = place.update(detail, valueToUpdate),
                        activeDialog = null
                    )
                }
                Log.d("PlaceDetailsViewModel", "Updated place accessibility detail: $place")
            } catch (e: Exception) {
                Log.e("PlaceDetailsViewModel", "Error updating place accessibility detail", e)
            }
        }
    }

    // Helper to get the current value of a property
    private fun getCurrentValue(place: Place, detail: PlaceDetailProperty): Any? =
        when (detail) {
            PlaceDetailProperty.GENERAL_ACCESSIBILITY -> place.generalAccessibility
            PlaceDetailProperty.INDOOR_ACCESSIBILITY -> place.indoorAccessibility
            PlaceDetailProperty.ENTRANCE_ACCESSIBILITY -> place.entranceAccessibility
            PlaceDetailProperty.RESTROOM_ACCESSIBILITY -> place.restroomAccessibility
            PlaceDetailProperty.ADDITIONAL_INFO -> place.additionalInfo
            PlaceDetailProperty.STEP_COUNT -> place.stepCount
            PlaceDetailProperty.STEP_HEIGHT -> place.stepHeight
            PlaceDetailProperty.RAMP -> place.ramp
            PlaceDetailProperty.LIFT -> place.lift
            PlaceDetailProperty.ENTRANCE_WIDTH -> place.width
            PlaceDetailProperty.DOOR_TYPE -> place.type
            PlaceDetailProperty.DOOR_WIDTH -> place.doorWidth
            PlaceDetailProperty.ROOM_MANEUVER -> place.roomManeuver
            PlaceDetailProperty.GRAB_RAILS -> place.grabRails
            PlaceDetailProperty.TOILET_SEAT -> place.toiletSeat
            PlaceDetailProperty.EMERGENCY_ALARM -> place.emergencyAlarm
            PlaceDetailProperty.SINK -> place.sink
            PlaceDetailProperty.EURO_KEY -> place.euroKey
        }

    private fun Place.update(detail: PlaceDetailProperty, newValue: Any?): Place =
        when (detail) {
            PlaceDetailProperty.GENERAL_ACCESSIBILITY -> copy(generalAccessibility = newValue as? AccessibilityStatus)
            PlaceDetailProperty.INDOOR_ACCESSIBILITY -> copy(indoorAccessibility = newValue as? AccessibilityStatus)
            PlaceDetailProperty.ENTRANCE_ACCESSIBILITY -> copy(entranceAccessibility = newValue as? AccessibilityStatus)
            PlaceDetailProperty.RESTROOM_ACCESSIBILITY -> copy(restroomAccessibility = newValue as? AccessibilityStatus)
            PlaceDetailProperty.ADDITIONAL_INFO -> copy(additionalInfo = newValue as? String)
            PlaceDetailProperty.STEP_COUNT -> copy(stepCount = newValue as? AccessibilityStatus)
            PlaceDetailProperty.STEP_HEIGHT -> copy(stepHeight = newValue as? AccessibilityStatus)
            PlaceDetailProperty.RAMP -> copy(ramp = newValue as? AccessibilityStatus)
            PlaceDetailProperty.LIFT -> copy(lift = newValue as? AccessibilityStatus)
            PlaceDetailProperty.ENTRANCE_WIDTH -> copy(width = newValue as? AccessibilityStatus)
            PlaceDetailProperty.DOOR_TYPE -> copy(type = newValue as? String)
            PlaceDetailProperty.DOOR_WIDTH -> copy(doorWidth = newValue as? AccessibilityStatus)
            PlaceDetailProperty.ROOM_MANEUVER -> copy(roomManeuver = newValue as? AccessibilityStatus)
            PlaceDetailProperty.GRAB_RAILS -> copy(grabRails = newValue as? AccessibilityStatus)
            PlaceDetailProperty.TOILET_SEAT -> copy(toiletSeat = newValue as? AccessibilityStatus)
            PlaceDetailProperty.EMERGENCY_ALARM -> copy(emergencyAlarm = newValue as? AccessibilityStatus)
            PlaceDetailProperty.SINK -> copy(sink = newValue as? AccessibilityStatus)
            PlaceDetailProperty.EURO_KEY -> copy(euroKey = newValue as? Boolean)
        }
}
