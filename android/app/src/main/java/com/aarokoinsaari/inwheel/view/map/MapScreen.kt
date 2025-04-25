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

package com.aarokoinsaari.inwheel.view.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.domain.intent.MapIntent
import com.aarokoinsaari.inwheel.domain.model.PlaceCategory
import com.aarokoinsaari.inwheel.domain.state.MapState
import com.aarokoinsaari.inwheel.view.components.Footer
import com.aarokoinsaari.inwheel.view.components.NotificationBar
import com.aarokoinsaari.inwheel.view.components.PlaceSearchBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    stateFlow: StateFlow<MapState>,
    modifier: Modifier = Modifier,
    onIntent: (MapIntent) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
) {
    val context = LocalContext.current
    val state by stateFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val defaultLocation = LatLng(46.462, 6.841) // Vevey
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            state.center ?: defaultLocation, state.zoomLevel ?: 8f
        )
    }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    // Remember if initial animation has been performed
    val initialAnimationPerformed = rememberSaveable { mutableStateOf(false) }

    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var showNotification by remember { mutableStateOf(false) }

    // Handle location permission state changes
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted && !initialAnimationPerformed.value) {
            onIntent(MapIntent.LocationPermissionGranted(true))

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try { // try to get last known location first
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val userLatLng = LatLng(location.latitude, location.longitude)
                            handleUserLocation(
                                userLatLng = userLatLng,
                                onIntent = onIntent,
                                cameraPositionState = cameraPositionState,
                                scope = scope,
                                animate = !initialAnimationPerformed.value
                            )
                            initialAnimationPerformed.value = true
                        } else {
                            Log.d("MapScreen", "Last location is null, requesting current location")
                            requestCurrentLocation(
                                fusedLocationClient = fusedLocationClient,
                                onIntent = onIntent,
                                cameraPositionState = cameraPositionState,
                                scope = scope,
                                initialAnimationPerformed = initialAnimationPerformed
                            )
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("MapScreen", "Location permission not granted", e)
                    onIntent(MapIntent.LocationPermissionGranted(false))
                    // Set animation as performed since we can't do it
                    initialAnimationPerformed.value = true
                }
            }
        }
    }

    // Request permission on initial launch only
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted && !initialAnimationPerformed.value) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxSize()
        ) {
            MapContent(
                state = state,
                cameraPositionState = cameraPositionState,
                onIntent = onIntent
            )

            Column(Modifier.fillMaxWidth()) {
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
                    isSearching = state.isSearching,
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
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .zIndex(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                ) {
                    val size = 40.dp
                    IconButton(
                        onClick = onOpenDrawer,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .size(size)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.content_desc_open_drawer),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(size),
                            strokeWidth = 2.dp
                        )
                    } else { // fill the space so the buttons stay always in the same place
                        Spacer(Modifier.size(40.dp))
                    }

                    IconButton(
                        onClick = {
                            state.userLocation?.let { userLocation ->
                                scope.launch {
                                    cameraPositionState.animate(
                                        update = CameraUpdateFactory.newLatLngZoom(
                                            userLocation,
                                            15f
                                        ),
                                        durationMs = 800
                                    )
                                }
                            }
                        },
                        enabled = state.userLocation != null,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .size(size)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = stringResource(id = R.string.content_desc_locate_me),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showNotification,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .zIndex(2f)
        ) {
            NotificationBar(
                message = stringResource(R.string.location_permissions_not_granted),
            )
        }

        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Footer(
                note = stringResource(R.string.map_footer_note)
            )
        }
    }
}

/**
 * Requests current location updates once. Stops after receiving first location.
 * Used as fallback when last known location is unavailable.
 */
private fun requestCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onIntent: (MapIntent) -> Unit,
    cameraPositionState: CameraPositionState,
    scope: CoroutineScope,
    initialAnimationPerformed: MutableState<Boolean> = mutableStateOf(true),
) {
    val locationRequest = LocationRequest.Builder(10000)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val userLatLng = LatLng(location.latitude, location.longitude)
                handleUserLocation(
                    userLatLng = userLatLng,
                    onIntent = onIntent,
                    cameraPositionState = cameraPositionState,
                    scope = scope,
                    animate = !initialAnimationPerformed.value
                )
                initialAnimationPerformed.value = true

                // Remove the callback after we get a location
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }
    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    } catch (e: SecurityException) {
        Log.e("MapScreen", "Error requesting location updates", e)
    }
}

/**
 * Updates UI state and animates camera to user location if needed.
 * Animation only happens on first launch, not when returning to map screen.
 */
private fun handleUserLocation(
    userLatLng: LatLng,
    onIntent: (MapIntent) -> Unit,
    cameraPositionState: CameraPositionState,
    scope: CoroutineScope,
    animate: Boolean = false,
) {
    onIntent(MapIntent.UpdateUserLocation(userLatLng))
    if (animate) {
        scope.launch {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(userLatLng, 15f),
                durationMs = 1000
            )

            delay(1100)
            cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { bounds ->
                onIntent(
                    MapIntent.MoveMap(
                        center = userLatLng,
                        zoomLevel = 15f,
                        bounds = bounds
                    )
                )
            }
        }
    } else {
        // still update the state, but without animation
        scope.launch {
            cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { bounds ->
                onIntent(
                    MapIntent.MoveMap(
                        center = userLatLng,
                        zoomLevel = cameraPositionState.position.zoom,
                        bounds = bounds
                    )
                )
            }
        }
    }
}
