package eu.buney.maps

import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.files.Blob

private fun makeBlob(buf: Uint8Array, mime: String): Blob =
    js("new Blob([buf], { type: mime })").unsafeCast<Blob>()

private fun createObjectUrlForBlob(blob: Blob): String =
    js("URL.createObjectURL(blob)").unsafeCast<String>()

internal actual fun encodedImageBytesToObjectUrl(bytes: ByteArray, mime: String): String {
    // Kotlin/JS exposes typed-array index assignment via the `dynamic` cast.
    val arr = Uint8Array(bytes.size).asDynamic()
    for (i in bytes.indices) arr[i] = bytes[i].toInt() and 0xFF
    return createObjectUrlForBlob(makeBlob(arr.unsafeCast<Uint8Array>(), mime))
}

internal actual fun argbBytesToDataUrl(bytes: ByteArray, width: Int, height: Int): String {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    val ctx = canvas.getContext("2d").unsafeCast<CanvasRenderingContext2D>()
    val imageData = ctx.createImageData(width.toDouble(), height.toDouble())
    val rgba = imageData.data.asDynamic()
    var src = 0
    var dst = 0
    while (src < bytes.size) {
        val a = bytes[src].toInt() and 0xFF
        val r = bytes[src + 1].toInt() and 0xFF
        val g = bytes[src + 2].toInt() and 0xFF
        val b = bytes[src + 3].toInt() and 0xFF
        rgba[dst] = r
        rgba[dst + 1] = g
        rgba[dst + 2] = b
        rgba[dst + 3] = a
        src += 4
        dst += 4
    }
    ctx.putImageData(imageData, 0.0, 0.0)
    return canvas.toDataURL("image/png")
}
