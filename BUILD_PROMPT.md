# Build Prompt — "Aware": a KSensor showcase in Compose Multiplatform

## Objective

Implement **Aware**, a Compose Multiplatform demo app that showcases the
**KSensor** library (`io.github.shadadman:KSensor:3.80.0`) by framing each
sensor/state around the everyday product feature it powers — not around the raw
reading. Ship one gallery of sixteen self-contained feature demos that together
exercise every sensor and every state type the library exposes.

Targets: Android and iOS, single `commonMain` codebase, Material3, Kotlin
2.2.x, Gradle 8.13. Package root and Android `applicationId`/`namespace`:
`com.umain.aware`.

## Non-negotiable engineering principles

Apply these throughout; they shape the file layout and the commit plan below.

**DRY.** The register/unregister lifecycle, the platform badge, the metric/card
chrome, and the "latest value of one type" pattern are written once in a core
module and reused by every screen. No screen re-implements sensor plumbing.

**SOLID.**
- *SRP*: split each feature into a pure logic unit (a "deriver" — transforms raw
  readings into feature state) and a dumb Compose screen that renders it. Derivers
  contain zero Compose and zero platform code.
- *DIP*: screens and derivers depend on the abstractions `SensorSource` /
  `StateSource`, never on the concrete `KSensor` / `KState` objects. The library
  is an implementation detail hidden behind those interfaces.
- *OCP*: adding a feature means adding a deriver + a screen + one registry entry;
  it must not require editing existing features.
- *ISP*: keep `SensorSource` and `StateSource` separate — a screen that only
  needs states must not transitively depend on sensor types.
- *LSP*: ship a `FakeSensorSource` / `FakeStateSource` for tests that are drop-in
  substitutes for the real ones.

**Koin — justified, not decorative.** `KSensor`/`KState` are global objects, so DI
is not required to *reach* them. It is required to satisfy DIP and keep derivers
testable: bind `SensorSource` → `KSensorSource` and `StateSource` → `KStateSource`
in a single `coreModule`, and inject the abstractions into the Compose layer via
`koinInject()`. Tests swap the bindings for the fakes. One core module is enough;
do not over-modularise.

**Microcommits.** Work commit-by-commit. Every commit is one atomic logical
change, compiles on its own, and uses a Conventional Commits message. The ordered
plan is in the final section — follow it.

## Module / package layout

```
com.umain.aware
├── core/
│   ├── SensorSource.kt        // interface: fun sensors(types, intervalMs?): Flow<SensorUpdate>
│   ├── StateSource.kt         // interface: fun states(types): Flow<StateUpdate>
│   ├── KSensorSource.kt       // wraps KSensor; emits, unregisters on cancellation
│   ├── KStateSource.kt        // wraps KState; emits, removes observer on cancellation
│   ├── Collect.kt             // CollectSensors / CollectStates / rememberSensor / rememberState
│   └── Ui.kt                  // PlatformBadge, MetricText, shared chrome
├── di/
│   └── CoreModule.kt          // Koin module + appModules() entry
├── feature/
│   ├── shake/ShakeDetector.kt + ShakeToReportScreen.kt
│   ├── steps/StepSession.kt + StepGoalScreen.kt
│   ├── floors/FloorCounter.kt + FloorClimbScreen.kt
│   ├── compass/Heading.kt + CompassScreen.kt
│   ├── trip/DistanceAccumulator.kt + TripTrackerScreen.kt
│   ├── tilt/TiltParallaxScreen.kt
│   ├── rotate/AutoRotateScreen.kt
│   ├── pocket/PocketModeScreen.kt
│   ├── light/ThemeDecider.kt + AutoDarkModeScreen.kt
│   ├── pad/SignaturePadScreen.kt
│   ├── network/DownloadPolicy.kt + SmartDownloadScreen.kt
│   ├── battery/BatteryPolicy.kt + BatterySaverScreen.kt
│   ├── privacy/ShieldPolicy.kt + PrivacyShieldScreen.kt
│   ├── volume/VolumeHudScreen.kt
│   ├── locale/RegionFormat.kt + LocaleAdaptScreen.kt
│   └── ble/NearbyDevicesScreen.kt
└── App.kt                     // gallery registry + back-stack navigation
```

Platform entry points (`MainActivity` on Android, `MainViewController` on iOS)
only call `App()` after Koin is started.

## KSensor API contract (the only surface that may be used)

Use exactly this surface, taken from the library README. Do not invent methods,
parameters, or fields beyond it.

