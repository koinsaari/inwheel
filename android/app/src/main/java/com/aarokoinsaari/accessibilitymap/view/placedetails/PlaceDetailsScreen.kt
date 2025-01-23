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

package com.aarokoinsaari.accessibilitymap.view.placedetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
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
import com.aarokoinsaari.accessibilitymap.domain.intent.PlaceDetailsIntent
import com.aarokoinsaari.accessibilitymap.domain.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo.GeneralAccessibility.EntranceAccessibility
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.LIMITED_ACCESSIBILITY
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.UNKNOWN
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.additionalInfo
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.doorType
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.doorWidth
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.emergencyAlarm
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.euroKey
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.grabRails
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.lift
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.parkingElevator
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.parkingSpotCount
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.parkingSurface
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.parkingType
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.ramp
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.roomManeuver
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.stepsCount
import com.aarokoinsaari.accessibilitymap.domain.state.PlaceDetailsState
import com.aarokoinsaari.accessibilitymap.view.extensions.getEmojiStringRes
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
    onIntent: (PlaceDetailsIntent) -> Unit = { },
) {
    val state by stateFlow.collectAsState()
    val place = state.place

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
            if (place != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                ) {
                    MapCard(
                        place = place,
                        onClick = { onIntent(PlaceDetailsIntent.ClickMap(place)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    )
                    ContactDetailsSection(
                        place = place,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    PlaceDetailsContentSection(place = place, modifier = Modifier)
                }
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    NoInformationAvailableText(
                        text = stringResource(R.string.no_information_available)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceTopAppBar(
    place: Place?,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailsIntent) -> Unit = { },
) {
    TopAppBar(
        title = {
            if (place != null) {
                Column {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(id = place.category.displayNameRes),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
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
            if (place != null) {
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
            }
        },
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
    onClick: (PlaceDetailsIntent) -> Unit = { },
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
                    snippet = stringResource(id = place.category.displayNameRes)
                )
            }
        }
    }
}

@Composable
fun ContactDetailsSection(
    place: Place,
    modifier: Modifier = Modifier,
) {
    val contactItems = listOfNotNull(
        place.contact.phone?.let {
            it to Pair(
                Icons.Outlined.Phone,
                stringResource(id = R.string.content_desc_phone)
            )
        },
        place.contact.email?.let {
            it to Pair(
                Icons.Outlined.Email,
                stringResource(id = R.string.content_desc_email)
            )
        },
        place.contact.website?.let {
            it to Pair(
                Icons.Outlined.Info,
                stringResource(id = R.string.content_desc_website)
            )
        },
        place.contact.address?.let {
            it to Pair(
                Icons.Outlined.LocationOn,
                stringResource(id = R.string.content_desc_address)
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
                        .padding(vertical = 8.dp)
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
fun PlaceDetailsContentSection(
    place: Place,
    modifier: Modifier = Modifier,
) {
    when (place.category.rawValue) {
        "toilets" -> {
            RestroomSection(place.accessibility, modifier)
        }

        "parking" -> {
            ParkingSection(place.accessibility, modifier)
        }

        else -> {
            EntranceSection(place.accessibility, modifier)
            RestroomSection(place.accessibility, modifier)
        }
    }
}

@Composable
fun EntranceSection(
    accessibilityInfo: AccessibilityInfo,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(true) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(
                horizontal = ExpandableSectionDefaults.START_PADDING / 2,
                vertical = ExpandableSectionDefaults.BOTTOM_PADDING
            )
    ) {
        Text(
            text = stringResource(R.string.entrance),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else
                Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy
            )
        ),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy
            )
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start,
            modifier = modifier
        ) {
            // Step count
            DetailRow(
                title = stringResource(R.string.step_count),
                content = accessibilityInfo.stepsCount?.toString()
                    ?: stringResource(id = R.string.emoji_question)
            )

            // Ramp
            DetailRow(
                title = stringResource(R.string.has_ramp),
                content = stringResource(id = accessibilityInfo.ramp.getEmojiStringRes())
            )

            // Elevator
            DetailRow(
                title = stringResource(R.string.lift),
                content = stringResource(id = accessibilityInfo.lift.getEmojiStringRes())
            )

            // Door width
            DetailRow(
                title = stringResource(R.string.door_width),
                content = stringResource(id = accessibilityInfo.doorWidth.getEmojiStringRes())
            )

            // Door type
            DetailRow(
                title = stringResource(R.string.door_type),
                content = accessibilityInfo.doorType ?: stringResource(id = R.string.emoji_question)
            )

            // Additional info
            val additionalInfo = accessibilityInfo.additionalInfo
            if (!additionalInfo.isNullOrBlank()) {
                AdditionalInfoRow(
                    title = stringResource(R.string.additional_info),
                    content = additionalInfo
                )
            }
        }
    }
}

@Composable
fun RestroomSection(
    restroomInfo: AccessibilityInfo?,
    modifier: Modifier = Modifier,
) {
    if (restroomInfo == null) {
        NoInformationAvailableText(text = stringResource(R.string.no_information_for_section))
        return
    }

    var isExpanded by remember { mutableStateOf(true) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(
                horizontal = ExpandableSectionDefaults.START_PADDING / 2,
                vertical = ExpandableSectionDefaults.BOTTOM_PADDING
            )
    ) {
        Text(
            text = stringResource(R.string.restroom),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else
                Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy
            )
        ),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy
            )
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start,
            modifier = modifier
        ) {
            // Grab rails
            DetailRow(
                title = stringResource(R.string.grab_rails),
                content = stringResource(id = restroomInfo.grabRails.getEmojiStringRes())
            )

            // Door width
            DetailRow(
                title = stringResource(R.string.door_width),
                content = stringResource(id = restroomInfo.doorWidth.getEmojiStringRes())
            )

            // Spacious enough
            DetailRow(
                title = stringResource(R.string.spacious_enough),
                content = stringResource(
                    id = restroomInfo.roomManeuver?.getEmojiStringRes() ?: R.string.emoji_question
                )
            )

            // Emergency alarm
            DetailRow(
                title = stringResource(R.string.emergency_alarm),
                content = stringResource(id = restroomInfo.emergencyAlarm.getEmojiStringRes())
            )

            // Euro key
            DetailRow(
                title = stringResource(R.string.euro_key),
                content = stringResource(id = restroomInfo.euroKey.getEmojiStringRes())
            )

            // Additional info
            val additionalInfo = restroomInfo.additionalInfo
            if (!additionalInfo.isNullOrBlank()) {
                AdditionalInfoRow(
                    title = stringResource(R.string.additional_info),
                    content = additionalInfo
                )
            }
        }
    }
}

