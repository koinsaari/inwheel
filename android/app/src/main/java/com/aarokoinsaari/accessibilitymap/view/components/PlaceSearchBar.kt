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

package com.aarokoinsaari.accessibilitymap.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.domain.model.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchResults: List<Place>,
    onPlaceSelected: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {
                    onExpandedChange(false)
                    onSearch(query)
                },
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                placeholder = {
                    Text(
                        text = stringResource(R.string.place_search_bar_placeholder),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    if (!expanded) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                onExpandedChange(false)
                            }
                        )
                    }
                },
                // TODO: comment out when some functionality implemented
                // trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) }
            )
        },
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        colors = SearchBarDefaults.colors(MaterialTheme.colorScheme.surfaceVariant), // TODO: Use MaterialTheme
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(false) }
        ) {
            items(searchResults) { place ->
                PlaceListItem(
                    place = place,
                    onClick = {
                        onPlaceSelected(place)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceListItem(place: Place, onClick: () -> Unit = { }) {
    ListItem(
        headlineContent = { Text(place.name) },
        overlineContent = { Text(place.category.toString()) },
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.clickable { onClick() }
    )
}
