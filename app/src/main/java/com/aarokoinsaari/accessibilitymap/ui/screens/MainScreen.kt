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
