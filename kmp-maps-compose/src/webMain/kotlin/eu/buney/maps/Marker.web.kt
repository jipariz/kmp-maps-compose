package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.geometry.Offset
import eu.buney.maps.jsinterop.MarkerCreateOptions
import eu.buney.maps.jsinterop.createMarker
import eu.buney.maps.jsinterop.setDraggable
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
                title = title,
                snippet = snippet,
                icon = icon,
                customInfoWindowImageUrl = null,
                onMarkerClick = onClick,
                onInfoWindowClick = onInfoWindowClick,
                onInfoWindowClose = onInfoWindowClose,
                onInfoWindowLongClick = onInfoWindowLongClick,
                map = mapApplier.map,
            )
        },
        update = {
            update(state.position) { p -> marker.setPosition(p.latitude, p.longitude) }
            update(title) {
                marker.setTitle(it)
                this.title = it
                refreshInfoWindowContent()
            }
            update(icon) { setIconAndRefcount(it) }
            update(alpha) { marker.setOpacity(it) }
            update(visible) { marker.setVisible(it) }
            update(draggable) { marker.setDraggable(it) }
            update(zIndex) { marker.setZIndex(it) }
            set(snippet) {
                this.snippet = it
                refreshInfoWindowContent()
            }
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
    MarkerWithCustomInfoWindow(
        state = state, alpha = alpha, anchor = anchor, draggable = draggable, flat = flat,
        icon = icon, infoWindowAnchor = infoWindowAnchor, rotation = rotation, snippet = snippet,
        tag = tag, title = title, visible = visible, zIndex = zIndex, onClick = onClick,
        onInfoWindowClick = onInfoWindowClick, onInfoWindowClose = onInfoWindowClose,
        onInfoWindowLongClick = onInfoWindowLongClick, content = content,
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
    // Web doesn't distinguish "custom content inside default frame" from "fully custom" —
    // both render the user's composable as an <img>. The InfoWindow frame styling is the
    // same in both cases.
    MarkerWithCustomInfoWindow(
        state = state, alpha = alpha, anchor = anchor, draggable = draggable, flat = flat,
        icon = icon, infoWindowAnchor = infoWindowAnchor, rotation = rotation, snippet = snippet,
        tag = tag, title = title, visible = visible, zIndex = zIndex, onClick = onClick,
        onInfoWindowClick = onInfoWindowClick, onInfoWindowClose = onInfoWindowClose,
        onInfoWindowLongClick = onInfoWindowLongClick, content = content,
    )
}

@Composable
@GoogleMapComposable
private fun MarkerWithCustomInfoWindow(
    state: MarkerState,
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
    // Pre-render the Compose info-window content to a BitmapDescriptor; pass its URL to
    // the MarkerNode so MarkerNode.ensureInfoWindow uses the image-based InfoWindow path.
    val infoBitmap: BitmapDescriptor? = if (content != null) {
        // Use the marker proxy as the lambda receiver. The bitmap is captured once per
        // composition; the marker proxy only carries position/title/snippet which the
        // rendered content can reference for layout.
        val markerProxy = Marker(state.position, title, snippet)
        rememberComposeBitmapDescriptor(state.position, title ?: "", snippet ?: "") { content(markerProxy) }
    } else null

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
                title = title,
                snippet = snippet,
                icon = icon,
                customInfoWindowImageUrl = infoBitmap?.url,
                onMarkerClick = onClick,
                onInfoWindowClick = onInfoWindowClick,
                onInfoWindowClose = onInfoWindowClose,
                onInfoWindowLongClick = onInfoWindowLongClick,
                map = mapApplier.map,
            )
        },
        update = {
            update(state.position) { p -> marker.setPosition(p.latitude, p.longitude) }
            update(title) {
                marker.setTitle(it)
                this.title = it
                refreshInfoWindowContent()
            }
            update(icon) { setIconAndRefcount(it) }
            update(alpha) { marker.setOpacity(it) }
            update(visible) { marker.setVisible(it) }
            update(draggable) { marker.setDraggable(it) }
            update(zIndex) { marker.setZIndex(it) }
            set(snippet) {
                this.snippet = it
                refreshInfoWindowContent()
            }
            set(infoBitmap?.url) {
                this.customInfoWindowImageUrl = it
                refreshInfoWindowContent()
            }
            set(onClick) { this.onMarkerClick = it }
            set(onInfoWindowClick) { this.onInfoWindowClick = it }
            set(onInfoWindowClose) { this.onInfoWindowClose = it }
            set(onInfoWindowLongClick) { this.onInfoWindowLongClick = it }
        },
    )
}
