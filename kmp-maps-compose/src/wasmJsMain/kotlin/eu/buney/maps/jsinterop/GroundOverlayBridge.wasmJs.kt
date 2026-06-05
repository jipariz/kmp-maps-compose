@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps.jsinterop

import eu.buney.maps.NativeMap
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

internal actual class GroundOverlayRef internal constructor(internal val handle: JsGroundOverlay)

private fun groundOverlayOpts(opacity: Double, clickable: Boolean, mapHandle: GMap): JsAny =
    js("({ opacity: opacity, clickable: clickable, map: mapHandle })")

private fun MapsEventListener.toToken(): ListenerToken = ListenerToken { remove() }

internal actual fun NativeMap.createGroundOverlay(c: GroundOverlayCreate): GroundOverlayRef {
    val bounds = newLatLngBoundsLiteral(
        north = c.bounds.northeast.latitude,
        south = c.bounds.southwest.latitude,
        east = c.bounds.northeast.longitude,
        west = c.bounds.southwest.longitude,
    )
    // android-maps + iOS use 'transparency' (0=opaque, 1=invisible); JS uses 'opacity'
    // (0=invisible, 1=opaque). Invert.
    val opts = groundOverlayOpts(
        opacity = (1f - c.transparency).toDouble(),
        clickable = c.clickable,
        mapHandle = handle,
    )
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
