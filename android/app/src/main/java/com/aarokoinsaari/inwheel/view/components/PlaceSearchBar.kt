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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.FULLY_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.AccessibilityStatus.PARTIALLY_ACCESSIBLE
import com.aarokoinsaari.inwheel.domain.model.Place
import com.aarokoinsaari.inwheel.domain.model.PlaceCategory
import com.aarokoinsaari.inwheel.view.utils.getAccessibilityStatusContentDescStringRes
import com.aarokoinsaari.inwheel.view.utils.getAccessibilityStatusDrawableRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceSearchBar(
    query: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    isSearching: Boolean = false,
    searchResults: List<Place> = emptyList(),
    onQueryChange: (String) -> Unit = { },
    onSearch: (String) -> Unit = { },
    onExpandedChange: (Boolean) -> Unit = { },
    onPlaceSelected: (Place) -> Unit = { },
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
                            contentDescription = stringResource(R.string.content_desc_back),
                            modifier = Modifier.clickable {
                                onExpandedChange(false)
                            }
                        )
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.content_desc_clear_search),
                            modifier = Modifier.clickable {
                                onQueryChange("")
                            }
                        )
                    }
                }
            )
        },
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        colors = SearchBarDefaults.colors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
    ) {
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
}

@Composable
fun PlaceListItem(
    place: Place,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
) {
    ListItem(
        headlineContent = {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium
            )
        },
        overlineContent = {
            Text(
                text = stringResource(id = place.category.displayNameRes).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            place.generalAccessibility?.let {
                Icon(
                    painter = painterResource(id = it.getAccessibilityStatusDrawableRes()),
                    contentDescription = stringResource(id = it.getAccessibilityStatusContentDescStringRes()),
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(42.dp)
                        .padding(4.dp),
                )
            }
        },
        supportingContent = {
            Column {
                place.address?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                place.region?.let {
                    Text(
                        text = it.replaceFirstChar { char -> char.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.clickable { onClick() }
    )
}

@Preview(showBackground = true)
@Composable
private fun PlaceSearchBarPreview() {
    MaterialTheme {
        val samplePlaces = listOf(
            Place(
                id = "1",
                name = "Example Cafe",
                category = PlaceCategory.CAFE,
                lat = 46.460071,
                lon = 6.843391,
                region = "Switzerland",
                address = "Quai Perdonnet 24, 1800 Vevey",
                generalAccessibility = PARTIALLY_ACCESSIBLE
            ),
            Place(
                id = "2",
                name = "Example Restaurant",
                category = PlaceCategory.RESTAURANT,
                lat = 46.460071,
                lon = 6.843391,
                region = "Switzerland",
                generalAccessibility = FULLY_ACCESSIBLE
            ),
            Place(
                id = "3",
                name = "Example Hotel",
                category = PlaceCategory.HOTEL,
                lat = 46.460071,
                lon = 6.843391,
                generalAccessibility = null
            )
        )

        PlaceSearchBar(
            query = "Example",
            searchResults = samplePlaces
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceListItemPreview() {
    MaterialTheme {
        PlaceListItem(
            place = Place(
                id = "1",
                name = "Example Cafe",
                category = PlaceCategory.CAFE,
                lat = 46.460071,
                lon = 6.843391,
                address = "Quai Perdonnet 24, 1800 Vevey, Switzerland",
                generalAccessibility = FULLY_ACCESSIBLE
            ),
        )
    }
}
