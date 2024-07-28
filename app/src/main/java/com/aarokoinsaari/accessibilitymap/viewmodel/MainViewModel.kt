/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarokoinsaari.accessibilitymap.intent.MainIntent
import com.aarokoinsaari.accessibilitymap.state.MainState
import com.aarokoinsaari.accessibilitymap.ui.components.Screen
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
                    _state.value.copy(currentScreen = Screen.Map)

                MainIntent.NavigateToPlaces -> _state.value =
                    _state.value.copy(currentScreen = Screen.Places)
            }
        }
    }
}
