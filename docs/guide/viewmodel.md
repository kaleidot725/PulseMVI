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

Called once, by `PulseContent`, the first time it observes the ViewModel. Use this to start long-running coroutines such as repository flows.

::: tip
The lifetime follows the `ViewModelStoreOwner` holding the ViewModel, not the composition. A composition restart, another destination covering the route, or a refresh of the subtree all keep the state, and none of them repeats `onSetup()`. The scope is cancelled when the owner is cleared.

For tying a ViewModel to a single destination, see [Navigation 3](/guide/navigation3).
:::

```kotlin
override fun onSetup() {
    coroutineScope.launch {
        repository.data.collect { data ->
            update { copy(items = data) }
        }
    }
}
```

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

`PulseContent` runs `onSetup()` for you, whichever way the ViewModel was built. What changes is
teardown. `close()` is called by `onCleared()`, and only a `ViewModelStore` calls that.

Hold a ViewModel in a plain `remember` and nothing ever clears it, so cancel it yourself. A
Container needs the same treatment.

::: warning
The ViewModel then lives exactly as long as this composition. Leaving and re-entering it builds a
new instance, and its state is lost. Add `pulsemvi-navigation3` when that matters.
:::

```kotlin
val viewModel = remember { CounterViewModel(repository) }
DisposableEffect(viewModel) {
    onDispose { viewModel.cancel() }
}

PulseContent(viewModel = viewModel) { state, onAction ->
    // Compose UI
}
```

```kotlin
val container = remember { CounterContainer(viewModels = listOf(viewModel)) }
DisposableEffect(container) {
    onDispose { container.close() }
}
```
