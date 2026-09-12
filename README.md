# PulseMVI

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.1-blue)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![](https://jitpack.io/v/kaleidot725/PulseMVI.svg)](https://jitpack.io/#kaleidot725/PulseMVI)

A lightweight MVI library for **Compose Desktop**.
PulseMVI adds **Broadcast** to notify all ViewModels simultaneously, **Unicast** to send child ViewModel messages up to the Container, and **View Refresh** to reconstruct the view tree on demand.

![demo](docs/demo.png)

## Features

- 🏗️ **MVI Architecture** — clear separation of State, Action, Event, Broadcast, and Unicast
- 📡 **Broadcast** — one typed message from a Container to every registered ViewModel at once
- ⬆️ **Unicast** — a typed message from a child ViewModel up to its Container
- 🖥️ **View Refresh** — reconstruct the view tree on demand without losing ViewModel state
- ⚡ **Coroutine-Based** — built on Kotlin Coroutines and StateFlow
- 🔌 **ViewModel-Backed** — `PulseViewModel` extends `androidx.lifecycle.ViewModel`, so `viewModel()`
  and `koinViewModel()` create one, and the owning `ViewModelStore` decides how long it lives

## Requirements

- Java 17 or higher
- Kotlin 2.0 or higher
- A Compose Desktop (JVM) project

## Installation

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.kaleidot725:pulsemvi:<version>")

    // Optional: back stack scoped lifetimes
    implementation("com.github.kaleidot725:pulsemvi-navigation3:<version>")
}
```

Replace `<version>` with a tag from [Releases](https://github.com/kaleidot725/PulseMVI/releases).

## Quick Start

A ViewModel owns one section of the screen:

```kotlin
class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count -> update { copy(count = count) } }
        }
    }

    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Increment -> coroutineScope.launch { repository.increment() }
        }
    }

    override fun onReceive(broadcast: CounterBroadcast) {
        when (broadcast) {
            CounterBroadcast.Reset -> update { copy(count = 0) }
        }
    }
}
```

A Container coordinates several of them, and `PulseContent` connects one to the UI:

```kotlin
@Composable
fun CounterScreen() {
    val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
    val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

    PulseHost(container = container) { onRefresh, onBroadcast ->
        PulseContent(viewModel = viewModel) { state, onAction ->
            Text("${state.count}")
            Button(onClick = { onAction(CounterAction.Increment) }) { Text("+") }
        }
    }
}
```

The full walkthrough, including the type definitions this skips, is in
[Getting Started](https://kaleidot725.github.io/PulseMVI/guide/getting-started).

## Navigation 3

`pulsemvi-navigation3` scopes a ViewModel to a back stack entry: it lives while its route is on the
stack, survives being covered by another destination, and is cancelled when the route is popped.

Pass the decorators to `NavDisplay`, then create ViewModels inside each destination:

```kotlin
NavDisplay(
    backStack = backStack,
    onBack = popLast,
    entryDecorators = rememberPulseNavEntryDecorators(),
    entryProvider =
        entryProvider {
            entry<Route.Counter> { CounterScreen() }
            entry<Route.CounterDetails> { route -> CounterDetailScreen(count = route.count) }
        },
)
```

`NavDisplay` defaults `entryDecorators` to the saveable state holder alone, so passing the ViewModel
decorator on its own would drop saveable state — `rememberPulseNavEntryDecorators()` returns both.

Nothing is created above `NavDisplay`: each destination builds its own ViewModels and Container, and
that is what ties them to the route. Full setup in the
[Navigation 3 guide](https://kaleidot725.github.io/PulseMVI/guide/navigation3).

## Documentation

📖 **[kaleidot725.github.io/PulseMVI](https://kaleidot725.github.io/PulseMVI/)**

| Guide | | API Reference | |
|---|---|---|---|
| [What is PulseMVI?](https://kaleidot725.github.io/PulseMVI/guide/) | the idea and installation | [PulseViewModel](https://kaleidot725.github.io/PulseMVI/api/pulse-viewmodel) | state, actions, events |
| [Getting Started](https://kaleidot725.github.io/PulseMVI/guide/getting-started) | build a counter | [PulseContainer](https://kaleidot725.github.io/PulseMVI/api/pulse-container) | broadcast, refresh |
| [Architecture](https://kaleidot725.github.io/PulseMVI/guide/architecture) | data flow and lifecycle | [Composables](https://kaleidot725.github.io/PulseMVI/api/composables) | the four composables |
| [Navigation 3](https://kaleidot725.github.io/PulseMVI/guide/navigation3) | back stack scoping | [Marker Interfaces](https://kaleidot725.github.io/PulseMVI/api/interfaces) | State, Action, Event |

## Example Application

The [`demo`](demo/) module is a pulse grid: four areas sharing one Container, each unaware of the
others.

```bash
./gradlew :demo:run
```

A tapped area owns its own state: it counts the tap itself, then announces it as a Unicast. The
Container broadcasts that to all four areas, and each compares the origin against its own position
to decide — the origin drops the copy of its own tap, an area sharing an edge counts it, and the
diagonal does nothing. One tap therefore moves three of the four counts.

"New Area" pushes another grid that starts at zero, and the one underneath is still there, untouched,
when you come back.

## Building

```bash
./gradlew build                                              # build
./gradlew test                                               # test
./gradlew :library:publishToMavenLocal :navigation3:publishToMavenLocal
```

## License

```
Copyright 2026 kaleidot725

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
