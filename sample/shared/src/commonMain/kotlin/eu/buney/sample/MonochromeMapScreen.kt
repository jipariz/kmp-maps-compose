package eu.buney.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.buney.maps.CameraPosition
import eu.buney.maps.GoogleMap
import eu.buney.maps.LatLng
import eu.buney.maps.MapColorScheme
import eu.buney.maps.MapProperties
import eu.buney.maps.MapStyleOptions
import eu.buney.maps.rememberCameraPositionState

private val london = LatLng(51.5074, -0.1278)

/**
 * Demonstrates monochrome / black-and-white map theming.
 *
 * Three approaches are showcased:
 *
 * - **Light B&W**: a [MapStyleOptions] grayscale style (`saturation: -100`) — works on
 *   all platforms (Android, iOS, Web) without any Cloud configuration. Best for editorial /
 *   data-overlay contexts where the map should recede.
 * - **Dark B&W (inverted)**: same grayscale plus `invert_lightness` — a high-contrast
 *   dark monochrome. Also pure styling, all platforms.
 * - **Native dark**: uses [MapColorScheme.DARK] with no style override. On Android this
 *   produces Google's first-party dark theme; on Web this requires a Cloud-configured
 *   `MapsConfig.mapId` to take effect (the classic raster renderer ignores `colorScheme`);
 *   on iOS the Google Maps SDK has no native color-scheme switch so this falls back to
 *   the default theme.
 */
private enum class Theme(val label: String, val json: String?, val colorScheme: MapColorScheme) {
    LightBlackAndWhite(
        label = "Light B&W",
        json = MONOCHROME_LIGHT_STYLE,
        colorScheme = MapColorScheme.LIGHT,
    ),
    DarkBlackAndWhite(
        label = "Dark B&W",
        json = MONOCHROME_DARK_STYLE,
        colorScheme = MapColorScheme.DARK,
    ),
    NativeDark(
        label = "Native dark",
        json = null,
        colorScheme = MapColorScheme.DARK,
    ),
    SystemDefault(
        label = "System default",
        json = null,
        colorScheme = MapColorScheme.FOLLOW_SYSTEM,
    ),
}

@Composable
fun MonochromeMapScreen(modifier: Modifier = Modifier) {
    var theme by remember { mutableStateOf(Theme.LightBlackAndWhite) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition(target = london, zoom = 11f)
                },
                properties = MapProperties(
                    mapStyleOptions = theme.json?.let { MapStyleOptions.fromJson(it) },
                    colorScheme = theme.colorScheme,
                ),
            )

            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                var expanded by remember { mutableStateOf(false) }

                FloatingActionButton(onClick = { expanded = true }) {
                    Text(
                        text = theme.label,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(200.dp),
                ) {
                    Theme.entries.forEach { entry ->
                        DropdownMenuItem(
                            text = { Text(entry.label) },
                            onClick = {
                                theme = entry
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

// Classic high-contrast B&W: desaturate everything, hide POI/transit clutter so road
// network and coastline read clearly. Works identically on Android, iOS, and Web.
private const val MONOCHROME_LIGHT_STYLE = """[
  {"stylers":[{"saturation":-100}]},
  {"featureType":"poi","stylers":[{"visibility":"off"}]},
  {"featureType":"transit","stylers":[{"visibility":"off"}]},
  {"elementType":"labels.icon","stylers":[{"visibility":"off"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#d9d9d9"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#ffffff"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#000000"},{"weight":0.5}]},
  {"featureType":"landscape","elementType":"geometry","stylers":[{"color":"#f5f5f5"}]}
]"""

// Same grayscale, with invert_lightness for a dark B&W look — ink-black background with
// road network in pale gray.
private const val MONOCHROME_DARK_STYLE = """[
  {"stylers":[{"saturation":-100},{"invert_lightness":true}]},
  {"featureType":"poi","stylers":[{"visibility":"off"}]},
  {"featureType":"transit","stylers":[{"visibility":"off"}]},
  {"elementType":"labels.icon","stylers":[{"visibility":"off"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#1a1a1a"},{"weight":0.5}]}
]"""
