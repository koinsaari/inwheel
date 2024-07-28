/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aarokoinsaari.accessibilitymap.ui.components.BottomNavigationBar
import com.aarokoinsaari.accessibilitymap.viewmodel.MainViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    mapViewModel: MapViewModel = koinViewModel()
) {
    val state by mainViewModel.state.collectAsState()
    val mapState = mapViewModel.state
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = state.currentScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("map") {
                MapScreen(
                    stateFlow = mapState,
                    onEvent = { intent -> mapViewModel.handleIntent(intent) }
                )
            }
            composable("places") { PlacesListScreen() }
        }
    }
}
