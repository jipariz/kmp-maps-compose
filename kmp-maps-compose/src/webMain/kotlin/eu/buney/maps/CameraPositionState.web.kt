package eu.buney.maps

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.ProjectionSnapshot
import eu.buney.maps.jsinterop.jsAddBoundsChangedListener
import eu.buney.maps.jsinterop.jsAddDragStartListener
import eu.buney.maps.jsinterop.jsAddIdleListener
import eu.buney.maps.jsinterop.jsCurrentProjection
import eu.buney.maps.jsinterop.jsFitBounds
import eu.buney.maps.jsinterop.jsGetCameraPosition
import eu.buney.maps.jsinterop.jsPanTo
import eu.buney.maps.jsinterop.jsSetCenter
import eu.buney.maps.jsinterop.jsSetHeadingAndTilt
import eu.buney.maps.jsinterop.jsSetZoom
import kotlinx.coroutines.CompletableDeferred

private class SnapshotProjection(private val s: ProjectionSnapshot) : Projection {
    override fun toScreenLocation(latLng: LatLng): ScreenPoint = s.toScreen(latLng)
    override fun fromScreenLocation(point: ScreenPoint): LatLng = s.fromScreen(point)
    override val visibleBounds: LatLngBounds get() = s.visibleBounds
    override fun contains(latLng: LatLng): Boolean {
        val ne = s.visibleBounds.northeast
        val sw = s.visibleBounds.southwest
        return latLng.latitude in sw.latitude..ne.latitude &&
            latLng.longitude in sw.longitude..ne.longitude
    }
}

