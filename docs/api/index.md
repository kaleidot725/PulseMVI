# API Overview

PulseMVI exposes a small, focused API surface. Everything you need is in the `jp.kaleidot725.pulse.mvi` package.

## Classes

| Class | Description |
|---|---|
| [`PulseStore`](/api/pulse-store) | Abstract base class for managing UI state |
| [`PulseContainer`](/api/pulse-container) | Coordinates multiple Stores |

## Composables

| Composable | Description |
|---|---|
| [`PulseApp`](/api/composables#pulseapp) | Wraps a Container; enables refresh and broadcast callbacks |
| [`PulseContent`](/api/composables#pulsecontent) | Observes a Store; provides state and action dispatcher |

## Marker Interfaces

| Interface | Description |
|---|---|
| [`PulseState`](/api/interfaces#pulsestate) | Marks a class as a Store's UI state |
| [`PulseAction`](/api/interfaces#pulseaction) | Marks a class as a user action |
| [`PulseEvent`](/api/interfaces#pulseevent) | Marks a class as a one-time side effect |
| [`PulseBroadcast`](/api/interfaces#pulsebroadcast) | Marks a class as a Container broadcast message |
| [`PulseUnicast`](/api/interfaces#pulseunicast) | Marks a class as a child-to-parent unicast message |
