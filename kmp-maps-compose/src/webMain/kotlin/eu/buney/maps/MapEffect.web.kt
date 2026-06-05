package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope

// MapEffect is composed inside the GoogleMap content lambda; the MapApplier supplies the
// NativeMap via LocalCameraPositionState's bound map, but for now we read the applier
// directly through a CurrentLocalNativeMap hook that's installed by GoogleMap. Until that
// hook exists (PR4), MapEffect resolves the map via the camera state, which holds the same
// reference internally. The user's block runs once the map is available.

@Composable
@GoogleMapComposable
actual fun MapEffect(key1: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    val map = currentNativeMap() ?: return
    LaunchedEffect(key1, map) { block(map) }
}

@Composable
@GoogleMapComposable
actual fun MapEffect(key1: Any?, key2: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    val map = currentNativeMap() ?: return
    LaunchedEffect(key1, key2, map) { block(map) }
}

@Composable
@GoogleMapComposable
actual fun MapEffect(
    key1: Any?,
    key2: Any?,
    key3: Any?,
    block: suspend CoroutineScope.(NativeMap) -> Unit,
) {
    val map = currentNativeMap() ?: return
    LaunchedEffect(key1, key2, key3, map) { block(map) }
}

@Composable
@GoogleMapComposable
actual fun MapEffect(vararg keys: Any?, block: suspend CoroutineScope.(NativeMap) -> Unit) {
    val map = currentNativeMap() ?: return
    LaunchedEffect(*keys, map) { block(map) }
}

/**
 * Resolves the [NativeMap] currently associated with the surrounding [GoogleMap] composable.
 *
 * Reads through [LocalCameraPositionState]; the camera state holds the bound map handle.
 * Returns null until the map has been instantiated (during MapsApiLoader load and the brief
 * window before [DisposableEffect] resolves).
 */
@Composable
private fun currentNativeMap(): NativeMap? {
    val state = currentCameraPositionState
    return state.boundNativeMapForEffect
}

