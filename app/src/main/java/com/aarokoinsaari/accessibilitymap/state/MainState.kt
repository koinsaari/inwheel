/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.state

import com.aarokoinsaari.accessibilitymap.ui.navigation.NavigationScreen

data class MainState(val currentScreen: NavigationScreen = NavigationScreen.Map)
