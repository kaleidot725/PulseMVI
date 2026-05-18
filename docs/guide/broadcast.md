# Broadcast

Broadcast is PulseMVI's mechanism for delivering a typed message from a `PulseContainer` to all of its registered `PulseStore` instances simultaneously.

## Defining a Broadcast

Implement `PulseBroadcast` with a sealed class:

```kotlin
sealed class AppBroadcast : PulseBroadcast {
    data object Sync : AppBroadcast()
    data object UserLoggedOut : AppBroadcast()
    data class ThemeChanged(val isDark: Boolean) : AppBroadcast()
}
```

## Sending a Broadcast

Call `broadcast()` on the Container:

```kotlin
container.broadcast(AppBroadcast.ThemeChanged(isDark = true))
```

## Receiving a Broadcast

Override `onReceive()` in each Store that needs to react:

```kotlin
override fun onReceive(broadcast: AppBroadcast) {
    when (broadcast) {
        AppBroadcast.Sync -> syncData()
        AppBroadcast.UserLoggedOut -> update { AppState() }
        is AppBroadcast.ThemeChanged -> update { copy(isDark = broadcast.isDark) }
    }
}
```

## Broadcast vs Event

| | Broadcast | Event |
|---|---|---|
| Direction | Container → all Stores | Store → UI |
| Cardinality | One-to-many | One-to-one |
| Purpose | Cross-Store coordination | One-time UI side effects |
| Type parameter | `PulseBroadcast` | `PulseEvent` |

## Example: Syncing Multiple Stores

```kotlin
// Two Stores sharing the same Broadcast and Unicast types
class HeaderStore : PulseStore<HeaderState, HeaderAction, HeaderEvent, AppBroadcast, AppUnicast>(...) {
    override fun onReceive(broadcast: AppBroadcast) {
        if (broadcast is AppBroadcast.UserLoggedOut) update { HeaderState() }
    }
}

class SidebarStore : PulseStore<SidebarState, SidebarAction, SidebarEvent, AppBroadcast, AppUnicast>(...) {
    override fun onReceive(broadcast: AppBroadcast) {
        if (broadcast is AppBroadcast.UserLoggedOut) update { SidebarState() }
    }
}

// One call resets both Stores
container.broadcast(AppBroadcast.UserLoggedOut)
```
