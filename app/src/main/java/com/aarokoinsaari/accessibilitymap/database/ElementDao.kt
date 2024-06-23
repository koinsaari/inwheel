package com.aarokoinsaari.accessibilitymap.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aarokoinsaari.accessibilitymap.model.Element

@Dao
interface ElementDao {
    @Query(
        """
        SELECT * FROM elements 
        WHERE lat BETWEEN :minLat AND :maxLat 
        AND lon BETWEEN :minLon AND :maxLon
    """
    )
    fun getElements(minLat: Double, minLon: Double, maxLat: Double, maxLon: Double): List<Element>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(elements: List<Element>)
}
