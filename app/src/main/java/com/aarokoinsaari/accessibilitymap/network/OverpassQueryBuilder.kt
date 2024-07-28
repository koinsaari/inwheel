/*
 * Copyright (c) 2024 Aaro Koinsaari
 */
package com.aarokoinsaari.accessibilitymap.network

object OverpassQueryBuilder {
    fun buildQuery(bbox: String, categories: List<String>): String {
        val categoryQueries = categories.joinToString(separator = "") { category ->
            """node["amenity"="$category"]($bbox);"""
        }
        return """
            [out:json];
            (
              $categoryQueries
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()
    }
}
