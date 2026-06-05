package eu.buney.maps

/**
 * Base interface for all map overlay nodes (markers, polylines, polygons, etc.).
 *
 * Each map element that can be added to the map should implement this interface.
 * Lifecycle methods are called by [MapApplier] when nodes are added to, removed from,
 * or cleared from the composition.
 *
 * Mirrors the iosMain `MapNode` pattern.
 */
internal interface MapNode {
    /** Called when the node is attached to the composition tree. */
    fun onAttached() {}

    /** Called when the node is removed from the composition tree. */
    fun onRemoved() {}

    /** Called when the entire map is cleared. */
    fun onCleared() {}
}

/** Root node for the [MapApplier]. Placeholder root of the composition tree. */
internal object MapNodeRoot : MapNode
