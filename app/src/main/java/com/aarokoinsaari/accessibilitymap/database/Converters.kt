package com.aarokoinsaari.accessibilitymap.database

import androidx.room.TypeConverter
import com.aarokoinsaari.accessibilitymap.model.Tags
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// This class is used to convert Tags objects to JSON strings and back for storing
// in the Room database because Room cannot handle complex objects directly.
class Converters {
    @TypeConverter
    fun fromTags(tags: Tags?): String {
        return Gson().toJson(tags)
    }

    @TypeConverter
    fun toTags(tagString: String?): Tags? {
        return Gson().fromJson(tagString, object : TypeToken<Tags>() {}.type)
    }
}
