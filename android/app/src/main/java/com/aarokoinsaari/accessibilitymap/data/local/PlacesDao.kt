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
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.aarokoinsaari.accessibilitymap.domain.model.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacesDao {

    @Query("""
        SELECT * FROM places 
        WHERE lat BETWEEN :southLat AND :northLat 
        AND lon BETWEEN :westLon AND :eastLon
        LIMIT :limit
    """)
    fun getPlacesWithinBounds(
        southLat: Double,
        northLat: Double,
        westLon: Double,
        eastLon: Double,
        limit: Int = 5000
    ): Flow<List<Place>>
    
    @Query("SELECT * FROM places WHERE tileId IN (:tileIds) AND fetchTimestamp > :minTimestamp")
    fun getPlacesInTiles(tileIds: List<String>, minTimestamp: Long): Flow<List<Place>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaces(places: List<Place>)
    
    @Transaction
    suspend fun insertPlacesInTile(tileId: String, places: List<Place>) {
        val placesWithTile = places.map { it.copy(tileId = tileId, lastVisited = System.currentTimeMillis()) }
        insertPlaces(placesWithTile)
    }

    @Query("UPDATE places SET generalAccessibility = :status, userModified = 1 WHERE id = :id")
    suspend fun updatePlaceGeneralAccessibility(id: String, status: String?)

    @RawQuery
    suspend fun updateAccessibilityDetail(query: SupportSQLiteQuery): Int
    
    suspend fun updatePlaceAccessibilityDetailString(id: String, columnName: String, newValue: String) {
        val queryString = "UPDATE places SET $columnName = ?, userModified = 1 WHERE id = ?"
        val args = arrayOf<Any>(newValue, id)
        val query = SimpleSQLiteQuery(queryString, args)
        updateAccessibilityDetail(query)
    }
    
    suspend fun updatePlaceAccessibilityDetailBoolean(id: String, columnName: String, newValue: Boolean) {
        val queryString = "UPDATE places SET $columnName = ?, userModified = 1 WHERE id = ?"
        val args = arrayOf<Any>(newValue, id)
        val query = SimpleSQLiteQuery(queryString, args)
        updateAccessibilityDetail(query)
    }

    @Query("DELETE FROM places WHERE lastVisited < :cutoffTimestamp AND tileId NOT IN (:activeTileIds)")
    suspend fun cleanupOldPlaces(cutoffTimestamp: Long, activeTileIds: List<String>): Int
}
