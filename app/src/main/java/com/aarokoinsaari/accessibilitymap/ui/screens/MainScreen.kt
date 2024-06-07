package com.aarokoinsaari.accessibilitymap.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aarokoinsaari.accessibilitymap.ui.components.BottomNavigationBar
import com.aarokoinsaari.accessibilitymap.ui.theme.AccessibilityMapTheme

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = "map",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("map") { MapScreen() }
            composable("places") { PlacesListScreen() }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppNavigationContainer_Preview() {
    AccessibilityMapTheme {
        MainScreen()
    }
}
