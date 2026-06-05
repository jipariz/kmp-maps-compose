package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.geometry.Offset
import eu.buney.maps.jsinterop.GroundOverlayCreate
import eu.buney.maps.jsinterop.createGroundOverlay
import eu.buney.maps.jsinterop.setTransparency

actual class GroundOverlay internal constructor(
    actual val bounds: LatLngBounds,
    actual val bearing: Float,
    actual val transparency: Float,
)

@Composable
@GoogleMapComposable
actual fun GroundOverlay(
    position: GroundOverlayPosition,
    image: BitmapDescriptor,
    anchor: Offset,
    bearing: Float,
    clickable: Boolean,
    tag: Any?,
    transparency: Float,
    visible: Boolean,
    zIndex: Float,
    onClick: (GroundOverlay) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier ?: return
    val bounds = position.resolveBounds()
        ?: return // location-based positioning isn't supported on web yet (PR5)

    ComposeNode<GroundOverlayNode, MapApplier>(
        factory = {
            GroundOverlayNode(
                groundOverlay = mapApplier.map.createGroundOverlay(
                    GroundOverlayCreate(
                        url = image.url,
                        bounds = bounds,
                        transparency = transparency,
                        clickable = clickable,
                    )
                ),
                bounds = bounds,
                bearing = bearing,
                transparency = transparency,
                onGroundOverlayClick = onClick,
            )
        },
        update = {
            set(transparency) {
                this.transparency = it
                groundOverlay.setTransparency(it)
            }
            set(bearing) { this.bearing = it }
            set(bounds) { this.bounds = it }
            set(onClick) { this.onGroundOverlayClick = it }
            // PR5: bearing and image changes require recreating the GroundOverlay because
            // google.maps.GroundOverlay doesn't expose setBounds / setImage. JS API limitation.
        },
    )
}

/**
 * Returns the bounding rectangle this position covers, or null when location-based
 * positioning is used (width/height in meters) — the JS GroundOverlay constructor only
 * accepts bounds, so location-based positioning needs a follow-up that computes a
 * synthetic bounds from the image aspect ratio.
 */
private fun GroundOverlayPosition.resolveBounds(): LatLngBounds? = latLngBounds
