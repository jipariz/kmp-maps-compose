@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps.jsinterop

import eu.buney.maps.NativeMap
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

internal actual class InfoWindowRef internal constructor(internal val handle: JsInfoWindow)

private fun MapsEventListener.toToken(): ListenerToken = ListenerToken { remove() }

private fun renderInfoWindowHtml(title: String?, snippet: String?): String {
    val safeTitle = title?.escapeHtml().orEmpty()
    val safeSnippet = snippet?.escapeHtml().orEmpty()
    return buildString {
        append("<div>")
        if (safeTitle.isNotEmpty()) append("<strong>").append(safeTitle).append("</strong>")
        if (safeSnippet.isNotEmpty()) append("<div>").append(safeSnippet).append("</div>")
        append("</div>")
    }
}

private fun String.escapeHtml(): String =
    replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

private fun stringContent(html: String): JsAny = js("html")

private fun imageElementContent(url: String): JsAny =
    js("(function() { var img = document.createElement('img'); img.src = url; img.style.maxWidth='320px'; img.style.maxHeight='240px'; return img; })()")

internal actual fun createInfoWindowDefault(title: String?, snippet: String?): InfoWindowRef {
    val opts = newInfoWindowOptions().apply {
        content = stringContent(renderInfoWindowHtml(title, snippet))
    }
    return InfoWindowRef(createInfoWindow(opts))
}

internal actual fun createInfoWindowImage(url: String): InfoWindowRef {
    val opts = newInfoWindowOptions().apply {
        content = imageElementContent(url)
    }
    return InfoWindowRef(createInfoWindow(opts))
}

internal actual fun InfoWindowRef.openAt(map: NativeMap, anchor: MarkerRef) {
    handle.open(map.handle, anchor.handle)
}

internal actual fun InfoWindowRef.close() {
    handle.close()
}

internal actual fun InfoWindowRef.updateContent(title: String?, snippet: String?) {
    handle.setContent(stringContent(renderInfoWindowHtml(title, snippet)))
}

internal actual fun InfoWindowRef.addCloseListener(handler: () -> Unit): ListenerToken =
    handle.addListener("closeclick") { _ -> handler() }.toToken()
