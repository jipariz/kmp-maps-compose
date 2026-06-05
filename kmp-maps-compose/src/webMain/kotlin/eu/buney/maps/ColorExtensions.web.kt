package eu.buney.maps

import androidx.compose.ui.graphics.Color

/**
 * Returns a CSS-compatible hex string for the color's RGB channels (without alpha).
 * Google Maps JS accepts a separate strokeOpacity / fillOpacity for the alpha channel.
 */
internal fun Color.toCssHex(): String {
    val r = (red * 255f).toInt().coerceIn(0, 255)
    val g = (green * 255f).toInt().coerceIn(0, 255)
    val b = (blue * 255f).toInt().coerceIn(0, 255)
    fun pad(v: Int): String = if (v < 16) "0${v.toString(16)}" else v.toString(16)
    return "#${pad(r)}${pad(g)}${pad(b)}"
}

internal val Color.opacityDouble: Double get() = alpha.toDouble()
