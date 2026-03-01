# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PulseMVI is a Kotlin MVI (Model-View-Intent) library for Compose Multiplatform. It provides a lightweight, coroutine-based MVI framework for building reactive UIs with Compose Desktop and other JVM Compose targets.

### Purpose

This library provides the core MVI building blocks — `MVIBase`, `MVIState`, `MVIAction`, `MVISideEffect`, and Composable helpers (`MVIRootContent`, `MVIChildContent`, `MVIDialogContent`) — that simplify state management in Compose Multiplatform applications.

## Development Commands

### Build and Testing
- **Build the project**: `./gradlew build`
- **Run tests**: `./gradlew test`
- **Clean build**: `./gradlew clean`
- **Check code quality**: `./gradlew ktlintCheck`
- **Publish to local Maven**: `./gradlew :library:publishToMavenLocal`

## Project Structure

```
PulseMVI/
├── library/                     # MVI library module
│   ├── build.gradle.kts
│   └── src/jvmMain/kotlin/
│       └── jp/kaleidot725/pulse/mvi/
│           ├── MVIAction.kt     # Marker interface for actions
│           ├── MVIBase.kt       # Abstract base class for ViewModels
│           ├── MVIContent.kt    # Compose helper functions
│           ├── MVISideEffect.kt # Marker interface for side effects
│           └── MVIState.kt      # Marker interface for state
├── build.gradle.kts             # Root build file
├── settings.gradle.kts          # Project settings
├── jitpack.yml                  # JitPack publish config
└── gradle.properties
```

## Technical Details

- **Language**: Kotlin (JVM target via Kotlin Multiplatform)
- **Build System**: Gradle with Kotlin DSL
- **Kotlin Version**: 2.3.10
- **Compose Multiplatform Version**: 1.10.1
- **Coroutines Version**: 1.10.2
- **JVM Toolchain**: Java 17
- **Code Style**: Official Kotlin code style with explicit API mode

## Key Configuration

- Group ID: `com.github.kaleidot725`
- Artifact ID: `pulsemvi`
- Package: `jp.kaleidot725.pulse.mvi`
- JitPack dependency: `implementation("com.github.kaleidot725:PulseMVI:<version>")`
