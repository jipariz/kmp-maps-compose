package eu.buney.maps.jsinterop

import eu.buney.maps.MapsConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Process-wide singleton that lazily injects Google's bootstrap snippet and exposes
 * [await] for composables that need the API loaded before they construct a map.
 *
 * The scope is process-wide rather than composable-scoped: multiple [eu.buney.maps.GoogleMap]
 * instances share one load, and cancellation of the first composable does not abort the
 * loader.
 */
internal object MapsApiLoader {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val deferred = CompletableDeferred<Unit>()
    private var started = false

    /**
     * Suspends until `google.maps.importLibrary("maps")` and any opt-in libraries
     * from [MapsConfig.libraries] have resolved. Triggers script injection on first call.
     *
     * @throws IllegalStateException if [MapsConfig.apiKey] has not been set.
     */
    suspend fun await() {
        if (!started) startLoad()
        deferred.await()
    }

    private fun startLoad() {
        started = true
        val apiKey = MapsConfig.apiKey
            ?: error(
                "MapsConfig.apiKey must be set before composing GoogleMap. " +
                    "See README — Web setup."
            )

        scope.launch {
            try {
                injectBootstrap(
                    apiKey = apiKey,
                    version = MapsConfig.version,
                    language = MapsConfig.language,
                    region = MapsConfig.region,
                )
                // Always load the core `maps` library.
                importLibrarySuspend("maps")
                // And any opt-in libraries the consumer requested.
                MapsConfig.libraries.forEach { name ->
                    if (name != "maps") importLibrarySuspend(name)
                }
                deferred.complete(Unit)
            } catch (t: Throwable) {
                deferred.completeExceptionally(t)
            }
        }
    }
}

/**
 * Injects Google's official bootstrap snippet into the page if it has not yet been injected.
 * The snippet exposes `google.maps.importLibrary(name)` returning a Promise.
 */
internal expect fun injectBootstrap(
    apiKey: String,
    version: String,
    language: String?,
    region: String?,
)

/**
 * Calls `google.maps.importLibrary(name)` and suspends until the returned Promise resolves.
 */
internal expect suspend fun importLibrarySuspend(name: String)
