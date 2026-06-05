package eu.buney.maps

import androidx.compose.runtime.Composable

/**
 * Web limitation: rendering Compose content to a bitmap requires reading pixels back from
 * Skia, which needs additional plumbing on Skiko web that hasn't been verified yet. Use
 * [BitmapDescriptorFactory.fromEncodedImage] or [rememberBitmapDescriptor] (with a
 * [androidx.compose.ui.graphics.ImageBitmap]) instead. Tracked in the README's Web-Specific
 * Notes.
 */
@Composable
@GoogleMapComposable
actual fun rememberComposeBitmapDescriptor(
    vararg keys: Any,
    content: @Composable () -> Unit,
): BitmapDescriptor = error(
    "rememberComposeBitmapDescriptor is not supported on web yet — use " +
        "BitmapDescriptorFactory.fromEncodedImage or rememberBitmapDescriptor instead."
)
