@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)

package eu.buney.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.HtmlElementView
import eu.buney.maps.jsinterop.ListenerToken
import eu.buney.maps.jsinterop.MapsApiLoader
import eu.buney.maps.jsinterop.createNativeMap
import eu.buney.maps.jsinterop.jsAddClickListener
import eu.buney.maps.jsinterop.jsAddPoiClickListener
import eu.buney.maps.jsinterop.jsAddRightClickListener
import eu.buney.maps.jsinterop.jsAddTilesLoadedListenerOnce
import eu.buney.maps.jsinterop.jsApplyProperties
import eu.buney.maps.jsinterop.jsApplyUiSettings
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement

@Composable
actual fun GoogleMap(
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    contentPadding: PaddingValues,
    onMapClick: ((LatLng) -> Unit)?,
    onMapLongClick: ((LatLng) -> Unit)?,
    onPOIClick: ((PointOfInterest) -> Unit)?,
    onMapLoaded: (() -> Unit)?,
    content: (@Composable @GoogleMapComposable () -> Unit)?,
) {
    // Trigger the API load on first composition. Subsequent compositions resolve immediately.
    var apiReady by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<Throwable?>(null) }
    LaunchedEffect(Unit) {
        runCatching { MapsApiLoader.await() }
            .onSuccess { apiReady = true }
            .onFailure { loadError = it }
    }

    when {
        loadError != null -> ErrorPlaceholder(modifier, loadError!!.message ?: "Failed to load Google Maps")
        !apiReady -> LoadingPlaceholder(modifier)
        else -> ReadyGoogleMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings,
            contentPadding = contentPadding,
            onMapClick = onMapClick,
            onMapLongClick = onMapLongClick,
            onPOIClick = onPOIClick,
            onMapLoaded = onMapLoaded,
            content = content,
        )
    }
}

@Composable
private fun ReadyGoogleMap(
    modifier: Modifier,
    cameraPositionState: CameraPositionState,
    properties: MapProperties,
    uiSettings: MapUiSettings,
    contentPadding: PaddingValues,
    onMapClick: ((LatLng) -> Unit)?,
    onMapLongClick: ((LatLng) -> Unit)?,
    onPOIClick: ((PointOfInterest) -> Unit)?,
    onMapLoaded: (() -> Unit)?,
    content: (@Composable @GoogleMapComposable () -> Unit)?,
) {
    // Capture freshly-created host div inside the factory so we don't have to DOM-scan.
    var hostDiv by remember { mutableStateOf<HTMLDivElement?>(null) }
    var nativeMap by remember { mutableStateOf<NativeMap?>(null) }
    val initialCameraPosition = remember { cameraPositionState.position }

    val onMapClickState = rememberUpdatedState(onMapClick)
    val onMapLongClickState = rememberUpdatedState(onMapLongClick)
    val onPOIClickState = rememberUpdatedState(onPOIClick)
    val onMapLoadedState = rememberUpdatedState(onMapLoaded)
    val currentContent by rememberUpdatedState(content)
    val parentComposition = rememberCompositionContext()

    HtmlElementView(
        factory = {
            (document.createElement("div") as HTMLDivElement).apply {
                style.width = "100%"
                style.height = "100%"
                hostDiv = this
            }
        },
        modifier = modifier,
    )

    DisposableEffect(hostDiv) {
        val host = hostDiv
        if (host != null) {
            val map = createNativeMap(host, properties, uiSettings, initialCameraPosition)
            nativeMap = map

            cameraPositionState.setMap(map)

            val tokens = mutableListOf<ListenerToken>()
            tokens += map.jsAddClickListener { latLng -> onMapClickState.value?.invoke(latLng) }
            tokens += map.jsAddRightClickListener { latLng -> onMapLongClickState.value?.invoke(latLng) }
            tokens += map.jsAddPoiClickListener { placeId, name, latLng ->
                // JS click events don't carry the POI's display name; PR4 can do a Places
                // lookup if needed. Pass empty string to keep PointOfInterest's contract.
                onPOIClickState.value?.invoke(PointOfInterest(latLng, name ?: "", placeId))
            }
            tokens += map.jsAddTilesLoadedListenerOnce { onMapLoadedState.value?.invoke() }

            val applier = MapApplier(map)
            val composition = Composition(applier, parentComposition)
            composition.setContent {
                CompositionLocalProvider(LocalCameraPositionState provides cameraPositionState) {
                    currentContent?.invoke()
                }
            }

            onDispose {
                composition.dispose()
                tokens.forEach { it.remove() }
                cameraPositionState.setMap(null)
                nativeMap = null
            }
        } else {
            onDispose { }
        }
    }

    // Apply properties / uiSettings reactively when they change.
    LaunchedEffect(properties, nativeMap) {
        nativeMap?.jsApplyProperties(properties)
    }
    LaunchedEffect(uiSettings, nativeMap) {
        nativeMap?.jsApplyUiSettings(uiSettings)
    }
}

@Composable
private fun LoadingPlaceholder(modifier: Modifier) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
        Text("Loading Google Maps…")
    }
}

@Composable
private fun ErrorPlaceholder(modifier: Modifier, message: String) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.errorContainer), contentAlignment = Alignment.Center) {
        Text("Map load failed: $message", color = MaterialTheme.colorScheme.onErrorContainer)
    }
}
