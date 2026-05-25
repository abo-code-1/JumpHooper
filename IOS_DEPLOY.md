# Getting Star Jump onto the iPhone — resume notes

> For a future Claude session (and me): where we are and exactly what to do next.
> The game is fully built and committed on the `enhancements` branch. The only
> thing left is the physical install onto the iPhone.

## TL;DR
1. Build the IPA (needs **JDK 17**, not the default JDK 23):
   ```
   cd StarJumpLibGDX
   JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew ios:createIPA -Probovm.iosSkipSigning=true
   ```
   → produces `ios/build/robovm/StarJump.ipa`
2. Make sure the iPhone is on **USB** (see blocker below):
   ```
   system_profiler SPUSBDataType | grep -i iPhone      # must show "iPhone ... Apple Inc."
   ```
3. Open **Sideloadly** (`/Applications/Sideloadly.app`), drag in the IPA, enter the
   Apple ID (`abbror2006@gmail.com`), select the **iPhone** in the iDevice dropdown
   (NOT "Apple Silicon" — that's the Mac), click **Start**.
4. On the phone: **Settings → General → VPN & Device Management → trust the cert**;
   enable **Developer Mode** if asked. Then tap **Star Jump** and tilt to play.

## Why Sideloadly (not Xcode)
The installed **Xcode is 16.2** (max iOS SDK 18.2). The iPhone is on **iOS 26.2.1**,
which Xcode 16.2 is far too old to deploy to — so the native `ios:launchIOSDevice`
path and Xcode "Run on device" don't work. Sideloadly does its own free-Apple-ID
signing and works regardless of the Xcode version. (If Xcode gets updated to an
iOS-26-capable version, `JAVA_HOME=$(/usr/libexec/java_home -v 17) ./gradlew
ios:launchIOSDevice` becomes a valid alternative.)

## ⚠️ Current blocker: flaky USB connection
Sideloadly needs the iPhone on a **USB data cable** (a Wi-Fi connection — i.e.
`iPhone.coredevice.local` in `xcrun devicectl list devices` — does NOT count).
The phone enumerated once with the **in-box USB-C cable** but kept dropping; the
USB bus usually reads empty. Most likely: a charge-only/marginal cable, a loose
seat, or **lint in the USB-C port**.

To get unstuck:
- Use the **white in-box USB-C cable**, plugged straight into the Mac (no adapter/hub).
- **Clean the iPhone's USB-C port** (lint is the #1 cause of "works once then drops").
- **Reboot the iPhone** (clears USB enumeration glitches), keep it unlocked.
- Verify it holds: `system_profiler SPUSBDataType | grep -i iPhone`.

Once it holds steady for ~30s, the Sideloadly step is one click.

## Facts / config
- iPhone 15, iOS 26.2.1, UDID `00008120-000A351C3E3BA01E`.
- JDK 17: `/usr/libexec/java_home -v 17` (Homebrew temurin/openjdk@17). RoboVM does NOT support JDK 23.
- `ios/robovm.xml` uses `<arch>arm64</arch>`; `ios/build.gradle` includes `gdx-freetype-platform:natives-ios`; RoboVM plugin `2.3.24`.
- The **desktop** build always works: `./gradlew lwjgl3:run`. Logic test: `./gradlew smokeTest`.
- Branches: `enhancements` = this game (live). `main` = the user's older JumpHooper project (restored on their request — leave it alone). Remote: `github.com/abo-code-1/JumpHooper`.
