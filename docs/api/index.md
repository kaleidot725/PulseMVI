# API Overview

PulseMVI exposes a small, focused API surface. The core artifact lives in `jp.kaleidot725.pulse.mvi`; `rememberPulseStore`, `rememberPulseContainer` and `rememberPulseNavEntryDecorators` come from `pulsemvi-navigation3`, in `jp.kaleidot725.pulse.mvi.navigation3`.

## Classes

| Class | Description |
|---|---|
| [`PulseStore`](/api/pulse-store) | Abstract base class for managing UI state |
| [`PulseContainer`](/api/pulse-container) | Coordinates multiple Stores |

## Composables

| Composable | Description |
|---|---|
| [`PulseHost`](/api/composables#pulsehost) | Scopes a Container to a subtree; enables refresh and broadcast callbacks |
| [`PulseContent`](/api/composables#pulsecontent) | Observes a Store; provides state and action dispatcher |
| [`rememberPulseStore`](/api/composables#rememberpulsestore) | Store owned by a `ViewModel` — `pulsemvi-navigation3` |
| [`rememberPulseContainer`](/api/composables#rememberpulsecontainer) | Container owned by a `ViewModel` — `pulsemvi-navigation3` |
| [`rememberPulseNavEntryDecorators`](/api/composables#rememberpulsenaventrydecorators) | `NavDisplay` decorators that scope Stores to a back stack entry — `pulsemvi-navigation3` |

## Marker Interfaces

| Interface | Description |
|---|---|
| [`PulseState`](/api/interfaces#pulsestate) | Marks a class as a Store's UI state |
| [`PulseAction`](/api/interfaces#pulseaction) | Marks a class as a user action |
| [`PulseEvent`](/api/interfaces#pulseevent) | Marks a class as a one-time side effect |
| [`PulseBroadcast`](/api/interfaces#pulsebroadcast) | Marks a class as a Container broadcast message |
| [`PulseUnicast`](/api/interfaces#pulseunicast) | Marks a class as a child-to-parent unicast message |
