@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps.jsinterop

import eu.buney.maps.CameraPosition
import eu.buney.maps.LatLng
import eu.buney.maps.LatLngBounds
import eu.buney.maps.MapProperties
import eu.buney.maps.MapType
import eu.buney.maps.MapUiSettings
import eu.buney.maps.NativeMap
import eu.buney.maps.ScreenPoint
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.math.pow
import org.w3c.dom.HTMLElement

private fun NativeMap.gmap(): GMap = handle

private fun MapsEventListener.toToken(): ListenerToken = ListenerToken { remove() }

internal actual fun createNativeMap(
    host: HTMLElement,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    initialCamera: CameraPosition,
): NativeMap {
    val options = newMapOptions().apply {
        center = newLatLngLiteral(initialCamera.target.latitude, initialCamera.target.longitude)
        zoom = initialCamera.zoom.toDouble()
        mapTypeId = properties.mapType.toJsMapTypeId()
        disableDefaultUI = false
        zoomControl = uiSettings.zoomControlsEnabled
        rotateControl = uiSettings.rotationGesturesEnabled
        streetViewControl = false
        fullscreenControl = false
        minZoom = properties.minZoomPreference.toDouble()
        maxZoom = properties.maxZoomPreference.toDouble()
        styles = when {
            properties.mapType == MapType.NONE -> parseStylesJson(BLANK_STYLE_JSON)
            properties.mapStyleOptions != null -> parseStylesJson(properties.mapStyleOptions!!.json)
            else -> null
        }
    }
    val map = createGMap(host.unsafeCast<JsAny>(), options)
    return NativeMap(map)
}

internal actual fun NativeMap.jsSetCenter(lat: Double, lng: Double) {
    gmap().setCenter(newLatLngLiteral(lat, lng))
}

internal actual fun NativeMap.jsSetZoom(zoom: Double) {
    gmap().setZoom(zoom)
}

internal actual fun NativeMap.jsPanTo(lat: Double, lng: Double) {
    gmap().panTo(newLatLngLiteral(lat, lng))
}

internal actual fun NativeMap.jsFitBounds(bounds: LatLngBounds, padding: Int) {
    gmap().fitBounds(
        newLatLngBoundsLiteral(
            north = bounds.northeast.latitude,
            south = bounds.southwest.latitude,
            east = bounds.northeast.longitude,
            west = bounds.southwest.longitude,
        ),
        padding = padding.toDouble(),
    )
}

private fun mapDivClientWidth(map: GMap): Double = js("map.getDiv().clientWidth")
private fun mapDivClientHeight(map: GMap): Double = js("map.getDiv().clientHeight")

internal actual fun NativeMap.jsCurrentProjection(): ProjectionSnapshot? {
    val map = gmap()
    val projection = map.getProjection() ?: return null
    val bounds = map.getBounds() ?: return null
    val scale = 2.0.pow(map.getZoom())
    val width = mapDivClientWidth(map)
    val height = mapDivClientHeight(map)
    val centerLatLng = map.getCenter()
    val centerWorld = projection.fromLatLngToPoint(centerLatLng)

    val ne = bounds.getNorthEast()
    val sw = bounds.getSouthWest()
    val visibleBounds = LatLngBounds(
        southwest = LatLng(sw.lat(), sw.lng()),
        northeast = LatLng(ne.lat(), ne.lng()),
    )

    return ProjectionSnapshot(
        toScreen = { latLng ->
            val asLatLng = newLatLngLiteralAsJsLatLng(latLng.latitude, latLng.longitude)
            val world = projection.fromLatLngToPoint(asLatLng)
            ScreenPoint(
                x = ((world.x - centerWorld.x) * scale + width / 2.0).toFloat(),
                y = ((world.y - centerWorld.y) * scale + height / 2.0).toFloat(),
            )
        },
        fromScreen = { sp ->
            val worldX = (sp.x.toDouble() - width / 2.0) / scale + centerWorld.x
            val worldY = (sp.y.toDouble() - height / 2.0) / scale + centerWorld.y
            val point = newJsPoint(worldX, worldY)
            val ll = projection.fromPointToLatLng(point)
            LatLng(ll.lat(), ll.lng())
        },
        visibleBounds = visibleBounds,
    )
}

// google.maps.Projection.fromLatLngToPoint accepts a JsLatLng (instance) — construct one from
// a LatLngLiteral via google.maps.LatLng.
private fun newLatLngLiteralAsJsLatLng(lat: Double, lng: Double): JsLatLng =
    js("new google.maps.LatLng(lat, lng)")

private fun newJsPoint(x: Double, y: Double): JsPoint =
    js("new google.maps.Point(x, y)")

internal actual fun NativeMap.jsGetCameraPosition(): CameraPosition {
    val g = gmap()
    val center = g.getCenter()
    return CameraPosition(
        target = LatLng(center.lat(), center.lng()),
        zoom = g.getZoom().toFloat(),
        // google.maps.Map JS API exposes neither bearing nor tilt for the classic 2D map.
        // VectorMap supports both; we'll wire those up in a follow-up if we adopt vector maps.
        bearing = 0f,
        tilt = 0f,
    )
}

