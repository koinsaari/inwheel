/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.database

import androidx.room.TypeConverter
import com.aarokoinsaari.accessibilitymap.model.AccessibilityInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * This class converts JSON strings to Maps and custom objects for Room
 * because Room cannot handle complex objects directly.
 */
class Converters {
    @TypeConverter
    fun fromStringToMap(value: String?): Map<String, String>? =
        value?.let {
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            Gson().fromJson(value, mapType)
        }

    @TypeConverter
    fun fromMapToString(map: Map<String, String>?): String? =
        Gson().toJson(map)

    @TypeConverter
    fun fromStringToAccessibilityInfo(value: String?): AccessibilityInfo? =
        value?.let {
            Gson().fromJson(value, AccessibilityInfo::class.java)
        }

    @TypeConverter
    fun fromAccessibilityInfoToString(info: AccessibilityInfo?): String? =
        Gson().toJson(info)
}
