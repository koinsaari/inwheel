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

package com.aarokoinsaari.accessibilitymap.view.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus.LIMITED_ACCESSIBILITY
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus.UNKNOWN
import com.aarokoinsaari.accessibilitymap.state.MapState
import com.aarokoinsaari.accessibilitymap.utils.PlaceCategory
import com.aarokoinsaari.accessibilitymap.view.model.PlaceClusterItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(
    stateFlow: StateFlow<MapState>,
    onIntent: (MapIntent) -> Unit = { }
) {
    val vevey = LatLng(46.462, 6.841)
    val state by stateFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            state.center ?: vevey, state.zoomLevel ?: 10f
        )
    }
    // For remembering the state when the screen is rotated for example
    var showNotification by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position }
            .distinctUntilChanged()
            .collect { position ->
                onIntent(
                    MapIntent.Move(
                        center = LatLng(position.target.latitude, position.target.longitude),
                        zoomLevel = position.zoom,
                        bounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
                            ?: return@collect
                    )
                )
            }
    }

    // Notification is shown for either 5 seconds or until user moves the map
    LaunchedEffect(Unit) {
        delay(5000L)
        if (showNotification) {
            showNotification = false
        }
    }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .filter { it }
            .collect {
                if (showNotification) {
                    showNotification = false
                }
            }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { onIntent(MapIntent.MapClick(null)) }
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
                    onIntent(MapIntent.MapClick(clusterItem))
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLng(clusterItem.position),
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
                            .size(24.dp)
                            .background(
                                color = item.placeData.accessibility.wheelchairAccess
                                    .getAccessibilityColor(),
                                shape = CircleShape
                            )
                            .padding(all = 6.dp),
                    )
                }
            )
        }

        AnimatedVisibility(
            visible = showNotification,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                NotificationBar(
                    message = stringResource(id = R.string.map_zoom_notification),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp)
                )
            }
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
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun MapPlaceMarker(category: PlaceCategory, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = category.iconResId),
            contentDescription = null
        )
    }
}

@Composable
fun MarkerInfoWindow(item: PlaceClusterItem, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        InfoWindowAccessibilityImage(
            status = item.placeData.accessibility.wheelchairAccess,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(96.dp)
                .background(
                    color = item.placeData.accessibility.wheelchairAccess.getAccessibilityColor(),
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
            text = stringResource(id = item.placeData.category.nameResId),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        InfoWindowAccessibilityInfo(
            infoLabel = stringResource(id = R.string.accessibility_wheelchair_label),
            status = stringResource(
                id = item.placeData.accessibility.wheelchairAccess
                    .getWheelchairAccessibilityStatusStringRes()
            )
        )
        InfoWindowAccessibilityInfo(
            infoLabel = stringResource(id = R.string.accessibility_elevator_label),
            status = stringResource(
                id = item.placeData.accessibility.hasElevator.getAccessibilityStatusStringRes()
            )
        )
        InfoWindowAccessibilityInfo(
            infoLabel = stringResource(id = R.string.accessibility_toilet_label),
            status = stringResource(
                id = item.placeData.accessibility.hasAccessibleToilet
                    .getAccessibilityStatusStringRes()
            )
        )
    }
}

@Composable
fun InfoWindowAccessibilityImage(
    status: WheelchairAccessStatus?,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(
                id = if (status != NOT_ACCESSIBLE) {
                    R.drawable.ic_accessible
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
    modifier: Modifier = Modifier
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
    modifier: Modifier = Modifier
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

private fun WheelchairAccessStatus.getWheelchairAccessibilityStatusStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.wheelchair_access_fully_accessible
        PARTIALLY_ACCESSIBLE -> R.string.wheelchair_access_partially_accessible
        LIMITED_ACCESSIBILITY -> R.string.wheelchair_access_limited_accessibility
        NOT_ACCESSIBLE -> R.string.wheelchair_access_not_accessible
        UNKNOWN -> R.string.wheelchair_access_unknown
    }

private fun WheelchairAccessStatus.getAccessibilityColor(): Color =
    when (this) { // TODO: Change to MaterialTheme
        FULLY_ACCESSIBLE -> Color.Green
        PARTIALLY_ACCESSIBLE -> Color.Yellow
        LIMITED_ACCESSIBILITY -> Color.Yellow // TODO: Change to orange
        NOT_ACCESSIBLE -> Color.Red
        UNKNOWN -> Color.Gray
    }

private fun Boolean?.getAccessibilityStatusStringRes(): Int =
    when (this) {
        true -> R.string.accessibility_status_yes
        false -> R.string.accessibility_status_no
        null -> R.string.accessibility_status_unknown
    }

@Preview(showBackground = true)
@Composable
private fun MapInfoPopup_Preview() {
    MaterialTheme {
        MarkerInfoWindow(
            PlaceClusterItem(
                place = Place(
                    id = 1L,
                    name = "Example Cafe",
                    category = PlaceCategory.CAFE,
                    lat = -37.813,
                    lon = 144.962,
                    tags = mapOf("category" to "cafe"),
                    accessibility = AccessibilityInfo(
                        wheelchairAccess = FULLY_ACCESSIBLE,
                        hasAccessibleToilet = true,
                        hasElevator = false,
                        additionalInfo = "Located on the ground floor"
                    )
                ),
                zIndex = 1f
            )
        )
    }
}
