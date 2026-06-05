package eu.buney.maps.jsinterop

import eu.buney.maps.NativeMap

internal expect class TileOverlayHandle

internal class TileOverlayCreate(
    /** Returns the tile URL for the requested (x, y, zoom). Null = no tile at this location. */
    val getTile: (x: Int, y: Int, zoom: Int) -> String?,
    val opacity: Float,
    val zIndex: Float,
)

/**
 * Forces all visible tiles to refetch by removing and re-inserting the ImageMapType into
 * the map's overlayMapTypes collection. JS Maps has no direct tile-cache-clear primitive,
 * so this is the standard workaround.
 */
internal expect fun TileOverlayHandle.refreshTiles(map: NativeMap, c: TileOverlayCreate)

internal expect fun NativeMap.addTileOverlay(c: TileOverlayCreate): TileOverlayHandle
internal expect fun TileOverlayHandle.removeFromMap(map: NativeMap)
internal expect fun TileOverlayHandle.setTileOverlayOpacity(map: NativeMap, opacity: Float)
