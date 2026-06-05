package eu.buney.maps

import eu.buney.maps.jsinterop.CircleRef
import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.addClickListener
import eu.buney.maps.jsinterop.removeFromMap

internal class CircleNode(
    val circle: CircleRef,
    var center: LatLng,
    var radius: Double,
    var onCircleClick: (Circle) -> Unit,
) : MapNode {

    private var listener: ListenerToken? = null

    override fun onAttached() {
        listener = circle.addClickListener {
            onCircleClick(Circle(center, radius))
        }
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        listener?.remove()
        listener = null
        circle.removeFromMap()
    }
}
