@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps.jsinterop

import eu.buney.maps.BitmapDescriptor
import eu.buney.maps.LatLng
import eu.buney.maps.NativeMap
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

internal actual class MarkerRef internal constructor(internal val handle: JsMarker)

private fun MarkerRef.gmap(): JsMarker = handle

private fun MapsEventListener.toToken(): ListenerToken = ListenerToken { remove() }

private fun toIconAny(url: String): JsAny = js("url")  // String → JsAny

internal actual fun NativeMap.createMarker(options: MarkerCreateOptions): MarkerRef {
    val opts = newMarkerOptions().apply {
        position = newLatLngLiteral(options.position.latitude, options.position.longitude)
        map = handle
        title = options.title
        icon = options.icon?.url?.let { toIconAny(it) }
        opacity = options.opacity.toDouble()
        visible = options.visible
        draggable = options.draggable
        zIndex = options.zIndex.toDouble()
    }
    return MarkerRef(createMarker(opts))
}

internal actual fun MarkerRef.setPosition(lat: Double, lng: Double) {
    gmap().setPosition(newLatLngLiteral(lat, lng))
}

internal actual fun MarkerRef.setTitle(title: String?) {
    gmap().setTitle(title)
}

internal actual fun MarkerRef.setIcon(icon: BitmapDescriptor?) {
    gmap().setIcon(icon?.url?.let { toIconAny(it) })
}

internal actual fun MarkerRef.setOpacity(alpha: Float) {
    gmap().setOpacity(alpha.toDouble())
}

internal actual fun MarkerRef.setVisible(visible: Boolean) {
    gmap().setVisible(visible)
}

internal actual fun MarkerRef.setDraggable(draggable: Boolean) {
    gmap().setDraggable(draggable)
}

internal actual fun MarkerRef.setZIndex(zIndex: Float) {
    gmap().setZIndex(zIndex.toDouble())
}

internal actual fun MarkerRef.removeFromMap() {
    gmap().setMap(null)
}

internal actual fun MarkerRef.getPosition(): LatLng? {
    val pos = gmap().getPosition() ?: return null
    return LatLng(pos.lat(), pos.lng())
}

internal actual fun MarkerRef.addClickListener(handler: () -> Unit): ListenerToken =
    gmap().addListener("click") { _ -> handler() }.toToken()

internal actual fun MarkerRef.addDragStartListener(handler: (LatLng) -> Unit): ListenerToken =
    gmap().addListener("dragstart") { _ ->
        val pos = gmap().getPosition() ?: return@addListener
        handler(LatLng(pos.lat(), pos.lng()))
    }.toToken()

internal actual fun MarkerRef.addDragListener(handler: (LatLng) -> Unit): ListenerToken =
    gmap().addListener("drag") { _ ->
        val pos = gmap().getPosition() ?: return@addListener
        handler(LatLng(pos.lat(), pos.lng()))
    }.toToken()

internal actual fun MarkerRef.addDragEndListener(handler: (LatLng) -> Unit): ListenerToken =
    gmap().addListener("dragend") { _ ->
        val pos = gmap().getPosition() ?: return@addListener
        handler(LatLng(pos.lat(), pos.lng()))
    }.toToken()
