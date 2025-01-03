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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.intent.PlaceDetailsIntent
import com.aarokoinsaari.accessibilitymap.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.model.Place
import com.aarokoinsaari.accessibilitymap.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.model.accessibility.ElevatorInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceDoor
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.EntranceSteps
import com.aarokoinsaari.accessibilitymap.model.accessibility.MiscellaneousInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.ParkingInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.ParkingInfo.ParkingType
import com.aarokoinsaari.accessibilitymap.model.accessibility.RestroomInfo
import com.aarokoinsaari.accessibilitymap.state.PlaceDetailsState
import com.aarokoinsaari.accessibilitymap.ui.theme.AccessibilityMapTheme
import com.aarokoinsaari.accessibilitymap.ui.extensions.getEmojiStringRes
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
                            onClick = { onIntent(PlaceDetailsIntent.ClickMap(place)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(8.dp)
                        )
                    }

                    item {
                        ContactDetailsSection(
                            place = place,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    // Entrance
                    item {
                        ExpandableItem(
                            title = stringResource(R.string.entrance),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            content = {
                                EntranceSection(place.accessibility?.entranceInfo)
                            }
                        )
                    }

                    // Restrooms
                    item {
                        ExpandableItem(
                            title = stringResource(R.string.restroom),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            content = {
                                RestroomSection(place.accessibility?.restroomInfo)
                            }
                        )
                    }

                    // Parking
                    item {
                        ExpandableItem(
                            title = stringResource(R.string.parking),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            content = {
                                ParkingSection(place.accessibility?.parkingInfo)
                            }
                        )
                    }

                    // Miscellaneous
                    item {
                        ExpandableItem(
                            title = stringResource(R.string.miscellaneous),
                            modifier = Modifier.padding(horizontal = 8.dp),
                            content = {
                                MiscellaneousSection(place.accessibility?.miscInfo)
                            }
                        )
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
            IconButton(onClick = { onIntent(PlaceDetailsIntent.ClickBack) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.content_desc_back_arrow)
                )
            }
        },
        actions = {
            IconButton(onClick = { onIntent(PlaceDetailsIntent.ClickFavorite(place)) }) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(id = R.string.content_desc_favourite)
                )
            }
            IconButton(onClick = { onIntent(PlaceDetailsIntent.ClickOptions(place)) }) {
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
                onMapClick = { onClick(PlaceDetailsIntent.ClickMap(place)) },
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
fun ContactDetailsSection(
    place: Place,
    modifier: Modifier = Modifier
) {
    val contactItems = listOfNotNull(
        place.contactInfo?.phone?.let {
            it to Pair(
                Icons.Outlined.Phone,
                stringResource(id = R.string.content_desc_phone)
            )
        },
        place.contactInfo?.email?.let {
            it to Pair(
                Icons.Outlined.Email,
                stringResource(id = R.string.content_desc_email)
            )
        },
        place.contactInfo?.website?.let {
            it to Pair(
                Icons.Outlined.Info,
                stringResource(id = R.string.content_desc_website)
            )
        }
    )

    if (contactItems.isNotEmpty()) {
        Column(
            modifier = modifier
        ) {
            contactItems.forEach { (info, icon) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon.first,
                        contentDescription = icon.second,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableItem(
    title: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
fun EntranceSection(
    entranceInfo: EntranceInfo?,
    modifier: Modifier = Modifier
) {
    if (entranceInfo == null) {
        Text(
            text = stringResource(R.string.no_information_for_section),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        // Step count
        val steps = entranceInfo.stepsInfo
        DetailRow(
            title = stringResource(R.string.step_count),
            content = steps?.stepCount?.toString() ?: stringResource(id = R.string.emoji_question),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Ramp
        DetailRow(
            title = stringResource(R.string.has_ramp),
            content = stringResource(id = steps?.ramp.getEmojiStringRes()),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Elevator
        DetailRow(
            title = stringResource(R.string.has_elevator),
            content = stringResource(id = steps?.elevator.getEmojiStringRes()),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Door width
        val door = entranceInfo.doorInfo
        DetailRow(
            title = stringResource(R.string.door_width),
            content = stringResource(id = door?.doorOpening.getEmojiStringRes()),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Automatic door
        DetailRow(
            title = stringResource(R.string.automatic_door),
            content = stringResource(id = door?.automaticDoor.getEmojiStringRes()),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Additional info
        if (!entranceInfo.additionalInfo.isNullOrBlank()) {
            AdditionalInfoRow(
                title = stringResource(R.string.additional_info),
                content = entranceInfo.additionalInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = ExpandableSectionDefaults.START_PADDING,
                        bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                    )
            )
        }
    }
}

@Composable
fun RestroomSection(
    restroomInfo: RestroomInfo?,
    modifier: Modifier = Modifier
) {
    if (restroomInfo == null) {
        Text(
            text = stringResource(R.string.no_information_for_section),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        // Grab rails
        DetailRow(
            title = stringResource(R.string.grab_rails),
            content = stringResource(
                id = restroomInfo.grabRails.getEmojiStringRes()
            ),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Door width
        DetailRow(
            title = stringResource(R.string.door_width),
            content = stringResource(
                id = restroomInfo.doorWidth.getEmojiStringRes()
            ),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Spacious enough
        DetailRow(
            title = stringResource(R.string.spacious_enough),
            content = stringResource(
                id = restroomInfo.roomSpaciousness.getEmojiStringRes()
            ),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Emergency alarm
        DetailRow(
            title = stringResource(R.string.emergency_alarm),
            content = stringResource(
                id = restroomInfo.hasEmergencyAlarm.getEmojiStringRes()
            ),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Euro key
        DetailRow(
            title = stringResource(R.string.euro_key),
            content = stringResource(
                id = restroomInfo.euroKey.getEmojiStringRes()
            ),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Additional info
        if (!restroomInfo.additionalInfo.isNullOrBlank()) {
            AdditionalInfoRow(
                title = stringResource(R.string.additional_info),
                content = restroomInfo.additionalInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = ExpandableSectionDefaults.START_PADDING,
                        bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                    )
            )
        }
    }
}

@Composable
fun ParkingSection(
    parkingInfo: ParkingInfo?,
    modifier: Modifier = Modifier
) {
    if (parkingInfo == null) {
        Text(
            text = stringResource(R.string.no_information_for_section),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        return
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        // Spot count
        val spotCount = parkingInfo.spotCount
        DetailRow(
            title = stringResource(R.string.accessible_parking_spots),
            content = when {
                spotCount != null && spotCount > 0 -> spotCount.toString()
                parkingInfo.hasAccessibleSpots != null ->
                    stringResource(
                        id = parkingInfo.hasAccessibleSpots.getEmojiStringRes()
                    )

                else -> stringResource(id = R.string.emoji_question)
            },
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // Surface
        DetailRow(
            title = stringResource(R.string.smooth_surface),
            content = stringResource(
                id = parkingInfo.hasSmoothSurface.getEmojiStringRes()
            ),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )

        // If parking place is not on surface level, we can show if there's an elevator
        if (parkingInfo.parkingType != ParkingType.SURFACE) {
            DetailRow(
                title = stringResource(R.string.parking_elevator),
                content = stringResource(
                    id = parkingInfo.hasElevator.getEmojiStringRes()
                ),
                modifier = Modifier
                    .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                    .padding(
                        start = ExpandableSectionDefaults.START_PADDING,
                        bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                    )
            )
        }

        // Additional info
        if (!parkingInfo.additionalInfo.isNullOrBlank()) {
            AdditionalInfoRow(
                title = stringResource(R.string.additional_info),
                content = parkingInfo.additionalInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = ExpandableSectionDefaults.START_PADDING,
                        bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                    )
            )
        }
    }
}

@Composable
fun MiscellaneousSection(
    miscInfo: MiscellaneousInfo?,
    modifier: Modifier = Modifier
) {
    if (miscInfo == null) {
        Text(
            text = stringResource(R.string.no_information_for_section),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        // floor / level
        DetailRow(
            title = stringResource(R.string.located_on_floor),
            content = miscInfo.level?.toString() ?: stringResource(id = R.string.emoji_question),
            modifier = Modifier
                .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                .padding(
                    start = ExpandableSectionDefaults.START_PADDING,
                    bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                )
        )
        // If the level information is given or if the place is located other than ground level,
        // we give information about the elevator to the floor
        if (miscInfo.level != null && miscInfo.level > 0) {
            // If DETAILED elevator data is available display elevator information indented under subtitle.
            if (miscInfo.hasElevator == true && miscInfo.elevatorInfo != null) {
                Text(
                    text = stringResource(R.string.title_elevator),
                    modifier = Modifier
                        .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                        .padding(
                            start = ExpandableSectionDefaults.START_PADDING,
                            bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                        )
                )

                // Elevator to the floor
                DetailRow(
                    title = stringResource(R.string.elevator_to_floor),
                    content = stringResource(
                        id = R.string.emoji_checkmark // condition is already checked above
                    ),
                    modifier = Modifier
                        .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                        .padding(
                            start = ExpandableSectionDefaults.START_PADDING.times(2),
                            bottom = ExpandableSectionDefaults.BOTTOM_PADDING.times(2)
                        )
                )

                // Elevator spacious enough
                val elevator = miscInfo.elevatorInfo
                DetailRow(
                    title = stringResource(R.string.elevator_spacious),
                    content = stringResource(
                        id = elevator.isSpaciousEnough.getEmojiStringRes()
                    ),
                    modifier = Modifier
                        .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                        .padding(
                            start = ExpandableSectionDefaults.START_PADDING.times(2),
                            bottom = ExpandableSectionDefaults.BOTTOM_PADDING.times(2)
                        )
                )

                // Braille buttons
                DetailRow(
                    title = stringResource(R.string.elevator_braille_buttons),
                    content = stringResource(
                        id = elevator.hasBrailleButtons.getEmojiStringRes()
                    ),
                    modifier = Modifier
                        .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                        .padding(
                            start = ExpandableSectionDefaults.START_PADDING.times(2),
                            bottom = ExpandableSectionDefaults.BOTTOM_PADDING.times(2)
                        )
                )

                // Audio announcements
                DetailRow(
                    title = stringResource(R.string.elevator_audio_announcements),
                    content = stringResource(
                        id = elevator.hasAudioAnnouncements.getEmojiStringRes()
                    ),
                    modifier = Modifier
                        .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                        .padding(
                            start = ExpandableSectionDefaults.START_PADDING.times(2),
                            bottom = ExpandableSectionDefaults.BOTTOM_PADDING.times(2)
                        )
                )

            } else {
                // If no detailed elevator data is available show only elevator to floor information
                // without subtitle or extra indentation.
                DetailRow(
                    title = stringResource(R.string.elevator_to_floor),
                    content = stringResource(
                        id = miscInfo.hasElevator.getEmojiStringRes()
                    ),
                    modifier = Modifier
                        .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
                        .padding(
                            start = ExpandableSectionDefaults.START_PADDING,
                            bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                        )
                )
            }
        }

        // Additional info
        if (!miscInfo.additionalInfo.isNullOrBlank()) {
            AdditionalInfoRow(
                title = stringResource(R.string.additional_info),
                content = miscInfo.additionalInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = ExpandableSectionDefaults.START_PADDING,
                        bottom = ExpandableSectionDefaults.BOTTOM_PADDING
                    )
            )
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

@Composable
fun AdditionalInfoRow(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic
        )
    }
}

// Used only for setting the expandable sections to have same padding values
// because for now each section is a unique composable.
object ExpandableSectionDefaults {
    const val FRACTION = 0.65f
    val START_PADDING = 16.dp
    val BOTTOM_PADDING = 6.dp
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailScreen_Preview() {
    val contactInfo = ContactInfo(
        email = "example@mail.com",
        phone = "+41123123123",
        website = "https://www.example.com"
    )

    val entranceSteps = EntranceSteps(
        hasStairs = true,
        stepCount = 1,
        ramp = AccessibilityStatus.FULLY_ACCESSIBLE,
        elevator = AccessibilityStatus.FULLY_ACCESSIBLE
    )

    val entranceDoor = EntranceDoor(
        doorOpening = AccessibilityStatus.FULLY_ACCESSIBLE,
        automaticDoor = false
    )

    val entranceInfo = EntranceInfo(
        stepsInfo = entranceSteps,
        doorInfo = entranceDoor,
        additionalInfo = "Main entrance has a moderately steep ramp and a non-automatic wide door."
    )

    val restroomInfo = RestroomInfo(
        grabRails = AccessibilityStatus.LIMITED_ACCESSIBILITY,
        doorWidth = true,
        roomSpaciousness = AccessibilityStatus.FULLY_ACCESSIBLE,
        hasEmergencyAlarm = false,
        euroKey = false,
        additionalInfo = "Accessible restroom on the first floor."
    )

    val parkingInfo = ParkingInfo(
        hasAccessibleSpots = true,
        spotCount = 3,
        parkingType = ParkingType.SURFACE,
        hasSmoothSurface = true,
        hasElevator = true,
        additionalInfo = null
    )

    val miscInfo = MiscellaneousInfo(
        level = 1,
        hasElevator = true,
//        elevatorInfo = null,
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
        miscInfo = miscInfo,
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
