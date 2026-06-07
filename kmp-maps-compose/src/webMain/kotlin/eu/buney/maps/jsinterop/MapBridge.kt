package eu.buney.maps.jsinterop

import eu.buney.maps.CameraPosition
import eu.buney.maps.LatLng
import eu.buney.maps.LatLngBounds
import eu.buney.maps.MapProperties
import eu.buney.maps.MapUiSettings
import eu.buney.maps.NativeMap
import eu.buney.maps.ScreenPoint
import org.w3c.dom.HTMLElement

/**
 * Token returned from listener-attaching helpers; calling [remove] detaches the listener.
 *
 * Holds the removal step as a captured lambda so webMain doesn't have to reference the
 * target-specific `google.maps.MapsEventListener` type. The per-target listener helpers
 * capture the underlying listener in the lambda and call `.remove()` on it.
 */
internal class ListenerToken(private val remover: () -> Unit) {
    fun remove() = remover()
}

/**
 * Creates a `google.maps.Map` against [host] with options derived from [properties] +
 * [initialCamera]. The caller is responsible for ensuring [MapsApiLoader.await] has resolved.
 */
internal expect fun createNativeMap(
    host: HTMLElement,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    initialCamera: CameraPosition,
): NativeMap

// Camera ----------------------------------------------------------------------------------------

internal expect fun NativeMap.jsSetCenter(lat: Double, lng: Double)
internal expect fun NativeMap.jsSetZoom(zoom: Double)
internal expect fun NativeMap.jsPanTo(lat: Double, lng: Double)
internal expect fun NativeMap.jsFitBounds(bounds: LatLngBounds, padding: Int)

/**
 * Push heading + tilt to the map. Only effective on vector maps (configured via
 * [eu.buney.maps.MapsConfig.mapId]) — silently no-ops on the classic 2D raster map.
 */
internal expect fun NativeMap.jsSetHeadingAndTilt(bearing: Float, tilt: Float)
internal expect fun NativeMap.jsGetCameraPosition(): CameraPosition

/**
 * Snapshot of the map's current projection state, sufficient to convert between geographic
 * and screen coordinates and to read the visible bounds. Returns null when the map hasn't
 * been laid out yet (no projection available).
 */
internal expect fun NativeMap.jsCurrentProjection(): ProjectionSnapshot?

internal class ProjectionSnapshot(
    val toScreen: (LatLng) -> ScreenPoint,
    val fromScreen: (ScreenPoint) -> LatLng,
    val visibleBounds: LatLngBounds,
)

// Properties / UI -------------------------------------------------------------------------------

internal expect fun NativeMap.jsApplyProperties(properties: MapProperties)
internal expect fun NativeMap.jsApplyUiSettings(uiSettings: MapUiSettings)

// Events ----------------------------------------------------------------------------------------

internal expect fun NativeMap.jsAddClickListener(handler: (LatLng) -> Unit): ListenerToken
internal expect fun NativeMap.jsAddPoiClickListener(
    handler: (placeId: String, name: String?, latLng: LatLng) -> Unit
): ListenerToken
internal expect fun NativeMap.jsAddRightClickListener(handler: (LatLng) -> Unit): ListenerToken
internal expect fun NativeMap.jsAddIdleListener(handler: () -> Unit): ListenerToken
internal expect fun NativeMap.jsAddBoundsChangedListener(handler: () -> Unit): ListenerToken
internal expect fun NativeMap.jsAddDragStartListener(handler: () -> Unit): ListenerToken
internal expect fun NativeMap.jsAddTilesLoadedListenerOnce(handler: () -> Unit): ListenerToken
