package eu.buney.maps.jsinterop

// Google Maps JavaScript API surface for Kotlin/JS (legacy/IR).
//
// Mirrors GoogleMapsJs.wasmJs.kt one-for-one at the call site. Differences from the wasmJs
// variant are language-level only:
//   - No JsAny base interface; Kotlin/JS external interfaces are plain.
//   - js("...") can appear in any expression context and returns `dynamic`. We cast.
//   - JsArray is replaced by regular Kotlin Array where it appears.

external interface GMap {
    fun setCenter(latLng: LatLngLiteral)
    fun setZoom(zoom: Double)
    fun setOptions(options: MapOptions)
    fun panTo(latLng: LatLngLiteral)
    fun fitBounds(bounds: LatLngBoundsLiteral, padding: Double = definedExternally)
    fun getCenter(): JsLatLng
    fun getZoom(): Double
    fun getBounds(): JsLatLngBounds?
    fun getProjection(): JsProjection?
    fun getHeading(): Double
    fun setHeading(heading: Double)
    fun getTilt(): Double
    fun setTilt(tilt: Double)
    fun addListener(eventName: String, handler: (dynamic) -> Unit): MapsEventListener
}

external interface MapOptions {
    var center: LatLngLiteral?
    var zoom: Double?
    var mapTypeId: String?
    var disableDefaultUI: Boolean?
    var zoomControl: Boolean?
    var gestureHandling: String?
    var rotateControl: Boolean?
    var streetViewControl: Boolean?
    var fullscreenControl: Boolean?
    var styles: dynamic
    var minZoom: Double?
    var maxZoom: Double?
    var colorScheme: String?
    var mapId: String?
    var heading: Double?
    var tilt: Double?
}

external interface LatLngLiteral {
    var lat: Double
    var lng: Double
}

external interface LatLngBoundsLiteral {
    var north: Double
    var south: Double
    var east: Double
    var west: Double
}

external interface JsLatLng {
    fun lat(): Double
    fun lng(): Double
}

external interface JsLatLngBounds {
    fun getNorthEast(): JsLatLng
    fun getSouthWest(): JsLatLng
}

external interface JsProjection {
    fun fromLatLngToPoint(latLng: JsLatLng): JsPoint
    fun fromPointToLatLng(point: JsPoint, noClampNoWrap: Boolean = definedExternally): JsLatLng
}

external interface JsPoint {
    var x: Double
    var y: Double
}

external interface MapsEventListener {
    fun remove()
}

external interface JsMarker {
    fun setMap(map: GMap?)
    fun setPosition(position: LatLngLiteral)
    fun setTitle(title: String?)
    fun setIcon(icon: dynamic)
    fun setOpacity(opacity: Double)
    fun setVisible(visible: Boolean)
    fun setDraggable(draggable: Boolean)
    fun setZIndex(zIndex: Double)
    fun getPosition(): JsLatLng?
    fun addListener(eventName: String, handler: (dynamic) -> Unit): MapsEventListener
}

external interface MarkerOptions {
    var position: LatLngLiteral?
    var map: GMap?
    var title: String?
    var icon: dynamic
    var opacity: Double?
    var visible: Boolean?
    var draggable: Boolean?
    var zIndex: Double?
    var anchorPoint: JsPoint?
}

external interface JsPolyline {
    fun setMap(map: GMap?)
    fun setOptions(options: PolylineOptions)
    fun addListener(eventName: String, handler: (dynamic) -> Unit): MapsEventListener
}

external interface PolylineOptions {
    var path: dynamic
    var map: GMap?
    var geodesic: Boolean?
    var strokeColor: String?
    var strokeOpacity: Double?
    var strokeWeight: Double?
    var clickable: Boolean?
    var visible: Boolean?
    var zIndex: Double?
}

external interface JsPolygon {
    fun setMap(map: GMap?)
    fun setOptions(options: PolygonOptions)
    fun addListener(eventName: String, handler: (dynamic) -> Unit): MapsEventListener
}

