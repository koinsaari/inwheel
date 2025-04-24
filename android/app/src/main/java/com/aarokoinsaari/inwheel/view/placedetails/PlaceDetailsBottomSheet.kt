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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.domain.intent.PlaceDetailIntent
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.UNKNOWN
import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.domain.model.PlaceCategory
import com.aarokoinsaari.inwheel.domain.model.PlaceDetailProperty
import com.aarokoinsaari.inwheel.domain.state.PlaceDetailState
import com.aarokoinsaari.inwheel.view.components.Footer
import com.aarokoinsaari.inwheel.view.theme.InWheelMapTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@ExperimentalMaterial3Api
@Composable
fun PlaceDetailsBottomSheet(
    stateFlow: StateFlow<PlaceDetailState>,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    val state = stateFlow.collectAsState()
    val place = state.value.place
    val activeDialog = state.value.activeDialog

    if (place != null) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                PlaceDetailsGeneralAccessibilitySection(
                    place = place,
                    onIntent = onIntent,
                )
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }
            if (place.category.rawValue != "toilets") {
                item {
                    PlaceDetailsContactInfoSection(place)
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                }
            }
            item {
                Text(
                    text = stringResource(id = R.string.entrance),
                    style = MaterialTheme.typography.labelSmall
                )
                PlaceDetailsEntranceDetailsSection(
                    place = place,
                    onIntent = onIntent,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                )
                Spacer(Modifier.padding(vertical = 6.dp))
                Text(
                    text = stringResource(id = R.string.restroom),
                    style = MaterialTheme.typography.labelSmall
                )
                PlaceDetailsRestroomDetailsSection(
                    place = place,
                    onIntent = onIntent,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                )
                Spacer(Modifier.padding(vertical = 6.dp))
                Text(
                    text = stringResource(id = R.string.additional_info),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = place.additionalInfo
                        ?: stringResource(id = R.string.additional_info_label),
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = if (place.additionalInfo.isNullOrEmpty())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    modifier = if (place.additionalInfo.isNullOrEmpty()) {
                        Modifier
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .clickable {
                                onIntent(
                                    PlaceDetailIntent.OpenDialog(
                                        place = place,
                                        property = PlaceDetailProperty.ADDITIONAL_INFO
                                    )
                                )
                            }
                    } else {
                        Modifier
                            .padding(vertical = 6.dp, horizontal = 20.dp)
                            .fillMaxWidth()
                            .clickable {
                                onIntent(
                                    PlaceDetailIntent.OpenDialog(
                                        place = place,
                                        property = PlaceDetailProperty.ADDITIONAL_INFO
                                    )
                                )
                            }
                    }
                )
            }

            item {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Footer(note = stringResource(id = R.string.place_detail_footer_note))
            }
        }
        activeDialog?.let { property ->
            PlaceDetailPropertyDialog(
                place = place,
                property = property,
                onIntent = onIntent,
                onDismiss = { onIntent(PlaceDetailIntent.CloseDialog(place, property)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailPropertyDialog(
    place: Place,
    property: PlaceDetailProperty,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Box {
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(id = property.dialogTitleRes),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(8.dp))

                    when (property) {
                        PlaceDetailProperty.GENERAL_ACCESSIBILITY -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(Modifier.weight(1f)) {
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
                                }
                                Box(Modifier.weight(1f)) {
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
                                }
                                Box(Modifier.weight(1f)) {
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
                        }

                        PlaceDetailProperty.ADDITIONAL_INFO -> {
                            var inputText by remember { mutableStateOf(place.additionalInfo ?: "") }
                            val maxChars = 1000
                            val currentChars = inputText.length

                            OutlinedTextField(
                                value = inputText,
                                onValueChange = {
                                    if (it.length <= maxChars) {
                                        inputText = it
                                    }
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = R.string.additional_info),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.additional_info_placeholder),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 120.dp),
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                maxLines = 5,
                                supportingText = {
                                    Text(
                                        text = "$currentChars/$maxChars",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentChars >= maxChars * 0.9)
                                            MaterialTheme.colorScheme.error
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )

                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextButton(
                                    onClick = { onDismiss() }
                                ) {
                                    Text(stringResource(id = R.string.cancel))
                                }

                                Spacer(Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        onIntent(
                                            PlaceDetailIntent.UpdateAccessibilityDetailString(
                                                place = place,
                                                detailProperty = property,
                                                value = inputText
                                            )
                                        )
                                        onDismiss()
                                    }
                                ) {
                                    Text(stringResource(id = R.string.save))
                                }
                            }
                        }

                        else -> {
                            // Property info dialog
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(id = property.getPropertyInfoStringRes()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { onDismiss() }
                                    ) {
                                        Text(stringResource(id = R.string.ok))
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider()

                    val footerMessage = if (property == PlaceDetailProperty.GENERAL_ACCESSIBILITY ||
                        property == PlaceDetailProperty.ADDITIONAL_INFO) {
                        stringResource(id = R.string.update_dialog_footer)
                    } else {
                        stringResource(id = R.string.info_dialog_footer)
                    }
                    Footer(note = footerMessage)
                }
            }
        }
    }
}

