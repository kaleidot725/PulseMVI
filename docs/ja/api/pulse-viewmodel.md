# PulseViewModel

```kotlin
abstract class PulseViewModel<
    UiState : PulseState,
    UiAction : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>(
    initialUiState: UiState,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
)
```

1 画面または 1 セクションの UI 状態を管理する抽象基底クラスです。

親の Container へメッセージを送る必要があるときは `unicast()` を使います。

## プロパティ

### `state`

```kotlin
val state: StateFlow<UiState>
```

現在の UI 状態を表す、読み取り専用のホットな `StateFlow` です。収集しても ViewModel のライフサイクルは変わりません。

---

### `currentState`

```kotlin
val currentState: UiState
```

現在の UI 状態の同期的なスナップショットです。`state.value` と同じです。

---

### `event`

```kotlin
val event: Flow<Event>
```

`event()` で発行された一度きりの副作用のコールドな `Flow` です。`PulseContent` が収集します。各 Event は単一の収集者に届くので、消費されるものであり、後から来た収集者に再生されることはありません。

---

### `unicast`

```kotlin
val unicast: SharedFlow<Unicast>
```

`unicast()` で発行された、子から親への Unicast のホットなストリームです。

---

### `coroutineScope`

```kotlin
val coroutineScope: CoroutineScope
```

`SupervisorJob` とコンストラクタに渡されたディスパッチャに支えられた `CoroutineScope` です。ディスパッチャの既定値は `Dispatchers.Default` で、必要に応じて `Dispatchers.Main` やテスト用ディスパッチャを渡せます。このスコープは `cancel()` でキャンセルされ、作り直されます。

## メソッド

### `onSetup()`

```kotlin
open fun onSetup()
```

`PulseContent` が ViewModel を最初に観測したときに一度だけ呼ばれます。インスタンスがコンポジションより長く生きるため、コンポジションを離れて戻ってきても繰り返されません。データ収集のコルーチンを開始するためにオーバーライドしてください。それらは `coroutineScope` で動き、スコープがキャンセルされると止まります。

1 つのインスタンスは同時に 1 つの `PulseContent` からだけ観測してください。`event` は単一消費者のチャネルなので、2 つ目の観測者がいると、最初の観測者に届かない Event が出てきます。

---

### `onAction(uiAction)`

```kotlin
abstract fun onAction(uiAction: UiAction)
```

UI が Action を発行するたびに呼ばれます。非同期処理はここでコルーチンを起動してください。

---

### `onReceive(broadcast)`

```kotlin
open fun onReceive(broadcast: Broadcast)
```

親の `PulseContainer` が Broadcast を届けたときに呼ばれます。既定の実装は何もしません。

---

### `update(block)`

```kotlin
fun update(block: UiState.() -> UiState)
```

不変な状態更新を適用します。ラムダは現在の状態を `this` として受け取り、次の状態を返します。

```kotlin
update { copy(count = count + 1) }
```

---

### `event(effect)`

```kotlin
fun event(effect: Event)
```

一度きりの副作用を UI 層へ発行します。`PulseContent` の `onEvent` ラムダが収集します。

バッファリングされるため中断せず、発行順序も保たれます。`PulseContent` が収集していない間（別の destination に覆われている間など）に発行された Event はバッファで待機し、収集者が戻ってきたときに届きます。バッファは 64 件で、それを超えると最も古い Event が捨てられます。

---

### `unicast(unicast)`

```kotlin
fun unicast(unicast: Unicast)
```

子から親へのメッセージを発行します。親の `PulseContainer` が ViewModel の `unicast` Flow を収集し、`onReceived()` で受け取ります。

---

### `cancel()`

```kotlin
fun cancel()
```

`onSetup()` で開始した処理をキャンセルし、状態を保ったままスコープを新しいものに置き換えます。次にこのインスタンスを観測した `PulseContent` が `onSetup()` を再実行します。ViewModel が再び有効になる可能性があるときに使ってください。

---

### `close()`

```kotlin
fun close()
```

`onSetup()` で開始した処理を完全にキャンセルします。スコープは置き換えません。所有する `ViewModelStore` が破棄されるときに `onCleared()` がこれを呼ぶため、破棄された ViewModel がそれより長く生きる処理を起動することはありません。

## 例

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}

class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count ->
                update { copy(count = count) }
            }
        }
    }

    override fun onAction(uiAction: CounterAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterAction.Increment -> repository.increment()
                CounterAction.Decrement -> repository.decrement()
            }
        }
    }

    override fun onReceive(broadcast: CounterBroadcast) {
        when (broadcast) {
            CounterBroadcast.Reset -> update { CounterState() }
        }
    }
}
```

```kotlin
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}

class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Reset -> {
                repository.reset()
                unicast(CounterUnicast.ResetRequested)
            }
            CounterAction.Increment -> repository.increment()
            CounterAction.Decrement -> repository.decrement()
        }
    }
}
```
