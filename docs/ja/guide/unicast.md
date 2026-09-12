# Unicast

Unicast は、`PulseViewModel` から親の `PulseContainer` へ型付きメッセージを送るための PulseMVI の仕組みです。

直近のユーザー操作は子の ViewModel が担当するが、その結果を兄弟の ViewModel に Broadcast するといった後続の調整を親の Container が行う必要がある、という場面で使います。

## Unicast を定義する

sealed interface または sealed class で `PulseUnicast` を実装します。

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data class CounterUpdated(val count: Int) : CounterUnicast
}
```

Unicast の型は、Container と、その Container に登録されるすべての ViewModel で共有されます。

```kotlin
class CounterViewModel : PulseViewModel<
    CounterState,
    CounterAction,
    CounterEvent,
    CounterBroadcast,
    CounterUnicast,
>(initialUiState = CounterState())
```

```kotlin
class CounterContainer(
    viewModels: List<PulseViewModel<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(viewModels = viewModels)
```

このジェネリクスの組み合わせにより、Container が理解できない Unicast 型を ViewModel が発行することはできません。

## Unicast を送る

ViewModel の中から `unicast()` を呼びます。

```kotlin
override fun onAction(uiAction: CounterAction) {
    when (uiAction) {
        CounterAction.Increment -> {
            repository.increment()
            unicast(CounterUnicast.CounterUpdated(repository.count.value))
        }
        CounterAction.Decrement -> {
            repository.decrement()
            unicast(CounterUnicast.CounterUpdated(repository.count.value))
        }
        CounterAction.Reset -> {
            repository.reset()
            unicast(CounterUnicast.CounterUpdated(repository.count.value))
        }
    }
}
```

`PulseContainer` は登録済みの各 ViewModel の `unicast` Flow を内部で収集します。

## Unicast を受け取る

Container で `onReceived()` をオーバーライドします。

```kotlin
override fun onReceived(unicast: CounterUnicast) {
    when (unicast) {
        is CounterUnicast.CounterUpdated ->
            broadcast(CounterBroadcast.CounterUpdated(unicast.count))
    }
}
```

この例では、Container が ViewModel → Container の Unicast を、Container → ViewModel 群の Broadcast に変換しています。

## Unicast・Broadcast・Event の違い

| | Unicast | Broadcast | Event |
|---|---|---|---|
| 方向 | ViewModel -> Container | Container -> すべての ViewModel | ViewModel -> UI |
| 多重度 | 多対 1 | 1 対多 | 1 対 1 |
| 目的 | 子の操作後に親が行う調整 | ViewModel 横断の通知 | 一度きりの UI 副作用 |
| 型パラメータ | `PulseUnicast` | `PulseBroadcast` | `PulseEvent` |

## 例: カウンターの更新を共有する

2 つのカウンター ViewModel がそれぞれ別のリポジトリを持ちながら、親の Container を通じて更新を共有できます。

```kotlin
sealed class CounterBroadcast : PulseBroadcast {
    data class CounterUpdated(val count: Int) : CounterBroadcast()
}

sealed interface CounterUnicast : PulseUnicast {
    data class CounterUpdated(val count: Int) : CounterUnicast
}
```

```kotlin
class CounterContainer(
    viewModels: List<PulseViewModel<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(viewModels = viewModels) {
    override fun onReceived(unicast: CounterUnicast) {
        when (unicast) {
            is CounterUnicast.CounterUpdated ->
                broadcast(CounterBroadcast.CounterUpdated(unicast.count))
        }
    }
}
```

```kotlin
class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Increment -> {
                repository.increment()
                unicast(CounterUnicast.CounterUpdated(repository.count.value))
            }
            CounterAction.Decrement -> {
                repository.decrement()
                unicast(CounterUnicast.CounterUpdated(repository.count.value))
            }
            CounterAction.Reset -> {
                repository.reset()
                unicast(CounterUnicast.CounterUpdated(repository.count.value))
            }
        }
    }

    override fun onReceive(broadcast: CounterBroadcast) {
        when (broadcast) {
            is CounterBroadcast.CounterUpdated -> repository.set(broadcast.count)
        }
    }
}
```

データの流れは次のとおりです。

```text
Counter A の操作
    -> CounterViewModel.unicast(CounterUpdated(count))
    -> CounterContainer.onReceived(CounterUpdated(count))
    -> CounterContainer.broadcast(CounterBroadcast.CounterUpdated(count))
    -> Counter A と Counter B が同じ count を受け取る
```
