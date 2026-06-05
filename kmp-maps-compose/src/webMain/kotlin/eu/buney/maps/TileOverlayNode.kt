package eu.buney.maps

import eu.buney.maps.jsinterop.TileOverlayHandle
import eu.buney.maps.jsinterop.removeFromMap

internal class TileOverlayNode(
    val map: NativeMap,
    val handle: TileOverlayHandle,
    val tileOverlayState: TileOverlayState,
) : MapNode {

    override fun onAttached() {
        // The JS API doesn't have a tile-cache-clear primitive on ImageMapType — the easiest
        // way to force a refresh is to re-create the overlay. PR5 can implement this by
        // tearing down + recreating; for v1 we no-op.
        tileOverlayState.clearCacheCallback = { /* TODO PR5: clear tile cache */ }
    }

    override fun onRemoved() = detach()
    override fun onCleared() = detach()

    private fun detach() {
        tileOverlayState.clearCacheCallback = null
        handle.removeFromMap(map)
    }
}
