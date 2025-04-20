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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.domain.intent.PlaceDetailIntent
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceDetailProperty

@Composable
fun PlaceDetailItem(
    place: Place,
    detailProperty: PlaceDetailProperty,
    value: String,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(Modifier.width(130.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = detailProperty.labelRes),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(
                        id = R.string.content_desc_property_info,
                        stringResource(id = detailProperty.labelRes)
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(12.dp)
                        .padding(top = 1.dp, start = 1.dp)
                        .clickable { 
                            onIntent(
                                PlaceDetailIntent.OpenDialog(
                                    place = place,
                                    property = detailProperty
                                )
                            )
                        }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable { expanded = true }
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = stringResource(
                    id = R.string.content_desc_show_options,
                    stringResource(id = detailProperty.labelRes)
                ),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            PlaceDetailPropertyOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = { intent ->
                    onIntent(intent)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun PlaceDetailPropertyOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    when (detailProperty) {
        PlaceDetailProperty.DOOR_TYPE -> {
            PlaceDetailDoorTypeOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.EURO_KEY -> {
            PlaceDetailBooleanOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        else -> {
            // Generic handler for all accessibility status options
            AccessibilityPropertyOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }
    }
}

@Composable
private fun PlaceDetailDoorTypeOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val doorTypeOptionIds = setOf(
        R.string.door_type_automatic_sliding,
        R.string.door_type_automatic_swing,
        R.string.door_type_manual_swing,
        R.string.door_type_manual_sliding,
        R.string.door_type_revolving,
        R.string.door_type_double
    )
    doorTypeOptionIds.forEach { doorTypeId ->
        val doorType = stringResource(id = doorTypeId)
        DropdownMenuItem(
            onClick = {
                onOptionSelected(
                    PlaceDetailIntent.UpdateAccessibilityDetailString(
                        place = place,
                        detailProperty = detailProperty,
                        value = doorType
                    )
                )
            },
            text = {
                Text(
                    text = doorType,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
            }
        )
    }
}

@Composable
private fun PlaceDetailBooleanOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val booleanOptions = listOf(
        R.string.checkmark_yes to true,
        R.string.cross_no to false,
    )
    booleanOptions.forEach { (displayText, value) ->
        DropdownMenuItem(
            onClick = {
                onOptionSelected(
                    PlaceDetailIntent.UpdateAccessibilityDetailBoolean(
                        place = place,
                        detailProperty = detailProperty,
                        value = value
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(id = displayText),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
            }
        )
    }
}

@Composable
private fun AccessibilityPropertyOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    detailProperty.getAccessibilityOptions().forEach { (displayText, status) ->
        DropdownMenuItem(
            onClick = {
                onOptionSelected(
                    PlaceDetailIntent.UpdateAccessibilityDetail(
                        place = place,
                        detailProperty = detailProperty,
                        status = status
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(id = displayText),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
            }
        )
    }
}

private fun PlaceDetailProperty.getAccessibilityOptions(): List<Pair<Int, AccessibilityStatus>> =
    when (this) {
        PlaceDetailProperty.STEP_COUNT -> listOf(
            R.string.step_count_option_none to FULLY_ACCESSIBLE,
            R.string.step_count_option_one to PARTIALLY_ACCESSIBLE,
            R.string.step_count_option_multiple to NOT_ACCESSIBLE
        )

        PlaceDetailProperty.STEP_HEIGHT -> listOf(
            R.string.step_height_option_low to FULLY_ACCESSIBLE,
            R.string.step_height_option_medium to PARTIALLY_ACCESSIBLE,
            R.string.step_height_option_high to NOT_ACCESSIBLE
        )

        PlaceDetailProperty.LIFT -> listOf(
            R.string.lift_option_accessible to FULLY_ACCESSIBLE,
            R.string.lift_option_partially_accessible to PARTIALLY_ACCESSIBLE,
            R.string.lift_option_none to NOT_ACCESSIBLE
        )

        PlaceDetailProperty.RAMP -> listOf(
            R.string.ramp_option_accessible to FULLY_ACCESSIBLE,
            R.string.ramp_option_partial to PARTIALLY_ACCESSIBLE,
            R.string.ramp_option_none to NOT_ACCESSIBLE
        )

        PlaceDetailProperty.DOOR_WIDTH, PlaceDetailProperty.ENTRANCE_WIDTH -> listOf(
            R.string.door_width_option_wide to FULLY_ACCESSIBLE,
            R.string.door_width_option_standard to PARTIALLY_ACCESSIBLE,
            R.string.door_width_option_narrow to NOT_ACCESSIBLE
        )

        PlaceDetailProperty.ROOM_MANEUVER -> listOf(
            R.string.room_maneuver_option_spacious to FULLY_ACCESSIBLE,
            R.string.room_maneuver_option_limited to PARTIALLY_ACCESSIBLE,
            R.string.room_maneuver_option_tight to NOT_ACCESSIBLE
        )

        PlaceDetailProperty.GRAB_RAILS -> listOf(
            R.string.grab_rails_option_both_sides to FULLY_ACCESSIBLE,
            R.string.grab_rails_option_one_side to PARTIALLY_ACCESSIBLE,
            R.string.grab_rails_option_none to NOT_ACCESSIBLE
        )

        PlaceDetailProperty.EMERGENCY_ALARM -> listOf(
            R.string.emergency_alarm_option_available to FULLY_ACCESSIBLE,
            R.string.emergency_alarm_option_limited to PARTIALLY_ACCESSIBLE,
            R.string.emergency_alarm_option_none to NOT_ACCESSIBLE
        )

        else -> listOf(
            R.string.accessibility_option_fully_accessible to FULLY_ACCESSIBLE,
            R.string.accessibility_option_partially_accessible to PARTIALLY_ACCESSIBLE,
            R.string.accessibility_option_not_accessible to NOT_ACCESSIBLE
        )
    }
