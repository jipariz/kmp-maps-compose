package eu.buney.maps.jsinterop

import eu.buney.maps.NativeMap

internal actual class GroundOverlayRef internal constructor(internal val handle: JsGroundOverlay)

private fun MapsEventListener.toToken(): ListenerToken = ListenerToken { remove() }

internal actual fun NativeMap.createGroundOverlay(c: GroundOverlayCreate): GroundOverlayRef {
    val bounds = newLatLngBoundsLiteral(
        north = c.bounds.northeast.latitude,
        south = c.bounds.southwest.latitude,
        east = c.bounds.northeast.longitude,
        west = c.bounds.southwest.longitude,
    )
    val opts: dynamic = js("({})")
    opts.opacity = (1f - c.transparency).toDouble()
    opts.clickable = c.clickable
    opts.map = handle
    return GroundOverlayRef(createGroundOverlay(c.url, bounds, opts))
}

internal actual fun GroundOverlayRef.setTransparency(transparency: Float) {
    handle.setOpacity((1f - transparency).toDouble())
}

internal actual fun GroundOverlayRef.removeFromMap() {
    handle.setMap(null)
}

internal actual fun GroundOverlayRef.addClickListener(handler: () -> Unit): ListenerToken =
    handle.addListener("click") { _ -> handler() }.toToken()
