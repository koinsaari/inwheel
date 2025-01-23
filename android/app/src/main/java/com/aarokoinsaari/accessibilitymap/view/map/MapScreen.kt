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

package com.aarokoinsaari.accessibilitymap.view.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.domain.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.domain.state.MapState
import com.aarokoinsaari.accessibilitymap.utils.extensions.getLastLocationSuspended
import com.aarokoinsaari.accessibilitymap.view.placedetails.PlaceDetailBottomSheet
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    stateFlow: StateFlow<MapState>,
    onIntent: (MapIntent) -> Unit = { },
) {
    val state by stateFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val defaultLocation = LatLng(46.462, 6.841) // Vevey
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            state.center ?: defaultLocation, state.zoomLevel ?: 8f
        )
    }

    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var showNotification by rememberSaveable { mutableStateOf(true) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            val selectedPlace = state.selectedClusterItem?.placeData
            if (selectedPlace != null) {
                PlaceDetailBottomSheet(
                    place = selectedPlace,
                    onClose = {
                        scope.launch {
                            scaffoldState.bottomSheetState.hide()
                        }
                        onIntent(MapIntent.ClickMap(null, null))

                    }
                )
            } else {
                Text(
                    text = "No place selected",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MapContent(
                state = state,
                cameraPositionState = cameraPositionState,
                onIntent = onIntent,
                bottomSheetScaffoldState = scaffoldState
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                PlaceSearchBar(
                    query = state.searchQuery,
                    onQueryChange = { text ->
                        onIntent(MapIntent.UpdateQuery(text))
                    },
                    onSearch = {
                        searchExpanded = false
                        onIntent(MapIntent.SearchPlace(state.searchQuery))
                    },
                    expanded = searchExpanded,
                    onExpandedChange = { searchExpanded = it },
                    searchResults = state.filteredPlaces,
                    onPlaceSelected = { place ->
                        onIntent(MapIntent.SelectPlace(place))
                        searchExpanded = false

                        // Moves map to the selected place
                        scope.launch {
                            cameraPositionState.animate(
                                update = CameraUpdateFactory.newLatLngZoom(
                                    LatLng(place.lat, place.lon),
                                    20f
                                ),
                                durationMs = 1000
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(1f)
                )

                FilterChipRow(
                    categories = PlaceCategory.entries,
                    selectedCategories = state.selectedCategories,
                    onIntent = onIntent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .zIndex(1f)
                )
            }

            AnimatedVisibility(
                visible = showNotification,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    NotificationBar(
                        message = stringResource(id = R.string.map_zoom_notification)
                    )
                }
            }
        }
    }

    // Startup notification
    LaunchedEffect(cameraPositionState, showNotification) {
        val delayJob = launch {
            delay(5000L)
            showNotification = false
        }

        snapshotFlow { cameraPositionState.isMoving }
            .filter { it }
            .collect {
                if (showNotification) {
                    showNotification = false
                    delayJob.cancel()
                }
            }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterChipRow(
    categories: List<PlaceCategory>,
    selectedCategories: Set<String>,
    modifier: Modifier = Modifier,
    onIntent: (MapIntent) -> Unit = { },
) {
    LazyRow(modifier = modifier) {
        items(categories) { category ->
            val isSelected = selectedCategories.contains(category.rawValue)
            val haptic = LocalHapticFeedback.current

            FilterChip(
                selected = isSelected,
                onClick = {
                    onIntent(MapIntent.ToggleFilter(category))
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) // TODO
                },
                label = {
                    Text(
                        text = stringResource(id = category.displayNameRes)
                    )
                },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else {
                    {
                        Icon(
                            painter = painterResource(id = category.iconRes),
                            contentDescription = null
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = false,
                    selected = isSelected
                ),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .animateItem()
            )
        }
    }
}

@Composable
fun NotificationBar(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 56.dp, vertical = 12.dp)
            .widthIn(min = 200.dp, max = 300.dp)
    ) {
        Text(
            text = message,
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

private fun moveCameraToUserLocation(
    fusedLocationProviderClient: FusedLocationProviderClient,
    cameraPositionState: CameraPositionState,
) {
    val location = fusedLocationProviderClient.getLastLocationSuspended()
    location?.let {
        val userLatLng = LatLng(it.latitude, it.longitude)
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
    }
}
