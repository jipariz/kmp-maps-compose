package eu.buney.maps.jsinterop

import eu.buney.maps.NativeMap

internal expect class TileOverlayHandle

internal class TileOverlayCreate(
    /** Returns the tile URL for the requested (x, y, zoom). Null = no tile at this location. */
    val getTile: (x: Int, y: Int, zoom: Int) -> String?,
    val opacity: Float,
    val zIndex: Float,
)

internal expect fun NativeMap.addTileOverlay(c: TileOverlayCreate): TileOverlayHandle
internal expect fun TileOverlayHandle.removeFromMap(map: NativeMap)
internal expect fun TileOverlayHandle.setTileOverlayOpacity(map: NativeMap, opacity: Float)
