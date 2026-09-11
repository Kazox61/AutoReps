import type { GoldieConfig } from "/opt/homebrew/lib/node_modules/goldie/dist/config.d.ts";

const APP_ROOT = "/Users/florianpawelka/dev/AutoReps2";

const config: GoldieConfig = {
  appRoot: APP_ROOT,
  // Release simulator build (xcodebuild -configuration Release, derivedDataPath iosApp/build/DD).
  appPath: `${APP_ROOT}/iosApp/build/DD/Build/Products/Release-iphonesimulator/AutoReps.app`,
  bundleId: "com.kazox.autoreps.AutoReps",

  // Google Play alongside the App Store. The debug APK is fine here: native
  // Compose app, no dev overlay.
  android: {
    appPath: `${APP_ROOT}/androidApp/build/outputs/apk/debug/androidApp-debug.apk`,
    applicationId: "com.kazox.autoreps",
  },

  devices: ["iphone-6.9", "pixel-10-pro"],
  // The app ships German-only, so the store copy is German too.
  locales: ["de-DE"],
  appearance: "light",

  // Silver bezel — the app is a monochrome zinc design, silver contrasts without competing.
  frame: { variant: "17-pro-silver" },

  theme: {
    // Warm neutral gradient, quiet like the app; near-black copy passes contrast.
    background: "linear-gradient(160deg, #FAFAF9 0%, #F0F0EE 55%, #E4E4E1 100%)",
    headlineColor: "#18181B",
    subheadColor: "#52525B",
    fontFamily: '-apple-system, "SF Pro Display", system-ui, sans-serif',
    // "classic" is broken in goldie 0.3.1 (device never renders); hero + friends all work.
    layout: "hero",
  },

  store: {
    name: "AutoReps",
    subtitle: { "de-DE": "Liegestütze automatisch zählen" },
    developer: "Kazox",
    category: "Health & Fitness",
    rating: 5,
    ratingCount: "12 Ratings",
    ageRating: "4+",
    price: "Free",
    description: {
      "de-DE":
        "AutoReps zählt deine Liegestütze – automatisch, per Kamera. Handy auf den Boden, loslegen: kein Tracker am Arm, kein Mitgezählt, kein Video.\n\n" +
        "Behalte Tagesziel, Serie und Fortschritt im Blick. Analysiere Tempo, Sätze und Pausen jeder Session und werde Workout für Workout stärker.",
    },
  },

  scenes: [
    {
      kind: "screenshot",
      id: "home",
      flow: "store-01-home",
      layout: "hero",
      headline: { "de-DE": "Immer wissen, wo du stehst" },
      subhead: { "de-DE": "Tagesziel, Serie und Fortschritt auf einen Blick." },
    },
    {
      kind: "screenshot",
      id: "record",
      flow: "store-02-record",
      headline: { "de-DE": "Einfach loslegen" },
      subhead: { "de-DE": "Die Kamera zählt mit – du machst nur die Liegestütze." },
    },
    {
      kind: "screenshot",
      id: "analysis",
      flow: "store-03-analysis",
      headline: { "de-DE": "Werde Session für Session stärker" },
      subhead: { "de-DE": "Tempo, Abfall und Pausen – jede Session ausgewertet." },
    },
    {
      kind: "screenshot",
      id: "history",
      flow: "store-04-history",
      layout: "offset",
      headline: { "de-DE": "Nichts geht verloren" },
      subhead: { "de-DE": "Jede Session mit Datum, Dauer und Wiederholungen." },
    },
    {
      kind: "screenshot",
      id: "settings",
      flow: "store-05-settings",
      layout: "hero",
      headline: { "de-DE": "Dein Training, deine Regeln" },
      subhead: { "de-DE": "Tagesziel, Satzpause und EMOM – alles verstellbar." },
    },
    {
      kind: "preview",
      id: "preview",
      segments: [
        { id: "home", flow: "store-preview-01-home" },
        { id: "record", flow: "store-preview-02-record" },
        { id: "analysis", flow: "store-preview-03-analysis", holdSeconds: 1 },
      ],
    },
  ],
};

export default config;
