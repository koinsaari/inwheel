package com.aarokoinsaari.accessibilitymap.model

data class OverpassApiResponse(
    val elements: List<ApiMapMarkers>
)

data class ApiMapMarkers(
    val type: String,
    val id: Long,
    val lat: Double,
    val lon: Double,
    val tags: Map<String, String>?
)
