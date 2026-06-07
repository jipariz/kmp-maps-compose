@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps.jsinterop

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

// Google Maps JavaScript API surface for Kotlin/Wasm.
//
// Google Maps is loaded at runtime via a global script tag (see MapsApiLoader). All access to
// google.maps.* constructors is therefore funneled through `js("...")` factory functions below
// rather than direct `external class` declarations — Kotlin/Wasm has no namespace-aware @JsName
// for nested globals, and `external interface` works without any JS-side binding because
// interfaces are compile-time descriptions only.

external interface GMap : JsAny {
    fun setCenter(latLng: LatLngLiteral)
    fun setZoom(zoom: Double)
    fun setOptions(options: MapOptions)
    fun panTo(latLng: LatLngLiteral)
    fun fitBounds(bounds: LatLngBoundsLiteral, padding: Double = definedExternally)
    fun getCenter(): JsLatLng
    fun getZoom(): Double
    fun getBounds(): JsLatLngBounds?
    fun getProjection(): JsProjection?
    // Vector-map only — returns 0 on classic raster maps. setters silently no-op there too.
    fun getHeading(): Double
    fun setHeading(heading: Double)
    fun getTilt(): Double
    fun setTilt(tilt: Double)
    fun addListener(eventName: String, handler: (JsAny?) -> Unit): MapsEventListener
}

external interface MapOptions : JsAny {
    var center: LatLngLiteral?
    var zoom: Double?
    var mapTypeId: String?
    var disableDefaultUI: Boolean?
    var zoomControl: Boolean?
    var gestureHandling: String?
    var rotateControl: Boolean?
    var streetViewControl: Boolean?
    var fullscreenControl: Boolean?
    var styles: JsAny?
    var minZoom: Double?
    var maxZoom: Double?
    var colorScheme: String?
    var mapId: String?
    var heading: Double?
    var tilt: Double?
}

external interface LatLngLiteral : JsAny {
    var lat: Double
    var lng: Double
}

external interface LatLngBoundsLiteral : JsAny {
    var north: Double
    var south: Double
    var east: Double
    var west: Double
}

external interface JsLatLng : JsAny {
    fun lat(): Double
    fun lng(): Double
}

external interface JsLatLngBounds : JsAny {
    fun getNorthEast(): JsLatLng
    fun getSouthWest(): JsLatLng
}

external interface JsProjection : JsAny {
    fun fromLatLngToPoint(latLng: JsLatLng): JsPoint
    fun fromPointToLatLng(point: JsPoint, noClampNoWrap: Boolean = definedExternally): JsLatLng
}

external interface JsPoint : JsAny {
    var x: Double
    var y: Double
}

external interface MapsEventListener : JsAny {
    fun remove()
}

external interface JsMarker : JsAny {
    fun setMap(map: GMap?)
    fun setPosition(position: LatLngLiteral)
    fun setTitle(title: String?)
    fun setIcon(icon: JsAny?)
    fun setOpacity(opacity: Double)
    fun setVisible(visible: Boolean)
    fun setDraggable(draggable: Boolean)
    fun setZIndex(zIndex: Double)
    fun getPosition(): JsLatLng?
    fun addListener(eventName: String, handler: (JsAny?) -> Unit): MapsEventListener
}

external interface MarkerOptions : JsAny {
    var position: LatLngLiteral?
    var map: GMap?
    var title: String?
    var icon: JsAny?
    var opacity: Double?
    var visible: Boolean?
    var draggable: Boolean?
    var zIndex: Double?
    var anchorPoint: JsPoint?
}

external interface JsPolyline : JsAny {
    fun setMap(map: GMap?)
    fun setOptions(options: PolylineOptions)
    fun addListener(eventName: String, handler: (JsAny?) -> Unit): MapsEventListener
}

external interface PolylineOptions : JsAny {
    var path: JsAny?
    var map: GMap?
    var geodesic: Boolean?
    var strokeColor: String?
    var strokeOpacity: Double?
    var strokeWeight: Double?
    var clickable: Boolean?
    var visible: Boolean?
    var zIndex: Double?
}

