package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import eu.buney.maps.jsinterop.PolygonCreate
import eu.buney.maps.jsinterop.createPolygon
import eu.buney.maps.jsinterop.update

actual class Polygon internal constructor(
    actual val points: List<LatLng>,
    actual val holes: List<List<LatLng>>,
)

@Composable
@GoogleMapComposable
actual fun Polygon(
    points: List<LatLng>,
    clickable: Boolean,
    fillColor: Color,
    geodesic: Boolean,
    holes: List<List<LatLng>>,
    strokeColor: Color,
    strokeJointType: JointType,
    strokePattern: List<PatternItem>?,
    strokeWidth: Float,
    tag: Any?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Polygon) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier ?: return
    val create = PolygonCreate(
        points = points,
        holes = holes,
        fillColor = fillColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        geodesic = geodesic,
        clickable = clickable,
        visible = visible,
        zIndex = zIndex,
    )
    ComposeNode<PolygonNode, MapApplier>(
        factory = {
            PolygonNode(
                polygon = mapApplier.map.createPolygon(create),
                points = points,
                holes = holes,
                onPolygonClick = onClick,
            )
        },
        update = {
            update(create) { polygon.update(it) }
            set(points) { this.points = it }
            set(holes) { this.holes = it }
            set(onClick) { this.onPolygonClick = it }
        },
    )
}
