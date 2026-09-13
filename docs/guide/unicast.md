# Unicast

Unicast is one of PulseMVI's mechanisms. It sends a typed message from a `PulseViewModel` up to its parent `PulseContainer`.

Use it when handling an action and coordinating the follow-up are split. The child ViewModel owns the immediate user action. The parent Container handles the follow-up, such as broadcasting the result to sibling ViewModels.

## Defining a Unicast

Implement `PulseUnicast` with a sealed interface or sealed class. The Unicast type is shared with the Container. Every ViewModel registered in that Container uses the same type. This generic pairing means a ViewModel cannot emit a Unicast type that the Container does not understand.

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data class CounterUpdated(val count: Int) : CounterUnicast
}
```

```kotlin
class CounterViewModel : PulseViewModel<
    CounterState,
    CounterAction,
    CounterEvent,
    CounterBroadcast,
    CounterUnicast,
>(initialUiState = CounterState())
```

```kotlin
class CounterContainer(
    viewModels: List<PulseViewModel<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(viewModels = viewModels)
```

## Sending a Unicast

Call `unicast()` from inside a ViewModel. `PulseContainer` collects each registered ViewModel's `unicast` flow internally.

```kotlin
override fun onAction(uiAction: CounterAction) {
    when (uiAction) {
        CounterAction.Increment -> {
            repository.increment()
            unicast(CounterUnicast.CounterUpdated(repository.count.value))
        }
        CounterAction.Decrement -> {
            repository.decrement()
            unicast(CounterUnicast.CounterUpdated(repository.count.value))
        }
        CounterAction.Reset -> {
            repository.reset()
            unicast(CounterUnicast.CounterUpdated(repository.count.value))
        }
    }
}
```

## Receiving a Unicast

Override `onReceived()` in the Container. In the example below, the Container converts a Unicast into a Broadcast. It takes the Unicast it received from a ViewModel and sends it back out to all ViewModels.

```kotlin
override fun onReceived(unicast: CounterUnicast) {
    when (unicast) {
        is CounterUnicast.CounterUpdated ->
            broadcast(CounterBroadcast.CounterUpdated(unicast.count))
    }
}
```

## Unicast vs Broadcast vs Event

| | Unicast | Broadcast | Event |
|---|---|---|---|
| Direction | ViewModel -> Container | Container -> all ViewModels | ViewModel -> UI |
| Cardinality | Many-to-one | One-to-many | One-to-one |
| Purpose | Parent coordination after a child action | Cross-ViewModel notification | One-time UI side effects |
| Type parameter | `PulseUnicast` | `PulseBroadcast` | `PulseEvent` |

## Example: Sharing Counter Updates

Two counter ViewModels keep separate local repositories. They still share updates, through their parent Container:

```kotlin
sealed class CounterBroadcast : PulseBroadcast {
    data class CounterUpdated(val count: Int) : CounterBroadcast()
}

sealed interface CounterUnicast : PulseUnicast {
    data class CounterUpdated(val count: Int) : CounterUnicast
}
```

```kotlin
class CounterContainer(
    viewModels: List<PulseViewModel<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(viewModels = viewModels) {
    override fun onReceived(unicast: CounterUnicast) {
        when (unicast) {
            is CounterUnicast.CounterUpdated ->
                broadcast(CounterBroadcast.CounterUpdated(unicast.count))
        }
    }
}
```

```kotlin
class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Increment -> {
                repository.increment()
                unicast(CounterUnicast.CounterUpdated(repository.count.value))
            }
            CounterAction.Decrement -> {
                repository.decrement()
                unicast(CounterUnicast.CounterUpdated(repository.count.value))
            }
            CounterAction.Reset -> {
                repository.reset()
                unicast(CounterUnicast.CounterUpdated(repository.count.value))
            }
        }
    }

    override fun onReceive(broadcast: CounterBroadcast) {
        when (broadcast) {
            is CounterBroadcast.CounterUpdated -> repository.set(broadcast.count)
        }
    }
}
```

### Data flow

```mermaid
flowchart TB
    A["Counter A action"]
    A --> B["CounterViewModel.unicast(CounterUpdated(count))"]
    B --> C["CounterContainer.onReceived(CounterUpdated(count))"]
    C --> D["CounterContainer.broadcast(CounterBroadcast.CounterUpdated(count))"]
    D --> E["Counter A and Counter B receive the same count"]
```