external interface PolygonOptions {
    var paths: dynamic
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

external interface JsCircle {
    fun setMap(map: GMap?)
    fun setOptions(options: CircleOptions)
    fun addListener(eventName: String, handler: (dynamic) -> Unit): MapsEventListener
}

external interface CircleOptions {
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

external interface JsGroundOverlay {
    fun setMap(map: GMap?)
    fun setOpacity(opacity: Double)
    fun addListener(eventName: String, handler: (dynamic) -> Unit): MapsEventListener
}

external interface JsImageMapType {
    fun releaseTile(tile: dynamic)
}

external interface JsInfoWindow {
    fun open(map: GMap?, anchor: dynamic = definedExternally)
    fun close()
    fun setContent(content: dynamic)
    fun addListener(eventName: String, handler: (dynamic) -> Unit): MapsEventListener
}

external interface InfoWindowOptions {
    var content: dynamic
    var position: LatLngLiteral?
    var maxWidth: Int?
}

external interface JsMapMouseEvent {
    val latLng: JsLatLng?
    val placeId: String?
    fun stop()
}

// -----------------------------------------------------------------------------------------------
// Factories. js("...") is unrestricted on Kotlin/JS — we just write the construct expressions
// directly and let the dynamic return type be coerced to the typed interface.
// -----------------------------------------------------------------------------------------------

@Suppress("UNUSED_PARAMETER")
internal fun newMapOptions(): MapOptions =
    js("({})").unsafeCast<MapOptions>()

internal fun newMarkerOptions(): MarkerOptions =
    js("({})").unsafeCast<MarkerOptions>()

internal fun newPolylineOptions(): PolylineOptions =
    js("({})").unsafeCast<PolylineOptions>()

internal fun newPolygonOptions(): PolygonOptions =
    js("({})").unsafeCast<PolygonOptions>()

internal fun newCircleOptions(): CircleOptions =
    js("({})").unsafeCast<CircleOptions>()

internal fun newInfoWindowOptions(): InfoWindowOptions =
    js("({})").unsafeCast<InfoWindowOptions>()

internal fun newLatLngLiteral(lat: Double, lng: Double): LatLngLiteral {
    val o = js("({})")
    o.lat = lat
    o.lng = lng
    return o.unsafeCast<LatLngLiteral>()
}

internal fun newLatLngBoundsLiteral(
    north: Double,
    south: Double,
    east: Double,
    west: Double,
): LatLngBoundsLiteral {
    val o = js("({})")
    o.north = north
    o.south = south
    o.east = east
    o.west = west
    return o.unsafeCast<LatLngBoundsLiteral>()
}

@Suppress("UNUSED_PARAMETER")
internal fun createGMap(host: dynamic, options: MapOptions): GMap =
    js("new google.maps.Map(host, options)").unsafeCast<GMap>()

internal fun createMarker(options: MarkerOptions): JsMarker =
    js("new google.maps.Marker(options)").unsafeCast<JsMarker>()

internal fun createPolyline(options: PolylineOptions): JsPolyline =
    js("new google.maps.Polyline(options)").unsafeCast<JsPolyline>()

internal fun createPolygon(options: PolygonOptions): JsPolygon =
    js("new google.maps.Polygon(options)").unsafeCast<JsPolygon>()

internal fun createCircle(options: CircleOptions): JsCircle =
    js("new google.maps.Circle(options)").unsafeCast<JsCircle>()

internal fun createInfoWindow(options: InfoWindowOptions): JsInfoWindow =
    js("new google.maps.InfoWindow(options)").unsafeCast<JsInfoWindow>()

@Suppress("UNUSED_PARAMETER")
internal fun createGroundOverlay(
    url: String,
    bounds: LatLngBoundsLiteral,
    opts: dynamic,
): JsGroundOverlay =
    js("new google.maps.GroundOverlay(url, bounds, opts)").unsafeCast<JsGroundOverlay>()
