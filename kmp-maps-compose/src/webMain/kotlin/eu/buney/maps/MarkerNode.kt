package eu.buney.maps

import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.MarkerRef
import eu.buney.maps.jsinterop.addClickListener
import eu.buney.maps.jsinterop.addDragEndListener
import eu.buney.maps.jsinterop.addDragListener
import eu.buney.maps.jsinterop.addDragStartListener
import eu.buney.maps.jsinterop.removeFromMap

internal enum class InfoWindowType {
    NONE,
    CONTENT,
    WINDOW,
}

/**
 * [MapNode] implementation for markers.
 *
 * Holds the [MarkerRef], the user-supplied [MarkerState], and the click/info-window
 * callbacks. Mirrors the iosMain MarkerNode pattern.
 */
internal class MarkerNode(
    val marker: MarkerRef,
    val markerState: MarkerState,
    var snippet: String?,
    var onMarkerClick: (Marker) -> Boolean,
    var onInfoWindowClick: (Marker) -> Unit,
    var onInfoWindowClose: (Marker) -> Unit,
    var onInfoWindowLongClick: (Marker) -> Unit,
) : MapNode {

    var infoWindowType: InfoWindowType = InfoWindowType.NONE

    private val listeners = mutableListOf<ListenerToken>()

    override fun onAttached() {
        markerState.platformMarker = marker
        listeners += marker.addClickListener {
            onMarkerClick(buildMarkerProxy())
        }
        listeners += marker.addDragStartListener { latLng ->
            markerState.isDragging = true
            markerState.position = latLng
        }
        listeners += marker.addDragListener { latLng ->
            markerState.position = latLng
        }
        listeners += marker.addDragEndListener { latLng ->
            markerState.position = latLng
            markerState.isDragging = false
        }
        // PR5: wire InfoWindow click/close/longclick events. Google Maps JS InfoWindow
        // exposes 'closeclick' but no built-in 'click' or 'longclick' on its content frame.
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        listeners.forEach { it.remove() }
        listeners.clear()
        markerState.platformMarker = null
        marker.removeFromMap()
    }

    private fun buildMarkerProxy(): Marker =
        Marker(position = markerState.position, title = null, snippet = snippet)
}
