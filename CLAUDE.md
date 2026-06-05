# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

`kmp-maps-compose` is a Kotlin Compose Multiplatform library that wraps Google Maps with a unified API across Android and iOS. It is designed as a drop-in multiplatform replacement for [android-maps-compose](https://github.com/googlemaps/android-maps-compose) — its API surface, naming, and behavior are intentionally kept close to upstream so that "ports" from `googlemaps/android-maps-compose` PRs are mechanical. `CHANGELOG.md` and PR descriptions routinely reference the upstream PR being ported.

Published artifacts under `eu.buney.maps`:
- `kmp-maps-compose` — the map composable, overlays, camera state.
- `kmp-maps-compose-utils` — clustering (compose-native port, not a wrapper) and a `WmsTileOverlay`. Only adds value beyond what's in `kmp-maps-compose`; not a thin wrapper around `android-maps-utils`.

## Common commands

Build everything and publish to local Maven (mirrors what CI runs):
```
./gradlew publishToMavenLocal --no-configuration-cache
```

Targeted builds:
```
./gradlew :kmp-maps-compose:assemble                  # build library (all targets)
./gradlew :kmp-maps-compose:compileKotlinIosArm64     # iOS arm64 only — fast iteration
./gradlew :kmp-maps-compose-utils:assemble
./gradlew :sample:androidApp:installDebug             # install sample on connected Android device
```

Publishing to Maven Central is a manual GitHub Actions dispatch (`workflow_dispatch` with `publish=true`); do not run `publishToMavenCentral` locally without the signing/credential env vars.

Running the sample app requires a Google Maps API key. Copy `secrets.default.properties` to `secrets.properties` and fill in `MAPS_API_KEY`. The iOS sample is generated with XcodeGen — `cd sample/iosApp && xcodegen generate` then open `iosApp.xcodeproj`. The `MainViewController.kt` calls `GMSServices.provideAPIKey(BuildKonfig.MAPS_API_KEY)` before composing.

There is no test suite in this repo (no `commonTest` source sets); "verification" means a successful multiplatform build plus exercising the sample app on both platforms.

## Architecture

### expect/actual layout

`kmp-maps-compose/src/` has `commonMain`, `androidMain`, `iosMain`. The public API lives in `commonMain` as `expect` declarations; actuals live in the two platform source sets. Filenames follow the convention `X.kt` in commonMain, `X.android.kt` / `X.ios.kt` in platform source sets.

The compiler flag `-Xexpect-actual-classes` is enabled because several public types (`CameraPositionState`, `BitmapDescriptor`, `MapStyleOptions`, `Marker`, `Polyline`, etc.) are `expect class` rather than `expect fun` — this is currently in beta in Kotlin.

### Android: thin wrapper over android-maps-compose

`androidMain` actuals delegate to `com.google.maps.android:maps-compose` (declared as `api(...)` so consumers get its types transitively). The `GoogleMap` actual translates this library's `MapProperties`/`MapUiSettings`/callbacks into the upstream types and forwards them. `CameraPositionState` on Android holds a reference to the upstream `CameraPositionState` (single source of truth, no bidirectional sync).

When porting an upstream android-maps-compose change, most of the work is in `commonMain` (API shape) and `iosMain` (replicating the behavior with the iOS SDK); `androidMain` usually only needs the new field/parameter passed through.

### iOS: native implementation via cinterop

iOS does not have a Compose-style maps library, so this module implements one. Key pieces in `iosMain`:

- **`GoogleMap.ios.kt`** — hosts a `GMSMapView` via `UIKitView`, owns the `GMSMapViewDelegate`, and launches a subcomposition.
- **`MapApplier.kt`** — custom `AbstractApplier<MapNode>` that bridges Compose's declarative subcomposition to the imperative `GMSMapView`. Each overlay node (`MarkerNode`, `CircleNode`, `PolylineNode`, `PolygonNode`, `GroundOverlayNode`, `TileOverlayNode`, `IOSMapPropertiesNode`) implements `MapNode` with `onAttached`/`onRemoved`/`onCleared` hooks that add/remove the corresponding GMS object on the map.
- **`MapUpdater` + `MapUpdaterState`** — `MapUpdaterState` is a `@Stable` holder of `mutableStateOf` fields, `remember`-ed once then mutated on every recomposition with `.also { it.X = X }`. The subcomposition reads from it via `ComposeNode { ... set(...) { } }`, so property changes (mapType, traffic, padding, etc.) reach the native `GMSMapView`. This pattern mirrors android-maps-compose's `MapUpdaterState`; reverting to capturing values in the subcomposition closure caused the 0.3.0 regression fixed in 0.6.0.
- **`GMSMapViewDelegate`** — single `NSObject` delegate routes overlay/marker callbacks through `MapApplier.findXNode(...)` to the right `MapNode`.

`expectedGoogleMapsBridge/` is an auto-generated local Swift Package produced by the `spm4kmp` Gradle plugin. The remote `googlemaps/ios-maps-sdk` SPM dependency is configured in `kmp-maps-compose/build.gradle.kts` under `swiftPackageConfig { ... }` with the version pulled from `libs.versions.toml` (`google-maps-ios`). Apps consuming this library either add the auto-generated bridge package locally in Xcode (recommended, keeps the SDK version in sync) or take a direct SPM dependency on the same Google Maps version.

### Composable content model

`@GoogleMapComposable` is an annotation that scopes content allowed inside `GoogleMap { ... }`. On both platforms the content runs in a subcomposition with `LocalCameraPositionState` provided. `MapEffect { nativeMap -> ... }` exposes the platform-native map object (`com.google.android.gms.maps.GoogleMap` or `GMSMapView`) via the `NativeMap` typealias — this is the escape hatch for SDK features that haven't been wrapped.

### Compose stability config

`compose_compiler_stability_config.conf` (applied to both library modules) marks several Google Maps SDK types and `platform.UIKit.UIImage` as stable so they don't force recomposition. When adding new platform types that are effectively immutable after construction, extend this file rather than wrapping them.

## Conventions worth knowing

- **Mirror upstream API names and parameters.** When porting an android-maps-compose change, keep the parameter names and default values identical so that callers can move between the two libraries with minimal friction. Document any unavoidable divergence in the README's feature-parity table.
- **iOS limitations are intentional, not TODOs.** Some Android-only features (stroke patterns, cap/joint types, terrain map type, advanced markers, scale bar, street view) have no iOS SDK equivalent. Don't fake support — leave them unimplemented on iOS and document the gap in README and in `MapProperties`/`MapUiSettings` doc comments.
- **Version coupling.** `play-services-maps`, `maps-compose`, and `google-maps-ios` versions in `libs.versions.toml` must stay consistent — the comment above `play-services-maps` documents the transitive constraint from `maps-compose`. Library version (`kmp-maps-compose`) is shared by both published modules.
- **Branch naming.** Active work uses `Yourname/feature-name` branches (e.g. `Jiri-Parizek/add-wasm-support`). Release prep uses `release/x.y.z`.
