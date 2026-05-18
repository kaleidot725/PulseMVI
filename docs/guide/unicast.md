# Unicast

Unicast is PulseMVI's mechanism for sending a typed message from a `PulseStore` up to its parent `PulseContainer`.

Use it when a child Store owns the immediate user action, but the parent Container needs to coordinate a follow-up such as broadcasting the result to sibling Stores.

## Defining a Unicast

Implement `PulseUnicast` with a sealed interface or sealed class:

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data class CounterUpdated(val count: Int) : CounterUnicast
}
```

The same Unicast type is shared by the Container and every Store registered in that Container:

```kotlin
class CounterStore : PulseStore<
    CounterState,
    CounterAction,
    CounterEvent,
    CounterBroadcast,
    CounterUnicast,
>(initialUiState = CounterState())
```

```kotlin
class CounterContainer(
    stores: List<PulseStore<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(stores = stores)
```

This generic pairing means a Store cannot emit a Unicast type that the Container does not understand.

## Sending a Unicast

Call `unicast()` from inside a Store:

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

`PulseContainer` collects each registered Store's `unicast` flow internally.

## Receiving a Unicast

Override `onReceived()` in the Container:

```kotlin
override fun onReceived(unicast: CounterUnicast) {
    when (unicast) {
        is CounterUnicast.CounterUpdated ->
            broadcast(CounterBroadcast.CounterUpdated(unicast.count))
    }
}
```

In this example, the Container converts a Store-to-Container Unicast into a Container-to-Stores Broadcast.

## Unicast vs Broadcast vs Event

| | Unicast | Broadcast | Event |
|---|---|---|---|
| Direction | Store -> Container | Container -> all Stores | Store -> UI |
| Cardinality | Many-to-one | One-to-many | One-to-one |
| Purpose | Parent coordination after a child action | Cross-Store notification | One-time UI side effects |
| Type parameter | `PulseUnicast` | `PulseBroadcast` | `PulseEvent` |

## Example: Sharing Counter Updates

Two counter Stores can keep separate local repositories while sharing updates through their parent Container:

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
    stores: List<PulseStore<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(stores = stores) {
    override fun onReceived(unicast: CounterUnicast) {
        when (unicast) {
            is CounterUnicast.CounterUpdated ->
                broadcast(CounterBroadcast.CounterUpdated(unicast.count))
        }
    }
}
```

```kotlin
class CounterStore(
    private val repository: CounterRepository,
) : PulseStore<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
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

The data flow is:

```text
Counter A action
    -> CounterStore.unicast(CounterUpdated(count))
    -> CounterContainer.onReceived(CounterUpdated(count))
    -> CounterContainer.broadcast(CounterBroadcast.CounterUpdated(count))
    -> Counter A and Counter B receive the same count
```
