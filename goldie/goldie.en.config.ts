import config from "./goldie.config.ts";

// One-locale view of goldie.config.ts for the English capture pass. goldie
// captures raw once per device (pinned to locales[0]), so per-language
// screenshots need two passes; see goldie/README.md.
config.locales = ["en-US"];

export default config;
