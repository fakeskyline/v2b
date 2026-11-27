# v2b — Chromium browser fork

**Browser engine base: [Bare Browser](https://github.com/BareBrowser/bare-browser), pinned at `019a612f7bb758da20c33ecf43c086e696391aec`.**

The previous Kotlin/Compose WebView prototype is not the browser foundation and does not implement Chrome extensions. v2b is moving to Bare's Chromium source/patch build, preserving the real Chromium engine and extension runtime instead of faking CRX support.

## Upstream
- Source: https://github.com/BareBrowser/bare-browser
- Pinned upstream commit: 019a612f7bb758da20c33ecf43c086e696391aec
- Chromium 158 series; Bare 2.0.9 upstream
- Supports Chrome Web Store extensions, CRX files and unpacked extensions; incognito permissions; Chromium tabs/runtime.

The upstream source is linked as a Git submodule at `upstream/bare-browser`. This pin is intentional: updates should be reviewed and merged deliberately.

## v2b customizations (planned/in progress)
- Branding: v2b icon and logotype
- Geological typeface
- v2b start page and DuckDuckGo search experience
- v2b navigation/toolbar appearance

## Build reality
Bare builds Chromium from a pinned source checkout and patch series using its Linux Docker builder. It is **not** a small Gradle dependency and cannot be built with the current simple Android Actions workflow. The old WebView APK workflow remains only for the prototype while the Chromium-based build pipeline is being established; it is not a v2b browser release.

See upstream `BUILDING.md` and `docs/build-from-source.md` for prerequisites and the build process.
