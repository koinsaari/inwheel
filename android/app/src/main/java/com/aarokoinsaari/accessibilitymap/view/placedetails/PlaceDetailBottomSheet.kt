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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import com.aarokoinsaari.accessibilitymap.domain.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityInfo.GeneralAccessibility.EntranceAccessibility
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.LIMITED_ACCESSIBILITY
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.AccessibilityStatus.UNKNOWN
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.accessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.entrance
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.indoorAccessibility
import com.aarokoinsaari.accessibilitymap.domain.model.accessibility.restroom
import com.aarokoinsaari.accessibilitymap.view.extensions.getAccessibilityStatusContentDescStringRes
import com.aarokoinsaari.accessibilitymap.view.extensions.getAccessibilityStatusDrawableRes
import com.aarokoinsaari.accessibilitymap.view.theme.AccessibilityMapTheme

@ExperimentalMaterial3Api
@Composable
fun PlaceDetailBottomSheet(
    place: Place,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            AccessibilityStatusDisplaySection(place)
        }
        if (place.category.rawValue != "toilets") {
            item {
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                ContactInfoSection(place.contact)
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

@Composable
fun AccessibilityStatusDisplaySection(
    place: Place,
    modifier: Modifier = Modifier,
) {
    if (place.accessibility.accessibilityStatus == null ||
        place.accessibility.accessibilityStatus == UNKNOWN
    ) {
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
                    descriptionText = stringResource(id = R.string.image_descript_fully_accessible),
                    contentDescription = stringResource(id = R.string.content_desc_fully_accessible)
                )

                AccessibilityStatusItem(
                    imageRes = R.drawable.accessibility_status_yellow,
                    descriptionText = stringResource(id = R.string.image_descript_limited_accessibility),
                    contentDescription = stringResource(id = R.string.content_desc_limited_accessibility)
                )

                AccessibilityStatusItem(
                    imageRes = R.drawable.accessibility_status_red,
                    descriptionText = stringResource(id = R.string.image_descript_not_accessible),
                    contentDescription = stringResource(id = R.string.content_desc_not_accessible)
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
                    id = place.accessibility.accessibilityStatus
                        .getAccessibilityStatusStringRes()
                ),
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(
                        id = place.accessibility.accessibilityStatus
                            .getAccessibilityStatusDrawableRes()
                    ),
                    contentDescription = stringResource(
                        id = place.accessibility.accessibilityStatus
                            .getAccessibilityStatusContentDescStringRes()
                    ),
                    modifier = Modifier.size(86.dp)
                )

                VerticalDivider(thickness = 1.dp, modifier = Modifier.padding(4.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val alpha = 0.7f

                    listOf(
                        place.accessibility.entrance?.accessibilityStatus to
                                place.accessibility.entrance?.accessibilityStatus
                                    .getEntranceAccessibilityLabelStringRes(),
                        place.accessibility.indoorAccessibility to
                                place.accessibility.indoorAccessibility
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
                                    LIMITED_ACCESSIBILITY -> Icons.Outlined.Warning
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
    descriptionText: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    imageSize: Dp = 86.dp,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(imageSize)
        )
        Text(
            text = descriptionText,
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(imageSize)
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

private fun AccessibilityStatus?.getAccessibilityStatusStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.fully_accessible_status_title
        LIMITED_ACCESSIBILITY -> R.string.limited_accessibility_status_title
        NOT_ACCESSIBLE -> R.string.not_accessible_status_title
        else -> R.string.wheelchair_access_unknown
    }

private fun AccessibilityStatus?.getEntranceAccessibilityLabelStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.entrance_fully_accessible_label
        LIMITED_ACCESSIBILITY -> R.string.entrance_limited_accessibility_label
        NOT_ACCESSIBLE -> R.string.entrance_not_accessible_label
        UNKNOWN -> R.string.wheelchair_access_unknown
        null -> R.string.wheelchair_access_unknown
    }

private fun AccessibilityStatus?.getIndoorAccessibilityStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.indoor_accessible_status_label
        LIMITED_ACCESSIBILITY -> R.string.indoor_limited_accessibility_status_label
        NOT_ACCESSIBLE -> R.string.indoor_not_accessible_status_label
        UNKNOWN -> R.string.wheelchair_access_unknown
        null -> R.string.wheelchair_access_unknown
    }

private fun AccessibilityStatus?.getRestroomAccessibilityStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.restroom_accessibility_status_label
        LIMITED_ACCESSIBILITY -> R.string.restroom_limited_accessibility_status_label
        NOT_ACCESSIBLE -> R.string.restroom_not_accessible_status_label
        UNKNOWN -> R.string.wheelchair_access_unknown
        null -> R.string.wheelchair_access_unknown
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
        indoorAccessibility = LIMITED_ACCESSIBILITY,
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

    AccessibilityMapTheme {
        PlaceDetailBottomSheet(place)
    }
}
