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

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.view.theme.InWheelMapTheme

@Composable
fun LicensesLegalScreen(
    modifier: Modifier = Modifier,
    onNavigateToLicenseReport: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = stringResource(R.string.legal_licenses_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // OpenStreetMap section
            SectionCard(
                title = stringResource(R.string.osm_data_section_title),
                content = {
                    Text(
                        text = stringResource(R.string.osm_data_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://www.openstreetmap.org/copyright".toUri()
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = stringResource(R.string.view_osm_copyright))
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Open source licenses section
            SectionCard(
                title = stringResource(R.string.open_source_licenses_title),
                content = {
                    Text(
                        text = stringResource(R.string.open_source_licenses_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedButton(
                        onClick = { onNavigateToLicenseReport() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = stringResource(R.string.view_licenses))
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Data disclaimer section
            SectionCard(
                title = stringResource(R.string.data_disclaimer_title),
                content = {
                    Text(
                        text = stringResource(R.string.data_disclaimer_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy policy section
            SectionCard(
                title = stringResource(R.string.privacy_policy_title),
                content = {
                    Text(
                        text = stringResource(R.string.privacy_policy_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedButton(
                        onClick = { onNavigateToPrivacyPolicy() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = stringResource(R.string.view_privacy_policy))
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App license section
            SectionCard(
                title = stringResource(R.string.app_license_title),
                content = {
                    Text(
                        text = stringResource(R.string.app_license_description),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                "https://www.apache.org/licenses/LICENSE-2.0".toUri()
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = stringResource(R.string.view_app_license))
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Copyright footer
            Row {
                val context = LocalContext.current
                Text(
                    text = stringResource(id = R.string.copyright_prefix),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " Aaro Koinsaari",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://www.linkedin.com/in/aarokoinsaari/".toUri()
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LicensesLegalScreenPreview() {
    InWheelMapTheme {
        LicensesLegalScreen()
    }
}
