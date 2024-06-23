package com.aarokoinsaari.accessibilitymap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aarokoinsaari.accessibilitymap.intent.MapIntent
import com.aarokoinsaari.accessibilitymap.model.BoundingBox
import com.aarokoinsaari.accessibilitymap.repository.PlaceRepository
import com.aarokoinsaari.accessibilitymap.state.MapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.events.MapAdapter
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint

class MapViewModel(private val placeRepository: PlaceRepository) : ViewModel() {
    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state

    val mapListener = object : MapAdapter() {
        override fun onZoom(event: ZoomEvent?): Boolean {
            return handleZoomOrScroll(event)
        }

        override fun onScroll(event: ScrollEvent?): Boolean {
            return handleZoomOrScroll(event)
        }

        private fun handleZoomOrScroll(event: Any?): Boolean {
            val zoomLevel = getZoomLevel(event)
            val center = getCenter(event)
            return if (zoomLevel != null && center != null) {
                if (zoomLevel > ZOOM_THRESHOLD) {
                    handleIntent(MapIntent.LoadMarkers(zoomLevel, center))
                }
                true
            } else {
                false
            }
        }

        private fun getZoomLevel(event: Any?): Double? {
            return when (event) {
                is ZoomEvent -> event.source.zoomLevelDouble
                is ScrollEvent -> event.source.zoomLevelDouble
                else -> null
            }
        }

        private fun getCenter(event: Any?): GeoPoint? {
            return when (event) {
                is ZoomEvent -> event.source.mapCenter as? GeoPoint
                is ScrollEvent -> event.source.mapCenter as? GeoPoint
                else -> null
            }
        }
    }

    fun handleIntent(intent: MapIntent) {
        viewModelScope.launch {
            when (intent) {
                is MapIntent.LoadMarkers -> loadMarkers(intent.zoomLevel, intent.center)
                MapIntent.ClearMarkers -> _state.value = _state.value.copy(markers = emptyList())
            }
        }
    }

    private suspend fun loadMarkers(zoomLevel: Double, center: GeoPoint) {
        _state.value = _state.value.copy(isLoading = true)
        val bbox = calculateBoundingBox(zoomLevel, center)
        val categories = listOf("restaurant", "cafe", "shop")
        placeRepository.getPlaces(bbox, categories).collect { places ->
            _state.value = _state.value.copy(markers = places, isLoading = false)
        }
    }

    private fun calculateBoundingBox(
        zoomLevel: Double,
        center: GeoPoint,
    ): BoundingBox {
        val latOffset = LAT_OFFSET_FACTOR / zoomLevel * EXPAND_FACTOR
        val lonOffset = LON_OFFSET_FACTOR / zoomLevel * EXPAND_FACTOR
        val minLat = center.latitude - latOffset
        val maxLat = center.latitude + latOffset
        val minLon = center.longitude - lonOffset
        val maxLon = center.longitude + lonOffset
        return BoundingBox(minLat, minLon, maxLat, maxLon)
    }

    companion object {
        private const val ZOOM_THRESHOLD = 10.0
        private const val LAT_OFFSET_FACTOR = 0.03
        private const val LON_OFFSET_FACTOR = 0.03
        private const val EXPAND_FACTOR = 1.5
    }
}
