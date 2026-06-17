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

## Running it

- Android: `./gradlew :androidApp:assembleDebug`
- iOS: open `iosApp/` in Xcode and run it.

Use a real device for most of this. Emulators have no barometer, cannot be shaken, and have never
climbed a flight of stairs in their lives.
