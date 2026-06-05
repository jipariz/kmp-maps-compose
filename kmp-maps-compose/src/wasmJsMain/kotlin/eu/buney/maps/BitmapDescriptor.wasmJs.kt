@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.ImageData
import org.w3c.files.Blob

// Index-write helpers — Kotlin/Wasm doesn't bind `arr[i] = v` for Uint8Array /
// Uint8ClampedArray the same way Kotlin/JS does, so we go through js() literals.
private fun uint8Set(arr: Uint8Array, index: Int, value: Int): Unit =
    js("arr[index] = value")

private fun imageDataSet(data: JsAny, index: Int, value: Int): Unit =
    js("data[index] = value")

private fun makeBlob(buf: Uint8Array, mime: String): Blob =
    js("new Blob([buf], { type: mime })")

private fun createObjectUrlForBlob(blob: Blob): String =
    js("URL.createObjectURL(blob)")

private fun canvasContext2d(canvas: HTMLCanvasElement): CanvasRenderingContext2D =
    js("canvas.getContext('2d')")

internal actual fun encodedImageBytesToObjectUrl(bytes: ByteArray, mime: String): String {
    val arr = Uint8Array(bytes.size)
    for (i in bytes.indices) uint8Set(arr, i, bytes[i].toInt() and 0xFF)
    return createObjectUrlForBlob(makeBlob(arr, mime))
}

internal actual fun argbBytesToDataUrl(bytes: ByteArray, width: Int, height: Int): String {
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = width
    canvas.height = height
    val ctx = canvasContext2d(canvas)
    val imageData = ctx.createImageData(width.toDouble(), height.toDouble())
    val rgba = imageData.data.unsafeCast<JsAny>()
    var src = 0
    var dst = 0
    while (src < bytes.size) {
        val a = bytes[src].toInt() and 0xFF
        val r = bytes[src + 1].toInt() and 0xFF
        val g = bytes[src + 2].toInt() and 0xFF
        val b = bytes[src + 3].toInt() and 0xFF
        imageDataSet(rgba, dst, r)
        imageDataSet(rgba, dst + 1, g)
        imageDataSet(rgba, dst + 2, b)
        imageDataSet(rgba, dst + 3, a)
        src += 4
        dst += 4
    }
    ctx.putImageData(imageData, 0.0, 0.0)
    return canvas.toDataURL("image/png")
}
