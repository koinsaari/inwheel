/*
 * Copyright (c) 2024–2025 Aaro Koinsaari
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

package com.aarokoinsaari.inwheel.view.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.view.theme.InWheelTheme

@Composable
fun TermsDialog(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(R.string.terms_dialog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(Modifier.verticalScroll(scrollState)) {
                Text(
                    text = stringResource(R.string.terms_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://inwheel.ch/terms-of-service".toUri()
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.view_terms_of_service),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }

                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://inwheel.ch/privacy-policy".toUri()
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.view_privacy_policy),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text(stringResource(R.string.terms_dialog_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text(stringResource(R.string.terms_dialog_decline))
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun TermsDialogPreview() {
    InWheelTheme {
        TermsDialog()
    }
}
