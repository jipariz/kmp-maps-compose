package eu.buney.maps

/**
 * Configuration for the Google Maps JavaScript API loader.
 *
 * On web targets (wasmJs / jsIr), the library loads Google Maps at runtime by injecting the
 * official bootstrap script. The consumer must set [apiKey] before any [GoogleMap] composable
 * runs — this mirrors how Android consumers add an `<meta-data>` API key to AndroidManifest.xml
 * and iOS consumers call `GMSServices.provideAPIKey(...)`.
 *
 * Example (in a `ComposeViewport`-hosted web entry point):
 * ```kotlin
 * fun main() {
 *     MapsConfig.apiKey = "AIza..."
 *     ComposeViewport(document.body!!) { App() }
 * }
 * ```
 */
object MapsConfig {
    /**
     * Google Maps JavaScript API key. Must be set before composing [GoogleMap] for the first
     * time. Setting this after the API has already loaded has no effect.
     */
    var apiKey: String? = null

    /**
     * Libraries to load alongside the core `maps` library. Default is empty (only `maps`).
     *
     * Common values: `"marker"` (advanced markers), `"places"` (Places API), `"geometry"`
     * (geometry helpers), `"drawing"` (drawing tools).
     */
    var libraries: List<String> = emptyList()

    /**
     * Maps JS API release channel. Defaults to `"weekly"`. Other valid values: `"quarterly"`,
     * `"beta"`, `"alpha"`, or a specific version like `"3.55"`.
     */
    var version: String = "weekly"

    /**
     * Optional language code (BCP-47), e.g. `"en"`, `"de"`. Affects UI labels and POI names.
     */
    var language: String? = null

    /**
     * Optional region code (ccTLD), e.g. `"US"`, `"DE"`. Biases geocoding and tile rendering.
     */
    var region: String? = null

    /**
     * Optional Cloud-configured Map ID. Configure styles, color scheme, and vector-map
     * features at https://console.cloud.google.com/google/maps-apis/studio/maps.
     *
     * Setting a mapId switches the map from the classic raster renderer to the vector
     * renderer, which is required for:
     * - [MapColorScheme.LIGHT] / [MapColorScheme.DARK] (the JS API ignores `colorScheme`
     *   on classic raster maps);
     * - non-zero `bearing` (heading) and `tilt` on [CameraPosition].
     *
     * Must be set before the first [GoogleMap] composes — mapId can only be specified at
     * `new google.maps.Map(...)` construction time.
     */
    var mapId: String? = null
}
