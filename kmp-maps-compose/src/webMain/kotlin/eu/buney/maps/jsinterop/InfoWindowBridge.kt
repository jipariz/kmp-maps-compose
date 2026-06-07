package eu.buney.maps.jsinterop

import eu.buney.maps.NativeMap

internal expect class InfoWindowRef

/**
 * Lazily creates a `google.maps.InfoWindow` whose content is plain HTML (title + snippet
 * formatted as `<div><h3>title</h3><div>snippet</div></div>` — matching Google Maps' default
 * info window styling).
 */
internal expect fun createInfoWindowDefault(title: String?, snippet: String?): InfoWindowRef

/**
 * Creates an InfoWindow whose content is an `<img>` element pointing at the given data /
 * Object URL. Used by [eu.buney.maps.MarkerInfoWindow] to display Compose-rendered bitmaps.
 */
internal expect fun createInfoWindowImage(url: String): InfoWindowRef

/** Opens the info window anchored to a marker on the given map. */
internal expect fun InfoWindowRef.openAt(map: NativeMap, anchor: MarkerRef)

internal expect fun InfoWindowRef.close()

internal expect fun InfoWindowRef.updateContent(title: String?, snippet: String?)

/** Fired when the user clicks the X to close the info window. */
internal expect fun InfoWindowRef.addCloseListener(handler: () -> Unit): ListenerToken
