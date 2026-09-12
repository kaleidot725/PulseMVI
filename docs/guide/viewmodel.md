# ViewModel

`PulseViewModel` is the core building block of PulseMVI. It manages the UI state for a single screen or section of your app.

## Creating a ViewModel

Extend `PulseViewModel` with your five type parameters:

```kotlin
class MyViewModel : PulseViewModel<MyState, MyAction, MyEvent, MyBroadcast, MyUnicast>(
    initialUiState = MyState(),
) {
    override fun onSetup() { /* start coroutines here */ }
    override fun onAction(uiAction: MyAction) { /* handle user intents */ }
    override fun onReceive(broadcast: MyBroadcast) { /* react to broadcasts */ }
}
```

## Lifecycle Hooks

### `onSetup()`

Called once, by `PulseContent`, the first time it observes the ViewModel. Use this to start long-running coroutines such as repository flows:

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
With `pulsemvi-navigation3`, create the ViewModel with `rememberPulseViewModel`. `PulseContent` runs `onSetup()` once, and the scope is cancelled when the owning `ViewModelStoreOwner` is cleared. A composition restart preserves state and does not repeat `onSetup()`.

Because the lifecycle follows the owner rather than the composition, covering the route with another Navigation 3 destination, or refreshing its subtree, never repeats setup.

To tie a ViewModel to a single destination instead of the whole screen, pass `rememberPulseNavEntryDecorators()` as `NavDisplay`'s `entryDecorators` and call `rememberPulseViewModel` inside the destination. The entry then owns the ViewModel, and popping the route cancels it.
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

`PulseContent` always runs `onSetup()` for you, whichever way the ViewModel was built. Teardown is
the part that depends on the owner: `close()` runs from `onCleared()`, and only a `ViewModelStore`
calls that. Hold a ViewModel in a plain `remember` and nothing ever clears it, so cancel it yourself:

```kotlin
val viewModel = remember { CounterViewModel(repository) }
DisposableEffect(viewModel) {
    onDispose { viewModel.cancel() }
}

PulseContent(viewModel = viewModel) { state, onAction ->
    // Compose UI
}
```

A Container needs the same treatment:

```kotlin
val container = remember { CounterContainer(viewModels = listOf(viewModel)) }
DisposableEffect(container) {
    onDispose { container.close() }
}
```

::: warning
The ViewModel then lives exactly as long as this composition. Leaving and re-entering it builds a
new instance, so its state is lost. Add `pulsemvi-navigation3` when that matters.
:::
