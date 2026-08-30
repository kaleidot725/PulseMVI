# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

PulseMVI is a Kotlin MVI (Model-View-Intent) library for Compose Multiplatform, targeting Android,
iOS and Desktop (JVM) from one `commonMain` source set.

### Purpose

The core artifact provides the MVI building blocks — the `PulseState`, `PulseAction`, `PulseEvent`,
`PulseBroadcast` and `PulseUnicast` marker interfaces, the `PulseStore` and `PulseContainer` base
classes, and the `PulseHost` and `PulseContent` composables.

`PulseContent` only observes a Store. Nothing in the core artifact starts or cancels a Store, so a
core-only caller drives `onSetup()` and `cancel()` themselves. The `pulsemvi-navigation3` artifact
hands that lifetime to a `ViewModel` through `rememberPulseStore` / `rememberPulseContainer`, and
`rememberPulseNavEntryDecorators()` scopes it to a Navigation 3 back stack entry.

## Development Commands

### Build and Testing
- **Build the project**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Clean build**: `./gradlew clean`
- **Check code quality**: `./gradlew ktlintCheck` (auto-fix with `./gradlew ktlintFormat`)
- **Publish to local Maven**: `./gradlew :library:publishToMavenLocal :navigation3:publishToMavenLocal`

Desktop UI tests live in `demo/src/jvmTest`, iOS UI tests in `iosApp/iosAppUITests` (Xcode).

## Project Structure

```
PulseMVI/
├── library/                          # Core artifact: pulsemvi
│   └── src/
│       ├── androidMain/AndroidManifest.xml
│       ├── commonMain/kotlin/jp/kaleidot725/pulse/mvi/
│       │   ├── PulseState.kt         # Marker interface for state
│       │   ├── PulseAction.kt        # Marker interface for actions
│       │   ├── PulseEvent.kt         # Marker interface for one-time events
│       │   ├── PulseBroadcast.kt     # Marker interface for Container to Store messages
│       │   ├── PulseUnicast.kt       # Marker interface for Store to Container messages
│       │   ├── PulseStore.kt         # Abstract Store: state, actions, events, unicast
│       │   ├── PulseContainer.kt     # Coordinates Stores: broadcast, refresh, close
│       │   └── PulseContent.kt       # PulseHost and PulseContent composables
│       └── commonTest/kotlin/…       # PulseStoreTest, PulseContainerTest
├── navigation3/                      # Optional artifact: pulsemvi-navigation3
│   └── src/
│       ├── androidMain/AndroidManifest.xml
│       ├── commonMain/kotlin/jp/kaleidot725/pulse/mvi/navigation3/
│       │   └── PulseNavigation.kt    # rememberPulseStore / rememberPulseContainer /
│       │                             # rememberPulseNavEntryDecorators
│       └── iosTest/kotlin/…          # PulseNavigationIosTest
├── demo/                             # Multiplatform demo app (Navigation 3)
│   └── src/{commonMain,androidMain,iosMain,jvmMain,jvmTest}/
├── iosApp/                           # Xcode project hosting the demo on iOS
├── docs/                             # VitePress documentation site
├── build.gradle.kts                  # Root build file
├── settings.gradle.kts               # Project settings
├── jitpack.yml                       # JitPack publish config
└── gradle.properties
```

## Technical Details

- **Language**: Kotlin Multiplatform — `jvm()`, `androidTarget()`, `iosArm64()`, `iosSimulatorArm64()`
  (core also publishes `iosX64()`; `navigation3` does not, since `navigation3-ui` has no such variant)
- **Build System**: Gradle 8.14.5 with Kotlin DSL, AGP 8.13.2
- **Kotlin Version**: 2.3.10
- **Compose Multiplatform Version**: 1.10.1
- **Coroutines Version**: 1.10.2
- **Lifecycle Version**: 2.10.0 (`navigation3` module only)
- **Navigation 3 Version**: 1.1.1 (`navigation3` module only)
- **JVM Toolchain**: Java 17
- **Android**: library `minSdk` 21 (demo 23); `compileSdk` 35 for the core and 36 for `navigation3`
- **Code Style**: Official Kotlin code style with explicit API mode

## Key Configuration

- Group ID: `com.github.kaleidot725`
- Artifact IDs: `pulsemvi`, `pulsemvi-navigation3` (plus `-android` variants)
- Packages: `jp.kaleidot725.pulse.mvi`, `jp.kaleidot725.pulse.mvi.navigation3`
- JitPack dependency: `implementation("com.github.kaleidot725:pulsemvi:<version>")`