internal actual fun NativeMap.jsApplyProperties(properties: MapProperties) {
    val options = newMapOptions().apply {
        mapTypeId = properties.mapType.toJsMapTypeId()
        minZoom = properties.minZoomPreference.toDouble()
        maxZoom = properties.maxZoomPreference.toDouble()
        // Style precedence (PR5): MapType.NONE blanks the base layer via a hide-everything
        // style; otherwise the consumer-supplied mapStyleOptions JSON applies.
        styles = when {
            properties.mapType == MapType.NONE -> parseStylesJson(BLANK_STYLE_JSON)
            properties.mapStyleOptions != null -> parseStylesJson(properties.mapStyleOptions!!.json)
            else -> null
        }
    }
    gmap().setOptions(options)
    // PR6: traffic / building / indoor layers, my-location.
}

private fun parseStylesJson(json: String): JsAny =
    js("JSON.parse(json)")

private const val BLANK_STYLE_JSON =
    """[{"featureType":"all","elementType":"all","stylers":[{"visibility":"off"}]}]"""

internal actual fun NativeMap.jsApplyUiSettings(uiSettings: MapUiSettings) {
    val options = newMapOptions().apply {
        zoomControl = uiSettings.zoomControlsEnabled
        rotateControl = uiSettings.rotationGesturesEnabled
        // gestureHandling balances scrollGesturesEnabled / zoomGesturesEnabled — JS bundles
        // them into one setting. We pick "auto" when all gestures are enabled, "none" when
        // they're all disabled, "cooperative" otherwise (matches mobile platform defaults).
        gestureHandling = when {
            !uiSettings.scrollGesturesEnabled && !uiSettings.zoomGesturesEnabled -> "none"
            uiSettings.scrollGesturesEnabled && uiSettings.zoomGesturesEnabled -> "auto"
            else -> "cooperative"
        }
    }
    gmap().setOptions(options)
}

internal actual fun NativeMap.jsAddClickListener(handler: (LatLng) -> Unit): ListenerToken {
    val listener = gmap().addListener("click") { ev ->
        // Map-level click; if placeId is non-null, it's a POI — let the POI handler take it.
        val mouseEvent = ev?.unsafeCast<JsMapMouseEvent>() ?: return@addListener
        if (mouseEvent.placeId != null) return@addListener
        val ll = mouseEvent.latLng ?: return@addListener
        handler(LatLng(ll.lat(), ll.lng()))
    }
    return listener.toToken()
}

internal actual fun NativeMap.jsAddPoiClickListener(
    handler: (placeId: String, name: String?, latLng: LatLng) -> Unit
): ListenerToken {
    // Google Maps JS POI clicks come through the regular 'click' event, distinguished by
    // a non-null placeId on the event payload. We register a second listener that filters
    // for placeId-bearing events and calls e.stop() to prevent the default POI info window.
    val listener = gmap().addListener("click") { ev ->
        val mouseEvent = ev?.unsafeCast<JsMapMouseEvent>() ?: return@addListener
        val placeId = mouseEvent.placeId ?: return@addListener
        val ll = mouseEvent.latLng ?: return@addListener
        mouseEvent.stop()
        handler(placeId, null, LatLng(ll.lat(), ll.lng()))
    }
    return listener.toToken()
}

internal actual fun NativeMap.jsAddRightClickListener(handler: (LatLng) -> Unit): ListenerToken {
    // 'rightclick' on classic Map is the closest analog to a long-press; PR4 may also
    // wire a touchstart-based long-press detector for touch devices.
    val listener = gmap().addListener("rightclick") { ev ->
        val mouseEvent = ev?.unsafeCast<JsMapMouseEvent>() ?: return@addListener
        val ll = mouseEvent.latLng ?: return@addListener
        handler(LatLng(ll.lat(), ll.lng()))
    }
    return listener.toToken()
}

internal actual fun NativeMap.jsAddIdleListener(handler: () -> Unit): ListenerToken {
    val listener = gmap().addListener("idle") { _ -> handler() }
    return listener.toToken()
}

internal actual fun NativeMap.jsAddBoundsChangedListener(handler: () -> Unit): ListenerToken {
    val listener = gmap().addListener("bounds_changed") { _ -> handler() }
    return listener.toToken()
}

internal actual fun NativeMap.jsAddDragStartListener(handler: () -> Unit): ListenerToken {
    val listener = gmap().addListener("dragstart") { _ -> handler() }
    return listener.toToken()
}

internal actual fun NativeMap.jsAddTilesLoadedListenerOnce(handler: () -> Unit): ListenerToken {
    // 'tilesloaded' fires after the first frame of tiles paints — the closest signal to
    // android-maps-compose's onMapLoaded callback.
    var listener: MapsEventListener? = null
    listener = gmap().addListener("tilesloaded") { _ ->
        listener?.remove()
        handler()
    }
    return listener.toToken()
}

private fun MapType.toJsMapTypeId(): String = when (this) {
    MapType.NONE -> "roadmap" // JS API has no NONE; PR4 will style-out the base layer.
    MapType.NORMAL -> "roadmap"
    MapType.SATELLITE -> "satellite"
    MapType.HYBRID -> "hybrid"
    MapType.TERRAIN -> "terrain"
}
