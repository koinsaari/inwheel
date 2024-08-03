package com.aarokoinsaari.accessibilitymap.ui.navigation

import androidx.annotation.DrawableRes
import com.aarokoinsaari.accessibilitymap.R

enum class NavigationScreen(val route: String, @DrawableRes val iconResId: Int) {
    Map("map", R.drawable.ic_map),
    Places("places", R.drawable.ic_places_list)
}
