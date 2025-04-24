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

package com.aarokoinsaari.inwheel.view.map

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.aarokoinsaari.inwheel.R
import com.aarokoinsaari.inwheel.domain.intent.MapIntent
import com.aarokoinsaari.inwheel.domain.model.PlaceCategory
import kotlin.collections.forEach

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun FilterChipRow(
    categories: List<PlaceCategory>,
    selectedCategories: Set<String>,
    modifier: Modifier = Modifier,
    onIntent: (MapIntent) -> Unit = {},
) {
    Box(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(Modifier.weight(1f)) {
                items(categories) { category ->
                    val isSelected = selectedCategories.contains(category.rawValue)
                    val categoryName = stringResource(id = category.displayNameRes)
                    val contentDescription = if (isSelected) {
                        stringResource(id = R.string.content_desc_filter_selected, categoryName)
                    } else {
                        stringResource(id = R.string.content_desc_filter_unselected, categoryName)
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onIntent(MapIntent.ToggleFilter(category)) },
                        label = { Text(text = categoryName) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(
                                        id = R.string.content_desc_filter_check_icon
                                    )
                                )
                            }
                        } else {
                            {
                                Icon(
                                    painter = painterResource(id = category.iconRes),
                                    contentDescription = stringResource(
                                        id = R.string.content_desc_filter_category_icon,
                                        categoryName
                                    )
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = false,
                            selected = isSelected
                        ),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .animateItem()
                            .semantics {
                                this.contentDescription = contentDescription
                            }
                    )
                }
            }
            FilterDropdown(
                categories = categories,
                selectedCategories = selectedCategories,
                onIntent = onIntent,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDropdown(
    categories: List<PlaceCategory>,
    selectedCategories: Set<String>,
    modifier: Modifier = Modifier,
    onIntent: (MapIntent) -> Unit = {},
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    var tempSelectedCategories by remember(dropdownExpanded) {
        mutableStateOf(selectedCategories)
    }

    Box(modifier) {
        IconButton(
            onClick = { dropdownExpanded = true },
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(id = R.string.content_desc_filter_menu),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(
            expanded = dropdownExpanded,
            onDismissRequest = { dropdownExpanded = false },
            modifier = Modifier.width(300.dp)
        ) {
            Text(
                text = stringResource(id = R.string.filter_categories),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(
                modifier = Modifier
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                categories.forEach { category ->
                    val isSelected = tempSelectedCategories.contains(category.rawValue)
                    val categoryName = stringResource(id = category.displayNameRes)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempSelectedCategories = if (isSelected) {
                                    tempSelectedCategories - category.rawValue
                                } else {
                                    tempSelectedCategories + category.rawValue
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                tempSelectedCategories = if (checked) {
                                    tempSelectedCategories + category.rawValue
                                } else {
                                    tempSelectedCategories - category.rawValue
                                }
                            }
                        )

                        Spacer(Modifier.width(8.dp))

                        Icon(
                            painter = painterResource(id = category.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextButton(
                    onClick = { tempSelectedCategories = emptySet() }
                ) {
                    Text(stringResource(id = R.string.reset))
                }

                TextButton(
                    onClick = {
                        categories.forEach { category ->
                            val wasSelected = selectedCategories.contains(category.rawValue)
                            val isNowSelected = tempSelectedCategories.contains(category.rawValue)

                            if (wasSelected != isNowSelected) {
                                onIntent(MapIntent.ToggleFilter(category))
                            }
                        }
                        dropdownExpanded = false
                    }
                ) {
                    Text(stringResource(id = R.string.apply))
                }
            }
        }
    }
}
