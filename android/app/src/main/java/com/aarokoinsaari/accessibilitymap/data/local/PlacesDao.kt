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

package com.aarokoinsaari.accessibilitymap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import com.aarokoinsaari.accessibilitymap.domain.model.PlaceFts
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacesDao {
    @Query("""
    SELECT * FROM places 
    WHERE lat BETWEEN :southLat AND :northLat 
      AND lon BETWEEN :westLon AND :eastLon 
    LIMIT :limit
""")
    fun getPlacesFlowWithinBounds(
        southLat: Double,
        northLat: Double,
        westLon: Double,
        eastLon: Double,
        limit: Int
    ): Flow<List<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<Place>)

    @Query("UPDATE places SET generalAccessibility = :status WHERE id = :id")
    suspend fun updatePlaceGeneralAccessibility(id: String, status: String)

    @RawQuery
    suspend fun updateAccessibilityDetail(query: SupportSQLiteQuery): Int

    suspend fun updatePlaceAccessibilityDetailString(id: String, columnName: String, newValue: String) {
        val queryString = "UPDATE places SET $columnName = ? WHERE id = ?"
        val args = arrayOf<Any>(newValue, id)
        val query = SimpleSQLiteQuery(queryString, args)
        updateAccessibilityDetail(query)
    }

    suspend fun updatePlaceAccessibilityDetailInt(id: String, columnName: String, newValue: Int) {
        val queryString = "UPDATE places SET $columnName = ? WHERE id = ?"
        val args = arrayOf<Any>(newValue, id)
        val query = SimpleSQLiteQuery(queryString, args)
        updateAccessibilityDetail(query)
    }

    suspend fun updatePlaceAccessibilityDetailBoolean(id: String, columnName: String, newValue: Boolean) {
        val queryString = "UPDATE places SET $columnName = ? WHERE id = ?"
        val args = arrayOf<Any>(newValue, id)
        val query = SimpleSQLiteQuery(queryString, args)
        updateAccessibilityDetail(query)
    }
}

@Dao
interface PlacesFtsDao {
    @Query(
        """
        SELECT p.*, 
        ((p.lat - :userLat)*(p.lat - :userLat) + (p.lon - :userLon)*(p.lon - :userLon)) AS distance 
        FROM places p 
        JOIN places_fts fts ON p.id = fts.rowid 
        WHERE fts.name MATCH :query 
        ORDER BY distance LIMIT 50
    """
    )
    suspend fun searchPlacesByName(query: String, userLat: Double, userLon: Double): List<Place>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(placesFts: List<PlaceFts>)
}
