@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps.jsinterop

import eu.buney.maps.LatLng
import eu.buney.maps.NativeMap
import eu.buney.maps.opacityDouble
import eu.buney.maps.toCssHex
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray

internal actual class PolylineRef internal constructor(internal val handle: JsPolyline)
internal actual class PolygonRef internal constructor(internal val handle: JsPolygon)
internal actual class CircleRef internal constructor(internal val handle: JsCircle)

private fun newJsArray(): JsAny = js("[]")
private fun pushLatLng(arr: JsAny, lat: Double, lng: Double): Unit =
    js("arr.push({ lat: lat, lng: lng })")

private fun pathToJsArray(points: List<LatLng>): JsAny {
    val arr = newJsArray()
    for (p in points) pushLatLng(arr, p.latitude, p.longitude)
    return arr
}

private fun nestedPathsToJsArray(outer: List<LatLng>, holes: List<List<LatLng>>): JsAny {
    val arr = newJsArray()
    pushPath(arr, pathToJsArray(outer))
    for (hole in holes) pushPath(arr, pathToJsArray(hole))
    return arr
}

private fun pushPath(outer: JsAny, path: JsAny): Unit =
    js("outer.push(path)")

private fun MapsEventListener.toToken(): ListenerToken = ListenerToken { remove() }

// ----- Polyline ---------------------------------------------------------------------------------

internal actual fun NativeMap.createPolyline(c: PolylineCreate): PolylineRef {
    val opts = newPolylineOptions().apply {
        path = pathToJsArray(c.points)
        map = handle
        geodesic = c.geodesic
        strokeColor = c.color.toCssHex()
        strokeOpacity = c.color.opacityDouble
        strokeWeight = c.width.toDouble()
        clickable = c.clickable
        visible = c.visible
        zIndex = c.zIndex.toDouble()
    }
    return PolylineRef(createPolyline(opts))
}

internal actual fun PolylineRef.update(c: PolylineCreate) {
    val opts = newPolylineOptions().apply {
        path = pathToJsArray(c.points)
        geodesic = c.geodesic
        strokeColor = c.color.toCssHex()
        strokeOpacity = c.color.opacityDouble
        strokeWeight = c.width.toDouble()
        clickable = c.clickable
        visible = c.visible
        zIndex = c.zIndex.toDouble()
    }
    handle.setOptions(opts)
}

internal actual fun PolylineRef.removeFromMap() {
    handle.setMap(null)
}

internal actual fun PolylineRef.addClickListener(handler: () -> Unit): ListenerToken =
    handle.addListener("click") { _ -> handler() }.toToken()

// ----- Polygon ----------------------------------------------------------------------------------

internal actual fun NativeMap.createPolygon(c: PolygonCreate): PolygonRef {
    val opts = newPolygonOptions().apply {
        paths = nestedPathsToJsArray(c.points, c.holes)
        map = handle
        geodesic = c.geodesic
        fillColor = c.fillColor.toCssHex()
        fillOpacity = c.fillColor.opacityDouble
        strokeColor = c.strokeColor.toCssHex()
        strokeOpacity = c.strokeColor.opacityDouble
        strokeWeight = c.strokeWidth.toDouble()
        clickable = c.clickable
        visible = c.visible
        zIndex = c.zIndex.toDouble()
    }
    return PolygonRef(createPolygon(opts))
}

internal actual fun PolygonRef.update(c: PolygonCreate) {
    val opts = newPolygonOptions().apply {
        paths = nestedPathsToJsArray(c.points, c.holes)
        geodesic = c.geodesic
        fillColor = c.fillColor.toCssHex()
        fillOpacity = c.fillColor.opacityDouble
        strokeColor = c.strokeColor.toCssHex()
        strokeOpacity = c.strokeColor.opacityDouble
        strokeWeight = c.strokeWidth.toDouble()
        clickable = c.clickable
        visible = c.visible
        zIndex = c.zIndex.toDouble()
    }
    handle.setOptions(opts)
}

internal actual fun PolygonRef.removeFromMap() {
    handle.setMap(null)
}

internal actual fun PolygonRef.addClickListener(handler: () -> Unit): ListenerToken =
    handle.addListener("click") { _ -> handler() }.toToken()

// ----- Circle -----------------------------------------------------------------------------------

internal actual fun NativeMap.createCircle(c: CircleCreate): CircleRef {
    val opts = newCircleOptions().apply {
        center = newLatLngLiteral(c.center.latitude, c.center.longitude)
        radius = c.radius
        map = handle
        fillColor = c.fillColor.toCssHex()
        fillOpacity = c.fillColor.opacityDouble
        strokeColor = c.strokeColor.toCssHex()
        strokeOpacity = c.strokeColor.opacityDouble
        strokeWeight = c.strokeWidth.toDouble()
        clickable = c.clickable
        visible = c.visible
        zIndex = c.zIndex.toDouble()
    }
    return CircleRef(createCircle(opts))
}

internal actual fun CircleRef.update(c: CircleCreate) {
    val opts = newCircleOptions().apply {
        center = newLatLngLiteral(c.center.latitude, c.center.longitude)
        radius = c.radius
        fillColor = c.fillColor.toCssHex()
        fillOpacity = c.fillColor.opacityDouble
        strokeColor = c.strokeColor.toCssHex()
        strokeOpacity = c.strokeColor.opacityDouble
        strokeWeight = c.strokeWidth.toDouble()
        clickable = c.clickable
        visible = c.visible
        zIndex = c.zIndex.toDouble()
    }
    handle.setOptions(opts)
}

internal actual fun CircleRef.removeFromMap() {
    handle.setMap(null)
}

internal actual fun CircleRef.addClickListener(handler: () -> Unit): ListenerToken =
    handle.addListener("click") { _ -> handler() }.toToken()
