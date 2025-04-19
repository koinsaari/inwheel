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

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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
                GeneralAccessibilityStatusDisplaySection(
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
fun GeneralAccessibilityStatusDisplaySection(
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
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
                                var expanded by remember { mutableStateOf(false) }

                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { expanded = true }
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
    val context = LocalContext.current
    val contactItems = listOfNotNull(
        place.phone?.let { phone ->
            Triple(
                phone,
                Icons.Outlined.Phone,
                stringResource(id = R.string.content_desc_phone)
            ) to {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = "tel:$phone".toUri()
                }
                context.startActivity(intent)
            }
        },
        place.email?.let { email ->
            Triple(
                email,
                Icons.Outlined.Email,
                stringResource(id = R.string.content_desc_email)
            ) to {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:$email".toUri()
                }
                context.startActivity(intent)
            }
        },
        place.website?.let { website ->
            Triple(
                website,
                Icons.Outlined.Public,
                stringResource(id = R.string.content_desc_website)
            ) to {
                val normalized = if (website.startsWith("http")) website else "https://$website"
                val intent = Intent(Intent.ACTION_VIEW, normalized.toUri())
                context.startActivity(intent)
            }
        }
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        if (contactItems.isNotEmpty()) {
            contactItems.forEach { (info, action) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = action)
                ) {
                    Icon(
                        imageVector = info.second,
                        contentDescription = info.third,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = info.first,
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
            value = stringResource(id = stepCount.getStepCountValueStringRes()),
            place = place,
            onIntent = onIntent
        )

        if (stepCount != FULLY_ACCESSIBLE && stepCount != UNKNOWN && stepCount != null) {
            DetailItem(
                detailProperty = PlaceDetailProperty.STEP_HEIGHT,
                value = stringResource(id = place.stepHeight.getStepHeightValueStringRes()),
                place = place,
                onIntent = onIntent
            )
        }
        DetailItem(
            detailProperty = PlaceDetailProperty.RAMP,
            value = stringResource(id = place.ramp.getRampValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.LIFT,
            value = stringResource(id = place.lift.getAccessibilityStringRes()),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.DOOR_TYPE,
            value = place.type ?: stringResource(id = R.string.emoji_question),
            place = place,
            onIntent = onIntent
        )
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
            value = stringResource(id = place.doorWidth.getDoorWidthValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.ROOM_MANEUVER,
            value = stringResource(id = place.roomManeuver.getRoomManeuverValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.GRAB_RAILS,
            value = stringResource(id = place.grabRails.getGrabRailsValueStringRes()),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.SINK,
            value = stringResource(id = place.sink.getAccessibilityStringRes()),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.TOILET_SEAT,
            value = stringResource(id = place.toiletSeat.getAccessibilityStringRes()),
            place = place,
            onIntent = onIntent
        )
        DetailItem(
            detailProperty = PlaceDetailProperty.EMERGENCY_ALARM,
            value = stringResource(id = place.emergencyAlarm.getEmergencyAlarmValueStringRes()),
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
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = stringResource(id = detailProperty.labelRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(120.dp)
        )

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
            DetailItemDropdownOptions(
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
private fun DetailItemDropdownOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    when (detailProperty) {
        PlaceDetailProperty.DOOR_TYPE -> {
            DoorTypeOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.EURO_KEY -> {
            BooleanOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.LIFT,
        PlaceDetailProperty.SINK,
        PlaceDetailProperty.TOILET_SEAT,
            -> {
            StandardAccessibilityOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.STEP_COUNT -> {
            StepCountOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.STEP_HEIGHT -> {
            StepHeightOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.RAMP -> {
            RampOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.DOOR_WIDTH -> {
            DoorWidthOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.ROOM_MANEUVER -> {
            RoomManeuverOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.GRAB_RAILS -> {
            GrabRailsOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        PlaceDetailProperty.EMERGENCY_ALARM -> {
            EmergencyAlarmOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }

        else -> {
            StandardAccessibilityOptions(
                place = place,
                detailProperty = detailProperty,
                onOptionSelected = onOptionSelected
            )
        }
    }
}

@Composable
private fun DoorTypeOptions(
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
private fun BooleanOptions(
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
private fun StandardAccessibilityOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val accessibilityOptions = listOf(
        R.string.accessibility_option_fully_accessible to FULLY_ACCESSIBLE,
        R.string.accessibility_option_partially_accessible to PARTIALLY_ACCESSIBLE,
        R.string.accessibility_option_not_accessible to NOT_ACCESSIBLE
    )
    accessibilityOptions.forEach { (displayText, status) ->
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

@Composable
private fun StepCountOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val options = listOf(
        R.string.step_count_option_none to FULLY_ACCESSIBLE,
        R.string.step_count_option_one to PARTIALLY_ACCESSIBLE,
        R.string.step_count_option_multiple to NOT_ACCESSIBLE
    )
    options.forEach { (displayText, status) ->
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

@Composable
private fun StepHeightOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val options = listOf(
        R.string.step_height_option_low to FULLY_ACCESSIBLE,
        R.string.step_height_option_medium to PARTIALLY_ACCESSIBLE,
        R.string.step_height_option_high to NOT_ACCESSIBLE
    )
    options.forEach { (displayText, status) ->
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

@Composable
private fun RampOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val options = listOf(
        R.string.ramp_option_accessible to FULLY_ACCESSIBLE,
        R.string.ramp_option_partial to PARTIALLY_ACCESSIBLE,
        R.string.ramp_option_none to NOT_ACCESSIBLE
    )
    options.forEach { (displayText, status) ->
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

@Composable
private fun DoorWidthOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val options = listOf(
        R.string.door_width_option_wide to FULLY_ACCESSIBLE,
        R.string.door_width_option_standard to PARTIALLY_ACCESSIBLE,
        R.string.door_width_option_narrow to NOT_ACCESSIBLE
    )
    options.forEach { (displayText, status) ->
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

@Composable
private fun RoomManeuverOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val options = listOf(
        R.string.room_maneuver_option_spacious to FULLY_ACCESSIBLE,
        R.string.room_maneuver_option_limited to PARTIALLY_ACCESSIBLE,
        R.string.room_maneuver_option_tight to NOT_ACCESSIBLE
    )
    options.forEach { (displayText, status) ->
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

@Composable
private fun GrabRailsOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val options = listOf(
        R.string.grab_rails_option_both_sides to FULLY_ACCESSIBLE,
        R.string.grab_rails_option_one_side to PARTIALLY_ACCESSIBLE,
        R.string.grab_rails_option_none to NOT_ACCESSIBLE
    )
    options.forEach { (displayText, status) ->
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

@Composable
private fun EmergencyAlarmOptions(
    place: Place,
    detailProperty: PlaceDetailProperty,
    onOptionSelected: (PlaceDetailIntent) -> Unit,
) {
    val options = listOf(
        R.string.emergency_alarm_option_available to FULLY_ACCESSIBLE,
        R.string.emergency_alarm_option_limited to PARTIALLY_ACCESSIBLE,
        R.string.emergency_alarm_option_none to NOT_ACCESSIBLE
    )
    options.forEach { (displayText, status) ->
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
        true -> R.string.checkmark_yes
        false -> R.string.cross_no
        null -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getStepCountValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.step_count_option_none
        PARTIALLY_ACCESSIBLE -> R.string.step_count_option_one
        NOT_ACCESSIBLE -> R.string.step_count_option_multiple
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getStepHeightValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.step_height_option_low
        PARTIALLY_ACCESSIBLE -> R.string.step_height_option_medium
        NOT_ACCESSIBLE -> R.string.step_height_option_high
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getDoorWidthValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.door_width_option_wide
        PARTIALLY_ACCESSIBLE -> R.string.door_width_option_standard
        NOT_ACCESSIBLE -> R.string.door_width_option_narrow
        else -> R.string.emoji_question
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

private fun AccessibilityStatus?.getRampValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.ramp_option_accessible
        PARTIALLY_ACCESSIBLE -> R.string.ramp_option_partial
        NOT_ACCESSIBLE -> R.string.ramp_option_none
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getRoomManeuverValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.room_maneuver_option_spacious
        PARTIALLY_ACCESSIBLE -> R.string.room_maneuver_option_limited
        NOT_ACCESSIBLE -> R.string.room_maneuver_option_tight
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getGrabRailsValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.grab_rails_option_both_sides
        PARTIALLY_ACCESSIBLE -> R.string.grab_rails_option_one_side
        NOT_ACCESSIBLE -> R.string.grab_rails_option_none
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getEmergencyAlarmValueStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.emergency_alarm_option_available
        PARTIALLY_ACCESSIBLE -> R.string.emergency_alarm_option_limited
        NOT_ACCESSIBLE -> R.string.emergency_alarm_option_none
        else -> R.string.emoji_question
    }

private fun AccessibilityStatus?.getAccessibilityStringRes(): Int =
    when (this) {
        FULLY_ACCESSIBLE -> R.string.accessibility_option_fully_accessible
        PARTIALLY_ACCESSIBLE -> R.string.accessibility_option_partially_accessible
        NOT_ACCESSIBLE -> R.string.accessibility_option_not_accessible
        else -> R.string.emoji_question
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