@Stable
actual class CameraPositionState actual constructor(
    position: CameraPosition,
) {
    private var _position by mutableStateOf(position)
    private var _isMoving by mutableStateOf(false)
    private var _cameraMoveStartedReason by mutableStateOf(CameraMoveStartedReason.NO_MOVEMENT_YET)

    // Bound map and its event listener tokens. setMap(null) tears them down.
    private var boundMap: NativeMap? = null
    private val mapListenerTokens = mutableListOf<ListenerToken>()

    /** Accessor for [MapEffect] / [eu.buney.maps.MapEffect] — exposes the bound map handle. */
    internal val boundNativeMapForEffect: NativeMap? get() = boundMap

    // Tracks the in-flight animate() call so its completion can be wired to the next idle event.
    private var idleCompletionListener: ListenerToken? = null
    private var pendingAnimateCompletion: CompletableDeferred<Unit>? = null

    // Set to true while we're programmatically writing position to the map (setCenter, setZoom,
    // panTo, fitBounds) so that the resulting idle event doesn't reflect back as a "user moved
    // the camera" change to rawPosition (which would race with the value we just wrote).
    private var suppressMapToStateSync = false

    actual val isMoving: Boolean get() = _isMoving

    actual val cameraMoveStartedReason: CameraMoveStartedReason get() = _cameraMoveStartedReason

    actual var position: CameraPosition
        get() = _position
        set(value) {
            _position = value
            // When code sets position directly (not via animate/move), push it to the map
            // immediately if bound.
            val map = boundMap ?: return
            suppressMapToStateSync = true
            map.jsSetCenter(value.target.latitude, value.target.longitude)
            map.jsSetZoom(value.zoom.toDouble())
            // Heading + tilt only take effect on vector maps (MapsConfig.mapId set); classic
            // raster maps silently no-op these setters.
            map.jsSetHeadingAndTilt(value.bearing, value.tilt)
        }

    actual val projection: Projection?
        get() = boundMap?.jsCurrentProjection()?.let(::SnapshotProjection)

    actual suspend fun animate(update: CameraUpdate, durationMs: Int) {
        val map = boundMap
        if (map == null) {
            // No map bound — fall back to instant move so position state still reflects the target.
            move(update)
            return
        }
        _cameraMoveStartedReason = CameraMoveStartedReason.DEVELOPER_ANIMATION

        // Custom-duration path: drive a Compose-side tween. SDK-default path: panTo + idle.
        if (durationMs != Int.MAX_VALUE && update !is CameraUpdate.NewLatLngBounds) {
            animateCustomDuration(map, update, durationMs)
            return
        }

        // Cancel any previous in-flight animation.
        pendingAnimateCompletion?.complete(Unit)
        idleCompletionListener?.remove()
        idleCompletionListener = null

        val completion = CompletableDeferred<Unit>()
        pendingAnimateCompletion = completion

        when (update) {
            is CameraUpdate.NewCameraPosition -> {
                suppressMapToStateSync = true
                map.jsPanTo(update.position.target.latitude, update.position.target.longitude)
                map.jsSetZoom(update.position.zoom.toDouble())
                _position = update.position
            }
            is CameraUpdate.NewLatLngZoom -> {
                suppressMapToStateSync = true
                map.jsPanTo(update.latLng.latitude, update.latLng.longitude)
                map.jsSetZoom(update.zoom.toDouble())
                _position = _position.copy(target = update.latLng, zoom = update.zoom)
            }
            is CameraUpdate.NewLatLngBounds -> {
                suppressMapToStateSync = true
                map.jsFitBounds(update.bounds, update.padding)
                _position = _position.copy(target = update.bounds.center)
            }
        }

        // Resolve the completion on the next idle event.
        idleCompletionListener = map.jsAddIdleListener {
            val deferred = pendingAnimateCompletion
            idleCompletionListener?.remove()
            idleCompletionListener = null
            pendingAnimateCompletion = null
            deferred?.complete(Unit)
        }
        completion.await()
    }

    private suspend fun animateCustomDuration(
        map: NativeMap,
        update: CameraUpdate,
        durationMs: Int,
    ) {
        val startTarget = _position.target
        val startZoom = _position.zoom
        val (endTarget, endZoom) = when (update) {
            is CameraUpdate.NewCameraPosition -> update.position.target to update.position.zoom
            is CameraUpdate.NewLatLngZoom -> update.latLng to update.zoom
            is CameraUpdate.NewLatLngBounds -> update.bounds.center to startZoom // shouldn't reach
        }
        suppressMapToStateSync = true
        val frames = durationMs / 16   // ~60fps step
        val totalFrames = frames.coerceAtLeast(1)
        for (i in 1..totalFrames) {
            val t = i.toFloat() / totalFrames
            val lat = startTarget.latitude + (endTarget.latitude - startTarget.latitude) * t
            val lng = startTarget.longitude + (endTarget.longitude - startTarget.longitude) * t
            val zoom = startZoom + (endZoom - startZoom) * t
            map.jsSetCenter(lat, lng)
            map.jsSetZoom(zoom.toDouble())
            kotlinx.coroutines.delay(16)
        }
        _position = _position.copy(target = endTarget, zoom = endZoom)
        // suppress flag clears on next idle, same path as default-duration animate
    }

    actual fun move(update: CameraUpdate) {
        _cameraMoveStartedReason = CameraMoveStartedReason.DEVELOPER_ANIMATION
        val newPosition = when (update) {
            is CameraUpdate.NewCameraPosition -> update.position
            is CameraUpdate.NewLatLngZoom ->
                _position.copy(target = update.latLng, zoom = update.zoom)
            is CameraUpdate.NewLatLngBounds ->
                _position.copy(target = update.bounds.center)
        }
        position = newPosition
        if (update is CameraUpdate.NewLatLngBounds) {
            boundMap?.jsFitBounds(update.bounds, update.padding)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Internal: map binding. Called from GoogleMap composable on attach/detach.
    // ---------------------------------------------------------------------------------------

    internal fun setMap(map: NativeMap?) {
        // Tear down existing bindings.
        mapListenerTokens.forEach { it.remove() }
        mapListenerTokens.clear()
        idleCompletionListener?.remove()
        idleCompletionListener = null
        pendingAnimateCompletion?.cancel()
        pendingAnimateCompletion = null
        boundMap = map
        if (map == null) return

        // Push initial state to the map.
        suppressMapToStateSync = true
        map.jsSetCenter(_position.target.latitude, _position.target.longitude)
        map.jsSetZoom(_position.zoom.toDouble())
        map.jsSetHeadingAndTilt(_position.bearing, _position.tilt)

        // Reflect map-driven changes back into state.
        mapListenerTokens += map.jsAddDragStartListener {
            _isMoving = true
            _cameraMoveStartedReason = CameraMoveStartedReason.GESTURE
        }
        mapListenerTokens += map.jsAddBoundsChangedListener {
            if (!suppressMapToStateSync) _isMoving = true
        }
        mapListenerTokens += map.jsAddIdleListener {
            if (suppressMapToStateSync) {
                suppressMapToStateSync = false
            } else {
                _position = map.jsGetCameraPosition()
            }
            _isMoving = false
        }
    }
}
