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

package com.aarokoinsaari.accessibilitymap.view.info

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.view.components.Footer

@Composable
fun AboutScreen() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(36.dp),
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            item {
                SectionContent(
                    title = stringResource(R.string.about_title),
                    body = stringResource(R.string.about_body)
                )
            }

            item {
                SectionContent(
                    title = stringResource(R.string.the_goal_title),
                    body = stringResource(R.string.the_goal_body)
                )
            }

            item {
                SectionContent(
                    title = stringResource(R.string.a_work_in_progress_title),
                    body = stringResource(R.string.a_work_in_progress_body)
                )
            }

            item {
                SectionContent(
                    title = stringResource(R.string.contributing_title),
                    body = stringResource(R.string.contributing_body)
                )
            }

            item {
                Column {
                    SectionContent(
                        title = stringResource(R.string.disclaimer_title),
                        body = stringResource(R.string.disclaimer_body),
                    )
                    Text(
                        text = stringResource(R.string.disclaimer_data_note),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Column {
                    SectionContent(
                        title = stringResource(R.string.contact_title),
                        body = stringResource(R.string.contact_body)
                    )
                    Spacer(Modifier.height(16.dp))
                    LinkRow(
                        icon = Icons.Outlined.Code,
                        text = "GitHub: koinsaari/accessibility-map",
                        url = "https://github.com/koinsaari/accessibility-map",
                        contentDescription = "GitHub Repository"
                    )

                    LinkRow(
                        icon = Icons.Default.Email,
                        text = "aaro.koinsaari@protonmail.ch",
                        url = "mailto:aaro.koinsaari@protonmail.ch",
                        contentDescription = "Email"
                    )
                }
            }

            item {
                Column {
                    HorizontalDivider(Modifier.padding(bottom = 8.dp))
                    Footer(note = stringResource(R.string.about_footer_note))
                }
            }
        }
    }
}

@Composable
private fun SectionContent(
    title: String,
    body: String,
    spacing: Dp = 16.dp
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(spacing))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    text: String,
    url: String,
    contentDescription: String
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            }
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview
@Composable
private fun AboutScreenPreview() {
    MaterialTheme {
        AboutScreen()
    }
}
