@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun triggerMapResize(map: NativeMap) {
    triggerResize(map.handle)
}

private fun triggerResize(map: eu.buney.maps.jsinterop.GMap): Unit =
    js("google.maps.event.trigger(map, 'resize')")
