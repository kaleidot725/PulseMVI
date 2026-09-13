# PulseMVI

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.10.1-blue)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![](https://jitpack.io/v/kaleidot725/PulseMVI.svg)](https://jitpack.io/#kaleidot725/PulseMVI)

PulseMVI is an MVI library for Compose Desktop. A `PulseViewModel` owns the state of one section
of the screen, and a `PulseContainer` coordinates several of them: **Broadcast** sends a message
from the Container to every ViewModel, **Unicast** sends one from a ViewModel up to the Container,
and **Refresh** rebuilds the view tree without losing state. Both extend
`androidx.lifecycle.ViewModel`, so the `ViewModelStore` that holds them decides how long they live.

Read [What is PulseMVI?](https://kaleidot725.github.io/PulseMVI/guide/) for the idea behind it.

![demo](docs/demo.png)

## Installation

Add JitPack to `settings.gradle.kts`, then the dependencies to `build.gradle.kts`. Replace
`<version>` with a tag from [Releases](https://github.com/kaleidot725/PulseMVI/releases).
`pulsemvi-navigation3` is optional and scopes ViewModels to a Navigation 3 back stack entry.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.kaleidot725.PulseMVI:pulsemvi:<version>")
    implementation("com.github.kaleidot725.PulseMVI:pulsemvi-navigation3:<version>")
}
```

## Requirements

| Requirement | Version |
|---|---|
| Java | 17+ |
| Kotlin | 2.0+ |
| Compose Multiplatform | 1.6+ |

## Getting started

A ViewModel handles actions and updates its state; a screen observes it with `PulseContent`.
The full walkthrough, including the Container, is in
[Getting Started](https://kaleidot725.github.io/PulseMVI/guide/getting-started).

```kotlin
class CounterViewModel : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Increment -> update { copy(count = count + 1) }
        }
    }
}

@Composable
fun CounterScreen() {
    val viewModel = rememberPulseViewModel { CounterViewModel() }

    PulseContent(viewModel = viewModel) { state, onAction ->
        Button(onClick = { onAction(CounterAction.Increment) }) {
            Text("${state.count}")
        }
    }
}
```

## Documentation

**[kaleidot725.github.io/PulseMVI](https://kaleidot725.github.io/PulseMVI/)** (English and Japanese).

## Demo

The `demo` module is a grid of four areas sharing one Container; a tap on one spreads to the two
it shares an edge with.

```bash
./gradlew :demo:run
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
