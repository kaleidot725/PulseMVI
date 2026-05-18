# Architecture

PulseMVI follows the MVI (Model-View-Intent) pattern and adds three Desktop-specific primitives: **Broadcast**, **Unicast**, and **View Refresh**.

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
        └──▶ Container.onUnicast(SaveRequested)
                  │
                  ├──▶ broadcast(...)
                  └──▶ refresh()
```

## View Refresh Flow

`Container.refresh()` forces the Compose view tree to reconstruct. Store states are **preserved** — only the Composables are re-created:

```
Container.refresh()
        │
        └──▶ PulseApp detects new key
                  │
                  └──▶ PulseContent re-created (via `key()`)
                            │
                            └──▶ Store.cancel() then Store re-subscribes
                                      │
                                      └──▶ onSetup() called again
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
| `PulseApp` | Compose wrapper that propagates container key |
| `PulseContent` | Compose wrapper that observes a Store |

## Lifecycle

```
PulseContent appears
        │
        └──▶ Store.state subscribed  ──▶  onSetup() called
                                               │
                                        coroutineScope active

PulseContent disappears
        │
        └──▶ Store.cancel() called
                  │
                  └──▶ coroutineScope cancelled + recreated
                            (Store is ready to be reused)
```

::: tip
`onSetup()` is called every time the Store is first subscribed to — including after a `refresh()`. Use it to start your data-collection coroutines.
:::
