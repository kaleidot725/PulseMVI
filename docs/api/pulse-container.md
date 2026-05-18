# PulseContainer

```kotlin
abstract class PulseContainer<Broadcast : PulseBroadcast, Unicast : PulseUnicast>(
    stores: List<PulseStore<*, *, *, Broadcast, Unicast>>,
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

### `onReceived(unicast)`

```kotlin
open fun onReceived(unicast: Unicast)
```

Called when a registered `PulseStore` emits an unicast. `PulseContainer` collects each Store's `unicast` flow internally and forwards each value to this hook.

```kotlin
override fun onReceived(unicast: AppUnicast) {
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
    stores: List<PulseStore<*, *, *, AppBroadcast, AppUnicast>>,
) : PulseContainer<AppBroadcast, AppUnicast>(stores = stores) {
    override fun onReceived(unicast: AppUnicast) {
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
