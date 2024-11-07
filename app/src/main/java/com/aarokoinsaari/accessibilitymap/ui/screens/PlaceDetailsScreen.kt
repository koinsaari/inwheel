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

package com.aarokoinsaari.accessibilitymap.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.intent.PlaceDetailsIntent
import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.EntranceInfo
import com.aarokoinsaari.accessibilitymap.model.FloorInfo
import com.aarokoinsaari.accessibilitymap.model.ParkingInfo
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.model.RestroomInfo
import com.aarokoinsaari.accessibilitymap.state.PlaceDetailsState
import com.aarokoinsaari.accessibilitymap.ui.extensions.getAccessibilityStatusShortStringRes
import com.aarokoinsaari.accessibilitymap.ui.theme.AccessibilityMapTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlaceDetailsScreen(
    stateFlow: StateFlow<PlaceDetailsState>,
    onIntent: (PlaceDetailsIntent) -> Unit = { }
) {
    val state by stateFlow.collectAsState()
    val place = state.place

    if (place != null) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                PlaceTopAppBar(
                    place = place,
                    onIntent = onIntent,
                    modifier = Modifier.fillMaxWidth()
                )
                MapCard(
                    place = place,
                    onClick = { onIntent(PlaceDetailsIntent.MapClick(place)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.place_details_basic_details_headline),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp)
                    )
                    PlaceBasicDetailsVertical(
                        place = place,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 70.dp) // TODO: Make more adaptive
                    )
                }
            }
        }
    } else {
        TODO()
    }
}

@Composable
fun PlaceBasicDetailsVertical(place: Place, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            // Entrance
            AccessibilityDetailItem(
                iconResId = R.drawable.ic_accessibility_entrance,
                descriptionResId = R.string.place_details_basic_details_entrance,
                statusText = stringResource(
                    id = place.accessibility?.entranceInfo?.determineAccessibilityStatus()
                        .getAccessibilityStatusShortStringRes()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            // Toilet
            AccessibilityDetailItem(
                iconResId = R.drawable.ic_wc,
                descriptionResId = R.string.place_details_basic_details_toilet,
                statusText = stringResource(
                    id = place.accessibility?.restroomInfo?.determineAccessibilityStatus()
                        .getAccessibilityStatusShortStringRes()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        VerticalDivider(Modifier.fillMaxHeight())
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            // Parking
            AccessibilityDetailItem(
                iconResId = R.drawable.ic_parking_area,
                descriptionResId = R.string.place_details_basic_details_parking,
                statusText = stringResource(
                    id = place.accessibility?.entranceInfo?.determineAccessibilityStatus()
                        .getAccessibilityStatusShortStringRes()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            // Floor
            AccessibilityDetailItem(
                iconResId = R.drawable.ic_stack,
                descriptionResId = R.string.place_details_basic_details_floor,
                statusText = place.accessibility?.floorInfo?.level?.toString()
                    ?: stringResource(id = R.string.unknown),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun AccessibilityDetailItem(
    @DrawableRes iconResId: Int,
    @StringRes descriptionResId: Int,
    statusText: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null // TODO
        )
        Text(
            text = stringResource(id = descriptionResId),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceTopAppBar(
    place: Place,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailsIntent) -> Unit = { }
) {
    TopAppBar(
        title = {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = { onIntent(PlaceDetailsIntent.BackClick) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.content_desc_back_arrow)
                )
            }
        },
        actions = {
            IconButton(onClick = { onIntent(PlaceDetailsIntent.FavouriteClick(place)) }) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.content_desc_favourite)
                )
            }
            IconButton(onClick = { onIntent(PlaceDetailsIntent.OptionsClick(place)) }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(id = R.string.content_desc_options)
                )
            }
        },
        modifier = modifier
    )
}

@Composable
fun MapCard(
    place: Place,
    modifier: Modifier = Modifier,
    onClick: (PlaceDetailsIntent) -> Unit = { }
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Box {
            GoogleMap(
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(place.lat, place.lon), 15f
                    )
                },
                onMapClick = { onClick(PlaceDetailsIntent.MapClick(place)) },
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // TODO: Change to the custom MapPlaceMarker as in MapScreen for consistency
                Marker(
                    state = rememberMarkerState(position = LatLng(place.lat, place.lon)),
                    title = place.name,
                    snippet = place.category.defaultName
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailScreen_Preview() {
    val samplePlace = Place(
        id = 1,
        name = "Sample Place",
        category = PlaceCategory.CAFE,
        lat = 46.462,
        lon = 6.841,
        tags = emptyMap(),
        accessibility = AccessibilityInfo()
    )
    val sampleState = PlaceDetailsState(place = samplePlace)
    val stateFlow = MutableStateFlow(sampleState)

    PlaceDetailsScreen(stateFlow = stateFlow)
}

@Preview(showBackground = true)
@Composable
private fun PlaceBasicDetailsVertical_Preview() {
    val accessibilityInfo = AccessibilityInfo(
        entranceInfo = EntranceInfo(
            stepCount = 0,
            isDoorWide = true
        ),
        restroomInfo = RestroomInfo(
            hasGrabRails = true
        ),
        parkingInfo = ParkingInfo(
            hasAccessibleSpots = true,
            spotCount = 3
        ),
        floorInfo = FloorInfo(
            level = 0,
            hasElevator = true
        )
    )
    AccessibilityMapTheme {
        PlaceBasicDetailsVertical(
            place = Place(
                id = 1,
                name = "Cafe",
                category = PlaceCategory.CAFE,
                lat = 0.0,
                lon = 0.0,
                tags = emptyMap(),
                accessibility = accessibilityInfo
            ),
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MapCard_Preview() {
    val samplePlace = Place(
        id = 1,
        name = "Sample Place",
        category = PlaceCategory.CAFE,
        lat = 46.462,
        lon = 6.841,
        tags = emptyMap(),
        accessibility = AccessibilityInfo()
    )
    MapCard(
        place = samplePlace,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun PlaceTopAppBar_Preview() {
    val samplePlace = Place(
        id = 1,
        name = "Sample Place",
        category = PlaceCategory.CAFE,
        lat = 46.462,
        lon = 6.841,
        tags = emptyMap(),
        accessibility = AccessibilityInfo()
    )
    PlaceTopAppBar(samplePlace)
}
