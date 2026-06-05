package eu.buney.maps

import eu.buney.maps.jsinterop.GroundOverlayRef
import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.addClickListener
import eu.buney.maps.jsinterop.removeFromMap

internal class GroundOverlayNode(
    val groundOverlay: GroundOverlayRef,
    var bounds: LatLngBounds,
    var bearing: Float,
    var transparency: Float,
    var onGroundOverlayClick: (GroundOverlay) -> Unit,
) : MapNode {

    private var listener: ListenerToken? = null

    override fun onAttached() {
        listener = groundOverlay.addClickListener {
            onGroundOverlayClick(GroundOverlay(bounds, bearing, transparency))
        }
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        listener?.remove()
        listener = null
        groundOverlay.removeFromMap()
    }
}
