package com.aarokoinsaari.accessibilitymap.model

data class OverpassResponse(
    val elements: List<Element>
)

data class Element(
    val type: String,
    val id: Long,
    val lat: Double?,
    val lon: Double?,
    val tags: Tags?
)

data class Tags(
    val amenity: String?,
    val name: String?,
    val brand: String?,
    val openingHours: String?,
)
