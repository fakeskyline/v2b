# v2b Android browser

Kotlin + Jetpack Compose starter for package `com.v2b`.

This first build uses Android System WebView (Chromium-based). Search opens DuckDuckGo HTML directly in the browser, avoiding brittle API scraping. It includes the v2b home screen, address/search field, navigation, simple in-app tabs, and an Extensions page.

**CRX caveat:** System WebView does not include Chrome's extension runtime. CRX installation/execution is not implemented in this starter. A dedicated extension-capable Chromium fork is a much larger native build integration.

## Build
Open in Android Studio with SDK 35 and Java 17, or run `gradle assembleDebug` with Gradle 8.9 installed. APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions
Push to `main` or manually run the `Build v2b APK` workflow. Download the `v2b-debug-apk` artifact.

Assets from the attached v2b ZIP: launcher icon, start-page logotype and Geological font.
