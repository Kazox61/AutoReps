# goldie — App Store & Play Store assets

Regenerates the store screenshots and preview videos: `goldie/out/` holds the
output (gitignored), everything that defines it is committed.

| File | Role |
|---|---|
| `goldie.config.ts` | Source of truth: scenes, headlines, backgrounds, bezel, store listing, preview story |
| `../.argent/flows/store-*.yaml` | One flow per screenshot scene + per preview segment |
| `out/` | Raw captures, framed screenshots, preview videos, studio data |

## Regenerating everything

```bash
# 1. iOS Release build (a Debug build is unusable for captures)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Release \
  -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath iosApp/build/DD build

# 2. Android debug APK (native Compose — paints no dev overlay, good enough)
./gradlew :androidApp:assembleDebug

# 3. Capture both stores, then frame and verify
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
export GOLDIE_CONFIG="$PWD/goldie/goldie.config.ts"
goldie doctor && goldie capture && goldie frame && goldie manifest \
  && goldie preview && goldie verify

# 4. Review at http://localhost:4321
goldie studio --no-open
```

Machine prerequisites: `brew` goldie (0.3.x) — use it, **not** `npx goldie`, so
the tool-server and the bundled argent version match — plus `ffmpeg`, the
`iPhone 17 Pro Max` simulator runtime, and a `Pixel10Pro` AVD
(`hw.device.name=pixel_10_pro`; goldie refuses other Pixel profiles).

## Gotchas baked into the flows

- The demo seed (`shared/src/commonMain/.../DemoSeed.kt`) only runs on
  simulator/emulator. goldie wipes app data before every flow, so the seed is
  what makes the screens look lived-in. Real devices never seed. The seeded
  workout names ("Morgen-Session", …) are user-content, not UI chrome — they
  read as German in both store languages, which is fine.
- argent's iOS flow tree cannot see Compose Multiplatform UI (it walks UIKit
  views, and Compose renders via Metal). Every flow therefore branches:
  `when: platform: android` uses measured coordinates + waits, `when:
  platform: ios` uses measured coordinates + waits. If you move an element,
  re-measure its centre — on iOS with argent `describe`, on Android with
  `adb shell uiautomator dump` — and update the flow's echo comment.
- The Android branches are **language-neutral on purpose**: they gate on
  locale-proof strings only — the seeded rep count "68", the seed name
  "Morgen-Session", and labels that are identical in both languages ("EMOM",
  "Tempo"). All taps are coordinates. Do not reintroduce UI-language text
  selectors.
- The flows pre-grant camera permission via `settings-permissions` before
  launching, so the record screen never shows the permission dialog mid-capture.
- `theme.layout: "classic"` is broken in goldie 0.3.1 (device never renders);
  the config uses hero / offset instead.

## Capturing both languages

The app speaks German and English (Compose Resources follows the device
locale), and `goldie.config.ts` carries `de-DE` + `en-US` store copy. goldie
0.3.1 captures raw **once per device**, pinned to `locales[0]` and only
overlays copy per locale — so per-language screenshots need two passes with
the one-locale configs in this directory:

```bash
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
export GOLDIE_CONFIG="$PWD/goldie/goldie.config.ts"

# 1. German pass: set both devices to German first
adb shell cmd locale set-device-locale de-DE
xcrun simctl spawn <sim-udid> defaults write -g AppleLanguages -array de
xcrun simctl spawn <sim-udid> defaults write -g AppleLocale -string de_DE
GOLDIE_CONFIG="$PWD/goldie/goldie.de.config.ts" goldie capture \
  && GOLDIE_CONFIG="$PWD/goldie/goldie.de.config.ts" goldie frame \
  && GOLDIE_CONFIG="$PWD/goldie/goldie.de.config.ts" goldie preview \
  && GOLDIE_CONFIG="$PWD/goldie/goldie.de.config.ts" goldie manifest

# 2. English pass: set both devices to English first
adb shell cmd locale set-device-locale en-US
xcrun simctl spawn <sim-udid> defaults write -g AppleLanguages -array en
xcrun simctl spawn <sim-udid> defaults write -g AppleLocale -string en_US
GOLDIE_CONFIG="$PWD/goldie/goldie.en.config.ts" goldie capture \
  && GOLDIE_CONFIG="$PWD/goldie/goldie.en.config.ts" goldie frame \
  && GOLDIE_CONFIG="$PWD/goldie/goldie.en.config.ts" goldie preview \
  && GOLDIE_CONFIG="$PWD/goldie/goldie.en.config.ts" goldie manifest

# 3. Re-run `goldie manifest` (and only manifest) with the main config so the
#    studio lists both locales. Never re-run `frame` with the main config —
#    it would render both locales from the last pass's raw captures.
```

Why the manual locale commands: goldie pins the iOS locale by writing the
device's `.GlobalPreferences.plist` file with the **host** `defaults`, but the
simulator's own cfprefsd never adopts that write — the app keeps rendering the
previous language. Writing through `simctl spawn defaults` updates the runtime
state goldie's file check then agrees with. goldie does not touch Android
locale at all, and Android's per-app locale (`cmd locale set-app-locales`)
does not survive goldie's uninstall-per-flow, so the device locale is set
instead (`adb shell cmd locale set-device-locale <tag>`). Both writes survive
reinstall and clear-data.

## Adding a locale (translations)

The app and the store copy speak German and English; a third language needs:

1. Compose Resources strings in `shared/src/commonMain/composeResources/values-<lang>/`,
   declared in `iosApp/iosApp/Info.plist` (`CFBundleLocalizations`).
2. A locale key on every copy record in `goldie.config.ts` (`headline`,
   `subhead`, `store.*`) and the code in `locales`.
3. A new one-locale pass config (`goldie.<lang>.config.ts`) and a line in the
   two-pass loop above; keep the flows' gate strings ("68", "Morgen-Session",
   "EMOM", "Tempo") identical in the new language, or replace them with
   language-proof ones.

Manual flow runs (kept from the old notes — use the brew goldie's own argent,
or the devtools handshake fails):
`node /opt/homebrew/lib/node_modules/goldie/node_modules/@swmansion/argent/dist/cli.js run flow-execute --name <flow> --project_root <repo> --device <udid>`
— flows with an `executionPrerequisite` additionally need
`--prerequisiteAcknowledged true`.
