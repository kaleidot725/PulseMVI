# Composables

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

Scopes a `PulseContainer` to this subtree. It emits no UI of its own. It publishes the Container key. `PulseContent` re-creates its content on that key. It also provides `onRefresh` and `onBroadcast` to the content block. All `PulseContent` composables placed inside respond to `container.refresh()`.

An app can contain several of them. In the demo, each destination that owns a Container hosts its own.

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

Observes a `PulseViewModel`. Provides state and an action dispatcher to the content block. Runs `onSetup()` the first time it observes an instance. It never cancels one. `event` is a single-consumer channel, so observe each instance from one `PulseContent` at a time.

### Parameters

| Parameter | Type | Description |
|---|---|---|
| `viewModel` | `PulseViewModel<State, Action, Event, Broadcast, Unicast>` | The ViewModel to observe |
| `onEvent` | `(Event) -> Unit` | Called for each one-time side effect emitted by the ViewModel |
| `content` | `@Composable (State, (Action) -> Unit) -> Unit` | Renders the current state; receives a dispatcher to send actions |

### Lifecycle behavior

- `PulseContent` never cancels the ViewModel; teardown belongs to whoever owns it
- `LaunchedEffect(viewModel)` collects `event`. It always delivers through the latest `onEvent` passed in
- `onSetup()` runs once, the first time a `PulseContent` observes the ViewModel
- The scope is cancelled when the owning `ViewModelStoreOwner` is cleared
- Leaving and re-entering composition — a navigation destination covering the route, for example — never repeats `onSetup()`
- Inside `PulseHost`, the composable is wrapped in `key(containerKey)`. It re-creates on `refresh()`

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