```
KSensor.registerSensors(types: List<SensorType>, locationIntervalMillis: Long = …): Flow<SensorUpdate>
KSensor.unregisterSensors(types: List<SensorType>)
KSensor.AskPermission(PermissionType.LOCATION) { status: PermissionStatus -> }   // composable
KState.addObserver(types: List<StateType>): Flow<StateUpdate>
KState.removeObserver(types: List<StateType>)

SensorUpdate.Data { data, platformType, timestamp }   SensorUpdate.Error
StateUpdate.Data  { data, platformType }              StateUpdate.Error
```

Sensor data models: `Accelerometer(x,y,z)`, `Gyroscope(x,y,z)`,
`Magnetometer(x,y,z)`, `Barometer(pressure)`, `StepCounter(steps)`,
`StepDetector()`, `Location(lat?,lon?,alt?)`, `Orientation(orientation,orientationInt)`,
`Proximity(distanceInCM,isNear)`, `LightIlluminance(illuminance)`,
`TouchGestures(x,y,type)`.

State data models: `AppVisibilityStatus(isAppVisible)`, `LocationStatus(isLocationOn)`,
`ScreenStatus(isScreenOn)`, `LockStatus(isDeviceLocked)`,
`CurrentActiveNetwork(activeNetwork)`, `ConnectivityStatus(isConnected)`,
`VolumeStatus(volumePercentage)`,
`LocaleInfo(languageCode,countryCode,fullLocaleString,displayName,isRTL)`,
`BatteryStatus(levelPercent?,chargingState,health?,temperatureC?)` with
`ChargingState{UNKNOWN,DISCHARGING,CHARGING,FULL}` and
`BatteryHealth{UNKNOWN,GOOD,OVERHEAT,DEAD,OVER_VOLTAGE,UNSPECIFIED_FAILURE,COLD}`,
`BleConnectionStatus(connectedDevices)`, `BleDiscoversStatus(discoveredDevices)`,
`BleDevice(id,name)`.

### Two documented unknowns — handle explicitly

1. **Package of the types is not documented.** Centralise the import in one place
   (e.g. a single `import io.github.shadadman.ksensor.*` per file, or `typealias`es
   in core) so it is a one-line fix once confirmed. Add a short README note about it.
2. **`*.Error` field shape is not documented.** Route errors through the source
   layer as a generic message (`update.toString()` is acceptable) and expose an
   optional error channel; screens may ignore it, but the abstraction must not
   pretend errors don't exist.

## Feature catalogue (real-world framing + derive logic)

Each feature renders the *product* behaviour, not raw axes.

- **Shake to report** (`ACCELEROMETER`) — `ShakeDetector`: g-force magnitude minus
  gravity, threshold + cooldown, emits discrete shake events. Screen counts filed reports.
- **Daily step goal** (`STEP_COUNTER`+`STEP_DETECTOR`) — `StepSession`: baseline the
  first counter reading, expose steps-since-start vs goal; detector events only light
  a "walking now" indicator.
- **Floors climbed** (`BAROMETER`) — `FloorCounter`: barometric altitude vs first
  reading, one floor per ~3 m.
- **Compass** (`MAGNETOMETER`) — `Heading`: `atan2(y,x)` normalised to 0–360°. Screen
  draws a needle.
- **Trip tracker** (`LOCATION` sensor + `LOCATION` state + `AskPermission`) —
  `DistanceAccumulator`: haversine sum between fixes at a fixed cadence; surface a
  "GPS is off" warning from the state; request permission up front.
- **Tilt parallax** (`GYROSCOPE`) — integrate+decay angular velocity into a small
  rotation applied to a hero card.
- **Auto-rotate video** (`DEVICE_ORIENTATION`) — map orientation → "fullscreen" vs
  "inline portrait".
- **Pocket mode** (`PROXIMITY`) — `isNear` pauses playback (cover-to-pause / ear mute).
- **Auto dark mode** (`LIGHT`) — `ThemeDecider`: lux thresholds with hysteresis to
  avoid flicker; recolours a reader pane.
- **Signature pad** (`TOUCH_GESTURES`) — build a `Path` from the gesture stream; match
  the gesture type defensively by string so it compiles regardless of enum names.
- **Smart download** (`CONNECTIVITY`+`ACTIVE_NETWORK`) — `DownloadPolicy`: offline →
  paused, Wi-Fi → HD, cellular → SD.
