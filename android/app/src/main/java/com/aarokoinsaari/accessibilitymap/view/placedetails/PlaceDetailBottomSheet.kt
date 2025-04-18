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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.domain.intent.PlaceDetailIntent
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.NOT_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.accessibilitymap.domain.model.AccessibilityStatus.UNKNOWN
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceCategory
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceDetailProperty
import com.aarokoinsaari.accessibilitymap.domain.state.PlaceDetailState
import com.aarokoinsaari.accessibilitymap.view.components.Footer
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
    val activeDialog = state.value.activeDialog

    if (place != null) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                AccessibilityStatusDisplaySection(
                    place = place,
                    onIntent = onIntent,
                )
                HorizontalDivider(Modifier.padding(vertical = 16.dp))
            }
            if (place.category.rawValue != "toilets") {
                item {
                    ContactInfoSection(place)
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                }
            }
            item {
                Text(
                    text = stringResource(id = R.string.entrance),
                    style = MaterialTheme.typography.labelSmall
                )
                EntranceDetailsSection(
                    place = place,
                    onIntent = onIntent,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                )
                Spacer(Modifier.padding(vertical = 6.dp))
                Text(
                    text = stringResource(id = R.string.restroom),
                    style = MaterialTheme.typography.labelSmall
                )
                RestroomDetailsSection(
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
            PropertyUpdateDialog(
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
fun PropertyUpdateDialog(
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
                                }
                                Box(Modifier.weight(1f)) {
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
                                }
                                Box(Modifier.weight(1f)) {
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
                            TODO()
                        }
                    }
                    HorizontalDivider()
                    Footer(note = stringResource(id = R.string.dialog_footer))
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
    val accessibilityStatus = place.generalAccessibility
    if (accessibilityStatus == UNKNOWN) {
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
                AccessibilityStatusItem(
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
                        place.entranceAccessibility to
                                place.entranceAccessibility
                                    .getEntranceAccessibilityLabelStringRes(),
                        place.indoorAccessibility to
                                place.indoorAccessibility
                                    .getIndoorAccessibilityStringRes(),
                        place.restroomAccessibility to
                                place.restroomAccessibility
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
    place: Place,
    modifier: Modifier = Modifier,
) {
    val contactItems = listOfNotNull(
        place.phone?.let {
            it to Pair(
                Icons.Outlined.Phone,
                stringResource(id = R.string.content_desc_phone)
            )
        },
        place.email?.let {
            it to Pair(
                Icons.Outlined.Email,
                stringResource(id = R.string.content_desc_email)
            )
        },
        place.website?.let {
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
fun EntranceDetailsSection(
    place: Place,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        val stepCount = place.stepCount
        DetailItem(
            detailProperty = PlaceDetailProperty.STEP_COUNT,
            value = stepCount?.toString() ?: stringResource(id = R.string.emoji_question),
            place = place,
            onIntent = onIntent
        )

        if (stepCount != 0) {
            DetailItem(
                detailProperty = PlaceDetailProperty.STEP_HEIGHT,
                value = stringResource(
                    id = place.stepHeight.getAccessibilityStatusEmojiStringRes()
                ),
                place = place,
                onIntent = onIntent
            )
            DetailItem(
                detailProperty = PlaceDetailProperty.RAMP,
                value = stringResource(
                    id = place.ramp.getAccessibilityStatusEmojiStringRes()
                ),
                place = place,
                onIntent = onIntent
            )
            DetailItem(
                detailProperty = PlaceDetailProperty.LIFT,
                value = stringResource(
                    id = place.lift.getAccessibilityStatusEmojiStringRes()
                ),
                place = place,
                onIntent = onIntent
            )
            DetailItem(
                detailProperty = PlaceDetailProperty.DOOR_TYPE,
                value = place.type
                    ?: stringResource(id = R.string.emoji_question),
                place = place,
                onIntent = onIntent
            )
        }
    }
}

@Composable
fun RestroomDetailsSection(
    place: Place,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        DetailItem(
            detailProperty = PlaceDetailProperty.DOOR_WIDTH,
            value = stringResource(
                id = place.doorWidth.getAccessibilityStatusEmojiStringRes()
            ),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.ROOM_MANEUVER,
            value = stringResource(
                id = place.roomManeuver.getAccessibilityStatusEmojiStringRes()
            ),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.GRAB_RAILS,
            value = stringResource(
                id = place.grabRails.getAccessibilityStatusEmojiStringRes()
            ),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.SINK,
            value = stringResource(
                id = place.sink.getAccessibilityStatusEmojiStringRes()
            ),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.TOILET_SEAT,
            value = stringResource(
                id = place.toiletSeat.getAccessibilityStatusEmojiStringRes()
            ),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.EMERGENCY_ALARM,
            value = stringResource(
                id = place.emergencyAlarm.getAccessibilityStatusEmojiStringRes()
            ),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.EURO_KEY,
            value = stringResource(id = place.euroKey.getBooleanEmojiStringRes()),
            place = place,
            onIntent = onIntent
        )
    }
}

@Composable
fun DetailItem(
    place: Place,
    detailProperty: PlaceDetailProperty,
    value: String,
    modifier: Modifier = Modifier,
    onIntent: (PlaceDetailIntent) -> Unit = {},
    isAdditionalInfo: Boolean = false,
) {
    val accessibilityOptions = listOf(
        R.string.accessibility_option_fully_accessible to FULLY_ACCESSIBLE,
        R.string.accessibility_option_partially_accessible to PARTIALLY_ACCESSIBLE,
        R.string.accessibility_option_not_accessible to NOT_ACCESSIBLE
    )
    val doorTypeOptionIds = setOf<Int>(
        R.string.door_type_automatic_sliding,
        R.string.door_type_automatic_swing,
        R.string.door_type_manual_swing,
        R.string.door_type_manual_sliding,
        R.string.door_type_revolving,
        R.string.door_type_double
    )
    val booleanOptions = listOf(
        R.string.emoji_checkmark to true,
        R.string.emoji_cross to false,
    )
    val stepsOptions = listOf(
        R.string.step_count_fully_accessible to FULLY_ACCESSIBLE,
        R.string.step_count_partially_accessible to PARTIALLY_ACCESSIBLE,
        R.string.step_count_not_accessible to NOT_ACCESSIBLE
    )

    var expanded by remember { mutableStateOf(false) }

    if (isAdditionalInfo) {
        Column(modifier) {
            Text(
                text = stringResource(id = detailProperty.labelRes),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.clickable {
                    PlaceDetailIntent.OpenDialog(place = place, property = detailProperty)
                }
            )
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth(0.7f)
        ) {
            Text(
                text = stringResource(id = detailProperty.labelRes),
                style = MaterialTheme.typography.bodyMedium
            )
            Box {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (detailProperty == PlaceDetailProperty.DOOR_TYPE) {
                        doorTypeOptionIds.forEach { doorTypeId ->
                            val doorType = stringResource(id = doorTypeId)
                            DropdownMenuItem(
                                onClick = {
                                    onIntent(
                                        PlaceDetailIntent.UpdateAccessibilityDetailString(
                                            place = place,
                                            detailProperty = detailProperty,
                                            value = doorType
                                        )
                                    )
                                    expanded = false
                                },
                                text = { Text(text = doorType) }
                            )
                        }
                    } else if (detailProperty == PlaceDetailProperty.EURO_KEY) {
                        booleanOptions.forEach { (displayText, status) ->
                            DropdownMenuItem(
                                onClick = {
                                    onIntent(
                                        PlaceDetailIntent.UpdateAccessibilityDetailBoolean(
                                            place = place,
                                            detailProperty = detailProperty,
                                            value = status
                                        )
                                    )
                                    expanded = false
                                },
                                text = { Text(text = stringResource(id = displayText)) }
                            )
                        }
                    } else if (detailProperty == PlaceDetailProperty.STEP_COUNT) {
                        stepsOptions.forEach { (displayText, status) ->
                            DropdownMenuItem(
                                onClick = {
                                    onIntent(
                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                            place = place,
                                            detailProperty = detailProperty,
                                            status = status
                                        )
                                    )
                                    expanded = false
                                },
                                text = { Text(text = stringResource(id = displayText)) }
                            )
                        }
                    } else {
                        accessibilityOptions.forEach { (displayText, status) ->
                            DropdownMenuItem(
                                onClick = {
                                    onIntent(
                                        PlaceDetailIntent.UpdateAccessibilityDetail(
                                            place = place,
                                            detailProperty = detailProperty,
                                            status = status
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
        stepCount = 1,
        stepHeight = PARTIALLY_ACCESSIBLE,
        ramp = PARTIALLY_ACCESSIBLE,
        lift = null,
        width = FULLY_ACCESSIBLE,
        type = "Automatic",
        restroomAccessibility = NOT_ACCESSIBLE,
        doorWidth = FULLY_ACCESSIBLE,
        roomManeuver = NOT_ACCESSIBLE,
        grabRails = PARTIALLY_ACCESSIBLE,
        toiletSeat = FULLY_ACCESSIBLE,
        emergencyAlarm = NOT_ACCESSIBLE,
        sink = FULLY_ACCESSIBLE,
        euroKey = false
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
    val place = Place(
        id = "1",
        name = "Example Cafe",
        category = PlaceCategory.CAFE,
        lat = 46.460071,
        lon = 6.843391,
        generalAccessibility = UNKNOWN
    )

    AccessibilityMapTheme {
        PlaceDetailBottomSheet(
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

    AccessibilityMapTheme {
        PropertyUpdateDialog(
            place = place,
            property = PlaceDetailProperty.ADDITIONAL_INFO,
        )
    }
}