private fun PlaceDetailProperty.getPropertyInfoStringRes(): Int {
    return when (this) {
        PlaceDetailProperty.GENERAL_ACCESSIBILITY -> R.string.info_general_accessibility
        PlaceDetailProperty.INDOOR_ACCESSIBILITY -> R.string.info_indoor_accessibility
        PlaceDetailProperty.ENTRANCE_ACCESSIBILITY -> R.string.info_entrance_accessibility
        PlaceDetailProperty.RESTROOM_ACCESSIBILITY -> R.string.info_restroom_accessibility
        PlaceDetailProperty.ADDITIONAL_INFO -> R.string.info_additional_info
        PlaceDetailProperty.STEP_COUNT -> R.string.info_step_count
        PlaceDetailProperty.STEP_HEIGHT -> R.string.info_step_height
        PlaceDetailProperty.RAMP -> R.string.info_ramp
        PlaceDetailProperty.LIFT -> R.string.info_lift
        PlaceDetailProperty.ENTRANCE_WIDTH -> R.string.info_entrance_width
        PlaceDetailProperty.DOOR_TYPE -> R.string.info_door_type
        PlaceDetailProperty.DOOR_WIDTH -> R.string.info_door_width
        PlaceDetailProperty.ROOM_MANEUVER -> R.string.info_room_maneuver
        PlaceDetailProperty.GRAB_RAILS -> R.string.info_grab_rails
        PlaceDetailProperty.SINK -> R.string.info_sink
        PlaceDetailProperty.TOILET_SEAT -> R.string.info_toilet_seat
        PlaceDetailProperty.EMERGENCY_ALARM -> R.string.info_emergency_alarm
        PlaceDetailProperty.EURO_KEY -> R.string.info_euro_key
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PlaceDetailBottomSheet_Preview() {
    val place = Place(
        id = "1",
        name = "Example Cafe",
        category = PlaceCategory.CAFE,
        lat = 46.460071,
        lon = 6.843391,
        email = "example@mail.com",
        phone = "+41 21 123 45 67",
        website = "https://www.example.com",
        address = "Grande Place 1, Vevey 1800",
        generalAccessibility = PARTIALLY_ACCESSIBLE,
        indoorAccessibility = PARTIALLY_ACCESSIBLE,
        additionalInfo = "This location is mostly accessible.",
        entranceAccessibility = PARTIALLY_ACCESSIBLE,
        stepCount = PARTIALLY_ACCESSIBLE,
        stepHeight = PARTIALLY_ACCESSIBLE,
        ramp = PARTIALLY_ACCESSIBLE,
        lift = null,
        entranceWidth = FULLY_ACCESSIBLE,
        doorType = "Automatic",
        restroomAccessibility = NOT_ACCESSIBLE,
        doorWidth = FULLY_ACCESSIBLE,
        roomManeuver = NOT_ACCESSIBLE,
        grabRails = PARTIALLY_ACCESSIBLE,
        toiletSeat = FULLY_ACCESSIBLE,
        emergencyAlarm = NOT_ACCESSIBLE,
        sink = FULLY_ACCESSIBLE,
        euroKey = false
    )
    InWheelMapTheme {
        PlaceDetailsBottomSheet(
            stateFlow = MutableStateFlow(PlaceDetailState(place))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun PlaceDetailBottomSheetUnknownAccessibility_Preview() {
    val place = Place(
        id = "1",
        name = "Example Cafe",
        category = PlaceCategory.CAFE,
        lat = 46.460071,
        lon = 6.843391,
        generalAccessibility = UNKNOWN
    )

    InWheelMapTheme {
        PlaceDetailsBottomSheet(
            stateFlow = MutableStateFlow(PlaceDetailState(place))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PropertyUpdateDialogPreview() {
    val place = Place(
        id = "1",
        name = "Example Cafe",
        category = PlaceCategory.CAFE,
        lat = 46.460071,
        lon = 6.843391,
        generalAccessibility = PARTIALLY_ACCESSIBLE
    )

    InWheelMapTheme {
        PlaceDetailPropertyDialog(
            place = place,
            property = PlaceDetailProperty.ADDITIONAL_INFO,
        )
    }
}
