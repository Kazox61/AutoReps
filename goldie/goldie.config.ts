import { fileURLToPath } from "node:url";
import path from "node:path";
import type { GoldieConfig } from "/opt/homebrew/lib/node_modules/goldie/dist/config.d.ts";

// goldie/ holds this config; the app repo is its parent directory.
const APP_ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");

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
  // The app ships German and English (Compose Resources follows the device
  // locale). Raw captures happen once per device per pass — see
  // goldie.de.config.ts / goldie.en.config.ts and README "Capturing both languages".
  locales: ["de-DE", "en-US"],
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
    subtitle: {
      "de-DE": "Liegestütze automatisch zählen",
      "en-US": "Count push-ups automatically",
    },
    developer: "Kazox",
    category: "Health & Fitness",
    rating: 5,
    ratingCount: "12 Bewertungen",
    ageRating: "4+",
    price: "Kostenlos",
    description: {
      "de-DE":
        "AutoReps zählt deine Liegestütze – automatisch, per Kamera. Handy auf den Boden, loslegen: kein Tracker am Arm, kein Mitzählen, kein Video.\n\n" +
        "Behalte Tagesziel, Serie und Fortschritt im Blick. Analysiere Tempo, Sätze und Pausen jeder Session und werde Workout für Workout stärker.",
      "en-US":
        "AutoReps counts your push-ups – automatically, with the camera. Put your phone on the floor and go: no tracker on your arm, no counting, no video.\n\n" +
        "Keep your daily goal, streak and progress in view. Analyze pace, sets and breaks of every session and get stronger workout by workout.",
    },
  },

  scenes: [
    {
      kind: "screenshot",
      id: "home",
      flow: "store-01-home",
      layout: "hero",
      headline: { "de-DE": "Immer wissen, wo du stehst", "en-US": "Always know where you stand" },
      subhead: { "de-DE": "Tagesziel, Serie und Fortschritt auf einen Blick.", "en-US": "Daily goal, streak and progress at a glance." },
    },
    {
      kind: "screenshot",
      id: "record",
      flow: "store-02-record",
      headline: { "de-DE": "Einfach loslegen", "en-US": "Just get started" },
      subhead: { "de-DE": "Die Kamera zählt mit – du machst nur die Liegestütze.", "en-US": "The camera counts — you just do the push-ups." },
    },
    {
      kind: "screenshot",
      id: "analysis",
      flow: "store-03-analysis",
      headline: { "de-DE": "Werde Session für Session stärker", "en-US": "Stronger session by session" },
      subhead: { "de-DE": "Tempo, Abfall und Pausen – jede Session ausgewertet.", "en-US": "Pace, drop-off and breaks — every session analyzed." },
    },
    {
      kind: "screenshot",
      id: "history",
      flow: "store-04-history",
      layout: "offset",
      headline: { "de-DE": "Nichts geht verloren", "en-US": "Nothing gets lost" },
      subhead: { "de-DE": "Jede Session mit Datum, Dauer und Wiederholungen.", "en-US": "Every session with date, duration and reps." },
    },
    {
      kind: "screenshot",
      id: "settings",
      flow: "store-05-settings",
      layout: "hero",
      headline: { "de-DE": "Dein Training, deine Regeln", "en-US": "Your training, your rules" },
      subhead: { "de-DE": "Tagesziel, Satzpause und EMOM – alles verstellbar.", "en-US": "Daily goal, rest time and EMOM — all adjustable." },
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
