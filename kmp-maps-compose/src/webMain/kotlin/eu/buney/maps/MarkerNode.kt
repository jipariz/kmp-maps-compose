package eu.buney.maps

import eu.buney.maps.jsinterop.InfoWindowRef
import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.MarkerRef
import eu.buney.maps.jsinterop.addClickListener
import eu.buney.maps.jsinterop.addCloseListener
import eu.buney.maps.jsinterop.addDragEndListener
import eu.buney.maps.jsinterop.addDragListener
import eu.buney.maps.jsinterop.addDragStartListener
import eu.buney.maps.jsinterop.close
import eu.buney.maps.jsinterop.createInfoWindowDefault
import eu.buney.maps.jsinterop.createInfoWindowImage
import eu.buney.maps.jsinterop.openAt
import eu.buney.maps.jsinterop.removeFromMap
import eu.buney.maps.jsinterop.setIcon
import eu.buney.maps.jsinterop.updateContent

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
    var title: String?,
    var snippet: String?,
    var icon: BitmapDescriptor?,
    /** When non-null, this Compose-rendered bitmap replaces the default title/snippet info window. */
    var customInfoWindowImageUrl: String?,
    var onMarkerClick: (Marker) -> Boolean,
    var onInfoWindowClick: (Marker) -> Unit,
    var onInfoWindowClose: (Marker) -> Unit,
    var onInfoWindowLongClick: (Marker) -> Unit,
    val map: NativeMap,
) : MapNode {

    var infoWindowType: InfoWindowType = InfoWindowType.NONE

    private val listeners = mutableListOf<ListenerToken>()
    private var infoWindow: InfoWindowRef? = null
    private var infoWindowCloseListener: ListenerToken? = null

    private fun ensureInfoWindow(): InfoWindowRef {
        val existing = infoWindow
        if (existing != null) return existing
        val fresh = customInfoWindowImageUrl?.let { createInfoWindowImage(it) }
            ?: createInfoWindowDefault(title, snippet)
        infoWindowCloseListener = fresh.addCloseListener {
            onInfoWindowClose(buildMarkerProxy())
        }
        infoWindow = fresh
        return fresh
    }

    private fun showInfoWindowInternal() {
        ensureInfoWindow().openAt(map, marker)
    }

    private fun hideInfoWindowInternal() {
        infoWindow?.close()
    }

    /** Called by the Marker composable when title/snippet/customInfoWindowImageUrl changes. */
    fun refreshInfoWindowContent() {
        val window = infoWindow ?: return
        val customUrl = customInfoWindowImageUrl
        if (customUrl != null) {
            // Switching to/from custom-image content requires recreating the InfoWindow
            // since setContent doesn't accept arbitrary HTMLElement transitions cleanly.
            window.close()
            infoWindowCloseListener?.remove()
            infoWindow = null
        } else {
            window.updateContent(title, snippet)
        }
    }

    override fun onAttached() {
        markerState.platformMarker = marker
        markerState.showInfoWindowCallback = ::showInfoWindowInternal
        markerState.hideInfoWindowCallback = ::hideInfoWindowInternal
        icon?.acquire()
        listeners += marker.addClickListener {
            // Default behavior: tap opens info window if there's content to show. The
            // user's onMarkerClick can return true to consume the event and suppress this.
            val consumed = onMarkerClick(buildMarkerProxy())
            if (!consumed && (title != null || snippet != null || customInfoWindowImageUrl != null)) {
                showInfoWindowInternal()
            }
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
    }

    /** Called from Marker composable's update block on icon change. */
    fun setIconAndRefcount(newIcon: BitmapDescriptor?) {
        if (newIcon === icon) return
        newIcon?.acquire()
        icon?.release()
        icon = newIcon
        marker.setIcon(newIcon)
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        infoWindowCloseListener?.remove()
        infoWindowCloseListener = null
        infoWindow?.close()
        infoWindow = null
        listeners.forEach { it.remove() }
        listeners.clear()
        markerState.platformMarker = null
        markerState.showInfoWindowCallback = null
        markerState.hideInfoWindowCallback = null
        marker.removeFromMap()
        icon?.release()
        icon = null
    }

    private fun buildMarkerProxy(): Marker =
        Marker(position = markerState.position, title = null, snippet = snippet)
}
