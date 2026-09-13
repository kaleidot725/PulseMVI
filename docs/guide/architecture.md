# Architecture

PulseMVI follows the MVI (Model-View-Intent) pattern and adds three coordination primitives: **Broadcast**, **Unicast**, and **View Refresh**.

## Data Flow

```mermaid
flowchart TB
    subgraph UI["Compose UI"]
        A["User interaction"]
    end
    subgraph VM["PulseViewModel"]
        OA["onAction()"]
        U["update { }"]
        S["StateFlow&lt;State&gt;"]
        EV["event(effect)"]
    end
    subgraph OUT["Compose UI"]
        R["PulseContent re-renders"]
        E["Side effect handled"]
    end
    A -- "onAction(action)" --> OA
    OA --> U --> S --> R
    OA --> EV -- "onEvent" --> E
```

## Broadcast Flow

When multiple ViewModels need to react to the same event, use `PulseContainer.broadcast()`:

```mermaid
flowchart LR
    C["Container.broadcast(Sync)"]
    C --> A["ViewModelA.onReceive(Sync)"] --> AU["update { }"] --> AR["UI re-renders"]
    C --> B["ViewModelB.onReceive(Sync)"] --> BU["update { }"] --> BR["UI re-renders"]
```

## Unicast Flow

When a child ViewModel needs to notify its parent Container, use `PulseViewModel.unicast()`:

```mermaid
flowchart LR
    A["ViewModelA.unicast(SaveRequested)"] --> C["Container.onReceived(SaveRequested)"]
    C --> B["broadcast(...)"]
    C --> R["refresh()"]
```

## View Refresh Flow

`Container.refresh()` forces the Compose view tree to reconstruct. ViewModel states are **preserved** — only the Composables are re-created:

```mermaid
flowchart TB
    R["Container.refresh()"] --> H["PulseHost sees a new key"]
    H --> P["PulseContent subtree re-created via key()"]
    P --> V["ViewModel untouched<br/>onSetup() is not repeated"]
```

## Component Responsibilities

| Component | Responsibility |
|---|---|
| `PulseState` | Immutable snapshot of UI data |
| `PulseAction` | User intent — what the user wants to do |
| `PulseEvent` | One-time side effect — navigation, dialog, snackbar |
| `PulseBroadcast` | Cross-ViewModel notification from Container |
| `PulseUnicast` | Child-to-parent notification from ViewModel |
| `PulseViewModel` | Owns state; handles actions and broadcasts; can emit unicasts |
| `PulseContainer` | Coordinates ViewModels; enables broadcast, unicast handling, and refresh |
| `PulseHost` | Compose wrapper that propagates container key |
| `PulseContent` | Compose wrapper that observes a ViewModel |

## Lifecycle

::: tip
`onSetup()` runs once when `PulseContent` first observes the ViewModel, and the ViewModel stays active for as long as its `ViewModelStoreOwner` lives. A composition restart never repeats setup, and neither does `refresh()`.

Which owner that is decides the ViewModel's lifetime. Creating the ViewModel under the host owner keeps it alive for the whole screen. Creating it inside a Navigation 3 destination, with `rememberPulseNavEntryDecorators()` as the `NavDisplay` decorators, scopes it to that back stack entry: covering the route with another destination keeps the ViewModel, popping the route cancels it. The demo builds every destination that way.
:::

```mermaid
flowchart TB
    subgraph create["Creation"]
        direction TB
        A["rememberPulseViewModel creates the ViewModel"]
        A --> B["Kept in the owner's ViewModelStore"]
        B --> C["PulseContent observes it"]
        C --> D["onSetup() runs once"]
        D --> E["coroutineScope active"]
    end
```

```mermaid
flowchart TB
    subgraph clear["Teardown"]
        direction TB
        F["ViewModelStoreOwner cleared"]
        F --> G["ViewModel.onCleared()"]
        G --> H["coroutineScope cancelled"]
    end
```
