# Architecture

PulseMVI follows the MVI (Model-View-Intent) pattern and adds three coordination primitives: **Broadcast**, **Unicast**, and **View Refresh**.

## Data Flow

```
┌─────────────────────────────────────────────────────┐
│                   Compose UI                        │
│                                                     │
│   User Interaction                                  │
│        │                                            │
│        ▼                                            │
│   onAction(action)  ──────────▶  PulseStore         │
│                                      │              │
│                               onAction()            │
│                                      │              │
│                               update { }            │
│                                      │              │
│                            StateFlow<State>         │
│                                      │              │
│        ◀──────────────────────────── │              │
│   PulseContent re-renders            │              │
│                                      │              │
│                               event(effect)         │
│                                      │              │
│        ◀──────────── onEvent ──────── │              │
│   Handle side effect                                │
└─────────────────────────────────────────────────────┘
```

## Broadcast Flow

When multiple Stores need to react to the same event, use `PulseContainer.broadcast()`:

```
Container.broadcast(MyBroadcast.Sync)
        │
        ├──▶ StoreA.onReceive(Sync)  ──▶ update { }  ──▶ UI re-renders
        │
        └──▶ StoreB.onReceive(Sync)  ──▶ update { }  ──▶ UI re-renders
```

## Unicast Flow

When a child Store needs to notify its parent Container, use `PulseStore.unicast()`:

```
StoreA.unicast(MyUnicast.SaveRequested)
        │
        └──▶ Container.onReceived(SaveRequested)
                  │
                  ├──▶ broadcast(...)
                  └──▶ refresh()
```

## View Refresh Flow

`Container.refresh()` forces the Compose view tree to reconstruct. Store states are **preserved** — only the Composables are re-created:

```
Container.refresh()
        │
        └──▶ PulseHost detects new key
                  │
                  └──▶ PulseContent's rendered subtree re-created (via `key()`)
                            │
                            └──▶ Store is untouched; onSetup() is not repeated
```

## Component Responsibilities

| Component | Responsibility |
|---|---|
| `PulseState` | Immutable snapshot of UI data |
| `PulseAction` | User intent — what the user wants to do |
| `PulseEvent` | One-time side effect — navigation, dialog, snackbar |
| `PulseBroadcast` | Cross-Store notification from Container |
| `PulseUnicast` | Child-to-parent notification from Store |
| `PulseStore` | Owns state; handles actions and broadcasts; can emit unicasts |
| `PulseContainer` | Coordinates Stores; enables broadcast, unicast handling, and refresh |
| `PulseHost` | Compose wrapper that propagates container key |
| `PulseContent` | Compose wrapper that observes a Store |

## Lifecycle

```
rememberPulseStore creates the Store
        │
        └──▶ Owned by a ViewModel scoped to the ViewModelStoreOwner
                  │
                  └──▶ onSetup() called once
                                │
                                └──▶ coroutineScope active

ViewModelStoreOwner cleared
        │
        └──▶ ViewModel.onCleared()
                  │
                  └──▶ coroutineScope cancelled
                                (Store is discarded with its owner)
```

::: tip
`onSetup()` runs once when `rememberPulseStore` creates the Store, and the Store stays active for as long as its `ViewModelStoreOwner` lives. A configuration change never repeats setup, and neither does `refresh()`.

Which owner that is decides the Store's lifetime. Creating the Store under the host owner keeps it alive for the whole screen. Creating it inside a Navigation 3 destination, with `rememberPulseNavEntryDecorators()` as the `NavDisplay` decorators, scopes it to that back stack entry: covering the route with another destination keeps the Store, popping the route cancels it. The demo builds every destination that way.
:::
