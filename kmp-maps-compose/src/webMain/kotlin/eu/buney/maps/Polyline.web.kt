package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import eu.buney.maps.jsinterop.PolylineCreate
import eu.buney.maps.jsinterop.PolylineRef
import eu.buney.maps.jsinterop.createPolyline
import eu.buney.maps.jsinterop.removeFromMap
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
    val mapApplier = currentComposer.applier as? MapApplier ?: return
    val segments = sliceSpans(points, spans)
    val creates = segments.map { (spanPoints, color) ->
        PolylineCreate(
            points = spanPoints,
            color = color,
            width = width,
            geodesic = geodesic,
            clickable = clickable,
            visible = visible,
            zIndex = zIndex,
        )
    }

    ComposeNode<PolylineSpansNode, MapApplier>(
        factory = {
            PolylineSpansNode(
                initialPolylines = creates.map { mapApplier.map.createPolyline(it) },
                points = points,
                onPolylineClick = onClick,
            )
        },
        update = {
            update(creates) { newCreates ->
                // Property changes require rebuilding the N polylines because the count
                // of spans may have changed. Detach old, attach new, then re-bind listeners.
                polylines.forEach { it.removeFromMap() }
                polylines.clear()
                polylines.addAll(newCreates.map { mapApplier.map.createPolyline(it) })
                reattachListeners()
            }
            set(points) { this.points = it }
            set(onClick) { this.onPolylineClick = it }
        },
    )
}

/**
 * Splits the polyline's point list into per-span point slices and resolves each span's
 * effective color. Fractional `segments` are rounded down; gradient spans use the midpoint
 * color (between fromColor and toColor). Stamp styles are ignored on web. If the spans
 * don't cover the full polyline, the last span's color extends to the remaining points.
 */
private fun sliceSpans(
    points: List<LatLng>,
    spans: List<StyleSpan>,
): List<Pair<List<LatLng>, Color>> {
    if (points.size < 2 || spans.isEmpty()) {
        return listOf(points to (spans.firstOrNull()?.color() ?: Color.Black))
    }
    val totalSegments = points.size - 1
    val result = mutableListOf<Pair<List<LatLng>, Color>>()
    var pointIdx = 0
    for ((i, span) in spans.withIndex()) {
        if (pointIdx >= totalSegments) break
        val isLast = (i == spans.lastIndex)
        val segmentsForSpan = if (isLast) {
            totalSegments - pointIdx
        } else {
            span.segments.toInt().coerceAtLeast(1).coerceAtMost(totalSegments - pointIdx)
        }
        val slice = points.subList(pointIdx, pointIdx + segmentsForSpan + 1)
        result += slice.toList() to span.color()
        // Subsequent spans start at the last point of the previous span so the polylines
        // visually join without gaps.
        pointIdx += segmentsForSpan
    }
    return result
}

private fun StyleSpan.color(): Color = when (val s = style) {
    is StrokeStyle.SolidColor -> s.color
    is StrokeStyle.Gradient -> Color(
        red = (s.fromColor.red + s.toColor.red) / 2f,
        green = (s.fromColor.green + s.toColor.green) / 2f,
        blue = (s.fromColor.blue + s.toColor.blue) / 2f,
        alpha = (s.fromColor.alpha + s.toColor.alpha) / 2f,
    )
}
