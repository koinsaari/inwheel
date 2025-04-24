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

package com.aarokoinsaari.inwheel.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.aarokoinsaari.inwheel.R

enum class PlaceCategory(
    val rawValue: String,
    @StringRes val displayNameRes: Int,
    @DrawableRes val iconRes: Int
) {
    RESTAURANT(
        rawValue = "restaurant",
        displayNameRes = R.string.category_restaurant,
        iconRes = R.drawable.ic_restaurant
    ),
    CAFE(
        rawValue = "cafe",
        displayNameRes = R.string.category_cafe,
        iconRes = R.drawable.ic_cafe
    ),
    BAR(
        rawValue = "bar",
        displayNameRes = R.string.category_bar,
        iconRes = R.drawable.ic_bar
    ),
    PUB(
        rawValue = "pub",
        displayNameRes = R.string.category_pub,
        iconRes = R.drawable.ic_pub
    ),
    SUPERMARKET(
        rawValue = "supermarket",
        displayNameRes = R.string.category_supermarket,
        iconRes = R.drawable.ic_shop
    ),
    CONVENIENCE(
        rawValue = "convenience",
        displayNameRes = R.string.category_convenience,
        iconRes = R.drawable.ic_shop
    ),
    BAKERY(
        rawValue = "bakery",
        displayNameRes = R.string.category_bakery,
        iconRes = R.drawable.ic_bakery
    ),
    PHARMACY(
        rawValue = "pharmacy",
        displayNameRes = R.string.category_pharmacy,
        iconRes = R.drawable.ic_pharmacy
    ),
    HOSPITAL(
        rawValue = "hospital",
        displayNameRes = R.string.category_hospital,
        iconRes = R.drawable.ic_hospital
    ),
    CLINIC(
        rawValue = "clinic",
        displayNameRes = R.string.category_clinic,
        iconRes = R.drawable.ic_clinic
    ),
    LIBRARY(
        rawValue = "library",
        displayNameRes = R.string.category_library,
        iconRes = R.drawable.ic_library
    ),
    BANK(
        rawValue = "bank",
        displayNameRes = R.string.category_bank,
        iconRes = R.drawable.ic_bank
    ),
    TOILETS(
        rawValue = "toilets",
        displayNameRes = R.string.category_toilets,
        iconRes = R.drawable.ic_toilet
    ),
    FUEL(
        rawValue = "fuel",
        displayNameRes = R.string.category_gas_station,
        iconRes = R.drawable.ic_gas_station
    ),
    HOTEL(
        rawValue = "hotel",
        displayNameRes = R.string.category_hotel,
        iconRes = R.drawable.ic_hotel
    ),
    HOSTEL(
        rawValue = "hostel",
        displayNameRes = R.string.category_hostel,
        iconRes = R.drawable.ic_hotel
    ),
    MUSEUM(
        rawValue = "museum",
        displayNameRes = R.string.category_museum,
        iconRes = R.drawable.ic_museum
    ),
    CINEMA(
        rawValue = "cinema",
        displayNameRes = R.string.category_cinema,
        iconRes = R.drawable.ic_cinema
    ),
    NIGHTCLUB(
        rawValue = "nightclub",
        displayNameRes = R.string.category_nightclub,
        iconRes = R.drawable.ic_nightclub
    ),
    COURTHOUSE(
        rawValue = "courthouse",
        displayNameRes = R.string.category_courthouse,
        iconRes = R.drawable.ic_courthouse
    ),
    ELECTRONICS(
        rawValue = "electronics",
        displayNameRes = R.string.category_electronics,
        iconRes = R.drawable.ic_electronics
    ),
    CLOTHES(
        rawValue = "clothes",
        displayNameRes = R.string.category_clothes,
        iconRes = R.drawable.ic_clothes
    ),
    SHOES(
        rawValue = "shoes",
        displayNameRes = R.string.category_shoes,
        iconRes = R.drawable.ic_shoe
    ),
    COSMETICS(
        rawValue = "cosmetics",
        displayNameRes = R.string.category_cosmetics,
        iconRes = R.drawable.ic_cosmetics
    ),
    JEWELRY(
        rawValue = "jewelry",
        displayNameRes = R.string.category_jewelry,
        iconRes = R.drawable.ic_jewelry
    ),
    FURNITURE(
        rawValue = "furniture",
        displayNameRes = R.string.category_furniture,
        iconRes = R.drawable.ic_furniture
    ),
    CAR(
        rawValue = "car",
        displayNameRes = R.string.category_car,
        iconRes = R.drawable.ic_car
    ),
    BICYCLE(
        rawValue = "bicycle",
        displayNameRes = R.string.category_bicycle,
        iconRes = R.drawable.ic_bicycle
    ),
    MOTORCYCLE(
        rawValue = "motorcycle",
        displayNameRes = R.string.category_motorcycle,
        iconRes = R.drawable.ic_motorcycle
    ),
    SPORTS(
        rawValue = "sports",
        displayNameRes = R.string.category_sports,
        iconRes = R.drawable.ic_sports
    ),
    BOOKS(
        rawValue = "books",
        displayNameRes = R.string.category_books,
        iconRes = R.drawable.ic_book
    ),
    KINDERGARTEN(
        rawValue = "kindergarten",
        displayNameRes = R.string.category_kindergarten,
        iconRes = R.drawable.ic_kindergarten
    ),
    SCHOOL(
        rawValue = "school",
        displayNameRes = R.string.category_school,
        iconRes = R.drawable.ic_school
    ),
    UNIVERSITY(
        rawValue = "university",
        displayNameRes = R.string.category_university,
        iconRes = R.drawable.ic_school
    ),
    COLLEGE(
        rawValue = "college",
        displayNameRes = R.string.category_college,
        iconRes = R.drawable.ic_school
    ),
    UNKNOWN(
        rawValue = "unknown",
        displayNameRes = R.string.category_default,
        iconRes = R.drawable.ic_default_marker
    );

    companion object {
        fun fromRawValue(value: String?): PlaceCategory {
            if (value.isNullOrBlank()) return UNKNOWN
            return PlaceCategory.entries.firstOrNull { it.rawValue.equals(value, ignoreCase = true) }
                ?: UNKNOWN
        }
    }
}
