# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

PulseMVI is a Kotlin MVI (Model-View-Intent) library for Compose Desktop. It publishes a JVM target
only — Android and iOS support was dropped so the library stays straightforward on the desktop.

### Purpose

The core artifact provides the MVI building blocks — the `PulseState`, `PulseAction`, `PulseEvent`,
`PulseBroadcast` and `PulseUnicast` marker interfaces, the `PulseViewModel` and `PulseContainer` base
classes, and the `PulseHost` and `PulseContent` composables.

`PulseViewModel` and `PulseContainer` extend `androidx.lifecycle.ViewModel`, so whichever
`ViewModelStore` holds them decides how long they live. `PulseContent` calls `setupOnce()`, which
runs `onSetup()` the first time an instance is observed and never again. The `pulsemvi-navigation3`
artifact adds `rememberPulseViewModel` / `rememberPulseContainer`, and
`rememberPulseNavEntryDecorators()` scopes an instance to a Navigation 3 back stack entry.

## Development Commands

### Build and Testing
- **Build the project**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Clean build**: `./gradlew clean`
- **Check code quality**: `./gradlew ktlintCheck` (auto-fix with `./gradlew ktlintFormat`)
- **Publish to local Maven**: `./gradlew :library:publishToMavenLocal :navigation3:publishToMavenLocal`
- **Run the demo**: `./gradlew :demo:run`

## Project Structure

```
PulseMVI/
├── library/                          # Core artifact: pulsemvi
│   └── src/
│       ├── commonMain/kotlin/jp/kaleidot725/pulse/mvi/
│       │   ├── PulseState.kt         # Marker interface for state
│       │   ├── PulseAction.kt        # Marker interface for actions
│       │   ├── PulseEvent.kt         # Marker interface for one-time events
│       │   ├── PulseBroadcast.kt     # Marker interface for Container to ViewModel messages
│       │   ├── PulseUnicast.kt       # Marker interface for ViewModel to Container messages
│       │   ├── PulseViewModel.kt     # Abstract ViewModel: state, actions, events, unicast
│       │   ├── PulseContainer.kt     # Coordinates ViewModels: broadcast, refresh, close
│       │   └── PulseContent.kt       # PulseHost and PulseContent composables
│       └── commonTest/kotlin/…       # PulseViewModelTest, PulseContainerTest
├── navigation3/                      # Optional artifact: pulsemvi-navigation3
│   └── src/commonMain/kotlin/jp/kaleidot725/pulse/mvi/navigation3/
│       └── PulseNavigation.kt        # rememberPulseViewModel / rememberPulseContainer /
│                                     # rememberPulseNavEntryDecorators
├── demo/                             # Pulse grid demo app (Navigation 3)
│   └── src/{commonMain,jvmMain}/
│       # Four areas share a Container: an area counts its own tap and announces
│       # it as a Unicast, the Container broadcasts it back to all four, and each
│       # decides what to do — the origin ignores the copy of its own tap
├── docs/                             # VitePress documentation site
├── build.gradle.kts                  # Root build file
├── settings.gradle.kts               # Project settings
├── jitpack.yml                       # JitPack publish config
└── gradle.properties
```

Sources sit in `commonMain` even though `jvm()` is the only target. Keeping the source set makes
re-adding a target a build-file change rather than a file move.

## Technical Details

- **Language**: Kotlin Multiplatform with a single `jvm()` target
- **Build System**: Gradle 8.14.5 with Kotlin DSL
- **Kotlin Version**: 2.3.10
- **Compose Multiplatform Version**: 1.10.1
- **Coroutines Version**: 1.10.2
- **Lifecycle Version**: 2.10.0
- **Navigation 3 Version**: 1.1.1 (`navigation3` module only)
- **JVM Toolchain**: Java 17
- **Code Style**: Official Kotlin code style with explicit API mode

## Key Configuration

- Group ID: `com.github.kaleidot725`
- Artifact IDs: `pulsemvi`, `pulsemvi-navigation3`
- Packages: `jp.kaleidot725.pulse.mvi`, `jp.kaleidot725.pulse.mvi.navigation3`
- JitPack dependency: `implementation("com.github.kaleidot725:pulsemvi:<version>")`
