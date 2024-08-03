/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.network

import com.aarokoinsaari.accessibilitymap.R

object CategoryConfig {
    val allCategories: Map<String, Int> = mapOf(
        "cafe" to R.drawable.ic_cafe,
        "restaurant" to R.drawable.ic_restaurant,
        "toilets" to R.drawable.ic_wc,
        "bus_station" to R.drawable.ic_bus_station,
        "train_station" to R.drawable.ic_train_station,
        "subway_station" to R.drawable.ic_subway_station,
        "parking" to R.drawable.ic_parking_area,
        "supermarket" to R.drawable.ic_grocery_store,
        "shop" to R.drawable.ic_shop,
        "pharmacy" to R.drawable.ic_pharmacy,
        "hospital" to R.drawable.ic_hospital,
        "beach" to R.drawable.ic_beach,
        "default" to R.drawable.ic_default_marker
    )
}
