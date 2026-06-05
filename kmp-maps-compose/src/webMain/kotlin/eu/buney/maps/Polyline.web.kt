package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import eu.buney.maps.jsinterop.PolylineCreate
import eu.buney.maps.jsinterop.createPolyline
import eu.buney.maps.jsinterop.update

actual class Polyline internal constructor(actual val points: List<LatLng>)

@Composable
@GoogleMapComposable
actual fun Polyline(
    points: List<LatLng>,
    clickable: Boolean,
    color: Color,
    endCap: Cap,
    geodesic: Boolean,
    jointType: JointType,
    pattern: List<PatternItem>?,
    startCap: Cap,
    tag: Any?,
    visible: Boolean,
    width: Float,
    zIndex: Float,
    onClick: (Polyline) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier ?: return
    val create = PolylineCreate(points, color, width, geodesic, clickable, visible, zIndex)
    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            PolylineNode(
                polyline = mapApplier.map.createPolyline(create),
                points = points,
                onPolylineClick = onClick,
            )
        },
        update = {
            update(create) { polyline.update(it) }
            set(points) { this.points = it }
            set(onClick) { this.onPolylineClick = it }
        },
    )
}

@Composable
@GoogleMapComposable
actual fun Polyline(
    points: List<LatLng>,
    spans: List<StyleSpan>,
    clickable: Boolean,
    endCap: Cap,
    geodesic: Boolean,
    jointType: JointType,
    pattern: List<PatternItem>?,
    startCap: Cap,
    tag: Any?,
    visible: Boolean,
    width: Float,
    zIndex: Float,
    onClick: (Polyline) -> Unit,
) {
    // PR4: styled polyline spans aren't supported by the JS API. Fall back to the first span's
    // color (matches the "Partial" entry in the iOS parity table — same limitation).
    val color = spans.firstOrNull()?.color() ?: Color.Black
    Polyline(
        points = points,
        clickable = clickable,
        color = color,
        endCap = endCap,
        geodesic = geodesic,
        jointType = jointType,
        pattern = pattern,
        startCap = startCap,
        tag = tag,
        visible = visible,
        width = width,
        zIndex = zIndex,
        onClick = onClick,
    )
}

private fun StyleSpan.color(): Color = (style as? StrokeStyle.SolidColor)?.color ?: Color.Black
