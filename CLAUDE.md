# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

The `pulsemvi-konsist` artifact publishes the conventions themselves as Konsist assertions, so an app
can check in its own test suite that it uses PulseMVI the way the library expects. Among them is the
view layout the demo follows: a **screen** at the root of a feature package creates the ViewModels and
lays out **sections**, a section binds one ViewModel with `PulseContent`, and a **component** takes
data and callbacks only. Packages decide which is which — `section` and `component` — and names are
free.

## Development Commands

### Build and Testing
- **Build the project**: `./gradlew build`
- **Run tests**: `./gradlew allTests`
- **Clean build**: `./gradlew clean`
- **Check code quality**: `./gradlew ktlintCheck` (auto-fix with `./gradlew ktlintFormat`)
- **Test coverage**: `./gradlew koverHtmlReport` (opens at `build/reports/kover/html/index.html`; XML via `koverXmlReport`)
- **Publish to local Maven**: `./gradlew :library:publishToMavenLocal :navigation3:publishToMavenLocal`
- **Run the demo**: `./gradlew :demo:run`

## Writing Tests

Name a test case with a backticked sentence, written from the subject's point of view, so a failing
test reads as the promise that broke. Every test class and test case also carries a KDoc line, and it
says what the name cannot: why the behavior matters, with links to the API it covers.

```kotlin
/**
 * Construction stays cheap: whoever owns the instance decides when the work starts, which for a
 * composition is [PulseContent] calling [PulseViewModel.setupOnce].
 */
@Test
fun `does not run onSetup until it is set up`() {
```

### Where a test goes

Put a test in the source set that matches what it needs.

| Source set | Use it for |
|---|---|
| `library/src/commonTest` | `PulseViewModel` and `PulseContainer` — anything that needs no composition |
| `library/src/jvmTest` | The composables, through `createComposeRule()` from `ui-test-junit4` |
| `navigation3/src/jvmTest` | `rememberPulseViewModel`, `rememberPulseContainer`, `rememberPulseNavEntryDecorators` |

### Coverage

Coverage is enforced rather than reported: `./gradlew koverVerify` fails when lines or instructions
fall below 95%, and the pull request check runs it. Branch coverage is left out of the gate on
purpose — the remaining branches are the `changedInstance` arms the Compose compiler generates for
default parameters, and reaching them takes tests written against the compiler rather than against
the library. A new public declaration is still expected to arrive with the test that covers it.

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
├── konsist/                          # Optional artifact: pulsemvi-konsist
│   └── src/
│       ├── jvmMain/kotlin/jp/kaleidot725/pulse/mvi/konsist/
│       │   └── PulseConventions.kt   # assertPulseMviConventions and the checks it runs
│       └── jvmTest/…                 # the checks, over conforming and violating fixtures
├── demo/                             # Pulse grid demo app (Navigation 3)
│   └── src/commonMain/kotlin/jp/kaleidot725/pulse/demo/
│       ├── DemoApp.kt                # NavDisplay and the back stack
│       └── count/                    # One feature: screen at its root
│           ├── PulseCountHost.kt     # Screen: creates the ViewModels and Container
│           ├── PulseCountContainer.kt
│           ├── component/            # Stateless composables of the screen
│           ├── section/area/         # Section: one ViewModel bound with PulseContent
│           │   ├── PulseAreaContent.kt
│           │   ├── PulseAreaViewModel.kt
│           │   ├── component/        # Stateless composables of the section
│           │   └── state/
│           └── state/
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
re-adding a target a build-file change rather than a file move. The exception is `konsist`, whose
sources are in `jvmMain` because Konsist publishes a JVM artifact only.

## Technical Details

- **Language**: Kotlin Multiplatform with a single `jvm()` target
- **Build System**: Gradle 8.14.5 with Kotlin DSL
- **Kotlin Version**: 2.3.10
- **Compose Multiplatform Version**: 1.10.1
- **Coroutines Version**: 1.10.2
- **Lifecycle Version**: 2.10.0
- **Navigation 3 Version**: 1.1.1 (`navigation3` module only)
- **Konsist Version**: 0.17.3 (`konsist` module only)
- **JVM Toolchain**: Java 17
- **Code Style**: Official Kotlin code style with explicit API mode

## Key Configuration

- Group ID: `com.github.kaleidot725.PulseMVI` (what JitPack serves for a multi-module repo)
- Artifact IDs: `pulsemvi`, `pulsemvi-navigation3`, `pulsemvi-konsist`
- Packages: `jp.kaleidot725.pulse.mvi`, `jp.kaleidot725.pulse.mvi.navigation3`,
  `jp.kaleidot725.pulse.mvi.konsist`
- JitPack dependency: `implementation("com.github.kaleidot725.PulseMVI:pulsemvi:<version>")`, and
  `testImplementation("com.github.kaleidot725.PulseMVI:pulsemvi-konsist:<version>")` for the
  convention checks
