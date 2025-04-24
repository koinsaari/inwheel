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

package com.aarokoinsaari.inwheel.view.placedetails

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.domain.intent.PlaceDetailIntent
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.UNKNOWN
import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.domain.model.PlaceDetailProperty

@Composable
fun PlaceDetailsGeneralAccessibilitySection(
    place: Place,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    val accessibilityStatus = place.generalAccessibility
    if (accessibilityStatus == UNKNOWN || accessibilityStatus == null) {
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
                PlaceDetailAccessibilityStatusItem(
                    imageRes = R.drawable.accessibility_status_green,
                    contentDescription = R.string.content_desc_fully_accessible,
                    imageText = R.string.image_desc_fully_accessible,
                    onClick = {
                        onIntent(
                            PlaceDetailIntent.UpdateGeneralAccessibility(
                                place = place,
                                status = FULLY_ACCESSIBLE
                            )
                        )
                    }
                )
                PlaceDetailAccessibilityStatusItem(
                    imageRes = R.drawable.accessibility_status_yellow,
                    contentDescription = R.string.content_desc_partially_accessible,
                    imageText = R.string.image_desc_partially_accessible,
                    onClick = {
                        onIntent(
                            PlaceDetailIntent.UpdateGeneralAccessibility(
                                place = place,
                                status = PARTIALLY_ACCESSIBLE
                            )
                        )
                    }
                )
                PlaceDetailAccessibilityStatusItem(
                    imageRes = R.drawable.accessibility_status_red,
                    contentDescription = R.string.content_desc_not_accessible,
                    imageText = R.string.image_desc_not_accessible,
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
                    id = place.generalAccessibility
                        .getAccessibilityStatusStringRes()
                ),
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PlaceDetailAccessibilityStatusItem(
                    imageRes = place.generalAccessibility
                        .getAccessibilityStatusDrawableRes(),
                    contentDescription = place.generalAccessibility
                        .getAccessibilityStatusContentDescStringRes(),
                    onClick = {
                        onIntent(
                            PlaceDetailIntent.OpenDialog(
                                place,
                                PlaceDetailProperty.GENERAL_ACCESSIBILITY
                            )
                        )
                    }
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val alpha = 0.7f
                    listOf(
                        Triple(
                            place.entranceAccessibility,
                            place.entranceAccessibility.getEntranceAccessibilityLabelStringRes(),
                            PlaceDetailProperty.ENTRANCE_ACCESSIBILITY
                        ),
                        Triple(
                            place.indoorAccessibility,
                            place.indoorAccessibility.getIndoorAccessibilityStringRes(),
                            PlaceDetailProperty.INDOOR_ACCESSIBILITY
                        ),
                        Triple(
                            place.restroomAccessibility,
                            place.restroomAccessibility.getRestroomAccessibilityStringRes(),
                            PlaceDetailProperty.RESTROOM_ACCESSIBILITY
                        )
                    ).forEach { (status, labelRes, property) ->
                        var expanded by remember { mutableStateOf(false) }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }
                        ) {
                            Icon(
                                imageVector = when (status) {
                                    FULLY_ACCESSIBLE -> Icons.Outlined.Check
                                    PARTIALLY_ACCESSIBLE -> Icons.Outlined.Warning
                                    NOT_ACCESSIBLE -> Icons.Outlined.Close
                                    UNKNOWN, null -> Icons.Outlined.QuestionMark
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                                modifier = Modifier.size(16.dp)
                            )

                            Text(
                                text = stringResource(id = labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                                modifier = Modifier.weight(1f)
                            )

                            Box {
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                                    modifier = Modifier.size(16.dp)
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    // Property-specific menu options
                                    when (property) {
                                        PlaceDetailProperty.ENTRANCE_ACCESSIBILITY -> {
                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = FULLY_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_checkmark),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(id = R.string.entrance_fully_accessible_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )

                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = PARTIALLY_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_warning),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(id = R.string.entrance_partially_accessible_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )

                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = NOT_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_cross),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(id = R.string.entrance_not_accessible_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )
                                        }

                                        PlaceDetailProperty.INDOOR_ACCESSIBILITY -> {
                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = FULLY_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_checkmark),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(id = R.string.indoor_fully_accessible_status_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )

                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = PARTIALLY_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_warning),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(id = R.string.indoor_partially_accessible_status_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )

                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = NOT_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_cross),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(id = R.string.indoor_not_accessible_status_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )
                                        }

                                        PlaceDetailProperty.RESTROOM_ACCESSIBILITY -> {
                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = FULLY_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_checkmark),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(id = R.string.restroom_fully_accessible_status_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )

                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = PARTIALLY_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_warning),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(
                                                                id = R.string.restroom_partially_accessible_status_label
                                                            ),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )

                                            DropdownMenuItem(
                                                onClick = {
                                                    onIntent(
                                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                                            place = place,
                                                            detailProperty = property,
                                                            status = NOT_ACCESSIBLE
                                                        )
                                                    )
                                                    expanded = false
                                                },
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = stringResource(id = R.string.emoji_cross),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                        Text(
                                                            text = stringResource(id = R.string.restroom_not_accessible_status_label),
                                                            style = MaterialTheme.typography.labelSmall,
                                                        )
                                                    }
                                                }
                                            )
                                        }

                                        else -> {
                                            // Fallback for other property types, though shouldn't need
                                            listOf(
                                                R.string.accessibility_option_fully_accessible to FULLY_ACCESSIBLE,
                                                R.string.accessibility_option_partially_accessible to PARTIALLY_ACCESSIBLE,
                                                R.string.accessibility_option_not_accessible to NOT_ACCESSIBLE
                                            ).forEach { (displayText, accessibilityStatus) ->
                                                DropdownMenuItem(
                                                    onClick = {
                                                        onIntent(
                                                            PlaceDetailIntent.UpdateAccessibilityDetail(
                                                                place = place,
                                                                detailProperty = property,
                                                                status = accessibilityStatus
                                                            )
                                                        )
                                                        expanded = false
                                                    },
                                                    text = { Text(text = stringResource(id = displayText)) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceDetailAccessibilityStatusItem(
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

private fun AccessibilityStatus?.getAccessibilityStatusStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.general_status_title_fully_accessible
        PARTIALLY_ACCESSIBLE -> R.string.general_status_title_partially_accessible
        NOT_ACCESSIBLE -> R.string.general_status_title_not_accessible
        else -> R.string.unknown_accessibility
    }

private fun AccessibilityStatus?.getEntranceAccessibilityLabelStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.entrance_fully_accessible_label
        PARTIALLY_ACCESSIBLE -> R.string.entrance_partially_accessible_label
        NOT_ACCESSIBLE -> R.string.entrance_not_accessible_label
        else -> R.string.entrance_unknown_label
    }

private fun AccessibilityStatus?.getIndoorAccessibilityStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.indoor_fully_accessible_status_label
        PARTIALLY_ACCESSIBLE -> R.string.indoor_partially_accessible_status_label
        NOT_ACCESSIBLE -> R.string.indoor_not_accessible_status_label
        else -> R.string.indoor_unknown_label
    }

private fun AccessibilityStatus?.getRestroomAccessibilityStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.restroom_fully_accessible_status_label
        PARTIALLY_ACCESSIBLE -> R.string.restroom_partially_accessible_status_label
        NOT_ACCESSIBLE -> R.string.restroom_not_accessible_status_label
        else -> R.string.restroom_unknown_label
    }

private fun AccessibilityStatus?.getAccessibilityStatusContentDescStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.content_desc_fully_accessible
        PARTIALLY_ACCESSIBLE -> R.string.content_desc_partially_accessible
        NOT_ACCESSIBLE -> R.string.content_desc_not_accessible
        else -> R.string.content_desc_unknown_accessibility
    }

private fun AccessibilityStatus?.getAccessibilityStatusDrawableRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.drawable.accessibility_status_green
        PARTIALLY_ACCESSIBLE -> R.drawable.accessibility_status_yellow
        NOT_ACCESSIBLE -> R.drawable.accessibility_status_red
        else -> R.drawable.accessibility_status_grey
    }
