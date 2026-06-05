package eu.buney.maps

import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.PolylineRef
import eu.buney.maps.jsinterop.addClickListener
import eu.buney.maps.jsinterop.removeFromMap

internal class PolylineNode(
    val polyline: PolylineRef,
    var points: List<LatLng>,
    var onPolylineClick: (Polyline) -> Unit,
) : MapNode {

    private var listener: ListenerToken? = null

    override fun onAttached() {
        listener = polyline.addClickListener {
            onPolylineClick(Polyline(points))
        }
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        listener?.remove()
        listener = null
        polyline.removeFromMap()
    }
}
