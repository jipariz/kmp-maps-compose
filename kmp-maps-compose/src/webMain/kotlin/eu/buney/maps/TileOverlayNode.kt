package eu.buney.maps

import eu.buney.maps.jsinterop.TileOverlayCreate
import eu.buney.maps.jsinterop.TileOverlayHandle
import eu.buney.maps.jsinterop.refreshTiles
import eu.buney.maps.jsinterop.removeFromMap

internal class TileOverlayNode(
    val map: NativeMap,
    val handle: TileOverlayHandle,
    val tileOverlayState: TileOverlayState,
    /** Captured TileOverlayCreate so clearTileCache() can rebuild the ImageMapType with the
     *  same getTileUrl callback. */
    val createOptions: TileOverlayCreate,
) : MapNode {

    override fun onAttached() {
        tileOverlayState.clearCacheCallback = {
            handle.refreshTiles(map, createOptions)
        }
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        tileOverlayState.clearCacheCallback = null
        handle.removeFromMap(map)
    }
}
