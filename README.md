# Stash

A cross-platform **pantry & food-storage tracker** built with **Kotlin Multiplatform**.
Know what you have, how much is left, and what expires soon — before it becomes waste.

- **Android** — Jetpack Compose
- **iOS** — SwiftUI

## Features

- **Dashboard** — items expiring in 3 / 7 / 30 days at a glance
- **Inventory** — quantities, locations and expiry dates for everything in your stash
- **Barcode scanning** — scan with the camera; product data autofills via
  [Open Food Facts](https://world.openfoodfacts.org/)
- **Expiry notifications** — local reminders before food goes bad
- **Shared core** — one Kotlin business-logic layer for both platforms

## Architecture

```
┌─────────────────┐     ┌─────────────────┐
│  androidApp     │     │  iosApp         │
│  Jetpack Compose│     │  SwiftUI        │
└────────┬────────┘     └────────┬────────┘
         └──────────┬────────────┘
              ┌─────▼─────┐
              │  :shared  │   Kotlin Multiplatform
              │ SQLDelight│   Ktor · kotlinx-serialization
              │   Koin    │   coroutines
              └───────────┘
```

- **Persistence**: SQLDelight
- **Networking**: Ktor (OkHttp on Android, Darwin on iOS)
- **DI**: Koin
- **Scanning**: ML Kit + CameraX (Android)

## Project structure

| Path | What |
|---|---|
| `shared/` | KMP module: models, repositories, use cases, platform expectations |
| `androidApp/` | Compose UI, navigation, barcode scanner, notifications |
| `iosApp/` | SwiftUI app consuming the shared framework |

## Building

### Android

```bash
./gradlew :androidApp:assembleDebug
```

### iOS

Open `iosApp/iosApp.xcodeproj` in Xcode (framework is built automatically by the
Gradle script phase), or build the shared framework first:

```bash
./gradlew :shared:assembleRelease
```

### Tests

```bash
./gradlew :shared:testAndroidHostTest    # Android-target tests (commonTest on JVM)
./gradlew :shared:iosSimulatorArm64Test  # iOS-target tests (macOS + Xcode required)
```

## Releases

CI (`.github/workflows/build.yml`) builds signed APKs and iOS archives via manual
dispatch, and runs the test suite on pushes/PRs.

## License

_None yet — all rights reserved until a license is chosen._
