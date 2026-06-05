package eu.buney.maps

import eu.buney.maps.jsinterop.GMap

/**
 * Wraps the underlying `google.maps.Map` instance.
 *
 * Held as a typed [GMap] reference rather than `Any` because Kotlin/Wasm's `Any` and `JsAny`
 * are separate type hierarchies — `unsafeCast<GMap>()` only works on `JsAny` ancestors.
 * The handle is `internal`: cross-web consumer code calls into the library via [MapEffect]
 * and the public composable surface, not by reaching into the underlying JS object directly.
 */
actual class NativeMap internal constructor(internal val handle: GMap)
