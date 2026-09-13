# PulseViewModel

```kotlin
abstract class PulseViewModel<
    UiState : PulseState,
    UiAction : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>(
    initialUiState: UiState,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
)
```

Abstract base class for managing the UI state of a single screen or section.

Use `unicast()` when the ViewModel needs to send messages up to its parent Container.

## Properties

### `state`

```kotlin
val state: StateFlow<UiState>
```

The current UI state as a hot, read-only `StateFlow`. Collecting it does not change the ViewModel lifecycle.

---

### `currentState`

```kotlin
val currentState: UiState
```

Synchronous snapshot of the current UI state. Equivalent to `state.value`.

---

### `event`

```kotlin
val event: Flow<Event>
```

A cold `Flow` of one-time side effects emitted via `event()`. Collected by `PulseContent`. Each
event goes to a single collector. Events are consumed. They are not replayed to later collectors.

---

### `unicast`

```kotlin
val unicast: SharedFlow<Unicast>
```

A hot stream of child-to-parent unicasts emitted via `unicast()`.

---

### `coroutineScope`

```kotlin
val coroutineScope: CoroutineScope
```

A `CoroutineScope` backed by `SupervisorJob` and the dispatcher passed to the constructor. The dispatcher defaults to `Dispatchers.Default`. Pass `Dispatchers.Main` or a test dispatcher when required. The owned scope is cancelled on `cancel()`. It is then recreated.

## Methods

### `onSetup()`

```kotlin
open fun onSetup()
```

Called once, by `PulseContent`, the first time it observes the ViewModel. The instance outlives the composition. So leaving and re-entering the composition does not repeat it. Override it to start data-collection coroutines. They run in `coroutineScope`. They stop when it is cancelled.

Observe an instance from one `PulseContent` at a time. `event` is a single-consumer channel. A second observer would take events the first never sees.

---

### `onAction(uiAction)`

```kotlin
abstract fun onAction(uiAction: UiAction)
```

Called each time the UI dispatches an action. Launch coroutines for async work here.

---

### `onReceive(broadcast)`

```kotlin
open fun onReceive(broadcast: Broadcast)
```

Called when the parent `PulseContainer` delivers a broadcast. Default implementation does nothing.

---

### `update(block)`

```kotlin
fun update(block: UiState.() -> UiState)
```

Applies an immutable state update. The lambda receives the current state as `this` and must return the next state:

```kotlin
update { copy(count = count + 1) }
```

---

### `event(effect)`

```kotlin
fun event(effect: Event)
```

Emits a one-time side effect to the UI layer. Collected by the `onEvent` lambda in `PulseContent`.

Buffered, so it never suspends. It keeps emission order. Events emitted while no `PulseContent` is
collecting wait in the buffer. That happens when the destination is covered by another one, say.
They arrive when a collector returns. The buffer holds 64. Beyond that, the oldest event is dropped.

---

### `unicast(unicast)`

```kotlin
fun unicast(unicast: Unicast)
```

Emits a child-to-parent message. The parent `PulseContainer` collects the ViewModel's `unicast` flow. It receives the message through `onReceived()`.

---

### `cancel()`

```kotlin
fun cancel()
```

Cancels the work started in `onSetup()`. Replaces the scope with a fresh one, with the state preserved. The next `PulseContent` to observe the instance runs `onSetup()` again. Use it when the ViewModel may become active again.

---

### `close()`

```kotlin
fun close()
```

Cancels the work started in `onSetup()` for good. It does not replace the scope. `onCleared()` calls it when the owning `ViewModelStore` is cleared. So a discarded ViewModel cannot launch anything that outlives it.

## Example

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}

class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count ->
                update { copy(count = count) }
            }
        }
    }

    override fun onAction(uiAction: CounterAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterAction.Increment -> repository.increment()
                CounterAction.Decrement -> repository.decrement()
            }
        }
    }

    override fun onReceive(broadcast: CounterBroadcast) {
        when (broadcast) {
            CounterBroadcast.Reset -> update { CounterState() }
        }
    }
}
```

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}

class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Reset -> {
                repository.reset()
                unicast(CounterUnicast.ResetRequested)
            }
            CounterAction.Increment -> repository.increment()
            CounterAction.Decrement -> repository.decrement()
        }
    }
}
```
