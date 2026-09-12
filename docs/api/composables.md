# Composables

## rememberPulseViewModel

::: tip Artifact
`rememberPulseViewModel`, `rememberPulseContainer` and `rememberPulseNavEntryDecorators` live in
`pulsemvi-navigation3`, package `jp.kaleidot725.pulse.mvi.navigation3`. The core artifact leaves the
ViewModel lifetime to the caller — see [ViewModel](/guide/viewmodel).
:::

```kotlin
@Composable
inline fun <reified ViewModel : PulseViewModel<*, *, *, *, *>> rememberPulseViewModel(
    key: String? = null,
    noinline factory: () -> ViewModel,
): ViewModel
```

Creates a ViewModel held in the `ViewModelStore` of the current `ViewModelStoreOwner`, so a
composition restart reuses the instance rather than rebuilding it.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `key` | `String?` | Unique key within the owner. Defaults to the ViewModel's qualified class name |
| `factory` | `() -> ViewModel` | Called once per owner to create the ViewModel |

### Lifecycle behavior

- The ViewModel is created on first composition and reused for every later composition under the same owner
- `onSetup()` runs once, the first time a `PulseContent` observes the ViewModel, so rebuilding the composition does not repeat it
- The ViewModel scope is cancelled when the owner's `ViewModelStore` is cleared
- State is kept in memory only; it is not restored after process death

### Owner resolution

Internally this reads `LocalViewModelStoreOwner.current` and keeps the instance in that owner's
`ViewModelStore`, keyed by `key`. Whatever owner is in scope at the call site therefore decides how
long the ViewModel lives, and anything that changes that owner changes the ViewModel's lifetime.

| Owner in scope | ViewModel lives |
|---|---|
| The Compose Desktop `Window` | As long as the screen; survives a composition restart |
| A Navigation 3 entry, via `rememberPulseNavEntryDecorators()` as `NavDisplay`'s `entryDecorators` | As long as the route stays on the back stack; cancelled when the route is popped |
| One you provide with `CompositionLocalProvider(LocalViewModelStoreOwner provides ...)` | As long as you keep that owner |
| None provided by the host | `rememberPulseViewModel` fails with a message: nothing owns a lifetime, so provide an owner or use the core artifact alone |

Two consequences worth planning for:

- **`key` is unique per owner, not globally.** Two ViewModels of the same type under one owner collide,
  and the default key is the class name. Give them explicit keys, or put them under different owners
- **Nothing removes a ViewModel from its owner before the owner is cleared.** Creating ViewModels under a
  long lived owner accumulates them for the life of that owner. Scope them to a narrower owner when
  a screen creates ViewModels it will not need again

::: tip
Tests can exercise both sides of this by providing their own owner: keep it across a composition
rebuild to reproduce a composition restart, or clear it to reproduce the screen going away.
:::

### Example

```kotlin
val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }

// Two instances of the same ViewModel type need distinct keys
val left = rememberPulseViewModel(key = "left") { CounterViewModel(leftRepository) }
val right = rememberPulseViewModel(key = "right") { CounterViewModel(rightRepository) }

// Scoped to a Navigation 3 destination instead of the whole screen
NavDisplay(
    backStack = backStack,
    entryDecorators = rememberPulseNavEntryDecorators(),
    entryProvider = entryProvider {
        entry<Route.Counter> {
            val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
            // ...
        }
    },
)
```

## rememberPulseContainer

```kotlin
@Composable
inline fun <reified Container : PulseContainer<*, *>> rememberPulseContainer(
    key: String? = null,
    noinline factory: () -> Container,
): Container
```

Creates a Container that survives a composition restart, keeping its Unicast subscriptions alive.
`PulseContainer.close()` is called when the owner's `ViewModelStore` is cleared.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `key` | `String?` | Unique key within the owner. Defaults to the Container's qualified class name |
| `factory` | `() -> Container` | Called once per owner to create the Container |

### Example

```kotlin
val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }
```

