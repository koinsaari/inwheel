package com.aarokoinsaari.accessibilitymap.state

import com.aarokoinsaari.accessibilitymap.model.Element

data class MapState(
    val markers: List<Element> = emptyList(),
    val isLoading: Boolean = false
)
