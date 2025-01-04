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

package com.aarokoinsaari.accessibilitymap.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.ui.navigation.NavigationScreen

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationScreen.Map,
        NavigationScreen.PlaceList
    )
    NavigationBar {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                icon = {
                    Icon(
                        painter = painterResource(
                            id = screen.iconResId ?: R.drawable.ic_default_marker
                        ),
                        contentDescription = stringResource(id = screen.getContentDescStringRes())
                    )
                },
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

private fun NavigationScreen.getContentDescStringRes(): Int =
    when (this) {
        NavigationScreen.Map -> R.string.content_desc_navigation_map
        NavigationScreen.PlaceList -> R.string.content_desc_navigation_place_list
        else -> R.string.content_desc_navigation_default
    }

@Preview(showBackground = true)
@Composable
fun BottomNavigationBar_Preview() {
    BottomNavigationBar(rememberNavController())
}
