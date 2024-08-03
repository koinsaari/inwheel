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
import com.aarokoinsaari.accessibilitymap.intent.MainIntent
import com.aarokoinsaari.accessibilitymap.state.MainState
import com.aarokoinsaari.accessibilitymap.ui.navigation.NavigationScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state

    fun handleIntent(intent: MainIntent) {
        viewModelScope.launch {
            when (intent) {
                MainIntent.NavigateToMap -> _state.value =
                    _state.value.copy(currentScreen = NavigationScreen.Map)

                MainIntent.NavigateToPlaces -> _state.value =
                    _state.value.copy(currentScreen = NavigationScreen.Places)
            }
        }
    }
}
