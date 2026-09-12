# Navigation 3

`pulsemvi-navigation3` is an optional artifact. It adds three composables and nothing else: two that
create a `PulseViewModel` or a `PulseContainer` under the `ViewModelStoreOwner` in scope, and one
that gives `NavDisplay` the decorators it needs for that owner to be the back stack entry.

The core artifact stays free of it, so `pulsemvi` on its own depends on the Compose runtime,
`androidx.lifecycle` and coroutines only.

## 1. Add the dependency

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.github.kaleidot725:pulsemvi:<version>")
    implementation("com.github.kaleidot725:pulsemvi-navigation3:<version>")
}
```

It brings Navigation 3 and the lifecycle artifacts with it, so you do not need to declare
`navigation3-ui`, `lifecycle-viewmodel-compose` or `lifecycle-viewmodel-navigation3` yourself.

## 2. Define the routes

A route is any `NavKey`. Keep the back stack in a `SnapshotStateList`:

```kotlin
sealed interface Route : NavKey {
    data object Counter : Route

    data class CounterDetails(val count: Int) : Route
}

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Route>(Route.Counter) }
    val popLast: () -> Unit = {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    // ...
}
```

## 3. Pass the decorators to `NavDisplay`

This is the step that scopes ViewModels to the back stack:

```kotlin
NavDisplay(
    backStack = backStack,
    onBack = popLast,
    entryDecorators = rememberPulseNavEntryDecorators(),
    entryProvider =
        entryProvider {
            entry<Route.Counter> {
                CounterScreen(onShowDetails = { backStack.add(Route.CounterDetails(it)) })
            }
            entry<Route.CounterDetails> { route ->
                CounterDetailScreen(count = route.count, onBack = popLast)
            }
        },
)
```

::: warning
`NavDisplay` defaults `entryDecorators` to the saveable state holder alone. Passing the ViewModel
decorator on its own would drop saveable state, so [`rememberPulseNavEntryDecorators`](/api/composables#rememberpulsenaventrydecorators)
returns both. Use it rather than assembling the list yourself.
:::

## 4. Create the ViewModels inside the destination

Each destination builds what it needs. Nothing is created above `NavDisplay`, which is what keeps
the lifetime tied to the route:

```kotlin
@Composable
fun CounterScreen(onShowDetails: (Int) -> Unit) {
    val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
    val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

    PulseHost(container = container) { onRefresh, onBroadcast ->
        PulseContent(viewModel = viewModel) { state, onAction ->
            // Compose UI
        }
    }
}
```

## What you get

| Event | What happens |
|---|---|
| The route is pushed | The ViewModel is created and `PulseContent` runs `onSetup()` once |
| Another destination covers it | The ViewModel is kept; state and running coroutines are untouched |
| You come back to it | The same instance is found; `onSetup()` is not repeated |
| The route is popped | The entry's `ViewModelStore` is cleared, so `onCleared()` cancels the scope and closes the Container |
| The composition restarts under a surviving owner | The ViewModel is reused, so state stands |

## Two ViewModels of the same type

`rememberPulseViewModel` defaults its key to the ViewModel's qualified class name, and a key is
unique per owner, not globally. Two instances of one type under a single owner would therefore
collide. Give them explicit keys:

```kotlin
val left = rememberPulseViewModel(key = "left") { CounterViewModel(leftRepository) }
val right = rememberPulseViewModel(key = "right") { CounterViewModel(rightRepository) }
```

The demo does this for all four of its areas, which are the same class four times over.

## Without Navigation 3

You do not have to use this artifact. `PulseViewModel` extends `androidx.lifecycle.ViewModel`, so
`viewModel()` and `koinViewModel()` build one just as well, and `PulseContent` runs `onSetup()`
whichever way it was built. What changes is teardown: `close()` runs from `onCleared()`, and only a
`ViewModelStore` calls that. See [Driving the lifecycle yourself](/guide/viewmodel#driving-the-lifecycle-yourself).

## Next Steps

- [Composables](/api/composables) — the full signatures and owner resolution rules
- [ViewModel](/guide/viewmodel) — lifecycle hooks and state updates
- [Container](/guide/container) — broadcast and refresh
