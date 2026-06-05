package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentComposer
import eu.buney.maps.jsinterop.TileOverlayCreate
import eu.buney.maps.jsinterop.addTileOverlay
import eu.buney.maps.jsinterop.setTileOverlayOpacity

/**
 * Backing store for a tile: a URL pointing to the image data. The JS `ImageMapType` accepts
 * a `getTileUrl` callback returning a URL string, so we materialize tile bytes into a data:
 * URL at the moment the JS API asks for a tile.
 */
actual class Tile internal constructor(internal val url: String)

actual object TileFactory {
    actual fun fromBytes(bytes: ByteArray, width: Int, height: Int): Tile =
        Tile(argbBytesToDataUrl(bytes, width, height))

    actual fun fromEncodedImage(data: ByteArray, width: Int, height: Int): Tile =
        Tile(encodedImageBytesToObjectUrl(data, sniffImageMime(data)))
}

actual fun urlBackedTileOrNull(url: String, width: Int, height: Int): Tile? = Tile(url)

@Stable
actual class TileOverlayState actual constructor() {
    internal var clearCacheCallback: (() -> Unit)? = null

    actual fun clearTileCache() {
        clearCacheCallback?.invoke()
    }
}

@Composable
@GoogleMapComposable
actual fun TileOverlay(
    tileProvider: TileProvider,
    state: TileOverlayState,
    fadeIn: Boolean,
    transparency: Float,
    visible: Boolean,
    zIndex: Float,
    onClick: (NativeTileOverlay) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier ?: return
    val create = TileOverlayCreate(
        getTile = { x, y, zoom -> tileProvider.getTile(x, y, zoom)?.url },
        opacity = 1f - transparency,
        zIndex = zIndex,
    )

    ComposeNode<TileOverlayNode, MapApplier>(
        factory = {
            TileOverlayNode(
                map = mapApplier.map,
                handle = mapApplier.map.addTileOverlay(create),
                tileOverlayState = state,
                createOptions = create,
            )
        },
        update = {
            set(transparency) { handle.setTileOverlayOpacity(map, 1f - it) }
            // PR5: zIndex / visibility changes require re-inserting in overlayMapTypes since
            // ImageMapType doesn't expose live setters for those.
        },
    )
}
