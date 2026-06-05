@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps.jsinterop

import eu.buney.maps.NativeMap
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

/**
 * Holds the JS-side `ImageMapType` and the index at which it was inserted into the map's
 * `overlayMapTypes` collection. The index is needed for `removeAt` — Google Maps doesn't
 * expose a "find this overlay" API.
 */
internal actual class TileOverlayHandle internal constructor(
    internal val overlay: JsImageMapType,
    internal var index: Int,
)

// Helpers — wrap `js("...")` patterns so the Wasm one-expression rule is respected.

private fun newSize(w: Int, h: Int): JsAny =
    js("new google.maps.Size(w, h)")

private fun buildImageMapType(
    getUrl: (Int, Int, Int) -> String?,
    tileSize: JsAny,
    opacity: Double,
): JsImageMapType =
    js("new google.maps.ImageMapType({ getTileUrl: function(c, z) { var u = getUrl(c.x, c.y, z); return u == null ? '' : u; }, tileSize: tileSize, opacity: opacity })")

private fun overlayMapTypes(map: GMap): JsAny =
    js("map.overlayMapTypes")

private fun lengthOf(arr: JsAny): Int =
    js("arr.getLength()")

private fun pushOverlay(arr: JsAny, overlay: JsImageMapType): Unit =
    js("arr.push(overlay)")

private fun removeAt(arr: JsAny, index: Int): Unit =
    js("arr.removeAt(index)")

private fun setOpacity(overlay: JsImageMapType, opacity: Double): Unit =
    js("overlay.setOpacity(opacity)")

internal actual fun NativeMap.addTileOverlay(c: TileOverlayCreate): TileOverlayHandle {
    val tileSize = newSize(256, 256)
    val overlay = buildImageMapType(c.getTile, tileSize, c.opacity.toDouble())
    val overlays = overlayMapTypes(handle)
    pushOverlay(overlays, overlay)
    return TileOverlayHandle(overlay, lengthOf(overlays) - 1)
}

internal actual fun TileOverlayHandle.removeFromMap(map: NativeMap) {
    val overlays = overlayMapTypes(map.handle)
    // The index may have shifted if other overlays were removed; search by reference.
    val len = lengthOf(overlays)
    var found = -1
    for (i in 0 until len) {
        if (overlayAt(overlays, i) === overlay) {
            found = i
            break
        }
    }
    if (found >= 0) removeAt(overlays, found)
}

private fun overlayAt(arr: JsAny, index: Int): JsImageMapType =
    js("arr.getAt(index)")

internal actual fun TileOverlayHandle.setTileOverlayOpacity(map: NativeMap, opacity: Float) {
    setOpacity(overlay, opacity.toDouble())
}
