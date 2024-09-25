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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.EntryAccessibilityStatus
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                    CustomMarker(
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
                        .background(Color.White)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun CustomMarker(category: PlaceCategory, modifier: Modifier = Modifier) {
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
        modifier = modifier
    ) {
        // TODO: Image representing the accessibility status here
        Text(
            text = item.placeData.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = stringResource(id = item.placeData.category.nameResId),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.accessibility_elevator_label),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    id = item.placeData.accessibility.hasElevator
                        .getAccessibilityStatusStringRes()
                ),
                style = MaterialTheme.typography.titleSmall
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.accessibility_toilet_label),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    id = item.placeData.accessibility.hasAccessibleToilet
                        .getAccessibilityStatusStringRes()
                ),
                style = MaterialTheme.typography.titleSmall
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.accessibility_entrance_label),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(
                    id = item.placeData.accessibility.entryAccessibility
                        .getEntryAccessibilityStringRes(),
                ),
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

private fun WheelchairAccessStatus.getAccessibilityColor(): Color =
    when (this) { // TODO: Change to MaterialTheme
        FULLY_ACCESSIBLE -> Color.Green
        PARTIALLY_ACCESSIBLE -> Color.Yellow
        LIMITED_ACCESSIBILITY -> Color.Yellow // TODO: Change to orange
        NOT_ACCESSIBLE -> Color.Red
        UNKNOWN -> Color.Gray
    }

private fun EntryAccessibilityStatus.getEntryAccessibilityStringRes(): Int =
    when (this) {
        EntryAccessibilityStatus.STEP_FREE -> R.string.entry_accessibility_step_free
        EntryAccessibilityStatus.ONE_STEP -> R.string.entry_accessibility_one_step
        EntryAccessibilityStatus.FEW_STEPS -> R.string.entry_accessibility_few_steps
        EntryAccessibilityStatus.SEVERAL_STEPS -> R.string.entry_accessibility_several_steps
        EntryAccessibilityStatus.UNKNOWN -> R.string.entry_accessibility_unknown
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
                        entryAccessibility = EntryAccessibilityStatus.STEP_FREE,
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
