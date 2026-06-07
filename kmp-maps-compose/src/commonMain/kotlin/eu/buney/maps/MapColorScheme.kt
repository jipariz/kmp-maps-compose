package eu.buney.maps

/**
 * Color scheme for the map's base UI (roads, water, terrain labels, etc.).
 *
 * For finer-grained styling (grayscale, custom palette, business POI hiding) use
 * [MapStyleOptions.fromJson] with a style JSON from the
 * [Google Maps Styling Wizard](https://mapstyle.withgoogle.com/) instead — that
 * applies on top of any selected color scheme.
 *
 * Platform support:
 * - **Android**: native (since `android-maps-compose` 8.x). Requires a Cloud-configured
 *   `mapId` for guaranteed-correct results; without one, the SDK falls back to a
 *   built-in default theme.
 * - **iOS**: not supported by the Google Maps iOS SDK; falls back to [LIGHT] visually,
 *   but consumers can apply a custom dark `mapStyleOptions` JSON to achieve the same
 *   look.
 * - **Web** (wasmJs / jsIr): native — sets the `colorScheme` option on
 *   `google.maps.Map`. Requires a Cloud-configured `mapId` (see [MapsConfig.mapId])
 *   for the LIGHT/DARK switch to take effect; with the default raster map, the SDK
 *   may ignore the setting.
 */
enum class MapColorScheme {
    /**
     * Use the system's current color scheme (light or dark).
     */
    FOLLOW_SYSTEM,

    /**
     * Force light mode regardless of the system setting.
     */
    LIGHT,

    /**
     * Force dark mode regardless of the system setting.
     */
    DARK,
}
