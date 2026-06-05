package eu.buney.maps.jsinterop

import androidx.compose.ui.graphics.Color
import eu.buney.maps.LatLng
import eu.buney.maps.NativeMap

// Shared per-target helpers for Polyline / Polygon / Circle. Each overlay has a small set of
// updatable properties; rather than declaring N×fields expect/actual setters we expose a
// "rebuild options bag and call setOptions" expect for each shape, since the JS API supports
// setOptions on all three.

internal expect class PolylineRef
internal expect class PolygonRef
internal expect class CircleRef

internal class PolylineCreate(
    val points: List<LatLng>,
    val color: Color,
    val width: Float,
    val geodesic: Boolean,
    val clickable: Boolean,
    val visible: Boolean,
    val zIndex: Float,
)

internal class PolygonCreate(
    val points: List<LatLng>,
    val holes: List<List<LatLng>>,
    val fillColor: Color,
    val strokeColor: Color,
    val strokeWidth: Float,
    val geodesic: Boolean,
    val clickable: Boolean,
    val visible: Boolean,
    val zIndex: Float,
)

internal class CircleCreate(
    val center: LatLng,
    val radius: Double,
    val fillColor: Color,
    val strokeColor: Color,
    val strokeWidth: Float,
    val clickable: Boolean,
    val visible: Boolean,
    val zIndex: Float,
)

internal expect fun NativeMap.createPolyline(c: PolylineCreate): PolylineRef
internal expect fun PolylineRef.update(c: PolylineCreate)
internal expect fun PolylineRef.removeFromMap()
internal expect fun PolylineRef.addClickListener(handler: () -> Unit): ListenerToken

internal expect fun NativeMap.createPolygon(c: PolygonCreate): PolygonRef
internal expect fun PolygonRef.update(c: PolygonCreate)
internal expect fun PolygonRef.removeFromMap()
internal expect fun PolygonRef.addClickListener(handler: () -> Unit): ListenerToken

internal expect fun NativeMap.createCircle(c: CircleCreate): CircleRef
internal expect fun CircleRef.update(c: CircleCreate)
internal expect fun CircleRef.removeFromMap()
internal expect fun CircleRef.addClickListener(handler: () -> Unit): ListenerToken
