package eu.buney.maps

internal actual fun triggerMapResize(map: NativeMap) {
    val handle = map.handle
    js("google.maps.event.trigger(handle, 'resize')")
}
