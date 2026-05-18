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

Called once when the Store's `state` is first collected. Use this to start long-running coroutines such as repository flows:

```kotlin
override fun onSetup() {
    coroutineScope.launch {
        repository.data.collect { data ->
            update { copy(items = data) }
        }
    }
}
```

::: warning
`onSetup()` is also called after `cancel()` (i.e., after a view refresh). Coroutines launched here are automatically cancelled when `cancel()` is called.
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
