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

import androidx.lifecycle.ViewModel
import com.aarokoinsaari.accessibilitymap.domain.intent.PlaceDetailsIntent
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.state.PlaceDetailsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaceDetailsViewModel(place: Place) : ViewModel() {
    private val _state = MutableStateFlow(PlaceDetailsState(place))
    val state: StateFlow<PlaceDetailsState> = _state.asStateFlow()

    fun handleIntent(intent: PlaceDetailsIntent) {
        when (intent) {
            // First two handled by NavHost
            is PlaceDetailsIntent.ClickBack -> Unit
            is PlaceDetailsIntent.ClickMap -> Unit
            is PlaceDetailsIntent.ClickFavorite -> TODO()
            is PlaceDetailsIntent.ClickOptions -> TODO()
        }
    }
}
