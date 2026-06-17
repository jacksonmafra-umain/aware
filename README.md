# Aware

Sixteen everyday phone features, each rebuilt around the sensor that does the actual work — a demo
for the KSensor library. Mostly it's proof that your phone knows more about you than you'd like:
what it's near, how it's being held, how much light you're sitting in, and precisely how few steps
you've managed today.

Android and iOS from a single codebase, because writing the whole thing twice is a punishment, not
an architecture.

## The demos

- **Shake to report** — accelerometer. Shake the phone like you're trying to wake it up; it files a
  bug report. Finally, a productive outlet for the rage.

- **Daily step goal** — step counter and detector. Counts your steps toward a goal you won't reach.
  The little "walking now" indicator is likely the most exercise it'll witness.

- **Floors climbed** — barometer. Counts floors from changes in air pressure. It cannot tell that
  you took the lift, and, in fairness, neither can your conscience.

- **Compass** — magnetometer. It points north. Genuinely revolutionary in 1300; these days it
  mostly comes alive the moment you lose signal somewhere green.

- **Trip tracker** — GPS. Adds up how far you've travelled between location fixes. Ideal for a run,
  or for confirming you walked to the fridge and back with real conviction.

- **Tilt marble** — accelerometer. Roll a marble around a box by tilting the phone. Decades of
  sensor research, and we've lovingly recreated the toy from a Christmas cracker.

- **Auto-rotate video** — device orientation. Goes fullscreen when you turn the phone sideways. The
  one time rotation does exactly what you wanted, unlike every other moment of your day.

- **Pocket mode** — proximity. Cover the top of the phone and playback pauses. The same trick that
  mutes you mid-sentence on every call you've ever been on.

- **Auto dark mode** — light sensor. Dims the page when the room goes dark, with a buffer so it
  doesn't flicker like a cheap disco. For reading in bed and ruining your sleep responsibly.

- **Signature pad** — touch. Sign with your finger. As legally binding, and as legible, as every
  signature you have ever produced.

- **Smart download** — connectivity and active network. HD on Wi-Fi, SD on mobile data, nothing at
  all offline. It respects your data plan rather more than your provider does.

- **Battery saver** — battery. Flips to saver mode under 20%, runs sync while charging, and warns
  you when it's overheating. It will not, however, stop you watching one more video at 3%.

- **Privacy shield** — app visibility, lock and screen state. Hides your bank balance the instant
  the app loses focus. It can protect the number. It cannot protect you from the number.

- **Volume HUD** — volume. A custom bar with a "too loud" warning past 80%. You'll ignore it now and
  complain about your ears later, on schedule.

- **Locale adapt** — locale. Swaps the currency symbol and flips the layout for right-to-left
  languages based on your region. Quietly competent, and a little smug about it.

- **Nearby devices** — Bluetooth. Lists devices connected versus merely in range. A "find my
  headphones" that still won't find the one that's under the sofa.

## KSensor, the library this is demoing

 KSensor a Kotlin Multiplatform library (`io.github.shadadman:KSensor:3.80.0`) that hands you live sensor and
device-state data on Android and iOS. 
 
Its documentation is, generously, aspirational, so here's the part that actually matters.

Add it to your `commonMain`:

```kotlin
implementation("io.github.shadadman:KSensor:3.80.0")
```

Mind the package. The Maven coordinate says `shadadman`; the classes live under `org.kmp.ksensor`
(`.sensor`, `.state`, `.permission`). 

The two have nothing to do with each other, naturally.

**Sensors**

```kotlin
KSensor.registerSensors(types, locationIntervalMillis = 1000L) // Flow<SensorUpdate>
KSensor.unregisterSensors(types)
KSensor.AskPermission(PermissionType.LOCATION) { status -> }    // @Composable
```

`SensorType`: ACCELEROMETER, GYROSCOPE, MAGNETOMETER, BAROMETER, STEP_COUNTER, STEP_DETECTOR,
LOCATION, DEVICE_ORIENTATION, PROXIMITY, LIGHT, TOUCH_GESTURES. Each emission is a
`SensorUpdate.Data(type, data, platformType, timestamp)` or a `SensorUpdate.Error(exception)`, where
`data` is nested in `SensorData` (e.g. `SensorData.Accelerometer`).

**States**

```kotlin
KState.addObserver(types) // Flow<StateUpdate>
KState.removeObserver(types)
```

`StateType`: SCREEN, APP_VISIBILITY, CONNECTIVITY, ACTIVE_NETWORK, LOCATION, VOLUME, LOCALE, BATTERY,
LOCK, BLE_CONNECTIONS, BLE_DISCOVERS. Emissions are `StateUpdate.Data(type, data, platformType)` or
`StateUpdate.Error(exception)`, with payloads nested in `StateData`. `PermissionStatus` is an enum:
GRANTED, DENIED, SHOW_RATIONAL, UNKNOWN.

**Things it won't tell you itself, learned the hard way (all true as of 3.80.0)**

- The Android accelerometer is divided by the sensor's maximum range, so the values aren't m/s², just
  a fraction of it. Use the direction, not the magnitude — see the Tilt marble.
- `VolumeStatus.volumePercentage` is the raw stream index, roughly 0–15, not a percentage. The field
  name is a work of fiction.
- Subscribing to more than one `StateType` in a single `addObserver` call crashes: it runs
  `awaitClose` once per type. Observe one at a time and merge.
- `TOUCH_GESTURES` emits nothing. The library never initialises its touch monitor, and the class is
  `internal`, so neither can you. The Signature pad falls back to Compose's own touch input.
- It requests no permissions for you. CONNECTIVITY/ACTIVE_NETWORK need `ACCESS_NETWORK_STATE`,
  STEP_COUNTER needs `ACTIVITY_RECOGNITION`, and BLE needs the runtime Bluetooth permissions.

If a future version fixes any of that, the list above doubles as your migration guide.

## Running it

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/` in Xcode and run it.

Use a real device for most of this. Emulators have no barometer, cannot be shaken, and have never
climbed a flight of stairs in their lives.

## Credits

KSensor — the library doing the actual sensing — is by
[Shadman Adman](https://github.com/ShadAdman/KSensor), and is licensed 0BSD, which is about as
permissive as a license gets before it's just a sticky note reading "do whatever." Quirks aside, it
saved writing a great deal of platform glue, twice.

Aware was made with love and tokens by [Jackson Mafra](https://github.com/jacksonmafra-umain). Source
lives at <https://github.com/jacksonmafra-umain/aware>.

## License

MIT — see [LICENSE](LICENSE). Do what you like with it; just don't come back when the marble rolls
the wrong way.
