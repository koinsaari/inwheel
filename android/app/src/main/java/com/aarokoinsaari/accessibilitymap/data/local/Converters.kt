/*
 * Copyright (c) 2025 Aaro Koinsaari
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

package com.aarokoinsaari.accessibilitymap.data.local


import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.aarokoinsaari.accessibilitymap.model.ContactInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityInfo
import com.aarokoinsaari.accessibilitymap.model.accessibility.AccessibilityStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@ProvidedTypeConverter
class Converters {

    private val jsonFormat = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    // AccessibilityStatus
    @TypeConverter
    fun fromAccessibilityStatus(value: AccessibilityStatus?): String? = value?.name

    @TypeConverter
    fun toAccessibilityStatus(value: String?): AccessibilityStatus? =
        if (value == null) null else AccessibilityStatus.valueOf(value)

    // AccessibilityInfo
    @TypeConverter
    fun fromAccessibilityInfo(value: AccessibilityInfo?): String? {
        if (value == null) return null
        return jsonFormat.encodeToString(value)
    }

    @TypeConverter
    fun toAccessibilityInfo(value: String?): AccessibilityInfo? {
        if (value == null) return null
        return jsonFormat.decodeFromString(value)
    }

    // ContactInfo
    @TypeConverter
    fun fromContactInfo(info: ContactInfo?): String? {
        if (info == null) return null
        return jsonFormat.encodeToString(info)
    }

    @TypeConverter
    fun toContactInfo(json: String?): ContactInfo? {
        if (json == null) return null
        return jsonFormat.decodeFromString(json)
    }

}
