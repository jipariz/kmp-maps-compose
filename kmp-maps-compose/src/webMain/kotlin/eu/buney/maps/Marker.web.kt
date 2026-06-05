package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.geometry.Offset
import eu.buney.maps.jsinterop.MarkerCreateOptions
import eu.buney.maps.jsinterop.createMarker
import eu.buney.maps.jsinterop.setDraggable
import eu.buney.maps.jsinterop.setIcon
import eu.buney.maps.jsinterop.setOpacity
import eu.buney.maps.jsinterop.setPosition
import eu.buney.maps.jsinterop.setTitle
import eu.buney.maps.jsinterop.setVisible
import eu.buney.maps.jsinterop.setZIndex

actual class Marker internal constructor(
    actual val position: LatLng,
    actual val title: String?,
    actual val snippet: String?,
)

@Composable
@GoogleMapComposable
actual fun Marker(
    state: MarkerState,
    contentDescription: String?,
    alpha: Float,
    anchor: Offset,
    draggable: Boolean,
    flat: Boolean,
    icon: BitmapDescriptor?,
    infoWindowAnchor: Offset,
    rotation: Float,
    snippet: String?,
    tag: Any?,
    title: String?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Marker) -> Boolean,
    onInfoWindowClick: (Marker) -> Unit,
    onInfoWindowClose: (Marker) -> Unit,
    onInfoWindowLongClick: (Marker) -> Unit,
) {
    val mapApplier = currentComposer.applier as? MapApplier ?: return

    ComposeNode<MarkerNode, MapApplier>(
        factory = {
            val ref = mapApplier.map.createMarker(
                MarkerCreateOptions(
                    position = state.position,
                    title = title,
                    icon = icon,
                    opacity = alpha,
                    visible = visible,
                    draggable = draggable,
                    zIndex = zIndex,
                )
            )
            MarkerNode(
                marker = ref,
                markerState = state,
                snippet = snippet,
                onMarkerClick = onClick,
                onInfoWindowClick = onInfoWindowClick,
                onInfoWindowClose = onInfoWindowClose,
                onInfoWindowLongClick = onInfoWindowLongClick,
            )
        },
        update = {
            update(state.position) { p -> marker.setPosition(p.latitude, p.longitude) }
            update(title) { marker.setTitle(it) }
            update(icon) { marker.setIcon(it) }
            update(alpha) { marker.setOpacity(it) }
            update(visible) { marker.setVisible(it) }
            update(draggable) { marker.setDraggable(it) }
            update(zIndex) { marker.setZIndex(it) }
            set(snippet) { this.snippet = it }
            set(onClick) { this.onMarkerClick = it }
            set(onInfoWindowClick) { this.onInfoWindowClick = it }
            set(onInfoWindowClose) { this.onInfoWindowClose = it }
            set(onInfoWindowLongClick) { this.onInfoWindowLongClick = it }
        },
    )
}

@Composable
@GoogleMapComposable
actual fun MarkerInfoWindow(
    state: MarkerState,
    contentDescription: String?,
    alpha: Float,
    anchor: Offset,
    draggable: Boolean,
    flat: Boolean,
    icon: BitmapDescriptor?,
    infoWindowAnchor: Offset,
    rotation: Float,
    snippet: String?,
    tag: Any?,
    title: String?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Marker) -> Boolean,
    onInfoWindowClick: (Marker) -> Unit,
    onInfoWindowClose: (Marker) -> Unit,
    onInfoWindowLongClick: (Marker) -> Unit,
    content: (@Composable (Marker) -> Unit)?,
) {
    // PR4: fall back to a plain Marker — custom info window rendering needs the
    // Compose-to-bitmap pipeline (RememberComposeBitmapDescriptor) which lands later in
    // this PR. The Marker still works with the default title/snippet info window.
    Marker(
        state = state, contentDescription = contentDescription, alpha = alpha, anchor = anchor,
        draggable = draggable, flat = flat, icon = icon, infoWindowAnchor = infoWindowAnchor,
        rotation = rotation, snippet = snippet, tag = tag, title = title, visible = visible,
        zIndex = zIndex, onClick = onClick, onInfoWindowClick = onInfoWindowClick,
        onInfoWindowClose = onInfoWindowClose, onInfoWindowLongClick = onInfoWindowLongClick,
    )
}

@Composable
@GoogleMapComposable
actual fun MarkerInfoWindowContent(
    state: MarkerState,
    contentDescription: String?,
    alpha: Float,
    anchor: Offset,
    draggable: Boolean,
    flat: Boolean,
    icon: BitmapDescriptor?,
    infoWindowAnchor: Offset,
    rotation: Float,
    snippet: String?,
    tag: Any?,
    title: String?,
    visible: Boolean,
    zIndex: Float,
    onClick: (Marker) -> Boolean,
    onInfoWindowClick: (Marker) -> Unit,
    onInfoWindowClose: (Marker) -> Unit,
    onInfoWindowLongClick: (Marker) -> Unit,
    content: (@Composable (Marker) -> Unit)?,
) {
    Marker(
        state = state, contentDescription = contentDescription, alpha = alpha, anchor = anchor,
        draggable = draggable, flat = flat, icon = icon, infoWindowAnchor = infoWindowAnchor,
        rotation = rotation, snippet = snippet, tag = tag, title = title, visible = visible,
        zIndex = zIndex, onClick = onClick, onInfoWindowClick = onInfoWindowClick,
        onInfoWindowClose = onInfoWindowClose, onInfoWindowLongClick = onInfoWindowLongClick,
    )
}
