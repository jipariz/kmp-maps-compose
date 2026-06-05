package eu.buney.maps.jsinterop

import eu.buney.maps.LatLngBounds
import eu.buney.maps.NativeMap

internal expect class GroundOverlayRef

internal class GroundOverlayCreate(
    val url: String,
    val bounds: LatLngBounds,
    val transparency: Float,
    val clickable: Boolean,
)

internal expect fun NativeMap.createGroundOverlay(c: GroundOverlayCreate): GroundOverlayRef
internal expect fun GroundOverlayRef.setTransparency(transparency: Float)
internal expect fun GroundOverlayRef.removeFromMap()
internal expect fun GroundOverlayRef.addClickListener(handler: () -> Unit): ListenerToken
