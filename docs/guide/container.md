# Container

`PulseContainer` sits above one or more ViewModels. It provides three coordination capabilities: **broadcast**, **unicast handling**, and **view refresh**.

## Creating a Container

Define a class that takes the list of ViewModels you want to coordinate. Instantiate it at the same
level as those ViewModels.

```kotlin
class AppContainer(
    viewModels: List<PulseViewModel<*, *, *, AppBroadcast, AppUnicast>>,
) : PulseContainer<AppBroadcast, AppUnicast>(viewModels = viewModels)
```

```kotlin
val sidebarViewModel = rememberPulseViewModel { SidebarViewModel() }
val contentViewModel = rememberPulseViewModel { ContentViewModel() }
val container = rememberPulseContainer {
    AppContainer(viewModels = listOf(sidebarViewModel, contentViewModel))
}
```

## Broadcast

Send a typed message to **all** registered ViewModels simultaneously. Every ViewModel in the list
receives `onReceive(AppBroadcast.UserLoggedOut)`. Each reacts independently.

```kotlin
container.broadcast(AppBroadcast.UserLoggedOut)
```

### When to use Broadcast

- Synchronizing state across multiple ViewModels (e.g., theme change, locale change)
- Notifying all ViewModels of a global event (e.g., session expiry, network reconnected)
- Propagating data that multiple ViewModels need (e.g., updated user profile)

## View Refresh

Force the entire Compose view tree under `PulseHost` to reconstruct.

::: tip What gets reset?
- **Compose state** (e.g., `remember { }` inside Composables) is **reset**
- **ViewModel state** (values in `PulseViewModel.state`) is **preserved**
:::

```kotlin
container.refresh()
```

### When to use Refresh

- Applying a theme or locale change that affects the whole layout
- Recovering from a corrupted Compose state
- Forcing re-creation of composables that don't respond to state changes

## Using inside PulseHost

`PulseHost` reads the Container's internal key. It wraps content in a `CompositionLocalProvider`. `PulseContent` composables inside `PulseHost` automatically respond to `refresh()`:

```kotlin
PulseHost(container = appContainer) { onRefresh, onBroadcast ->
    // onRefresh() calls container.refresh()
    // onBroadcast(b) calls container.broadcast(b)

    Button(onClick = { onBroadcast(AppBroadcast.Sync) }) {
        Text("Sync All")
    }
    Button(onClick = { onRefresh() }) {
        Text("Refresh View")
    }
}
```
