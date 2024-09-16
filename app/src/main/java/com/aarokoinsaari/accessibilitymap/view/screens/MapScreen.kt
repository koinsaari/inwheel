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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.EntryAccessibilityStatus
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.PlaceClusterItem
import com.aarokoinsaari.accessibilitymap.model.WheelchairAccessStatus
import com.aarokoinsaari.accessibilitymap.state.MapState
import com.aarokoinsaari.accessibilitymap.utils.CategoryConfig
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreen(
    stateFlow: StateFlow<MapState>,
    onIntent: (MapIntent) -> Unit = { }
) {
    val vevey = LatLng(46.462, 6.841)
    val state by stateFlow.collectAsState()
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

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Clustering(
            items = state.clusterItems,
            onClusterClick = {
                cameraPositionState.move(
                    CameraUpdateFactory.zoomIn()
                )
                false
            },
            onClusterItemClick = {
                onIntent(MapIntent.ClusterItemClick(it))
                false
            },
            // Workaround for issue: https://github.com/googlemaps/android-maps-compose/issues/409
            // Seems like Google's Marker Composable cannot be used here, so for now workaround
            // is using basic Composables instead.
            clusterItemContent = { item ->
                CustomMarker(
                    iconType = item.placeData.type
                )
            }
        )
    }
}

@Composable
fun CustomMarker(iconType: String) {
    val iconResId =
        CategoryConfig.allCategories[iconType] ?: CategoryConfig.allCategories["default"]!!

    Image(
        painter = painterResource(id = iconResId),
        contentDescription = null
    )
}

@Composable
fun MapInfoPopup(item: PlaceClusterItem, modifier: Modifier = Modifier) {
    Box(modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(all = 10.dp)
        ) {
            Text(text = item.placeData.name)
            Text(text = item.placeData.type)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MapInfoPopup_Preview() {
    MaterialTheme {
        MapInfoPopup(
            PlaceClusterItem(
                place = Place(
                    id = 1L,
                    name = "Example Cafe",
                    type = "Cafe",
                    lat = -37.813,
                    lon = 144.962,
                    tags = mapOf("category" to "cafe"),
                    accessibility = AccessibilityInfo(
                        wheelchairAccess = WheelchairAccessStatus.FULLY_ACCESSIBLE,
                        entry = EntryAccessibilityStatus.STEP_FREE,
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
