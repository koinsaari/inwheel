/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aarokoinsaari.accessibilitymap.model.Place

@Dao
interface PlacesDao {
    @Query(
        """
        SELECT * FROM places 
        WHERE lat BETWEEN :southLat AND :northLat 
        AND lon BETWEEN :westLon AND :eastLon
    """
    )
    fun getPlaces(
        northLat: Double,
        eastLon: Double,
        southLat: Double,
        westLon: Double
    ): List<Place>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(places: List<Place>)
}
