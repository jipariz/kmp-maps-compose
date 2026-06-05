package eu.buney.maps

import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.PolygonRef
import eu.buney.maps.jsinterop.addClickListener
import eu.buney.maps.jsinterop.removeFromMap

internal class PolygonNode(
    val polygon: PolygonRef,
    var points: List<LatLng>,
    var holes: List<List<LatLng>>,
    var onPolygonClick: (Polygon) -> Unit,
) : MapNode {

    private var listener: ListenerToken? = null

    override fun onAttached() {
        listener = polygon.addClickListener {
            onPolygonClick(Polygon(points, holes))
        }
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        listener?.remove()
        listener = null
        polygon.removeFromMap()
    }
}
