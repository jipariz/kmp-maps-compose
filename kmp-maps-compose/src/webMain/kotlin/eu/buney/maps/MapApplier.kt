package eu.buney.maps

import androidx.compose.runtime.AbstractApplier

/**
 * Custom [AbstractApplier] for managing map overlay nodes in the composition tree on web.
 *
 * Bridges Compose's declarative model and the imperative Google Maps JavaScript API.
 * Mirrors the iosMain [MapApplier] one-for-one.
 *
 * @param map The [NativeMap] this applier writes to. Per-target extension helpers in
 *   `eu.buney.maps.jsinterop` operate on the underlying `google.maps.Map` handle.
 */
internal class MapApplier(
    val map: NativeMap,
) : AbstractApplier<MapNode>(MapNodeRoot) {

    private val decorations = mutableListOf<MapNode>()

    override fun onClear() {
        decorations.forEach { it.onCleared() }
        decorations.clear()
    }

    override fun insertBottomUp(index: Int, instance: MapNode) {
        decorations.add(index, instance)
        instance.onAttached()
    }

    override fun insertTopDown(index: Int, instance: MapNode) {
        // insertBottomUp is preferred for this use case (matches iosMain)
    }

    override fun move(from: Int, to: Int, count: Int) {
        decorations.move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        repeat(count) {
            decorations[index + it].onRemoved()
        }
        decorations.remove(index, count)
    }
}
