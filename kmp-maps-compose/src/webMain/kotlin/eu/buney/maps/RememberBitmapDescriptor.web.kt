package eu.buney.maps

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toPixelMap

internal actual fun ImageBitmap.toBitmapDescriptor(): BitmapDescriptor {
    val pixels: PixelMap = toPixelMap()
    val w = width
    val h = height
    val argb = ByteArray(w * h * 4)
    var dst = 0
    for (y in 0 until h) {
        for (x in 0 until w) {
            val c = pixels[x, y]
            // Compose's Color.toArgb returns 0xAARRGGBB.
            argb[dst] = ((c.alpha * 255f).toInt() and 0xFF).toByte()
            argb[dst + 1] = ((c.red * 255f).toInt() and 0xFF).toByte()
            argb[dst + 2] = ((c.green * 255f).toInt() and 0xFF).toByte()
            argb[dst + 3] = ((c.blue * 255f).toInt() and 0xFF).toByte()
            dst += 4
        }
    }
    return BitmapDescriptorFactory.fromBytes(argb, w, h)
}
