package com.aarokoinsaari.accessibilitymap.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aarokoinsaari.accessibilitymap.model.Element

@Database(entities = [Element::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun elementDao(): ElementDao
}