external interface JsPolygon : JsAny {
    fun setMap(map: GMap?)
    fun setOptions(options: PolygonOptions)
    fun addListener(eventName: String, handler: (JsAny?) -> Unit): MapsEventListener
}

external interface PolygonOptions : JsAny {
    var paths: JsAny?
    var map: GMap?
    var geodesic: Boolean?
    var fillColor: String?
    var fillOpacity: Double?
    var strokeColor: String?
    var strokeOpacity: Double?
    var strokeWeight: Double?
    var clickable: Boolean?
    var visible: Boolean?
    var zIndex: Double?
}

external interface JsCircle : JsAny {
    fun setMap(map: GMap?)
    fun setOptions(options: CircleOptions)
    fun addListener(eventName: String, handler: (JsAny?) -> Unit): MapsEventListener
}

external interface CircleOptions : JsAny {
    var center: LatLngLiteral?
    var radius: Double?
    var map: GMap?
    var fillColor: String?
    var fillOpacity: Double?
    var strokeColor: String?
    var strokeOpacity: Double?
    var strokeWeight: Double?
    var clickable: Boolean?
    var visible: Boolean?
    var zIndex: Double?
}

external interface JsGroundOverlay : JsAny {
    fun setMap(map: GMap?)
    fun setOpacity(opacity: Double)
    fun addListener(eventName: String, handler: (JsAny?) -> Unit): MapsEventListener
}

external interface JsImageMapType : JsAny {
    fun releaseTile(tile: JsAny?)
}

external interface JsInfoWindow : JsAny {
    fun open(map: GMap?, anchor: JsAny? = definedExternally)
    fun close()
    fun setContent(content: JsAny?)
    fun addListener(eventName: String, handler: (JsAny?) -> Unit): MapsEventListener
}

external interface InfoWindowOptions : JsAny {
    var content: JsAny?
    var position: LatLngLiteral?
    var maxWidth: Int?
}

// MapMouseEvent carries latLng (an instance of google.maps.LatLng) and, on map-level click events,
// optionally a placeId field that identifies POI clicks.
external interface JsMapMouseEvent : JsAny {
    val latLng: JsLatLng?
    val placeId: String?
    fun stop()
}

// -----------------------------------------------------------------------------------------------
// Factories. Each `js("...")` call must be a single expression in a package-level function body —
// this is the Kotlin/Wasm constraint. All construction of google.maps.* objects is funneled here.
// -----------------------------------------------------------------------------------------------

internal fun newMapOptions(): MapOptions =
    js("({})")

internal fun newMarkerOptions(): MarkerOptions =
    js("({})")

internal fun newPolylineOptions(): PolylineOptions =
    js("({})")

internal fun newPolygonOptions(): PolygonOptions =
    js("({})")

internal fun newCircleOptions(): CircleOptions =
    js("({})")

internal fun newInfoWindowOptions(): InfoWindowOptions =
    js("({})")

internal fun newLatLngLiteral(lat: Double, lng: Double): LatLngLiteral =
    js("({ lat: lat, lng: lng })")

internal fun newLatLngBoundsLiteral(
    north: Double,
    south: Double,
    east: Double,
    west: Double,
): LatLngBoundsLiteral =
    js("({ north: north, south: south, east: east, west: west })")

internal fun createGMap(host: JsAny, options: MapOptions): GMap =
    js("new google.maps.Map(host, options)")

internal fun createMarker(options: MarkerOptions): JsMarker =
    js("new google.maps.Marker(options)")

internal fun createPolyline(options: PolylineOptions): JsPolyline =
    js("new google.maps.Polyline(options)")

internal fun createPolygon(options: PolygonOptions): JsPolygon =
    js("new google.maps.Polygon(options)")

internal fun createCircle(options: CircleOptions): JsCircle =
    js("new google.maps.Circle(options)")

internal fun createInfoWindow(options: InfoWindowOptions): JsInfoWindow =
    js("new google.maps.InfoWindow(options)")

internal fun createGroundOverlay(
    url: String,
    bounds: LatLngBoundsLiteral,
    opts: JsAny,
): JsGroundOverlay =
    js("new google.maps.GroundOverlay(url, bounds, opts)")
