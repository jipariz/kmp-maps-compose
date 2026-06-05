package eu.buney.maps

/**
 * Holds a URL pointing to the image used as a map icon (Object URL or data URL).
 *
 * `ownsUrl=true` means the URL was created via `URL.createObjectURL` and should ideally be
 * revoked when the last referencing node detaches. PR4 ships without refcounted revocation —
 * Object URLs survive until page reload. A follow-up can wire `FinalizationRegistry` or a
 * Node-level refcount through `MarkerNode.onRemoved`.
 */
actual class BitmapDescriptor internal constructor(
    val url: String,
    internal val ownsUrl: Boolean,
)

actual object BitmapDescriptorFactory {
    actual fun fromBytes(bytes: ByteArray, width: Int, height: Int): BitmapDescriptor {
        require(bytes.size == width * height * 4) {
            "fromBytes: ARGB ByteArray length ${bytes.size} != width($width) * height($height) * 4"
        }
        // toDataURL produces a synchronous data: URL — no Object URL lifecycle to manage.
        return BitmapDescriptor(url = argbBytesToDataUrl(bytes, width, height), ownsUrl = false)
    }

    actual fun fromEncodedImage(data: ByteArray): BitmapDescriptor {
        val mime = sniffImageMime(data)
        return BitmapDescriptor(url = encodedImageBytesToObjectUrl(data, mime), ownsUrl = true)
    }
}

/** Returns the most appropriate MIME for a PNG / JPEG / WebP / GIF byte payload. */
internal fun sniffImageMime(data: ByteArray): String {
    if (data.size < 4) return "image/png"
    val b0 = data[0].toInt() and 0xFF
    val b1 = data[1].toInt() and 0xFF
    val b2 = data[2].toInt() and 0xFF
    val b3 = data[3].toInt() and 0xFF
    return when {
        b0 == 0x89 && b1 == 0x50 && b2 == 0x4E && b3 == 0x47 -> "image/png"
        b0 == 0xFF && b1 == 0xD8 && b2 == 0xFF -> "image/jpeg"
        b0 == 'G'.code && b1 == 'I'.code && b2 == 'F'.code && b3 == '8'.code -> "image/gif"
        b0 == 'R'.code && b1 == 'I'.code && b2 == 'F'.code && b3 == 'F'.code -> "image/webp"
        else -> "image/png"
    }
}

/**
 * Converts a raw ARGB byte buffer into a `data:image/png;base64,...` URL via a hidden
 * `HTMLCanvasElement` and `canvas.toDataURL`. Per-target implementations differ only in how
 * the typed-array conversion is expressed for JsAny vs dynamic.
 */
internal expect fun argbBytesToDataUrl(bytes: ByteArray, width: Int, height: Int): String

/**
 * Wraps an encoded image byte payload (PNG / JPEG / etc.) in a `Blob` and creates an
 * Object URL pointing at it. The URL must be revoked via `URL.revokeObjectURL` when the
 * descriptor is no longer needed; for PR4 we don't revoke (see [BitmapDescriptor] docs).
 */
internal expect fun encodedImageBytesToObjectUrl(bytes: ByteArray, mime: String): String
