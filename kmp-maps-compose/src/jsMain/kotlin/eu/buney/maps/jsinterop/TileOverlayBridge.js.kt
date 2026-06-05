package eu.buney.maps.jsinterop

import eu.buney.maps.NativeMap

internal actual class TileOverlayHandle internal constructor(
    internal var overlay: JsImageMapType,
)

@Suppress("UNUSED_PARAMETER")
private fun buildImageMapType(
    getUrl: (Int, Int, Int) -> String?,
    opacity: Double,
): JsImageMapType =
    js("new google.maps.ImageMapType({ getTileUrl: function(c, z) { var u = getUrl(c.x, c.y, z); return u == null ? '' : u; }, tileSize: new google.maps.Size(256, 256), opacity: opacity })")
        .unsafeCast<JsImageMapType>()

internal actual fun NativeMap.addTileOverlay(c: TileOverlayCreate): TileOverlayHandle {
    val overlay = buildImageMapType(c.getTile, c.opacity.toDouble())
    val map: dynamic = handle
    map.overlayMapTypes.push(overlay)
    return TileOverlayHandle(overlay)
}

internal actual fun TileOverlayHandle.removeFromMap(map: NativeMap) {
    val m: dynamic = map.handle
    val arr = m.overlayMapTypes
    val len = arr.getLength().unsafeCast<Int>()
    for (i in 0 until len) {
        if (arr.getAt(i) === overlay) {
            arr.removeAt(i)
            return
        }
    }
}

internal actual fun TileOverlayHandle.setTileOverlayOpacity(map: NativeMap, opacity: Float) {
    overlay.asDynamic().setOpacity(opacity.toDouble())
}

internal actual fun TileOverlayHandle.refreshTiles(map: NativeMap, c: TileOverlayCreate) {
    val m: dynamic = map.handle
    val arr = m.overlayMapTypes
    val len = arr.getLength().unsafeCast<Int>()
    for (i in 0 until len) {
        if (arr.getAt(i) === overlay) {
            arr.removeAt(i)
            break
        }
    }
    overlay = buildImageMapType(c.getTile, c.opacity.toDouble())
    arr.push(overlay)
}
