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
  what makes the screens look lived-in. Real devices never seed.
- argent's iOS flow tree cannot see Compose Multiplatform UI (it walks UIKit
  views, and Compose renders via Metal). Every flow therefore branches:
  `when: platform: android` uses text selectors, `when: platform: ios` uses
  measured coordinates + waits. If you move an iOS element, re-measure its
  centre with argent `describe` and update the flow's echo comment.
- Use the brew goldie's own argent for manual flow runs, or the devtools
  handshake fails:
  `node /opt/homebrew/lib/node_modules/goldie/node_modules/@swmansion/argent/dist/cli.js run flow-execute --name <flow> --project_root <repo> --device <udid>`
- `theme.layout: "classic"` is broken in goldie 0.3.1 (device never renders);
  the config uses hero / offset instead.
- The flows pre-grant camera permission via `settings-permissions` before
  launching, so the record screen never shows the permission dialog mid-capture.

## Adding a locale (translations)

The app currently renders German only; store copy in `goldie.config.ts` is
already structured for more locales:

1. When the app itself speaks another language, add its locale key to every
   copy record in `goldie.config.ts` (`headline`, `subhead`, `store.*`) and add
   the code to `locales`.
2. goldie then re-captures and re-frames per locale automatically.
3. The flows' **Android** text selectors (`Heute`, `Verwerfen`, …) match the
   app's UI language. Either record a per-locale flow set, or (better, once the
   app sets test tags) switch those selectors to `id:` so one flow serves every
   locale. The iOS branches are coordinate- and wait-based already and need no
   translation.
4. `store-01-home` and `store-preview-01-home` await `Noch 32 Wiederholungen` —
   that string comes from the demo seed's fixed 68/100 goal; keep it in sync
   with the locale's UI strings.
