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

package com.aarokoinsaari.accessibilitymap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.intent.PlaceDetailsIntent
import com.aarokoinsaari.accessibilitymap.intent.PlaceListIntent
import com.aarokoinsaari.accessibilitymap.view.components.BottomNavigationBar
import com.aarokoinsaari.accessibilitymap.view.navigation.NavigationScreen
import com.aarokoinsaari.accessibilitymap.view.screens.MapScreen
import com.aarokoinsaari.accessibilitymap.view.screens.PlaceDetailsScreen
import com.aarokoinsaari.accessibilitymap.view.screens.PlaceListScreen
import com.aarokoinsaari.accessibilitymap.view.theme.AccessibilityMapTheme
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.PlaceDetailsViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.PlaceListViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.SharedPlaceViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessibilityMapTheme {
                val navController = rememberNavController()
                val sharedPlaceViewModel: SharedPlaceViewModel = koinViewModel()

                Scaffold(
                    bottomBar = { BottomNavigationBar(navController = navController) },
                    contentWindowInsets = WindowInsets(0)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavigationScreen.Map.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavigationScreen.Map.route) {
                            val viewModel: MapViewModel = koinViewModel()
                            MapScreen(
                                stateFlow = viewModel.state,
                                onIntent = { intent ->
                                    when (intent) {
                                        is MapIntent.SelectPlace -> {
                                            sharedPlaceViewModel.selectPlace(intent.place)
                                            navController.navigate(NavigationScreen.PlaceDetails.route)
                                        }

                                        else -> viewModel.handleIntent(intent)
                                    }
                                }
                            )
                        }
                        composable(NavigationScreen.PlaceList.route) {
                            val viewModel: PlaceListViewModel = koinViewModel()
                            PlaceListScreen(
                                stateFlow = viewModel.state,
                                onIntent = { intent ->
                                    when (intent) {
                                        is PlaceListIntent.SelectPlace -> {
                                            sharedPlaceViewModel.selectPlace(intent.place)
                                            navController.navigate(NavigationScreen.PlaceDetails.route)
                                        }

                                        else -> viewModel.handleIntent(intent)
                                    }
                                }

                            )
                        }
                        composable(NavigationScreen.PlaceDetails.route) {
                            val selectedPlace =
                                sharedPlaceViewModel.selectedPlace.collectAsState().value
                            if (selectedPlace != null) {
                                val viewModel: PlaceDetailsViewModel = koinViewModel {
                                    parametersOf(selectedPlace)
                                }
                                PlaceDetailsScreen(
                                    stateFlow = viewModel.state,
                                    onIntent = { intent ->
                                        when (intent) {
                                            is PlaceDetailsIntent.BackClick -> {
                                                navController.popBackStack()
                                            }

                                            else -> viewModel.handleIntent(intent)
                                        }
                                    }
                                )
                            } else {
                                LaunchedEffect(Unit) {
                                    navController.popBackStack() // Navigate back to latest view
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
