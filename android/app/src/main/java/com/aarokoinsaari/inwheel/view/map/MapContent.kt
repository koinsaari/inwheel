/*
 * Copyright (c) 2025 Aaro Koinsaari
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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.aarokoinsaari.inwheel.domain.intent.MapIntent
import com.aarokoinsaari.inwheel.domain.state.MapState
import com.aarokoinsaari.inwheel.view.models.PlaceClusterItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class)
@Composable
fun MapContent(
    state: MapState,
    cameraPositionState: CameraPositionState,
    modifier: Modifier = Modifier,
    onIntent: (MapIntent) -> Unit = {},
) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val screenWidthDp = config.screenWidthDp
    val screenHeightDp = config.screenHeightDp

    // Track map movement and update state
    LaunchedEffect(cameraPositionState) {
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

    GoogleMap(
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = {
            GoogleMapOptions().mapId("e030cf9b88fce692")
        },
        properties = MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = state.locationPermissionGranted,
        ),
        uiSettings = MapUiSettings(
            compassEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false, // TODO: Add custom location button
        ),
        modifier = modifier.fillMaxSize()
    ) {
        val clusterManagerState =
            remember { mutableStateOf<ClusterManager<PlaceClusterItem>?>(null) }

        MapEffect(context) { map ->
            clusterManagerState.value = ClusterManager<PlaceClusterItem>(context, map).apply {
                setAlgorithm(
                    NonHierarchicalViewBasedAlgorithm<PlaceClusterItem>(
                        screenWidthDp,
                        screenHeightDp
                    )
                )
                setAnimation(true)
                setOnClusterClickListener { cluster ->
                    cameraPositionState.move(CameraUpdateFactory.zoomIn())
                    false
                }
                setOnClusterItemClickListener { item ->
                    onIntent(MapIntent.SelectPlace(item.placeData))
                    map.animateCamera(CameraUpdateFactory.newLatLng(item.position))
                    true
                }
                setRenderer(
                    PlaceClusterRenderer(
                        context = context,
                        clusterItemSize = 72,
                        map = map,
                        clusterManager = this
                    )
                )
            }
        }

        val clusterManager = clusterManagerState.value
        if (clusterManager != null) {
            Clustering(
                items = state.clusterItems,
                clusterManager = clusterManager
            )
        }
    }
}
