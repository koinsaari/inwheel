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
    FAST_FOOD(
        rawValue = "fast_food",
        displayNameResId = R.string.category_fast_food,
        iconResId = R.drawable.ic_fast_food
    ),
    BAR(
        rawValue = "bar",
        displayNameResId = R.string.category_bar,
        iconResId = R.drawable.ic_bar
    ),
    PUB(
        rawValue = "pub",
        displayNameResId = R.string.category_pub,
        iconResId = R.drawable.ic_pub
    ),
    SUPERMARKET(
        rawValue = "supermarket",
        displayNameResId = R.string.category_supermarket,
        iconResId = R.drawable.ic_shop
    ),
    CONVENIENCE(
        rawValue = "convenience",
        displayNameResId = R.string.category_convenience,
        iconResId = R.drawable.ic_shop
    ),
    BAKERY(
        rawValue = "bakery",
        displayNameResId = R.string.category_bakery,
        iconResId = R.drawable.ic_bakery
    ),
    PHARMACY(
        rawValue = "pharmacy",
        displayNameResId = R.string.category_pharmacy,
        iconResId = R.drawable.ic_pharmacy
    ),
    HOSPITAL(
        rawValue = "hospital",
        displayNameResId = R.string.category_hospital,
        iconResId = R.drawable.ic_hospital
    ),
    CLINIC(
        rawValue = "clinic",
        displayNameResId = R.string.category_clinic,
        iconResId = R.drawable.ic_clinic
    ),
    LIBRARY(
        rawValue = "library",
        displayNameResId = R.string.category_library,
        iconResId = R.drawable.ic_library
    ),
    BANK(
        rawValue = "bank",
        displayNameResId = R.string.category_bank,
        iconResId = R.drawable.ic_bank
    ),
    POST_OFFICE(
        rawValue = "post_office",
        displayNameResId = R.string.category_post_office,
        iconResId = R.drawable.ic_post_office
    ),
    TOILETS(
        rawValue = "toilets",
        displayNameResId = R.string.category_toilets,
        iconResId = R.drawable.ic_toilet
    ),
    PARKING(
        rawValue = "parking",
        displayNameResId = R.string.category_parking,
        iconResId = R.drawable.ic_parking
    ),
    GAS_STATION(
        rawValue = "fuel",
        displayNameResId = R.string.category_gas_station,
        iconResId = R.drawable.ic_gas_station
    ),
    HOTEL(
        rawValue = "hotel",
        displayNameResId = R.string.category_hotel,
        iconResId = R.drawable.ic_hotel
    ),
    HOSTEL(
        rawValue = "hostel",
        displayNameResId = R.string.category_hostel,
        iconResId = R.drawable.ic_hotel
    ),
    MOTEL(
        rawValue = "motel",
        displayNameResId = R.string.category_motel,
        iconResId = R.drawable.ic_hotel
    ),
    GUEST_HOUSE(
        rawValue = "guest-house",
        displayNameResId = R.string.category_guest_house,
        iconResId = R.drawable.ic_hotel
    ),
    CHALET(
        rawValue = "chalet",
        displayNameResId = R.string.category_chalet,
        iconResId = R.drawable.ic_chalet
    ),
    MUSEUM(
        rawValue = "museum",
        displayNameResId = R.string.category_museum,
        iconResId = R.drawable.ic_museum
    ),
    CINEMA(
        rawValue = "cinema",
        displayNameResId = R.string.category_cinema,
        iconResId = R.drawable.ic_cinema
    ),
    CASINO(
        rawValue = "casino",
        displayNameResId = R.string.category_casino,
        iconResId = R.drawable.ic_casino
    ),
    NIGHTCLUB(
        rawValue = "nightclub",
        displayNameResId = R.string.category_nightclub,
        iconResId = R.drawable.ic_nightclub
    ),
    COURTHOUSE(
        rawValue = "courthouse",
        displayNameResId = R.string.category_courthouse,
        iconResId = R.drawable.ic_courthouse
    ),
    ELECTRONICS(
        rawValue = "electronics",
        displayNameResId = R.string.category_electronics,
        iconResId = R.drawable.ic_electronics
    ),
    CLOTHES(
        rawValue = "clothes",
        displayNameResId = R.string.category_clothes,
        iconResId = R.drawable.ic_clothes
    ),
    COSMETICS(
        rawValue = "cosmetics",
        displayNameResId = R.string.category_cosmetics,
        iconResId = R.drawable.ic_cosmetics
    ),
    FURNITURE(
        rawValue = "furniture",
        displayNameResId = R.string.category_furniture,
        iconResId = R.drawable.ic_furniture
    ),
    CAR(
        rawValue = "car",
        displayNameResId = R.string.category_car,
        iconResId = R.drawable.ic_car
    ),
    BICYCLE(
        rawValue = "bicycle",
        displayNameResId = R.string.category_bicycle,
        iconResId = R.drawable.ic_bicycle
    ),
    MOTORCYCLE(
        rawValue = "motorcycle",
        displayNameResId = R.string.category_motorcycle,
        iconResId = R.drawable.ic_motorcycle
    ),
    SPORTS(
        rawValue = "sports",
        displayNameResId = R.string.category_sports,
        iconResId = R.drawable.ic_sports
    ),
    BOOKS(
        rawValue = "books",
        displayNameResId = R.string.category_books,
        iconResId = R.drawable.ic_book
    ),
    KINDERGARTEN(
        rawValue = "kindergarten",
        displayNameResId = R.string.category_kindergarten,
        iconResId = R.drawable.ic_kindergarten
    ),
    SCHOOL(
        rawValue = "school",
        displayNameResId = R.string.category_school,
        iconResId = R.drawable.ic_school
    ),
    UNIVERSITY(
        rawValue = "university",
        displayNameResId = R.string.category_university,
        iconResId = R.drawable.ic_school
    ),
    COLLEGE(
        rawValue = "college",
        displayNameResId = R.string.category_college,
        iconResId = R.drawable.ic_school
    ),
    UNKNOWN(
        rawValue = "unknown",
        displayNameResId = R.string.category_default,
        iconResId = R.drawable.ic_default_marker
    );

    companion object {
        fun fromRawValue(value: String?): PlaceCategory {
            if (value.isNullOrBlank()) return UNKNOWN
            return PlaceCategory.entries.firstOrNull { it.rawValue.equals(value, ignoreCase = true) }
                ?: UNKNOWN
        }
    }
}
