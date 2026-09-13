# Marker Interfaces

PulseMVI uses marker interfaces. They enforce type safety at the generic level. Each interface has no members. They exist solely to constrain type parameters.

## PulseState

```kotlin
interface PulseState
```

Marks a class as the UI state managed by a `PulseViewModel`. Implement with a `data class`. That makes `copy()` available for immutable updates.

```kotlin
data class CounterState(
    val count: Int = 0,
    val isLoading: Boolean = false,
) : PulseState
```

---

## PulseAction

```kotlin
interface PulseAction
```

Marks a class as a user intent dispatched to a `PulseViewModel`. Implement with a `sealed class`. That lets you enumerate all possible actions exhaustively.

```kotlin
sealed class CounterAction : PulseAction {
    data object Increment : CounterAction()
    data object Decrement : CounterAction()
    data class SetValue(val value: Int) : CounterAction()
}
```

---

## PulseEvent

```kotlin
interface PulseEvent
```

Marks a class as a one-time side effect emitted from a `PulseViewModel` to the UI. Implement with a `sealed class`.

```kotlin
sealed class CounterEvent : PulseEvent {
    data class ShowMessage(val message: String) : CounterEvent()
    data object NavigateBack : CounterEvent()
}
```

---

## PulseBroadcast

```kotlin
interface PulseBroadcast
```

Marks a class as a broadcast message. `PulseContainer` delivers it to all registered `PulseViewModel` instances. Implement with a `sealed class`.

```kotlin
sealed class AppBroadcast : PulseBroadcast {
    data object UserLoggedOut : AppBroadcast()
    data class ThemeChanged(val isDark: Boolean) : AppBroadcast()
}
```

---

## PulseUnicast

```kotlin
interface PulseUnicast
```

Marks a class as a unicast message. A child `PulseViewModel` emits it to its parent `PulseContainer`. Implement with a `sealed interface` or `sealed class`.

```kotlin
sealed interface AppUnicast : PulseUnicast {
    data object SaveRequested : AppUnicast
    data class ItemSelected(val id: String) : AppUnicast
}
```
