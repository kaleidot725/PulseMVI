# PulseContainer

```kotlin
abstract class PulseContainer<Broadcast : PulseBroadcast, Unicast : PulseUnicast>(
    viewModels: List<PulseViewModel<*, *, *, Broadcast, Unicast>>,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
)
```

Coordinates multiple `PulseViewModel` instances. Provides broadcast delivery, child unicast handling, and view refresh.

The internal unicast collector uses `Dispatchers.Default`. Another dispatcher can be passed to the constructor.

## Methods

### `broadcast(broadcast)`

```kotlin
fun broadcast(broadcast: Broadcast)
```

Delivers `broadcast` to every `PulseViewModel` registered at construction time. It calls each ViewModel's `onReceive()`.

```kotlin
container.broadcast(AppBroadcast.UserLoggedOut)
```

---

### `onReceived(unicast)`

```kotlin
open fun onReceived(unicast: Unicast)
```

Called when a registered `PulseViewModel` emits an unicast. `PulseContainer` collects each ViewModel's `unicast` flow internally. It forwards each value to this hook.

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

Bumps the container's internal key. `PulseHost` then re-creates every `PulseContent` block inside it. ViewModel state is preserved. Only Compose state is discarded.

```kotlin
container.refresh()
```

---

### `close()`

```kotlin
fun close()
```

Cancels the Container scope. Stops collecting Unicast messages from the ViewModels. Call it when the Container is gone for good. `rememberPulseContainer` calls it for you when the owning `ViewModelStore` is cleared.

```kotlin
container.close()
```

## Example

```kotlin
class AppContainer(
    viewModels: List<PulseViewModel<*, *, *, AppBroadcast, AppUnicast>>,
) : PulseContainer<AppBroadcast, AppUnicast>(viewModels = viewModels) {
    override fun onReceived(unicast: AppUnicast) {
        when (unicast) {
            AppUnicast.SaveRequested -> broadcast(AppBroadcast.SaveStarted)
        }
    }
}

// Usage
val container = rememberPulseContainer {
    AppContainer(viewModels = listOf(sidebarViewModel, contentViewModel))
}

// Send to all ViewModels
container.broadcast(AppBroadcast.Sync)

// Reconstruct view tree
container.refresh()
```
