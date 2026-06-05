package eu.buney.maps

/**
 * Wraps a Google Maps style JSON for later application via `map.setOptions({ styles: ... })`.
 *
 * The JSON is validated eagerly (mirrors the iOS behavior) and stored as a string;
 * parsing into the JS object form is deferred until PR3 wires up the map.
 */
actual class MapStyleOptions internal constructor(internal val json: String) {
    actual companion object {
        actual fun fromJson(json: String): MapStyleOptions {
            // Lightweight validation: a Google Maps style JSON must be a top-level array.
            // Defer deep validation to the JS side at apply time.
            val trimmed = json.trimStart()
            check(trimmed.startsWith("[")) {
                "MapStyleOptions JSON must be a top-level array. Get one from " +
                    "https://mapstyle.withgoogle.com/"
            }
            return MapStyleOptions(json)
        }
    }
}

