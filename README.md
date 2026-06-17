# Aware

**Aware** is a Compose Multiplatform demo (Android + iOS) that showcases the
[KSensor](https://github.com/ShadAdman/KSensor) library by framing each sensor and device state
around the everyday **product feature** it powers — not around the raw reading. The gallery ships
sixteen self-contained demos that together exercise every sensor and every state type the library
exposes.

## Why "Aware"

The app is about a device being *aware* of its physical and system context — motion, light, air
pressure, location, network, battery, locale, nearby radios — and turning that awareness into a
useful product behaviour. Every screen answers "what does this signal let the product *do*?": shake
to file a report, dim the page when the room goes dark, pause playback when the phone is pocketed,
hide a bank balance the moment the app loses focus.

## The sixteen features

| Feature | Product behaviour | Signal(s) |
|---|---|---|
| Shake to report | Shake the phone to file a bug report | `ACCELEROMETER` |
| Daily step goal | Steps since you opened the app vs a goal | `STEP_COUNTER` + `STEP_DETECTOR` |
| Floors climbed | Counts floors from air-pressure change | `BAROMETER` |
| Compass | A needle pointing to magnetic north | `MAGNETOMETER` |
| Trip tracker | Distance travelled between GPS fixes | `LOCATION` sensor + state |
| Tilt parallax | A hero card that leans as you tilt | `GYROSCOPE` |
| Auto-rotate video | Fullscreen in landscape, inline in portrait | `DEVICE_ORIENTATION` |
| Pocket mode | Pauses playback when covered | `PROXIMITY` |
| Auto dark mode | Dims the page in low light (with hysteresis) | `LIGHT` |
| Signature pad | Rebuilds your strokes from touch gestures | `TOUCH_GESTURES` |
| Smart download | HD on Wi-Fi, SD on cellular, paused offline | `CONNECTIVITY` + `ACTIVE_NETWORK` |
| Battery saver | Saver < 20%, sync while charging, overheat warning | `BATTERY` |
| Privacy shield | Hides a balance when backgrounded/locked/screen-off | `APP_VISIBILITY` + `LOCK` + `SCREEN_STATE` |
| Volume HUD | Custom volume bar with a "too loud" hint | `VOLUME` |
| Locale adapt | Currency symbol and text direction follow the region | `LOCALE` |
| Nearby devices | Connected vs in-range Bluetooth devices | `BLE_CONNECTIONS` + `BLE_DISCOVERS` |

## Architecture

```
com.umain.aware
├── core/      SensorSource/StateSource abstractions, KSensor wrappers, Collect helpers, UI chrome
├── di/        one Koin module binding the abstractions to the KSensor implementations
├── feature/   one package per feature: a pure "deriver" + a dumb Compose screen
└── App.kt     the gallery registry + one-level back navigation
```

The design follows a few non-negotiable principles:

**DRY.** The register/collect/unregister lifecycle, the platform badge, and the metric/card chrome
are written once in `core` (`Collect.kt`, `Ui.kt`) and reused by every screen. No screen
re-implements sensor plumbing.

**SOLID.**
- *SRP* — each feature is split into a pure logic unit (a "deriver" such as `ShakeDetector`,
  `FloorCounter`, `Heading`, `DistanceAccumulator`, `ThemeDecider`, `DownloadPolicy`,
  `BatteryPolicy`, `ShieldPolicy`, `RegionFormat`) and a dumb Compose screen that renders it.
  Derivers contain **zero Compose, zero KSensor, zero platform code** — they take plain primitives,
  which is also why they are trivially unit-tested.
- *DIP* — screens and derivers depend only on the `SensorSource` / `StateSource` abstractions and on
  Aware's own domain types (`SensorType`, `Reading`, `StateReading`, …), never on the concrete
  `KSensor` / `KState` objects. The library is hidden behind those interfaces.
- *OCP* — adding a feature means adding a deriver + a screen + **one** entry in `awareFeatures()`. No
  existing feature is edited.
- *ISP* — `SensorSource` and `StateSource` are separate, so a screen that only needs states never
  transitively depends on sensor types.
- *LSP* — `FakeSensorSource` / `FakeStateSource` (in `commonTest`) are drop-in substitutes for the
  real sources and honour the same lifecycle contract.

**Koin — justified, not decorative.** `KSensor`/`KState` are global objects, so DI is not needed to
*reach* them. It earns its place by satisfying DIP: `di/CoreModule.kt` is the single place that binds
`SensorSource → KSensorSource` and `StateSource → KStateSource`; screens `koinInject()` the
abstractions, and tests swap the bindings for the fakes. One module is enough — we did not
over-modularise.

**No leaked listeners.** `KSensorSource`/`KStateSource` return cold flows that register on first
collection and `unregister`/`removeObserver` in `onCompletion`. Screens collect via `CollectSensors`
/`CollectStates`, which are scoped to composition, so leaving a screen cancels the collection and
provably tears the subscription down. `SourceLifecycleTest` asserts this register-once /
unregister-on-cancel contract against the fakes.

## Testing

Every deriver has `commonTest` coverage feeding inputs directly (no device needed): `ShakeDetector`
threshold + cooldown, `StepSession` baselining, `FloorCounter` conversion, `Heading` wrap-around,
`DistanceAccumulator` against a known two-point distance, `ThemeDecider` hysteresis, and the
`DownloadPolicy` / `BatteryPolicy` / `ShieldPolicy` / `RegionFormat` decision/mapping tables. Screens
are not unit-tested. `SourceLifecycleTest` covers the source lifecycle via the fakes.

## KSensor caveats

A few things about the library are worth recording, because they shaped the code:

1. **Coordinates.** The dependency is `io.github.shadadman:KSensor:3.80.0` (GitHub user `ShadAdman`).
   Note there is an older, similarly-named publication, `io.github.shadmanadman:KSensor` (latest
   `3.60.0`), with a slightly different permission API — Aware uses the `shadadman` `3.80.0`
   artifact, which matches this project's spec (Kotlin 2.2, `AskPermission(PermissionType.LOCATION)`,
   `timestamp` on updates, and the BLE state types).

2. **The package of the public types is not documented.** Every KSensor import is therefore confined
   to exactly three files — `core/KSensorSource.kt`, `core/KStateSource.kt`, and
   `core/LocationPermission.kt` — each behind a clearly marked import block. If the real package
   differs from the assumed `io.github.shadadman.ksensor`, the fix is those import lines only;
   nothing else in the app references a KSensor type, because the wrappers map the library's models
   onto Aware's own `Reading` / `StateReading` domain types.

3. **The `*.Error` field shape is not documented.** Errors are routed through the source layer as a
   generic, already-stringified message (`update.toString()`) and surfaced as `SensorUpdate.Error` /
   `StateUpdate.Error`. Screens may ignore them, but the abstraction does not pretend errors don't
   happen.

## Building

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/` in Xcode and run.

Targets: Android + iOS from a single `commonMain`, Material 3. The toolchain follows the project
scaffold (Kotlin 2.4 / Compose Multiplatform 1.11 / AGP 9). KSensor is built with Kotlin 2.2, which a
newer Kotlin compiler consumes without issue. A JDK 17+ is required to run Gradle.
