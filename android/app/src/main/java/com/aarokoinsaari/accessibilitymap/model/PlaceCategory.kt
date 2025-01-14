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

package com.aarokoinsaari.accessibilitymap.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aarokoinsaari.accessibilitymap.R

enum class PlaceCategory(
    val rawValue: String,
    @StringRes val displayNameResId: Int,
    @DrawableRes val iconResId: Int
) {

    RESTAURANT(
        rawValue = "restaurant",
        displayNameResId = R.string.category_restaurant,
        iconResId = R.drawable.ic_restaurant
    ),
    CAFE(
        rawValue = "cafe",
        displayNameResId = R.string.category_cafe,
        iconResId = R.drawable.ic_cafe
    ),
    PARKING(
        rawValue = "parking",
        displayNameResId = R.string.category_parking,
        iconResId = R.drawable.ic_parking_area
    ),
    TOILETS(
        rawValue = "toilets",
        displayNameResId = R.string.category_toilets,
        iconResId = R.drawable.ic_wc
    ),
    UNKNOWN(
        rawValue = "unknown",
        displayNameResId = R.string.category_default,
        iconResId = R.drawable.ic_default_marker
    );
    // TODO

    companion object {
        fun fromRawValue(value: String?): PlaceCategory {
            if (value.isNullOrBlank()) return UNKNOWN
            return PlaceCategory.entries.firstOrNull { it.rawValue.equals(value, ignoreCase = true) }
                ?: UNKNOWN
        }
    }
}
