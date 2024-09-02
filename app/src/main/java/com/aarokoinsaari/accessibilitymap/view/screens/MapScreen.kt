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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.state.MapState
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
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            state.center ?: vevey, state.zoomLevel ?: 10f
        )
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
            onClusterItemClick = { _ -> true }
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
}
