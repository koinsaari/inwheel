package com.aarokoinsaari.accessibilitymap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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

@Composable
fun MainScreen() {
    TODO()
}

@Preview(showBackground = true)
@Composable
fun MainScreen_Preview() {
    AccessibilityMapTheme {
        MainScreen()
    }
}