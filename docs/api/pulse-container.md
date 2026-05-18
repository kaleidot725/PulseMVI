# PulseContainer

```kotlin
abstract class PulseContainer<Broadcast : PulseBroadcast>(
    stores: List<PulseStore<*, *, *, Broadcast>>,
)
```

Coordinates multiple `PulseStore` instances. Provides broadcast delivery, child unicast handling, and view refresh.

## Methods

### `broadcast(broadcast)`

```kotlin
fun broadcast(broadcast: Broadcast)
```

Delivers `broadcast` to every `PulseStore` registered at construction time by calling each Store's `onReceive()`.

```kotlin
container.broadcast(AppBroadcast.UserLoggedOut)
```

---

### `onUnicast(unicast)`

```kotlin
open fun onUnicast(unicast: PulseUnicast)
```

Called when a registered `PulseStore` emits an unicast. `PulseContainer` collects each Store's `unicast` flow internally and forwards each value to this hook.

```kotlin
override fun onUnicast(unicast: PulseUnicast) {
    when (unicast) {
        AppUnicast.SaveRequested -> broadcast(AppBroadcast.SaveStarted)
    }
}
```

---

### `refresh()`

```kotlin
fun refresh()
```

Assigns a new UUID to the container's internal key, causing `PulseApp` to trigger a recomposition of all `PulseContent` blocks inside it. Store state is preserved; only Compose state is discarded.

```kotlin
container.refresh()
```

## Example

```kotlin
class AppContainer(
    stores: List<PulseStore<*, *, *, AppBroadcast>>,
) : PulseContainer<AppBroadcast>(stores = stores) {
    override fun onUnicast(unicast: PulseUnicast) {
        when (unicast) {
            AppUnicast.SaveRequested -> broadcast(AppBroadcast.SaveStarted)
        }
    }
}

// Usage
val container = remember {
    AppContainer(stores = listOf(sidebarStore, contentStore))
}

// Send to all Stores
container.broadcast(AppBroadcast.Sync)

// Reconstruct view tree
container.refresh()
```
