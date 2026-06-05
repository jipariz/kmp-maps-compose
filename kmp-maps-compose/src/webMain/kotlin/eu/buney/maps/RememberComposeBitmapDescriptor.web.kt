package eu.buney.maps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

/**
 * Web implementation: render Compose content into a CPU-only Skia raster image via
 * [ImageComposeScene] / [renderComposeScene] (the same path the iOS implementation uses),
 * then encode the result to PNG and wrap as an Object URL.
 *
 * Two-pass measurement: ImageComposeScene's surface size is fixed at construction, so a 1x1
 * warmup scene measures intrinsic content size, then a second scene renders at the measured
 * size.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
@GoogleMapComposable
actual fun rememberComposeBitmapDescriptor(
    vararg keys: Any,
    content: @Composable () -> Unit,
): BitmapDescriptor {
    return remember(*keys) {
        captureComposableToBitmapDescriptor(content)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun captureComposableToBitmapDescriptor(
    content: @Composable () -> Unit,
): BitmapDescriptor {
    // Browser DPR is read via window.devicePixelRatio in real apps; for raster captures we use
    // 1.0 since the output is downstream rasterized to whatever marker size the icon attains.
    val density = Density(1f)

    val contentSize = ImageComposeScene(
        width = 1, height = 1, density = density, content = content,
    ).use { scene ->
        scene.render()
        scene.calculateContentSize()
    }

    val w = contentSize.width.coerceAtLeast(1)
    val h = contentSize.height.coerceAtLeast(1)

    val image: Image = renderComposeScene(
        width = w, height = h, density = density, content = content,
    )

    val pngData = image.encodeToData(EncodedImageFormat.PNG)
        ?: error("Image.encodeToData(PNG) returned null on Skiko web — fall back to BitmapDescriptorFactory.fromEncodedImage")
    val bytes: ByteArray = pngData.bytes
    return BitmapDescriptorFactory.fromEncodedImage(bytes)
}
