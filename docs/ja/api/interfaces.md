# マーカーインターフェース

PulseMVI はマーカーインターフェースを使います。ジェネリクスのレベルで型安全性を保証するためです。どのインターフェースもメンバーを持ちません。型パラメータを制約するためだけに存在します。

## PulseState

```kotlin
interface PulseState
```

クラスを `PulseViewModel` が管理する UI 状態として印付けます。`data class` で実装してください。不変な更新のために `copy()` が使えます。

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

クラスを `PulseViewModel` に発行されるユーザーの意図として印付けます。`sealed class` で実装してください。取りうる Action をすべて列挙できます。

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

クラスを `PulseViewModel` から UI へ発行される一度きりの副作用として印付けます。`sealed class` で実装してください。

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

クラスを Broadcast メッセージとして印付けます。`PulseContainer` から、登録済みのすべての `PulseViewModel` インスタンスへ届けられます。`sealed class` で実装してください。

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

クラスを Unicast メッセージとして印付けます。子の `PulseViewModel` から親の `PulseContainer` へ発行されます。`sealed interface` または `sealed class` で実装してください。

```kotlin
sealed interface AppUnicast : PulseUnicast {
    data object SaveRequested : AppUnicast
    data class ItemSelected(val id: String) : AppUnicast
}
```
