/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aarokoinsaari.accessibilitymap.model.MapMarker

@Dao
interface MapMarkerDao {
    @Query(
        """
        SELECT * FROM map_markers 
        WHERE lat BETWEEN :southLat AND :northLat 
        AND lon BETWEEN :westLon AND :eastLon
    """
    )
    fun getMarkers(
        northLat: Double,
        eastLon: Double,
        southLat: Double,
        westLon: Double
    ): List<MapMarker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(mapMarkers: List<MapMarker>)
}
