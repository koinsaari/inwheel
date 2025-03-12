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

package com.aarokoinsaari.accessibilitymap.view.placedetails

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.domain.intent.PlaceDetailIntent
import com.aarokoinsaari.accessibilitymap.domain.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.UNKNOWN
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.EntranceAccessibility
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.GeneralAccessibility
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.RestroomAccessibility
import com.aarokoinsaari.accessibilitymap.domain.state.PlaceDetailState
import com.aarokoinsaari.accessibilitymap.view.extensions.getAccessibilityStatusContentDescStringRes
import com.aarokoinsaari.accessibilitymap.view.extensions.getAccessibilityStatusDrawableRes
import com.aarokoinsaari.accessibilitymap.view.theme.AccessibilityMapTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@ExperimentalMaterial3Api
@Composable
fun PlaceDetailBottomSheet(
    stateFlow: StateFlow<PlaceDetailState>,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    val state = stateFlow.collectAsState()
    val place = state.value.place
    if (place != null) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                AccessibilityStatusDisplaySection(
                    place = place,
                    onIntent = onIntent
                )
            }
            if (place.category.rawValue != "toilets") {
                item {
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                    ContactInfoSection(place.contact)
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                }
            }
            item {
                Text(
                    text = stringResource(id = R.string.details_title_entrance),
                    style = MaterialTheme.typography.labelSmall
                )
                EntranceDetailsSection(
                    place = place,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                )
                Spacer(Modifier.padding(vertical = 6.dp))
                Text(
                    text = stringResource(id = R.string.details_title_restroom),
                    style = MaterialTheme.typography.labelSmall
                )
                RestroomDetailsSection(
                    place = place,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                )
                Spacer(Modifier.padding(vertical = 6.dp))
            }
        }

        if (state.value.showGeneralAccessibilityUpdateDialog) {
            GeneralAccessibilityUpdateDialog(
                place = place,
                onIntent = onIntent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralAccessibilityUpdateDialog(
    place: Place,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    BasicAlertDialog(
        onDismissRequest = { onIntent(PlaceDetailIntent.CloseGeneralAccessibilityUpdateDialog(place)) },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.widthIn(max = 500.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.general_accessibility_update_dialog_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(Modifier.weight(1f)) {
                            AccessibilityStatusItem(
                                imageRes = R.drawable.accessibility_status_green,
                                contentDescription = R.string.content_desc_fully_accessible,
                                imageText = R.string.image_descript_fully_accessible,
                                onClick = {
                                    onIntent(
                                        PlaceDetailIntent.UpdateGeneralAccessibility(
                                            place,
                                            FULLY_ACCESSIBLE
                                        )
                                    )
                                }
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            AccessibilityStatusItem(
                                imageRes = R.drawable.accessibility_status_yellow,
                                contentDescription = R.string.content_desc_partially_accessible,
                                imageText = R.string.image_descript_partially_accessible,
                                onClick = {
                                    onIntent(
                                        PlaceDetailIntent.UpdateGeneralAccessibility(
                                            place,
                                            PARTIALLY_ACCESSIBLE
                                        )
                                    )
                                }
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            AccessibilityStatusItem(
                                imageRes = R.drawable.accessibility_status_red,
                                contentDescription = R.string.content_desc_not_accessible,
                                imageText = R.string.image_descript_not_accessible,
                                onClick = {
                                    onIntent(
                                        PlaceDetailIntent.UpdateGeneralAccessibility(
                                            place,
                                            NOT_ACCESSIBLE
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilityStatusDisplaySection(
    place: Place,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    val accessibilityStatus = place.accessibility.general?.accessibilityStatus
    if (accessibilityStatus == null || accessibilityStatus == UNKNOWN) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
        ) {
            Text(
                text = stringResource(id = R.string.how_accessible_question),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AccessibilityStatusItem(
                    imageRes = R.drawable.accessibility_status_green,
                    contentDescription = R.string.content_desc_fully_accessible,
                    imageText = R.string.image_descript_fully_accessible,
                    onClick = {
                        onIntent(
                            PlaceDetailIntent.UpdateGeneralAccessibility(
                                place = place,
                                status = FULLY_ACCESSIBLE
                            )
                        )
                    }
                )
                AccessibilityStatusItem(
                    imageRes = R.drawable.accessibility_status_yellow,
                    contentDescription = R.string.content_desc_partially_accessible,
                    imageText = R.string.image_descript_partially_accessible,
                    onClick = {
                        onIntent(
                            PlaceDetailIntent.UpdateGeneralAccessibility(
                                place = place,
                                status = PARTIALLY_ACCESSIBLE
                            )
                        )
                    }
                )
                AccessibilityStatusItem(
                    imageRes = R.drawable.accessibility_status_red,
                    contentDescription = R.string.content_desc_not_accessible,
                    imageText = R.string.image_descript_not_accessible,
                    onClick = {
                        onIntent(
                            PlaceDetailIntent.UpdateGeneralAccessibility(
                                place = place,
                                status = NOT_ACCESSIBLE
                            )
                        )
                    }
                )
            }
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(
                    id = place.accessibility.general.accessibilityStatus
                        .getAccessibilityStatusStringRes()
                ),
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AccessibilityStatusItem(
                    imageRes = place.accessibility.general.accessibilityStatus
                        .getAccessibilityStatusDrawableRes(),
                    contentDescription = place.accessibility.general.accessibilityStatus
                        .getAccessibilityStatusContentDescStringRes(),
                    onClick = {
                        onIntent(
                            PlaceDetailIntent.OpenGeneralAccessibilityUpdateDialog(place)
                        )
                    }
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val alpha = 0.7f

                    listOf(
                        place.accessibility.entrance?.accessibilityStatus to
                                place.accessibility.entrance?.accessibilityStatus
                                    .getEntranceAccessibilityLabelStringRes(),
                        place.accessibility.general.indoorAccessibility to
                                place.accessibility.general.indoorAccessibility
                                    .getIndoorAccessibilityStringRes(),
                        place.accessibility.restroom?.accessibility to
                                place.accessibility.restroom?.accessibility
                                    .getRestroomAccessibilityStringRes()
                    ).forEach { (status, labelRes) ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (status) {
                                    FULLY_ACCESSIBLE -> Icons.Outlined.Check
                                    PARTIALLY_ACCESSIBLE -> Icons.Outlined.Warning
                                    NOT_ACCESSIBLE -> Icons.Outlined.Close
                                    UNKNOWN, null -> Icons.Outlined.QuestionMark
                                },
                                contentDescription = null, // TODO
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(id = labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccessibilityStatusItem(
    @DrawableRes imageRes: Int,
    @StringRes contentDescription: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    imageSize: Dp = 86.dp,
    @StringRes imageText: Int? = null,
) {
    if (imageText != null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = stringResource(id = contentDescription),
                modifier = modifier
                    .size(imageSize)
                    .clickable(onClick = onClick)
            )
            Text(
                text = stringResource(id = imageText),
                style = MaterialTheme.typography.labelSmall,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(86.dp)
            )
        }
    } else {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = stringResource(id = contentDescription),
            modifier = modifier
                .size(imageSize)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun ContactInfoSection(
    info: ContactInfo,
    modifier: Modifier = Modifier,
) {
    val contactItems = listOfNotNull(
        info.phone?.let {
            it to Pair(
                Icons.Outlined.Phone,
                stringResource(id = R.string.content_desc_phone)
            )
        },
        info.email?.let {
            it to Pair(
                Icons.Outlined.Email,
                stringResource(id = R.string.content_desc_email)
            )
        },
        info.website?.let {
            it to Pair(
                Icons.Outlined.Language,
                stringResource(id = R.string.content_desc_website)
            )
        }
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        if (contactItems.isNotEmpty()) {
            contactItems.forEach { (info, icon) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon.first,
                        contentDescription = icon.second,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Text(
                text = stringResource(id = R.string.no_contact_info_available),
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun EntranceDetailsSection(place: Place, modifier: Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        val stepCount = place.accessibility.entrance?.stepCount
        DetailItem(
            label = stringResource(id = R.string.details_entrance_step_count),
            value = stepCount?.toString() ?: stringResource(id = R.string.emoji_question)
        )

        if (stepCount != 0) {
            DetailItem(
                label = stringResource(id = R.string.details_entrance_step_height),
                value = stringResource(
                    id = place.accessibility.entrance?.stepHeight.getAccessibilityStatusEmojiStringRes()
                ),
            )

            DetailItem(
                label = stringResource(id = R.string.details_entrance_ramp),
                value = stringResource(
                    id = place.accessibility.entrance?.ramp.getAccessibilityStatusEmojiStringRes()
                ),
            )

            DetailItem(
                label = stringResource(id = R.string.details_entrance_lift),
                value = stringResource(
                    id = place.accessibility.entrance?.lift.getAccessibilityStatusEmojiStringRes()
                ),
            )
        }

        val additionalInfo = place.accessibility.entrance?.additionalInfo
        if (additionalInfo != null && additionalInfo.isNotEmpty()) {
            DetailItem(
                label = stringResource(id = R.string.details_additional_info),
                value = additionalInfo,
                isAdditionalInfo = true
            )
        }
    }
}

@Composable
fun RestroomDetailsSection(place: Place, modifier: Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        DetailItem(
            label = stringResource(id = R.string.details_restroom_door_width),
            value = stringResource(
                id = place.accessibility.restroom?.doorWidth.getAccessibilityStatusEmojiStringRes()
            )
        )

        DetailItem(
            label = stringResource(id = R.string.details_restroom_room_maneuver),
            value = stringResource(
                id = place.accessibility.restroom?.roomManeuver.getAccessibilityStatusEmojiStringRes()
            )
        )

        DetailItem(
            label = stringResource(id = R.string.details_restroom_grab_rails),
            value = stringResource(
                id = place.accessibility.restroom?.grabRails.getAccessibilityStatusEmojiStringRes()
            )
        )

        DetailItem(
            label = stringResource(id = R.string.details_restroom_sink),
            value = stringResource(
                id = place.accessibility.restroom?.sink.getAccessibilityStatusEmojiStringRes()
            )
        )

        DetailItem(
            label = stringResource(id = R.string.details_restroom_toilet_seat),
            value = stringResource(
                id = place.accessibility.restroom?.toiletSeat.getAccessibilityStatusEmojiStringRes()
            )
        )

        DetailItem(
            label = stringResource(id = R.string.details_restroom_emergency_alarm),
            value = stringResource(
                id = place.accessibility.restroom?.emergencyAlarm.getAccessibilityStatusEmojiStringRes()
            )
        )

        DetailItem(
            label = stringResource(id = R.string.details_restroom_accessible_via),
            value = place.accessibility.restroom?.accessibleVia
                ?: stringResource(id = R.string.emoji_question)
        )

        DetailItem(
            label = stringResource(id = R.string.details_restroom_euro_key),
            value = stringResource(id = place.accessibility.restroom?.euroKey.getBooleanEmojiStringRes())
        )

        val additionalInfo = place.accessibility.restroom?.additionalInfo
        if (additionalInfo != null && additionalInfo.isNotEmpty()) {
            DetailItem(
                label = stringResource(id = R.string.details_additional_info),
                value = additionalInfo,
                isAdditionalInfo = true
            )
        }
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isAdditionalInfo: Boolean = false,
) {
    if (isAdditionalInfo) {
        Column(modifier) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic
            )
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun AccessibilityStatus?.getAccessibilityStatusStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.fully_accessible_status_title
        PARTIALLY_ACCESSIBLE -> R.string.limited_accessibility_status_title
        NOT_ACCESSIBLE -> R.string.not_accessible_status_title
        else -> R.string.wheelchair_access_unknown
    }

private fun AccessibilityStatus?.getAccessibilityStatusEmojiStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.emoji_checkmark
        PARTIALLY_ACCESSIBLE -> R.string.emoji_warning
        NOT_ACCESSIBLE -> R.string.emoji_cross
        else -> R.string.emoji_question
    }

private fun Boolean?.getBooleanEmojiStringRes(): Int =
    when (this) {
        true -> R.string.emoji_checkmark
        false -> R.string.emoji_cross
        null -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getEntranceAccessibilityLabelStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.entrance_fully_accessible_label
        PARTIALLY_ACCESSIBLE -> R.string.entrance_limited_accessibility_label
        NOT_ACCESSIBLE -> R.string.entrance_not_accessible_label
        UNKNOWN -> R.string.entrance_unknown_label
        null -> R.string.entrance_unknown_label
    }

private fun AccessibilityStatus?.getIndoorAccessibilityStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.indoor_accessible_status_label
        PARTIALLY_ACCESSIBLE -> R.string.indoor_limited_accessibility_status_label
        NOT_ACCESSIBLE -> R.string.indoor_not_accessible_status_label
        UNKNOWN -> R.string.indoor_unknown_label
        null -> R.string.indoor_unknown_label
    }

private fun AccessibilityStatus?.getRestroomAccessibilityStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.restroom_accessibility_status_label
        PARTIALLY_ACCESSIBLE -> R.string.restroom_limited_accessibility_status_label
        NOT_ACCESSIBLE -> R.string.restroom_not_accessible_status_label
        UNKNOWN -> R.string.restroom_unknown_label
        null -> R.string.restroom_unknown_label
    }

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PlaceDetailBottomSheet_Preview() {
    val contactInfo = ContactInfo(
        email = "example@mail.com",
        phone = "+41 21 123 45 67",
        website = "https://www.example.com",
        address = "Grande Place 1, Vevey 1800"
    )
    val entrance = EntranceAccessibility(
        accessibilityStatus = PARTIALLY_ACCESSIBLE,
        additionalInfo = "Entrance is fully accessible with automatic doors.",
        stepCount = 1,
        stepHeight = PARTIALLY_ACCESSIBLE,
        ramp = PARTIALLY_ACCESSIBLE,
        lift = null,
        width = FULLY_ACCESSIBLE,
        type = "Automatic"
    )
    val restroom = RestroomAccessibility(
        accessibility = NOT_ACCESSIBLE,
        doorWidth = FULLY_ACCESSIBLE,
        roomManeuver = NOT_ACCESSIBLE,
        grabRails = PARTIALLY_ACCESSIBLE,
        toiletSeat = FULLY_ACCESSIBLE,
        emergencyAlarm = NOT_ACCESSIBLE,
        sink = FULLY_ACCESSIBLE,
        euroKey = false,
        accessibleVia = "Elevator",
        additionalInfo = "Not accessible restroom on the ground floor."
    )
    val general = GeneralAccessibility(
        accessibilityStatus = PARTIALLY_ACCESSIBLE,
        indoorAccessibility = PARTIALLY_ACCESSIBLE,
        additionalInfo = "This location is mostly accessible."
    )
    val place = Place(
        id = "1",
        name = "Example Cafe",
        category = PlaceCategory.CAFE,
        lat = 46.460071,
        lon = 6.843391,
        contact = contactInfo,
        accessibility = AccessibilityInfo(
            general = general,
            entrance = entrance,
            restroom = restroom
        )
    )
    AccessibilityMapTheme {
        PlaceDetailBottomSheet(
            stateFlow = MutableStateFlow(PlaceDetailState(place))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PlaceDetailBottomSheetUnknownAccessibility_Preview() {
    val contactInfo = ContactInfo(
        email = "example@mail.com",
        phone = "+41 21 123 45 67",
        website = "https://www.example.com",
        address = "Grande Place 1, Vevey 1800"
    )
    val place = Place(
        id = "1",
        name = "Example Cafe",
        category = PlaceCategory.CAFE,
        lat = 46.460071,
        lon = 6.843391,
        contact = contactInfo,
        accessibility = AccessibilityInfo(
            general = null,
            entrance = null,
            restroom = null
        )
    )

    AccessibilityMapTheme {
        PlaceDetailBottomSheet(
            stateFlow = MutableStateFlow(PlaceDetailState(place))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GeneralAccessibilityUpdateDialog_Preview() {
    val contactInfo = ContactInfo(
        email = "example@mail.com",
        phone = "+41 21 123 45 67",
        website = "https://www.example.com",
        address = "Grande Place 1, Vevey 1800"
    )
    val entrance = EntranceAccessibility(
        accessibilityStatus = PARTIALLY_ACCESSIBLE,
        additionalInfo = "Entrance is fully accessible with automatic doors.",
        stepCount = 1,
        stepHeight = PARTIALLY_ACCESSIBLE,
        ramp = PARTIALLY_ACCESSIBLE,
        lift = null,
        width = FULLY_ACCESSIBLE,
        type = "Automatic"
    )
    val restroom = RestroomAccessibility(
        accessibility = NOT_ACCESSIBLE,
        doorWidth = FULLY_ACCESSIBLE,
        roomManeuver = NOT_ACCESSIBLE,
        grabRails = PARTIALLY_ACCESSIBLE,
        toiletSeat = FULLY_ACCESSIBLE,
        emergencyAlarm = NOT_ACCESSIBLE,
        sink = FULLY_ACCESSIBLE,
        euroKey = false,
        accessibleVia = "Elevator",
        additionalInfo = "Not accessible restroom on the ground floor."
    )
    val general = GeneralAccessibility(
        accessibilityStatus = PARTIALLY_ACCESSIBLE,
        indoorAccessibility = PARTIALLY_ACCESSIBLE,
        additionalInfo = "This location is mostly accessible."
    )
    val place = Place(
        id = "1",
        name = "Example Cafe",
        category = PlaceCategory.CAFE,
        lat = 46.460071,
        lon = 6.843391,
        contact = contactInfo,
        accessibility = AccessibilityInfo(
            general = general,
            entrance = entrance,
            restroom = restroom
        )
    )

    AccessibilityMapTheme {
        GeneralAccessibilityUpdateDialog(
            place = place
        )
    }
}