- **Battery saver** (`BATTERY`) — `BatteryPolicy`: <20% & not charging → saver on;
  charging → run deferred sync; temperature >40 °C → overheat warning.
- **Privacy shield** (`APP_VISIBILITY`+`LOCK`+`SCREEN_STATE`) — `ShieldPolicy`: hide a
  bank balance whenever the app is backgrounded, locked, or screen off.
- **Volume HUD** (`VOLUME`) — custom overlay bar + "too loud" hint above 80%.
- **Locale adapt** (`LOCALE`) — `RegionFormat`: country → currency symbol; reflect
  layout direction from `isRTL`.
- **Nearby devices** (`BLE_DISCOVERS`+`BLE_CONNECTIONS`) — list connected vs in-range
  `BleDevice`s, a find-my-headphones screen.

## Testing

Every deriver is a pure, multiplatform-safe unit with `commonTest` coverage using
`FakeSensorSource`/`FakeStateSource` or direct input feeding: ShakeDetector
threshold+cooldown, StepSession baselining, FloorCounter conversion, Heading
wrap-around, DistanceAccumulator against a known two-point distance, ThemeDecider
hysteresis, DownloadPolicy/BatteryPolicy/ShieldPolicy decision tables, RegionFormat
mapping. Screens are not unit-tested.

## Definition of done

App builds for both targets; gallery lists sixteen demos; each demo registers its
sensors on entry and is provably unregistered on exit (no leaked listeners); all
derivers have passing tests; only the documented API surface is used; the package
unknown and error-shape unknown are handled as specified; README explains the name,
the architecture, and the two caveats.

## Microcommit plan (ordered, Conventional Commits)

Each line is one commit. Each must compile. Do not batch.

**Setup**
1. `chore: scaffold Compose Multiplatform app (android + ios, material3)`
2. `build: add KSensor 3.80.0 and Koin dependencies to commonMain`
3. `chore: set namespace/applicationId com.umain.aware and location permissions`

**Core abstraction + DI**
4. `feat(core): define SensorSource and StateSource interfaces`
5. `feat(core): implement KSensorSource wrapping KSensor with lifecycle cleanup`
6. `feat(core): implement KStateSource wrapping KState with lifecycle cleanup`
7. `feat(di): add Koin coreModule and startKoin entry point`
8. `feat(core): add CollectSensors/CollectStates and rememberSensor/rememberState`
9. `test(core): add FakeSensorSource/FakeStateSource and lifecycle tests`

**Shell**
10. `feat(ui): add shared chrome (PlatformBadge, MetricText)`
11. `feat(ui): add App gallery registry with back navigation`
12. `feat(ui): wire android and ios entry points to App()`

**Features — logic before UI; one feature per pair**
13. `feat(shake): add ShakeDetector with tests`
14. `feat(shake): add ShakeToReportScreen and register in gallery`
15. `feat(steps): add StepSession with tests`
16. `feat(steps): add StepGoalScreen and register in gallery`
17. `feat(floors): add FloorCounter with tests`
18. `feat(floors): add FloorClimbScreen and register in gallery`
19. `feat(compass): add Heading with tests`
20. `feat(compass): add CompassScreen and register in gallery`
21. `feat(trip): add DistanceAccumulator with tests`
22. `feat(trip): add TripTrackerScreen with permission and location-state warning`
23. `feat(light): add ThemeDecider with tests`
24. `feat(light): add AutoDarkModeScreen and register in gallery`
25. `feat(network): add DownloadPolicy with tests`
26. `feat(network): add SmartDownloadScreen and register in gallery`
27. `feat(battery): add BatteryPolicy with tests`
28. `feat(battery): add BatterySaverScreen and register in gallery`
29. `feat(privacy): add ShieldPolicy with tests`
30. `feat(privacy): add PrivacyShieldScreen and register in gallery`
31. `feat(locale): add RegionFormat with tests`
32. `feat(locale): add LocaleAdaptScreen and register in gallery`
33. `feat(tilt): add TiltParallaxScreen and register in gallery`
34. `feat(rotate): add AutoRotateScreen and register in gallery`
35. `feat(pocket): add PocketModeScreen and register in gallery`
36. `feat(pad): add SignaturePadScreen and register in gallery`
37. `feat(volume): add VolumeHudScreen and register in gallery`
38. `feat(ble): add NearbyDevicesScreen and register in gallery`

**Wrap-up**
39. `docs: add README (name rationale, architecture, KSensor caveats)`
40. `chore: final pass — confirm no leaked listeners and both targets build`
