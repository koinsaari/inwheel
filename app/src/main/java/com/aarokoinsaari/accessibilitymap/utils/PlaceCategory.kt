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

package com.aarokoinsaari.accessibilitymap.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aarokoinsaari.accessibilitymap.R
import com.aarokoinsaari.accessibilitymap.utils.PlaceCategory.DEFAULT

enum class PlaceCategory(
    val amenityTag: String,
    @DrawableRes val iconResId: Int,
    @StringRes val nameResId: Int
) {
    CAFE("cafe", R.drawable.ic_cafe, R.string.category_cafe),
    RESTAURANT("restaurant", R.drawable.ic_restaurant, R.string.category_restaurant),
    TOILETS("toilets", R.drawable.ic_wc, R.string.category_toilets),
    BUS_STATION("bus_station", R.drawable.ic_bus_station, R.string.category_bus_station),
    TRAIN_STATION("train_station", R.drawable.ic_train_station, R.string.category_train_station),
    SUBWAY_STATION("subway_station", R.drawable.ic_subway_station, R.string.category_subway_station),
    PARKING("parking", R.drawable.ic_parking_area, R.string.category_parking),
    SUPERMARKET("supermarket", R.drawable.ic_grocery_store, R.string.category_supermarket),
    SHOP("shop", R.drawable.ic_shop, R.string.category_shop),
    PHARMACY("pharmacy", R.drawable.ic_pharmacy, R.string.category_pharmacy),
    HOSPITAL("hospital", R.drawable.ic_hospital, R.string.category_hospital),
    BEACH("beach", R.drawable.ic_beach, R.string.category_beach),
    DEFAULT("default", R.drawable.ic_default_marker, R.string.category_default)
}

fun mapApiTagToCategory(apiTag: String): PlaceCategory =
    PlaceCategory.entries.find { it.amenityTag == apiTag.lowercase() } ?: DEFAULT
