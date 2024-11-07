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

package com.aarokoinsaari.accessibilitymap.data.remote

import com.aarokoinsaari.accessibilitymap.model.PlaceCategory

object OverpassQueryBuilder {
    fun buildQuery(bounds: String): String {
        val categories = PlaceCategory.entries
            .filter { it != PlaceCategory.DEFAULT }
            .map { it.amenityTag }
        val categoryQuery = categories.joinToString(separator = "|") { it }

        return """
            [out:json][timeout:25];
            (
              node["amenity"~"^($categoryQuery)$"]($bounds);
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()
    }
}
