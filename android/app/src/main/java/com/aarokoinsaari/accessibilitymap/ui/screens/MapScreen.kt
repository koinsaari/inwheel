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

package com.aarokoinsaari.accessibilitymap.ui.screens

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityInfo.GeneralAccessibility.EntranceAccessibility
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus.LIMITED_ACCESSIBILITY
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus.UNKNOWN
import com.aarokoinsaari.accessibilitymap.model.accessibility.accessibilityStatus
import com.aarokoinsaari.accessibilitymap.state.ErrorState
import com.aarokoinsaari.accessibilitymap.state.MapState
import com.aarokoinsaari.accessibilitymap.ui.components.PlaceSearchBar
import com.aarokoinsaari.accessibilitymap.ui.extensions.getAccessibilityStatusColor
import com.aarokoinsaari.accessibilitymap.ui.extensions.getEmojiStringRes
import com.aarokoinsaari.accessibilitymap.ui.models.PlaceClusterItem
import com.aarokoinsaari.accessibilitymap.utils.extensions.getLastLocationSuspended
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(MapsComposeExperimentalApi::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    stateFlow: StateFlow<MapState>,
    onIntent: (MapIntent) -> Unit = { },
) {
    val state by stateFlow.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val defaultLocation = LatLng(46.462, 6.841) // Vevey
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            state.center ?: defaultLocation, state.zoomLevel ?: 10f
        )
    }
    val errorMessage = state.errorState.getErrorStringRes()?.let { stringResource(it) }

    var showNotification by rememberSaveable { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf(false) } // Search list

    LaunchedEffect(locationPermissionState, cameraPositionState) {
        if (locationPermissionState.status.isGranted) {
            moveCameraToUserLocation(fusedLocationProviderClient, cameraPositionState)
        }

        // Track map movement and update state
        snapshotFlow { cameraPositionState.position }
            .distinctUntilChanged()
            .collect { position ->
                onIntent(
                    MapIntent.MoveMap(
                        center = LatLng(position.target.latitude, position.target.longitude),
                        zoomLevel = position.zoom,
                        bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                            ?: return@collect
                    )
                )
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

    // Error message
    LaunchedEffect(state.errorState) {
        if (state.errorState != ErrorState.None) {
            showError = true
            delay(5000L)
            showError = false
        } else {
            showError = false
        }
    }

    Box {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            onMapClick = { onIntent(MapIntent.ClickMap(null)) },
            modifier = Modifier.fillMaxSize()
        ) {
            Clustering(
                items = state.clusterItems,
                onClusterClick = {
                    cameraPositionState.move(
                        CameraUpdateFactory.zoomIn()
                    )
                    false
                },
                onClusterItemClick = { clusterItem ->
                    onIntent(MapIntent.ClickMap(clusterItem))
                    coroutineScope.launch {
                        val adjustedPosition = LatLng(
                            clusterItem.position.latitude + 0.003,
                            clusterItem.position.longitude
                        )
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLng(adjustedPosition),
                            durationMs = 200
                        )
                    }
                    true
                },
                // Workaround for issue: https://github.com/googlemaps/android-maps-compose/issues/409
                // Google's Marker Composable cannot be used here, so for now a workaround
                // is using basic Composables instead.
                clusterItemContent = { item ->
                    MapPlaceMarker(
                        category = item.placeData.category,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = item.placeData.accessibility.accessibilityStatus
                                    .getAccessibilityStatusColor(),
                                shape = CircleShape
                            )
                            .padding(all = 6.dp),
                    )
                }
            )
        }

        // Since ClusterItem info windows currently cannot be customized,
        // for now the only reasonable solution is to insert the custom info
        // window according to the selected cluster item. This should be changed
        // when the issue mentioned above is closed and Marker Composables can be used instead.
        state.selectedClusterItem?.let { selectedItem ->
            val screenPosition =
                cameraPositionState.projection?.toScreenLocation(selectedItem.position)

            if (screenPosition != null) {
                var popupSize by remember { mutableStateOf(IntSize.Zero) }
                val markerHeight =
                    with(LocalDensity.current) { 24.dp.roundToPx() }

                MarkerInfoWindow(
                    item = selectedItem,
                    onClick = { onIntent(MapIntent.SelectPlace(selectedItem.placeData)) },
                    onClose = { onIntent(MapIntent.ClickMap(null)) },
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            popupSize = coordinates.size
                        }
                        .offset {
                            IntOffset(
                                x = screenPosition.x - (popupSize.width / 2),
                                y = screenPosition.y - popupSize.height - markerHeight
                            )
                        }
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }
        }

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
                    expanded = false
                    onIntent(MapIntent.SearchPlace(state.searchQuery))
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                searchResults = state.filteredPlaces,
                onPlaceSelected = { place ->
                    onIntent(MapIntent.ClickClusterItem(place))
                    expanded = false

                    // Moves map to the selected place
                    coroutineScope.launch {
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

        AnimatedVisibility(
            visible = showError && errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                NotificationBar(
                    message = errorMessage!!
                )
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
                        text = stringResource(id = category.displayNameResId)
                    )
                },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else {
                    {
                        Icon(
                            painter = painterResource(id = category.iconResId),
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
fun MapPlaceMarker(
    category: PlaceCategory,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = category.iconResId),
            contentDescription = stringResource(id = category.displayNameResId)
        )
    }
}

@Composable
fun MarkerInfoWindow(
    item: PlaceClusterItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    onClose: () -> Unit = { }
) {
    Box(modifier) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(id = R.string.content_desc_close),
                tint = Color.LightGray // TODO: Use MaterialTheme
            )
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(16.dp)
        ) {
            InfoWindowAccessibilityImage(
                status = item.placeData.accessibility.accessibilityStatus,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(96.dp)
                    .background(
                        color = item.placeData.accessibility.accessibilityStatus
                            .getAccessibilityStatusColor(),
                        shape = CircleShape
                    )
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = item.placeData.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 200.dp)
            )
            Text(
                text = stringResource(id = item.placeData.category.displayNameResId),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            InfoWindowAccessibilityInfo(
                infoLabel = stringResource(id = R.string.accessible_entrance),
                status = stringResource(
                    id = item.placeData.accessibility.accessibilityStatus
                        .getEmojiStringRes()
                )
            )
            InfoWindowAccessibilityInfo(
                infoLabel = stringResource(id = R.string.accessibility_toilet_label),
                status = stringResource(
                    id = item.placeData.accessibility.accessibilityStatus
                        .getEmojiStringRes()
                )
            )
            InfoWindowAccessibilityInfo(
                infoLabel = stringResource(id = R.string.info_window_parking),
                status = stringResource(
                    id = item.placeData.accessibility.accessibilityStatus
                        .getEmojiStringRes()
                )
            )
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = onClick,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.info_window_view_details))
            }
        }
    }
}

