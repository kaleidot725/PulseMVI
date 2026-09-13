# Composables

## PulseHost

```kotlin
@Composable
fun <Broadcast : PulseBroadcast, Unicast : PulseUnicast> PulseHost(
    container: PulseContainer<Broadcast, Unicast>,
    content: @Composable (
        onRefresh: () -> Unit,
        onBroadcast: (Broadcast) -> Unit,
    ) -> Unit = { _, _ -> },
)
```

`PulseContainer` をこのサブツリーにスコープします。自身は UI を描画しません。`PulseContent` がコンテンツを作り直す基準となる Container のキーを公開し、コンテンツブロックに `onRefresh` と `onBroadcast` を渡します。内側に置かれたすべての `PulseContent` が `container.refresh()` に反応します。

アプリには複数置くことができます。Container を所有する各 destination がそれぞれ自分の `PulseHost` を持つ、というのがデモの各画面の作り方です。

### パラメータ

| パラメータ | 型 | 説明 |
|---|---|---|
| `container` | `PulseContainer<Broadcast, Unicast>` | 観測する Container |
| `content` | `@Composable (onRefresh, onBroadcast) -> Unit` | 2 つのコールバックを受け取るコンテンツブロック |

### 例

```kotlin
PulseHost(container = appContainer) { onRefresh, onBroadcast ->
    Column {
        Button(onClick = { onBroadcast(AppBroadcast.Sync) }) {
            Text("Sync All")
        }
        Button(onClick = { onRefresh() }) {
            Text("Refresh View")
        }
        MyContent(viewModel = myViewModel)
    }
}
```

---

## PulseContent

```kotlin
@Composable
fun <
    State : PulseState,
    Action : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>
PulseContent(
    viewModel: PulseViewModel<State, Action, Event, Broadcast, Unicast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable (State, (Action) -> Unit) -> Unit = { _, _ -> },
)
```

`PulseViewModel` を観測し、コンテンツブロックに状態と Action のディスパッチャを渡します。インスタンスを最初に観測したときに `onSetup()` を実行します。キャンセルは決して行いません。`event` は単一消費者のチャネルなので、1 つのインスタンスは同時に 1 つの `PulseContent` からだけ観測してください。

### パラメータ

| パラメータ | 型 | 説明 |
|---|---|---|
| `viewModel` | `PulseViewModel<State, Action, Event, Broadcast, Unicast>` | 観測する ViewModel |
| `onEvent` | `(Event) -> Unit` | ViewModel が発行した一度きりの副作用ごとに呼ばれる |
| `content` | `@Composable (State, (Action) -> Unit) -> Unit` | 現在の状態を描画する。Action を送るためのディスパッチャを受け取る |

### ライフサイクルの挙動

- `PulseContent` は ViewModel をキャンセルしない。後始末は所有者の責務
- `LaunchedEffect(viewModel)` が `event` を収集し、常に最新の `onEvent` を経由して届ける
- `onSetup()` は `PulseContent` が ViewModel を最初に観測したときに一度だけ実行され、所有する `ViewModelStoreOwner` が破棄されるとスコープがキャンセルされる
- コンポジションを離れて戻ってきても（別のナビゲーション destination がルートを覆う場合など）`onSetup()` は繰り返されない
- `PulseHost` の内側では `key(containerKey)` で包まれ、`refresh()` で作り直される

### 例

```kotlin
PulseContent(
    viewModel = counterViewModel,
    onEvent = { event ->
        when (event) {
            is CounterEvent.ShowMessage ->
                scope.launch { snackbarHostState.showSnackbar(event.message) }
        }
    },
) { state, onAction ->
    Column {
        Text("Count: ${state.count}")
        Button(onClick = { onAction(CounterAction.Increment) }) {
            Text("+")
        }
    }
}
```
