package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import eu.buney.maps.jsinterop.CircleCreate
import eu.buney.maps.jsinterop.createCircle
import eu.buney.maps.jsinterop.update

actual class Circle internal constructor(
    actual val center: LatLng,
    actual val radius: Double,
)

@Composable
@GoogleMapComposable
actual fun Circle(
    center: LatLng,
    clickable: Boolean,
    fillColor: Color,
    radius: Double,
    strokeColor: Color,
    strokePattern: List<PatternItem>?,
    strokeWidth: Float,
    tag: Any?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Circle) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier ?: return
    val create = CircleCreate(
        center = center,
        radius = radius,
        fillColor = fillColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        clickable = clickable,
        visible = visible,
        zIndex = zIndex,
    )
    ComposeNode<CircleNode, MapApplier>(
        factory = {
            CircleNode(
                circle = mapApplier.map.createCircle(create),
                center = center,
                radius = radius,
                onCircleClick = onClick,
            )
        },
        update = {
            update(create) { circle.update(it) }
            set(center) { this.center = it }
            set(radius) { this.radius = it }
            set(onClick) { this.onCircleClick = it }
        },
    )
}
