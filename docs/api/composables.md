# Composables

## rememberPulseStore

::: tip Artifact
`rememberPulseStore`, `rememberPulseContainer` and `rememberPulseNavEntryDecorators` live in
`pulsemvi-navigation3`, package `jp.kaleidot725.pulse.mvi.navigation3`. The core artifact leaves the
Store lifetime to the caller — see [Store](/guide/store).
:::

```kotlin
@Composable
inline fun <reified Store : PulseStore<*, *, *, *, *>> rememberPulseStore(
    key: String? = null,
    noinline factory: () -> Store,
): Store
```

Creates a Store that survives configuration changes. The Store is owned by a `ViewModel` scoped to
the current `ViewModelStoreOwner`, so an Android configuration change rebuilds the composition
without rebuilding the Store.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `key` | `String?` | Unique key within the owner. Defaults to the Store's qualified class name |
| `factory` | `() -> Store` | Called once per owner to create the Store |

### Lifecycle behavior

- The Store is created on first composition and reused for every later composition under the same owner
- `onSetup()` runs once, when the Store is created, so rebuilding the composition does not repeat it
- The Store scope is cancelled when the owner's `ViewModelStore` is cleared
- State is kept in memory only; it is not restored after process death
- On iOS and Desktop there is no configuration change, so the Store simply lives as long as its host: on iOS the owner is cleared when the `ComposeUIViewController` is destroyed

### Owner resolution

Internally this reads `LocalViewModelStoreOwner.current` and stores a `ViewModel` in that owner's
`ViewModelStore`, keyed by `key`. Whatever owner is in scope at the call site therefore decides how
long the Store lives, and anything that changes that owner changes the Store's lifetime.

| Owner in scope | Store lives |
|---|---|
| The host (`ComponentActivity`, `ComposeUIViewController`, Desktop `Window`) | As long as the screen; survives configuration changes |
| A Navigation 3 entry, via `rememberPulseNavEntryDecorators()` as `NavDisplay`'s `entryDecorators` | As long as the route stays on the back stack; cancelled when the route is popped |
| One you provide with `CompositionLocalProvider(LocalViewModelStoreOwner provides ...)` | As long as you keep that owner |
| None provided by the host | `rememberPulseStore` fails with a message: nothing owns a lifetime, so provide an owner or use the core artifact alone |

Two consequences worth planning for:

- **`key` is unique per owner, not globally.** Two Stores of the same type under one owner collide,
  and the default key is the class name. Give them explicit keys, or put them under different owners
- **Nothing removes a Store from its owner before the owner is cleared.** Creating Stores under a
  long lived owner accumulates them for the life of that owner. Scope them to a narrower owner when
  a screen creates Stores it will not need again

::: tip
Tests can exercise both sides of this by providing their own owner: keep it across a composition
rebuild to reproduce a configuration change, or clear it to reproduce the screen going away.
:::

### Example

```kotlin
val store = rememberPulseStore { CounterStore(CounterRepository()) }

// Two instances of the same Store type need distinct keys
val left = rememberPulseStore(key = "left") { CounterStore(leftRepository) }
val right = rememberPulseStore(key = "right") { CounterStore(rightRepository) }

// Scoped to a Navigation 3 destination instead of the whole screen
NavDisplay(
    backStack = backStack,
    entryDecorators = rememberPulseNavEntryDecorators(),
    entryProvider = entryProvider {
        entry<Route.Counter> {
            val store = rememberPulseStore { CounterStore(CounterRepository()) }
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

Creates a Container that survives configuration changes, keeping its Unicast subscriptions alive.
`PulseContainer.close()` is called when the owner's `ViewModelStore` is cleared.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `key` | `String?` | Unique key within the owner. Defaults to the Container's qualified class name |
| `factory` | `() -> Container` | Called once per owner to create the Container |

### Example

```kotlin
val store = rememberPulseStore { CounterStore(CounterRepository()) }
val container = rememberPulseContainer { CounterContainer(stores = listOf(store)) }
```

## rememberPulseNavEntryDecorators

```kotlin
@Composable
fun <T : Any> rememberPulseNavEntryDecorators(): List<NavEntryDecorator<T>>
```

The `NavEntryDecorator` list `NavDisplay` needs for Stores to be scoped to a back stack entry: the
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
            val store = rememberPulseStore { CounterStore(CounterRepository()) }
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
        MyContent(store = myStore)
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
    store: PulseStore<State, Action, Event, Broadcast, Unicast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable (State, (Action) -> Unit) -> Unit = { _, _ -> },
)
```

Observes a `PulseStore` and provides state and an action dispatcher to the content block. Automatically cancels the Store when removed from the composition.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `store` | `PulseStore<State, Action, Event, Broadcast, Unicast>` | The Store to observe |
| `onEvent` | `(Event) -> Unit` | Called for each one-time side effect emitted by the Store |
| `content` | `@Composable (State, (Action) -> Unit) -> Unit` | Renders the current state; receives a dispatcher to send actions |

### Lifecycle behavior

- `PulseContent` only observes: it never starts or cancels the Store lifecycle
- `LaunchedEffect(store)` collects `event`
- `onSetup()` runs once when `rememberPulseStore` creates the Store, and the scope is cancelled when the owning `ViewModelStoreOwner` is cleared
- Leaving and re-entering composition — a navigation destination covering the route, for example — never repeats `onSetup()`
- When inside `PulseHost`, the composable is wrapped in `key(containerKey)` and re-creates on `refresh()`

### Example

```kotlin
PulseContent(
    store = counterStore,
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
