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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.intent.PlaceDetailsIntent
import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.model.ElevatorInfo
import com.aarokoinsaari.accessibilitymap.model.EntranceInfo
import com.aarokoinsaari.accessibilitymap.model.FloorInfo
import com.aarokoinsaari.accessibilitymap.model.ParkingInfo
import com.aarokoinsaari.accessibilitymap.model.ParkingInfo.ParkingType
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.model.RestroomInfo
import com.aarokoinsaari.accessibilitymap.state.PlaceDetailsState
import com.aarokoinsaari.accessibilitymap.ui.extensions.getAccessibilityStatusEmojiStringRes
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
@Suppress("CyclomaticComplexMethod")
fun PlaceDetailsScreen(
    stateFlow: StateFlow<PlaceDetailsState>,
    onIntent: (PlaceDetailsIntent) -> Unit = { }
) {
    val state by stateFlow.collectAsState()
    val place = state.place

    if (place != null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            topBar = {
                PlaceTopAppBar(
                    place = place,
                    onIntent = onIntent,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            content = { innerPadding ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    item {
                        MapCard(
                            place = place,
                            onClick = { onIntent(PlaceDetailsIntent.MapClick(place)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(8.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            val contactDetails = listOfNotNull(
                                place.contactInfo?.phone?.let { it to Icons.Outlined.Phone },
                                place.contactInfo?.email?.let { it to Icons.Outlined.Email },
                                place.contactInfo?.website?.let { it to Icons.Outlined.Info }
                            )

                            contactDetails.forEach { (info, icon) ->
                                PlaceBasicDetailsRow(
                                    info = info,
                                    icon = icon,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                )
                            }
                            HorizontalDivider(Modifier.padding(8.dp))
                            // Entrance
                            ExpandableItem(
                                title = stringResource(id = R.string.entrance),
                                content = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val fraction = 0.6f
                                        // Step count
                                        DetailRow(
                                            title = stringResource(id = R.string.step_count),
                                            content = place.accessibility?.entranceInfo?.stepCount?.toString()
                                                ?: stringResource(id = R.string.emoji_question),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Ramp
                                        DetailRow(
                                            title = stringResource(id = R.string.ramp),
                                            content = stringResource(
                                                id = place.accessibility?.entranceInfo?.hasRamp
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Steepness
                                        DetailRow(
                                            title = stringResource(id = R.string.entrance_steepness),
                                            content = stringResource(
                                                id = place.accessibility?.entranceInfo?.notTooSteepEntrance
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Door width
                                        DetailRow(
                                            title = stringResource(id = R.string.door_width),
                                            content = stringResource(
                                                id = place.accessibility?.entranceInfo?.isDoorWide
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Automatic door
                                        DetailRow(
                                            title = stringResource(id = R.string.automatic_door),
                                            content = stringResource(
                                                id = place.accessibility?.entranceInfo?.hasAutomaticDoor
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Additional info
                                        if (place.accessibility?.entranceInfo?.additionalInfo != null) {
                                            Column(
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, bottom = 6.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.additional_info),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = place.accessibility.entranceInfo.additionalInfo,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                    }
                                    // TODO: Images
                                },
                                modifier = Modifier.padding(6.dp)
                            )

                            // Restrooms
                            ExpandableItem(
                                title = stringResource(id = R.string.restroom),
                                content = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val fraction = 0.6f
                                        // Grab rails
                                        DetailRow(
                                            title = stringResource(id = R.string.grab_rails),
                                            content = stringResource(
                                                id = place.accessibility?.restroomInfo?.hasGrabRails
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Door width
                                        DetailRow(
                                            title = stringResource(id = R.string.door_width),
                                            content = stringResource(
                                                id = place.accessibility?.restroomInfo?.isDoorWideEnough
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // WC Size
                                        DetailRow(
                                            title = stringResource(id = R.string.spacious_enough),
                                            content = stringResource(
                                                id = place.accessibility?.restroomInfo?.isLargeEnough
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Emergency alarm
                                        DetailRow(
                                            title = stringResource(id = R.string.emergency_alarm),
                                            content = stringResource(
                                                id = place.accessibility?.restroomInfo?.hasEmergencyAlarm
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Euro key
                                        DetailRow(
                                            title = stringResource(id = R.string.euro_key),
                                            content = stringResource(
                                                id = place.accessibility?.restroomInfo?.euroKey
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Additional info
                                        if (place.accessibility?.restroomInfo?.additionalInfo != null) {
                                            Column(
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, bottom = 6.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.additional_info),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = place.accessibility.restroomInfo.additionalInfo,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                    }
                                    // TODO: Images
                                },
                                modifier = Modifier.padding(6.dp)
                            )

                            // Parking
                            ExpandableItem(
                                title = stringResource(id = R.string.parking),
                                content = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val fraction = 0.6f
                                        val spotCount = place.accessibility?.parkingInfo?.spotCount

                                        // Accessible spots
                                        DetailRow(
                                            title = stringResource(id = R.string.accessible_parking_spots),
                                            content = when {
                                                spotCount != null && spotCount > 0 -> {
                                                    spotCount.toString()
                                                }

                                                place.accessibility?.parkingInfo?.hasAccessibleSpots != null -> {
                                                    stringResource(
                                                        id = place.accessibility.parkingInfo.hasAccessibleSpots
                                                            .getAccessibilityStatusEmojiStringRes()
                                                    )
                                                }

                                                else -> {
                                                    stringResource(id = R.string.emoji_question)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Smooth surface
                                        DetailRow(
                                            title = stringResource(id = R.string.smooth_surface),
                                            content = stringResource(
                                                id = place.accessibility?.parkingInfo?.hasSmoothSurface
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // TODO: Parking type

                                        // Elevator if parking is in other than SURFACE
                                        if (place.accessibility?.parkingInfo?.parkingType != ParkingType.SURFACE) {
                                            DetailRow(
                                                title = stringResource(id = R.string.parking_elevator),
                                                content = stringResource(
                                                    id = place.accessibility?.parkingInfo?.hasElevator
                                                        .getAccessibilityStatusEmojiStringRes()
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .padding(start = 16.dp, bottom = 6.dp)
                                            )
                                        }

                                        if (place.accessibility?.parkingInfo?.additionalInfo != null) {
                                            Column(
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, bottom = 6.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.additional_info),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = place.accessibility.parkingInfo.additionalInfo,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                    }
                                    // TODO: Images
                                },
                                modifier = Modifier.padding(6.dp)
                            )

                            // Miscellaneous
                            ExpandableItem(
                                title = stringResource(id = R.string.miscellaneous),
                                content = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val fraction = 0.6f
                                        val level = place.accessibility?.floorInfo?.level

                                        // Level
                                        DetailRow(
                                            title = stringResource(id = R.string.located_on_floor),
                                            content = if (level == null) {
                                                stringResource(id = R.string.emoji_question)
                                            } else {
                                                level.toString()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        // Elevator to the floor
                                        DetailRow(
                                            title = stringResource(id = R.string.elevator_to_floor),
                                            content = stringResource(
                                                id = place.accessibility?.floorInfo?.hasElevator
                                                    .getAccessibilityStatusEmojiStringRes()
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .padding(start = 16.dp, bottom = 6.dp)
                                        )

                                        if (level != null && level > 0) {
                                            HorizontalDivider(Modifier.padding(horizontal = 8.dp))

                                            Text(
                                                text = stringResource(id = R.string.title_elevator),
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.6f
                                                ),
                                                modifier = Modifier.padding(start = 16.dp)
                                            )

                                            // Elevator spacious enough
                                            DetailRow(
                                                title = stringResource(id = R.string.elevator_spacious),
                                                content = stringResource(
                                                    id = place.accessibility.floorInfo.elevatorInfo?.isSpaciousEnough
                                                        .getAccessibilityStatusEmojiStringRes()
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .padding(start = 16.dp, bottom = 6.dp)
                                            )

                                            // Elevator has braille buttons
                                            DetailRow(
                                                title = stringResource(id = R.string.elevator_braille_buttons),
                                                content = stringResource(
                                                    id = place.accessibility.floorInfo.elevatorInfo?.hasBrailleButtons
                                                        .getAccessibilityStatusEmojiStringRes()
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .padding(start = 16.dp, bottom = 6.dp)
                                            )

                                            // Elevator has audio announcements
                                            DetailRow(
                                                title = stringResource(id = R.string.elevator_audio_announcements),
                                                content = stringResource(
                                                    id = place.accessibility.floorInfo.elevatorInfo
                                                        ?.hasAudioAnnouncements
                                                        .getAccessibilityStatusEmojiStringRes()
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .padding(start = 16.dp, bottom = 6.dp)
                                            )
                                        }

                                        // Floor additional info
                                        if (place.accessibility?.floorInfo?.additionalInfo != null) {
                                            Column(
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, bottom = 6.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.additional_info),
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Text(
                                                    text = place.accessibility.floorInfo.additionalInfo,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        TODO()
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
            Column {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(id = place.category.nameResId),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
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
        // TODO: new colors
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
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

@Composable
fun PlaceBasicDetailsRow(
    info: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = info,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ExpandableItem(
    title: String,
    initiallyExpanded: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            content()
        }
    }
}

@Composable
fun DetailRow(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailScreen_Preview() {
    val contactInfo = ContactInfo(
        email = "example@mail.com",
        phone = "+41123123123",
        website = "https://www.example.com"
    )

    val entranceInfo = EntranceInfo(
        hasRamp = true,
        notTooSteepEntrance = true,
        stepCount = 0,
        isDoorWide = true,
        hasAutomaticDoor = false,
        additionalInfo = "Main entrance has a smooth surface."
    )

    val restroomInfo = RestroomInfo(
        hasGrabRails = true,
        isDoorWideEnough = true,
        isLargeEnough = true,
        hasEmergencyAlarm = false,
        euroKey = false,
        additionalInfo = "Accessible restroom on the first floor."
    )

    val parkingInfo = ParkingInfo(
        hasAccessibleSpots = true,
        spotCount = 3,
        parkingType = ParkingType.SURFACE,
        hasSmoothSurface = true,
        hasElevator = false,
        additionalInfo = "Parking spots near the entrance."
    )

    val floorInfo = FloorInfo(
        level = 1,
        hasElevator = true,
        elevatorInfo = ElevatorInfo(
            isAvailable = true,
            isSpaciousEnough = true,
            hasBrailleButtons = true,
            hasAudioAnnouncements = true,
            additionalInfo = "Elevator has braille and audio guidance."
        ),
        additionalInfo = "Ground level accessible without stairs."
    )

    val accessibilityInfo = AccessibilityInfo(
        entranceInfo = entranceInfo,
        restroomInfo = restroomInfo,
        parkingInfo = parkingInfo,
        floorInfo = floorInfo,
        additionalInfo = "Very accessible."
    )

    val place = Place(
        id = 1,
        name = "Place",
        category = PlaceCategory.CAFE,
        lat = 46.462,
        lon = 6.841,
        tags = emptyMap(),
        accessibility = accessibilityInfo,
        address = "221B Baker Street",
        contactInfo = contactInfo
    )

    val stateFlow = MutableStateFlow(PlaceDetailsState(place = place))

    AccessibilityMapTheme {
        PlaceDetailsScreen(stateFlow = stateFlow)
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceTopAppBar_Preview() {
    val place = Place(
        id = 1,
        name = "Place",
        category = PlaceCategory.CAFE,
        lat = 46.462,
        lon = 6.841,
        tags = emptyMap(),
        accessibility = AccessibilityInfo(),
        address = null,
        contactInfo = null
    )
    AccessibilityMapTheme {
        PlaceTopAppBar(place)
    }
}
