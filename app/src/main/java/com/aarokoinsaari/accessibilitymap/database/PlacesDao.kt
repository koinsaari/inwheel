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
        southLat: Double,
        northLat: Double,
        westLon: Double,
        eastLon: Double
    ): List<Place>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(places: List<Place>)
}
