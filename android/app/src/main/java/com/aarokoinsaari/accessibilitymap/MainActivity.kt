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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aarokoinsaari.accessibilitymap.domain.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.view.about.AboutScreen
import com.aarokoinsaari.accessibilitymap.view.info.InfoScreen
import com.aarokoinsaari.accessibilitymap.view.map.MapScreen
import com.aarokoinsaari.accessibilitymap.view.navigation.NavigationRoutes
import com.aarokoinsaari.accessibilitymap.view.placedetails.PlaceDetailBottomSheet
import com.aarokoinsaari.accessibilitymap.view.theme.AccessibilityMapTheme
import com.aarokoinsaari.accessibilitymap.viewmodel.MapViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.PlaceDetailsViewModel
import com.aarokoinsaari.accessibilitymap.viewmodel.SharedViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessibilityMapTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val sharedViewModel: SharedViewModel = koinViewModel()
    val selectedPlaceState = sharedViewModel.selectedPlace.collectAsState()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Hidden,
            skipHiddenState = false
        )
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    ModalNavigationDrawer(
        // this has to be false when the map is visible or else cannot swipe right without opening the drawer
        gesturesEnabled = currentRoute != NavigationRoutes.MAP,
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Map") },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    selected = currentRoute == NavigationRoutes.MAP,
                    onClick = {
                        if (currentRoute != NavigationRoutes.MAP) {
                            navController.navigate(NavigationRoutes.MAP) {
                                popUpTo(NavigationRoutes.MAP)
                                launchSingleTop = true
                            }
                        }
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("About") },
                    icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    selected = currentRoute == NavigationRoutes.ABOUT,
                    onClick = {
                        if (currentRoute != NavigationRoutes.ABOUT) {
                            navController.navigate(NavigationRoutes.ABOUT) {
                                popUpTo(NavigationRoutes.ABOUT)
                                launchSingleTop = true
                            }
                        }
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Info") },
                    icon = { Icon(Icons.Default.Accessibility, contentDescription = null) },
                    selected = currentRoute == NavigationRoutes.INFO,
                    onClick = {
                        if (currentRoute != NavigationRoutes.INFO) {
                            navController.navigate(NavigationRoutes.INFO) {
                                popUpTo(NavigationRoutes.INFO)
                                launchSingleTop = true
                            }
                        }
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
            )
        ) {
            val sheetPeek = calculateBottomSheetPeekHeight(selectedPlaceState.value)
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0), // needs to set explicitly to 0 to avoid top inset
                topBar = {
                    if (currentRoute != NavigationRoutes.MAP && currentRoute != null) {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch { drawerState.open() }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Open Navigation Drawer"
                                    )
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                BottomSheetScaffold(
                    scaffoldState = bottomSheetScaffoldState,
                    sheetPeekHeight = sheetPeek,
                    sheetContent = {
                        val place = selectedPlaceState.value
                        if (place != null) {
                            val placeDetailsViewModel: PlaceDetailsViewModel = koinViewModel()
                            placeDetailsViewModel.setPlace(place)
                            PlaceDetailBottomSheet(
                                stateFlow = placeDetailsViewModel.state,
                                onIntent = { intent ->
                                    placeDetailsViewModel.handleIntent(intent)
                                },
                            )
                        }
                    },
                    sheetDragHandle = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            val place = selectedPlaceState.value
                            if (place != null) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(top = 36.dp)
                                ) {
                                    Text(
                                        text = place.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = stringResource(id = place.category.displayNameRes),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    val contactAddress = place.address
                                    if (contactAddress != null) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = stringResource(id = R.string.content_desc_address),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = place.address,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                    HorizontalDivider(Modifier.padding(top = 24.dp, bottom = 8.dp))
                                }
                            }

                            DragHandle(Modifier.align(Alignment.TopCenter))

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        bottomSheetScaffoldState.bottomSheetState.hide()
                                    }
                                    sharedViewModel.clearSelectedPlace()
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 16.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.close_detail_sheet_button),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavigationRoutes.MAP
                    ) {
                        composable(NavigationRoutes.MAP) {
                            val viewModel: MapViewModel = koinViewModel()
                            MapScreen(
                                stateFlow = viewModel.state,
                                onIntent = { intent ->
                                    when (intent) {
                                        is MapIntent.SelectPlace -> {
                                            Log.d("MainScreen", "Selected place: ${intent.place}")
                                            sharedViewModel.selectPlace(intent.place)

                                            scope.launch {
                                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                                            }
                                        }

                                        else -> viewModel.handleIntent(intent)
                                    }
                                },
                                onOpenDrawer = {
                                    scope.launch { drawerState.open() }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        composable(NavigationRoutes.ABOUT) {
                            AboutScreen()
                        }
                        composable(NavigationRoutes.INFO) {
                            InfoScreen()
                        }
                    }
                }
            }
        }
    }
}

private fun calculateBottomSheetPeekHeight(place: Place?): Dp =
    when {
        place == null -> 56.dp
        place.address != null -> 128.dp
        place.generalAccessibility != AccessibilityStatus.UNKNOWN -> 108.dp

        else -> 108.dp
    }
