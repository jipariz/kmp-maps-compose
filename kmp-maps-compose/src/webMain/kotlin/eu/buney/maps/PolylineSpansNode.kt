package eu.buney.maps

import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.PolylineRef
import eu.buney.maps.jsinterop.addClickListener
import eu.buney.maps.jsinterop.removeFromMap

/**
 * Node for the styled [Polyline] overload — backed by N plain JS polylines (one per span)
 * since `google.maps.Polyline` doesn't support per-segment coloring natively. All N
 * polylines share the same click callback and removal lifecycle.
 */
internal class PolylineSpansNode(
    initialPolylines: List<PolylineRef>,
    var points: List<LatLng>,
    var onPolylineClick: (Polyline) -> Unit,
) : MapNode {

    val polylines: MutableList<PolylineRef> = initialPolylines.toMutableList()
    private val listeners = mutableListOf<ListenerToken>()

    override fun onAttached() {
        attachListeners()
    }

    /** Rebuild listeners after [polylines] has been replaced (e.g. spans count changed). */
    fun reattachListeners() {
        listeners.forEach { it.remove() }
        listeners.clear()
        attachListeners()
    }

    private fun attachListeners() {
        polylines.forEach { ref ->
            listeners += ref.addClickListener {
                onPolylineClick(Polyline(points))
            }
        }
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        listeners.forEach { it.remove() }
        listeners.clear()
        polylines.forEach { it.removeFromMap() }
        polylines.clear()
    }
}