## rememberPulseNavEntryDecorators

```kotlin
@Composable
fun <T : Any> rememberPulseNavEntryDecorators(): List<NavEntryDecorator<T>>
```

The `NavEntryDecorator` list `NavDisplay` needs for ViewModels to be scoped to a back stack entry: the
saveable state holder decorator plus the ViewModel one.

`NavDisplay` defaults `entryDecorators` to the saveable state holder alone, so passing the ViewModel
decorator on its own would silently drop saveable state. This returns both.

```kotlin
NavDisplay(
    backStack = backStack,
    entryDecorators = rememberPulseNavEntryDecorators(),
    entryProvider = entryProvider {
        entry<Route.Counter> {
            // Scoped to this entry: cancelled when the route is popped
            val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
            // ...
        }
    },
)
```

## PulseHost

```kotlin
@Composable
fun <Broadcast : PulseBroadcast, Unicast : PulseUnicast> PulseHost(
    container: PulseContainer<Broadcast, Unicast>,
    content: @Composable (
        onRefresh: () -> Unit,
        onBroadcast: (Broadcast) -> Unit,
    ) -> Unit = { _, _ -> },
)
```

Scopes a `PulseContainer` to this subtree. It emits no UI of its own: it publishes the Container key that `PulseContent` re-creates its content on, and provides `onRefresh` and `onBroadcast` to the content block. All `PulseContent` composables placed inside respond to `container.refresh()`.

An app can contain several of them. Each destination that owns a Container hosts its own, which is how the demo builds every screen.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `container` | `PulseContainer<Broadcast, Unicast>` | The container to observe |
| `content` | `@Composable (onRefresh, onBroadcast) -> Unit` | Content block receiving the two callbacks |

### Example

```kotlin
PulseHost(container = appContainer) { onRefresh, onBroadcast ->
    Column {
        Button(onClick = { onBroadcast(AppBroadcast.Sync) }) {
            Text("Sync All")
        }
        Button(onClick = { onRefresh() }) {
            Text("Refresh View")
        }
        MyContent(viewModel = myViewModel)
    }
}
```

---

## PulseContent

```kotlin
@Composable
fun <
    State : PulseState,
    Action : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>
PulseContent(
    viewModel: PulseViewModel<State, Action, Event, Broadcast, Unicast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable (State, (Action) -> Unit) -> Unit = { _, _ -> },
)
```

Observes a `PulseViewModel` and provides state and an action dispatcher to the content block. Runs `onSetup()` the first time it observes an instance; it never cancels one. Observe each instance from one `PulseContent` at a time, since `event` is a single-consumer channel.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `viewModel` | `PulseViewModel<State, Action, Event, Broadcast, Unicast>` | The ViewModel to observe |
| `onEvent` | `(Event) -> Unit` | Called for each one-time side effect emitted by the ViewModel |
| `content` | `@Composable (State, (Action) -> Unit) -> Unit` | Renders the current state; receives a dispatcher to send actions |

### Lifecycle behavior

- `PulseContent` never cancels the ViewModel; teardown belongs to whoever owns it
- `LaunchedEffect(viewModel)` collects `event`, always through the latest `onEvent` passed in
- `onSetup()` runs once, the first time a `PulseContent` observes the ViewModel; the scope is cancelled when the owning `ViewModelStoreOwner` is cleared
- Leaving and re-entering composition — a navigation destination covering the route, for example — never repeats `onSetup()`
- When inside `PulseHost`, the composable is wrapped in `key(containerKey)` and re-creates on `refresh()`

### Example

```kotlin
PulseContent(
    viewModel = counterViewModel,
    onEvent = { event ->
        when (event) {
            is CounterEvent.ShowMessage ->
                scope.launch { snackbarHostState.showSnackbar(event.message) }
        }
    },
) { state, onAction ->
    Column {
        Text("Count: ${state.count}")
        Button(onClick = { onAction(CounterAction.Increment) }) {
            Text("+")
        }
    }
}
```