@Composable
fun InfoWindowAccessibilityImage(
    status: AccessibilityStatus?,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(
                id = if (status != NOT_ACCESSIBLE) {
                    R.drawable.ic_accessible_general
                } else {
                    R.drawable.ic_not_accessible
                }
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.8f)
        )
    }
}

@Composable
fun InfoWindowAccessibilityInfo(
    infoLabel: String,
    status: String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = infoLabel,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = status,
            style = MaterialTheme.typography.titleSmall
        )
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

private fun ErrorState.getErrorStringRes(): Int? =
    when (this) {
        is ErrorState.NoInternet -> R.string.map_error_no_internet
        is ErrorState.Timeout -> R.string.map_error_timeout
        is ErrorState.ApiError -> R.string.map_error_api_failure
        is ErrorState.Unknown -> R.string.map_error_unknown
        ErrorState.None -> null
    }

@Preview(showBackground = true)
@Composable
private fun MapInfoPopup_Preview() {
    val contactInfo = ContactInfo(
        email = "example@mail.com",
        phone = "+41 21 123 45 67",
        website = "https://www.example.com",
        address = "Grande Place 1, Vevey 1800"
    )

    val entranceAccessibility = EntranceAccessibility(
        accessibilityStatus = FULLY_ACCESSIBLE,
        steps = EntranceAccessibility.StepsAccessibility(
            stepCount = 0,
            stepHeight = null,
            ramp = FULLY_ACCESSIBLE,
            lift = UNKNOWN
        ),
        door = EntranceAccessibility.DoorAccessibility(
            doorWidth = FULLY_ACCESSIBLE,
            doorType = "Automatic"
        ),
        additionalInfo = "Entrance is fully accessible with automatic doors."
    )

    val restroomAccessibility = AccessibilityInfo.GeneralAccessibility.RestroomAccessibility(
        accessibility = NOT_ACCESSIBLE,
        doorWidth = FULLY_ACCESSIBLE,
        roomManeuver = NOT_ACCESSIBLE,
        grabRails = LIMITED_ACCESSIBILITY,
        toiletSeat = FULLY_ACCESSIBLE,
        emergencyAlarm = NOT_ACCESSIBLE,
        sink = FULLY_ACCESSIBLE,
        euroKey = false,
        accessibleVia = "Elevator",
        additionalInfo = "Not accessible restroom on the ground floor."
    )

    val generalAccessibility = AccessibilityInfo.GeneralAccessibility(
        accessibilityStatus = LIMITED_ACCESSIBILITY,
        indoorAccessibility = FULLY_ACCESSIBLE,
        entrance = entranceAccessibility,
        restroom = restroomAccessibility,
        additionalInfo = "This location is mostly accessible."
    )

    val place = Place(
        id = "1",
        name = "Example Cafe",
        category = PlaceCategory.CAFE,
        lat = 46.460071,
        lon = 6.843391,
        contact = contactInfo,
        accessibility = generalAccessibility
    )

    MaterialTheme {
        MarkerInfoWindow(
            PlaceClusterItem(
                place = place,
                zIndex = 1f
            )
        )
    }
}
