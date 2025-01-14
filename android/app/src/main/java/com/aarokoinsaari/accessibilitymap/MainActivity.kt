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

package com.aarokoinsaari.accessibilitymap

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.intent.PlaceDetailsIntent
import com.aarokoinsaari.accessibilitymap.ui.components.BottomNavigationBar
import com.aarokoinsaari.accessibilitymap.ui.navigation.NavigationScreen
import com.aarokoinsaari.accessibilitymap.ui.screens.MapScreen
import com.aarokoinsaari.accessibilitymap.ui.screens.PlaceDetailsScreen
import com.aarokoinsaari.accessibilitymap.ui.theme.AccessibilityMapTheme
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.PlaceDetailsViewModel
import org.koin.androidx.compose.koinViewModel

@Suppress("LongMethod")
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessibilityMapTheme {
                val navController = rememberNavController()

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
                                            Log.d("MainActivity", "Selected place: ${intent.place}")
                                            navController.navigate(
                                                NavigationScreen.PlaceDetails.route
                                            )
                                        }

                                        else -> viewModel.handleIntent(intent)
                                    }
                                }
                            )
                        }
                        composable(NavigationScreen.PlaceDetails.route) {
                            val viewModel: PlaceDetailsViewModel = koinViewModel()
                            PlaceDetailsScreen(
                                stateFlow = viewModel.state,
                                onIntent = { intent ->
                                    when (intent) {
                                        is PlaceDetailsIntent.ClickBack,
                                        is PlaceDetailsIntent.ClickMap -> {
                                            Log.d("MainActivity", "Intent: $intent")
                                            val currentRoute =
                                                navController.previousBackStackEntry?.destination?.route
                                            Log.d("MainActivity", "currentRoute: $currentRoute")
                                            if (currentRoute == NavigationScreen.PlaceList.route) {
                                                navController.navigate(NavigationScreen.Map.route) {
                                                    popUpTo(NavigationScreen.Map.route) {
                                                        inclusive = false
                                                    }
                                                }
                                            } else {
                                                navController.popBackStack()
                                                Log.d("MainActivity", "popBackStack()")
                                            }
                                        }

                                        else -> viewModel.handleIntent(intent)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
