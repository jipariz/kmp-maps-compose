package eu.buney.maps.jsinterop

import eu.buney.maps.BitmapDescriptor
import eu.buney.maps.LatLng
import eu.buney.maps.NativeMap

/**
 * Opaque per-target handle to a `google.maps.Marker`. webMain code only constructs / mutates
 * markers via the helper functions below; the actual `JsMarker` reference is hidden.
 */
internal expect class MarkerRef

internal class MarkerCreateOptions(
    val position: LatLng,
    val title: String? = null,
    val icon: BitmapDescriptor? = null,
    val opacity: Float = 1f,
    val visible: Boolean = true,
    val draggable: Boolean = false,
    val zIndex: Float = 0f,
)

internal expect fun NativeMap.createMarker(options: MarkerCreateOptions): MarkerRef
internal expect fun MarkerRef.setPosition(lat: Double, lng: Double)
internal expect fun MarkerRef.setTitle(title: String?)
internal expect fun MarkerRef.setIcon(icon: BitmapDescriptor?)
internal expect fun MarkerRef.setOpacity(alpha: Float)
internal expect fun MarkerRef.setVisible(visible: Boolean)
internal expect fun MarkerRef.setDraggable(draggable: Boolean)
internal expect fun MarkerRef.setZIndex(zIndex: Float)
internal expect fun MarkerRef.removeFromMap()
internal expect fun MarkerRef.getPosition(): LatLng?

internal expect fun MarkerRef.addClickListener(handler: () -> Unit): ListenerToken
internal expect fun MarkerRef.addDragStartListener(handler: (LatLng) -> Unit): ListenerToken
internal expect fun MarkerRef.addDragListener(handler: (LatLng) -> Unit): ListenerToken
internal expect fun MarkerRef.addDragEndListener(handler: (LatLng) -> Unit): ListenerToken
