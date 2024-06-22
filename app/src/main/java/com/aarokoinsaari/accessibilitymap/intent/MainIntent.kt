package com.aarokoinsaari.accessibilitymap.intent

sealed class MainIntent {
    data object NavigateToMap : MainIntent()
    data object NavigateToPlaces : MainIntent()
}
