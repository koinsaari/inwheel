/*
 * Copyright (c) 2024 Aaro Koinsaari
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

package com.aarokoinsaari.accessibilitymap.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.aarokoinsaari.accessibilitymap.intent.PlaceListIntent
import com.aarokoinsaari.accessibilitymap.state.PlaceListState
import com.aarokoinsaari.accessibilitymap.view.components.PlaceSearchBar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreen(
    stateFlow: StateFlow<PlaceListState>,
    onIntent: (PlaceListIntent) -> Unit = { }
) {
    val state by stateFlow.collectAsState()
    var expanded by remember { mutableStateOf(state.isExpanded) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            PlaceSearchBar(
                query = state.searchQuery,
                onQueryChange = { text ->
                    onIntent(PlaceListIntent.UpdateQuery(text))
                },
                onSearch = {
                    expanded = false
                    onIntent(PlaceListIntent.Search(state.searchQuery))
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                searchResults = state.filteredPlaces,
                onPlaceSelected = { place ->
                    onIntent(PlaceListIntent.SelectPlace(place))
                    expanded = false
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceListScreen_Preview() {
    MaterialTheme {
        PlaceListScreen(stateFlow = MutableStateFlow(PlaceListState()))
    }
}
