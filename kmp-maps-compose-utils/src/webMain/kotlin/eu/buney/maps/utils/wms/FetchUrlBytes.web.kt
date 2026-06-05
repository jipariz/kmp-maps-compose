package eu.buney.maps.utils.wms

/**
 * Web limitation: `TileProvider.getTile` is a synchronous function but browsers don't expose
 * synchronous network — `XMLHttpRequest` synchronous mode is deprecated and disabled in
 * workers. PR5 should change the `TileProvider` contract to a suspend function so this can
 * use `window.fetch().await().arrayBuffer().await()`. For now WMS tile fetches on web silently
 * fail (return null → blank tile).
 */
internal actual fun fetchUrlBytes(url: String): ByteArray? = null

