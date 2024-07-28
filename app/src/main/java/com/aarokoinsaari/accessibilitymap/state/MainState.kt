/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.state

import com.aarokoinsaari.accessibilitymap.ui.components.Screen

data class MainState(val currentScreen: Screen = Screen.Map)
