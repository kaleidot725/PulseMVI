# Container

`PulseContainer` sits above one or more Stores and provides three coordination capabilities: **broadcast**, **unicast handling**, and **view refresh**.

## Creating a Container

Pass the list of Stores you want to coordinate:

```kotlin
class AppContainer(
    stores: List<PulseStore<*, *, *, AppBroadcast, AppUnicast>>,
) : PulseContainer<AppBroadcast, AppUnicast>(stores = stores)
```

Instantiate it at the same level as your Stores:

```kotlin
val sidebarStore = remember { SidebarStore() }
val contentStore = remember { ContentStore() }
val container = remember {
    AppContainer(stores = listOf(sidebarStore, contentStore))
}
```

## Broadcast

Send a typed message to **all** registered Stores simultaneously:

```kotlin
container.broadcast(AppBroadcast.UserLoggedOut)
```

Every Store in the list receives `onReceive(AppBroadcast.UserLoggedOut)` and can react independently.

### When to use Broadcast

- Synchronizing state across multiple Stores (e.g., theme change, locale change)
- Notifying all Stores of a global event (e.g., session expiry, network reconnected)
- Propagating data that multiple Stores need (e.g., updated user profile)

## View Refresh

Force the entire Compose view tree under `PulseApp` to reconstruct:

```kotlin
container.refresh()
```

::: tip What gets reset?
- **Compose state** (e.g., `remember { }` inside Composables) is **reset**
- **Store state** (values in `PulseStore.state`) is **preserved**
:::

### When to use Refresh

- Applying a theme or locale change that affects the whole layout
- Recovering from a corrupted Compose state
- Forcing re-creation of composables that don't respond to state changes

## Using inside PulseApp

`PulseApp` reads the Container's internal key and wraps content in a `CompositionLocalProvider`. `PulseContent` composables inside `PulseApp` automatically respond to `refresh()`:

```kotlin
PulseApp(container = appContainer) { onRefresh, onBroadcast ->
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