@Composable
fun ParkingSection(
    parkingInfo: AccessibilityInfo?,
    modifier: Modifier = Modifier,
) {
    if (parkingInfo == null) {
        NoInformationAvailableText(text = stringResource(R.string.no_information_for_section))
        return
    }

    var isExpanded by remember { mutableStateOf(true) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(
                horizontal = ExpandableSectionDefaults.START_PADDING / 2,
                vertical = ExpandableSectionDefaults.BOTTOM_PADDING
            )
    ) {
        Text(
            text = stringResource(R.string.parking),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else
                Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy
            )
        ),
        exit = shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy
            )
        )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start,
            modifier = modifier
        ) {
            // Spot count
            val spotCount = parkingInfo.parkingSpotCount
            DetailRow(
                title = stringResource(R.string.accessible_parking_spots),
                content = when (spotCount) {
                    null -> stringResource(R.string.emoji_question)
                    0 -> stringResource(R.string.emoji_cross)
                    else -> spotCount.toString()
                }
            )

            // Surface
            DetailRow(
                title = stringResource(R.string.parking_surface),
                content = parkingInfo.parkingSurface ?: stringResource(id = R.string.emoji_question)
            )

            val parkingType = parkingInfo.parkingType
            DetailRow(
                title = stringResource(id = R.string.parking_type),
                content = parkingType ?: stringResource(id = R.string.emoji_question)
            )

            // If parking place is not on surface level, we can show if there's an elevator
            if (parkingType?.lowercase() != "surface") {
                DetailRow(
                    title = stringResource(R.string.parking_elevator),
                    content = stringResource(
                        id = parkingInfo.parkingElevator.getEmojiStringRes()
                    )
                )
            }

            // Additional info
            val additionalInfo = parkingInfo.additionalInfo
            if (!additionalInfo.isNullOrBlank()) {
                AdditionalInfoRow(
                    title = stringResource(R.string.additional_info),
                    content = additionalInfo
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    title: String,
    content: String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth(ExpandableSectionDefaults.FRACTION)
            .padding(
                start = ExpandableSectionDefaults.START_PADDING,
                bottom = ExpandableSectionDefaults.BOTTOM_PADDING
            )
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
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = ExpandableSectionDefaults.START_PADDING,
                bottom = ExpandableSectionDefaults.BOTTOM_PADDING
            )
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

@Composable
fun NoInformationAvailableText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = modifier
    )
}

// Used only for setting the expandable sections to have same padding values
// because for now each section is a unique composable.
object ExpandableSectionDefaults {
    const val FRACTION = 0.65f
    val START_PADDING = 24.dp
    val BOTTOM_PADDING = 6.dp
}

@Preview(showBackground = true)
@Composable
private fun PlaceDetailsScreen_Preview() {
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

    val stateFlow = MutableStateFlow(PlaceDetailsState(place = place))
    MaterialTheme {
        PlaceDetailsScreen(stateFlow)
    }
}

@Preview
@Composable
private fun PlaceDetailsScreenPlaceNull_Preview() {
    val stateFlow = MutableStateFlow(PlaceDetailsState(place = null))
    MaterialTheme {
        PlaceDetailsScreen(stateFlow)
    }
}
