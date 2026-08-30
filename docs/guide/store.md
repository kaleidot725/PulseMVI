# Store

`PulseStore` is the core building block of PulseMVI. It manages the UI state for a single screen or section of your app.

## Creating a Store

Extend `PulseStore` with your five type parameters:

```kotlin
class MyStore : PulseStore<MyState, MyAction, MyEvent, MyBroadcast, MyUnicast>(
    initialUiState = MyState(),
) {
    override fun onSetup() { /* start coroutines here */ }
    override fun onAction(uiAction: MyAction) { /* handle user intents */ }
    override fun onReceive(broadcast: MyBroadcast) { /* react to broadcasts */ }
}
```

## Lifecycle Hooks

### `onSetup()`

Called once by whoever owns the Store's lifetime. `PulseContent` never calls it — see [Driving the lifecycle yourself](#driving-the-lifecycle-yourself) for the core artifact. Use this to start long-running coroutines such as repository flows:

```kotlin
override fun onSetup() {
    coroutineScope.launch {
        repository.data.collect { data ->
            update { copy(items = data) }
        }
    }
}
```

::: tip
With `pulsemvi-navigation3`, create the Store with `rememberPulseStore`. It owns the setup lifecycle: `onSetup()` runs once when the Store is created, and the scope is cancelled when the owning `ViewModelStoreOwner` is cleared. Rotation preserves state and does not repeat `onSetup()`.

Because the lifecycle follows the owner rather than the composition, covering the route with another Navigation 3 destination, or refreshing its subtree, never repeats setup.

To tie a Store to a single destination instead of the whole screen, pass `rememberPulseNavEntryDecorators()` as `NavDisplay`'s `entryDecorators` and call `rememberPulseStore` inside the destination. The entry then owns the Store, and popping the route cancels it.
:::

### `onAction(uiAction)`

Called each time the UI dispatches an action. Keep this non-blocking — launch coroutines for async work:

```kotlin
override fun onAction(uiAction: MyAction) {
    coroutineScope.launch {
        when (uiAction) {
            MyAction.Load -> loadData()
            is MyAction.Select -> selectItem(uiAction.id)
        }
    }
}
```

### `onReceive(broadcast)`

Called when the parent Container delivers a broadcast. You can update state or emit events:

```kotlin
override fun onReceive(broadcast: MyBroadcast) {
    when (broadcast) {
        MyBroadcast.Refresh -> update { copy(isRefreshing = true) }
        is MyBroadcast.UserChanged -> update { copy(userId = broadcast.id) }
    }
}
```

## Updating State

Use `update { }` to produce the next immutable state. The lambda receives the current state as `this`:

```kotlin
update { copy(count = count + 1, isLoading = false) }
```

## Emitting Events

Use `event()` to send a one-time side effect to the UI:

```kotlin
event(MyEvent.NavigateTo(Screen.Detail))
event(MyEvent.ShowError("Something went wrong"))
```

## Accessing Current State

Read the latest state snapshot synchronously via `currentState`:

```kotlin
override fun onAction(uiAction: MyAction) {
    if (currentState.isLoading) return  // guard check
    coroutineScope.launch { /* ... */ }
}
```

## Driving the lifecycle yourself

`rememberPulseStore` lives in `pulsemvi-navigation3`. With the core artifact alone, nothing calls
`onSetup()` for you — `PulseContent` only observes. Create the Store however you like and drive it
from the composition:

```kotlin
val store = remember { CounterStore(repository) }
DisposableEffect(store) {
    store.onSetup()
    onDispose { store.cancel() }
}

PulseContent(store = store) { state, onAction ->
    // Compose UI
}
```

A Container you own needs only the teardown, since it has no setup step:

```kotlin
val container = remember { CounterContainer(stores = listOf(store)) }
DisposableEffect(container) {
    onDispose { container.close() }
}
```

::: warning
The Store then lives exactly as long as this composition. Leaving and re-entering it repeats
`onSetup()`, and an Android configuration change starts over, losing the Store's state. Add
`pulsemvi-navigation3` when that matters.
:::
