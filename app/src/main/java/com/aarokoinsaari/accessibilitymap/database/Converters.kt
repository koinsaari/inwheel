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

package com.aarokoinsaari.accessibilitymap.database

import androidx.room.TypeConverter
import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.utils.PlaceCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * This class converts JSON strings to Maps and custom objects for Room
 * because Room cannot handle complex objects directly.
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringToMap(value: String?): Map<String, String>? =
        value?.let {
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(value, mapType)
        }

    @TypeConverter
    fun fromMapToString(map: Map<String, String>?): String? =
        gson.toJson(map)

    @TypeConverter
    fun fromStringToAccessibilityInfo(value: String?): AccessibilityInfo? =
        value?.let {
            gson.fromJson(value, AccessibilityInfo::class.java)
        }

    @TypeConverter
    fun fromAccessibilityInfoToString(info: AccessibilityInfo?): String? =
        gson.toJson(info)

    @TypeConverter
    fun fromPlaceCategoryToString(value: String?): PlaceCategory? =
        value?.let { PlaceCategory.fromAmenityTag(it) }

    @TypeConverter
    fun placeCategoryToString(category: PlaceCategory?): String? =
        category?.amenityTag
}
