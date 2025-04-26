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

package com.aarokoinsaari.inwheel.view.info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AccessibleForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Elevator
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.material.icons.rounded.Wc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.view.components.Footer
import com.aarokoinsaari.inwheel.view.theme.InWheelMapTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityGuidelinesScreen(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.accessibility_guidelines),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxSize(1f)
                )
            }

            item { AccessibilityGuidelinesDisclaimerCard() }

            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }

            item {
                AccessibilityInfoCard(
                    title = stringResource(id = R.string.general_accessibility_title),
                    content = stringResource(R.string.info_general_accessibility),
                    icon = Icons.AutoMirrored.Rounded.AccessibleForward
                )
            }

            item {
                AccessibilityInfoCard(
                    title = stringResource(id = R.string.indoor_accessibility),
                    content = stringResource(R.string.info_indoor_accessibility),
                    icon = Icons.Rounded.MeetingRoom
                )
            }

            item {
                AccessibilityInfoCard(
                    title = stringResource(id = R.string.entrance_accessibility),
                    content = stringResource(R.string.info_entrance_accessibility),
                    icon = Icons.Rounded.DoorFront
                )
            }

            item {
                AccessibilityInfoCard(
                    title = stringResource(id = R.string.step_count_and_step_height),
                    content = stringResource(R.string.info_step_count) + "\n\n" +
                            stringResource(R.string.info_step_height),
                    icon = Icons.Rounded.Stairs
                )
            }

            item {
                AccessibilityInfoCard(
                    title = stringResource(id = R.string.ramp_and_lift_availability),
                    content = stringResource(R.string.info_ramp) + "\n\n" +
                            stringResource(R.string.info_lift),
                    icon = Icons.Rounded.Elevator
                )
            }

            item {
                AccessibilityInfoCard(
                    title = stringResource(id = R.string.restroom_accessibility),
                    content = stringResource(R.string.info_restroom_accessibility),
                    icon = Icons.Rounded.Wc
                )
            }

            item { AccessibilityStatusSectionCard() }

            item {
                Footer(
                    note = stringResource(R.string.info_dialog_footer),
                )
            }
        }
    }
}

@Composable
fun AccessibilityGuidelinesDisclaimerCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(id = R.string.content_desc_accessibility_disclaimer_info),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 16.dp)
            )
            Text(
                text = stringResource(id = R.string.accessibility_guidelines_disclaimer),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun AccessibilityInfoCard(
    title: String,
    content: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.content_desc_property_info, title),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(6.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )
        }
    }
}

@Composable
fun AccessibilityStatusSectionCard(modifier: Modifier = Modifier) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.content_desc_property_info, 
                        stringResource(R.string.general_accessibility)),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(6.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.general_accessibility),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Status level cards
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusLevelItem(
                    emoji = stringResource(R.string.emoji_checkmark),
                    title = stringResource(R.string.accessibility_option_fully_accessible),
                    description = stringResource(R.string.general_status_title_fully_accessible),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    borderColor = MaterialTheme.colorScheme.tertiary
                )

                StatusLevelItem(
                    emoji = stringResource(R.string.emoji_warning),
                    title = stringResource(R.string.accessibility_option_partially_accessible),
                    description = stringResource(R.string.general_status_title_partially_accessible),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    borderColor = MaterialTheme.colorScheme.secondary
                )

                StatusLevelItem(
                    emoji = stringResource(R.string.emoji_cross),
                    title = stringResource(R.string.accessibility_option_not_accessible),
                    description = stringResource(R.string.general_status_title_not_accessible),
                    color = MaterialTheme.colorScheme.errorContainer,
                    borderColor = MaterialTheme.colorScheme.error
                )

                StatusLevelItem(
                    emoji = stringResource(R.string.emoji_question),
                    title = stringResource(R.string.unknown_accessibility),
                    description = stringResource(id = R.string.general_status_title_unknown),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    borderColor = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun StatusLevelItem(
    emoji: String,
    title: String,
    description: String,
    color: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = color,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(end = 12.dp)
            )

            Column {
                Text(
                    text = title.replace(emoji, "").trim(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AccessibilityGuidelinesScreenPreview() {
    InWheelMapTheme {
        AccessibilityGuidelinesScreen()
    }
}
