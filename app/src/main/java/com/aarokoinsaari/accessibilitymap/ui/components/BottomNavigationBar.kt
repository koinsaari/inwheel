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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aarokoinsaari.accessibilitymap.ui.navigation.NavigationScreen

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationScreen.Map,
        NavigationScreen.Places
    )
    NavigationBar {
        val currentRoute = navController.currentRoute()
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconResId),
                        contentDescription = getContentDescriptionForScreen(screen)
                    )
                },
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
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

private fun NavController.currentRoute(): String? =
    currentBackStackEntry?.destination?.route

private fun getContentDescriptionForScreen(navigationScreen: NavigationScreen): String =
    when (navigationScreen) {
        NavigationScreen.Map -> "View the map"
        NavigationScreen.Places -> "View a list of places"
    }

@Preview(showBackground = true)
@Composable
fun BottomNavigationBar_Preview() {
    BottomNavigationBar(rememberNavController())
}
