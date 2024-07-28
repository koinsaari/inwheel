/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.aarokoinsaari.accessibilitymap.ui.screens.MainScreen
import com.aarokoinsaari.accessibilitymap.ui.theme.AccessibilityMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccessibilityMapTheme {
                MainScreen()
            }
        }
    }
}
