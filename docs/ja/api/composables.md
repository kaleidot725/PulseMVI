# Composable

## rememberPulseViewModel

::: tip アーティファクト
`rememberPulseViewModel`、`rememberPulseContainer`、`rememberPulseNavEntryDecorators` は `pulsemvi-navigation3` のパッケージ `jp.kaleidot725.pulse.mvi.navigation3` にあります。コアのアーティファクトでは ViewModel のライフタイムは呼び出し側に委ねられます — [ViewModel](/ja/guide/viewmodel) を参照してください。
:::

```kotlin
@Composable
inline fun <reified ViewModel : PulseViewModel<*, *, *, *, *>> rememberPulseViewModel(
    key: String? = null,
    noinline factory: () -> ViewModel,
): ViewModel
```

現在の `ViewModelStoreOwner` の `ViewModelStore` に保持される ViewModel を生成します。コンポジションが再起動しても作り直されず、同じインスタンスが再利用されます。

### パラメータ

| パラメータ | 型 | 説明 |
|---|---|---|
| `key` | `String?` | オーナー内で一意なキー。既定値は ViewModel の完全修飾クラス名 |
| `factory` | `() -> ViewModel` | オーナーごとに一度だけ呼ばれ、ViewModel を生成する |

### ライフサイクルの挙動

- ViewModel は最初のコンポジションで生成され、同じオーナー配下の以降のコンポジションでは再利用される
- `onSetup()` は `PulseContent` が ViewModel を最初に観測したときに一度だけ実行され、コンポジションを作り直しても繰り返されない
- オーナーの `ViewModelStore` が破棄されると ViewModel のスコープはキャンセルされる
- 状態はメモリ上にのみ保持され、プロセス終了後には復元されない

### オーナーの解決

内部では `LocalViewModelStoreOwner.current` を読み、そのオーナーの `ViewModelStore` に `key` でインスタンスを保持します。したがって呼び出し箇所でスコープにあるオーナーが ViewModel の寿命を決め、オーナーを変えるものはすべて ViewModel の寿命を変えます。

| スコープにあるオーナー | ViewModel の寿命 |
|---|---|
| Compose Desktop の `Window` | 画面が生きている間。コンポジションの再起動をまたいで生き残る |
| Navigation 3 のエントリ（`NavDisplay` の `entryDecorators` に `rememberPulseNavEntryDecorators()` を渡した場合） | ルートがバックスタックにある間。ルートが pop されるとキャンセルされる |
| `CompositionLocalProvider(LocalViewModelStoreOwner provides ...)` で自分で与えたもの | そのオーナーを保持している間 |
| ホストが何も提供しない | `rememberPulseViewModel` はメッセージ付きで失敗する。寿命を所有するものが無いので、オーナーを与えるかコアのみで使う |

#### 計画しておくべき 2 つの帰結

- **`key` はグローバルではなくオーナー単位で一意です。** 同じオーナーの下に同じ型の ViewModel を 2 つ置くと衝突します。既定のキーはクラス名です。明示的なキーを与えるか、別のオーナーの下に置いてください
- **オーナーが破棄されるまで、ViewModel がオーナーから取り除かれることはありません。** 長生きするオーナーの下で ViewModel を生成し続けると、オーナーの寿命の間ずっと溜まっていきます。画面が二度と使わない ViewModel を作るなら、より狭いオーナーにスコープしてください

::: tip
テストでは独自のオーナーを与えることで両方の側面を確認できます。コンポジションの作り直しをまたいで保持すればコンポジションの再起動を、破棄すれば画面が消える状況を再現できます。
:::

### 例

```kotlin
val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }

// 同じ ViewModel 型のインスタンスを 2 つ置くには別々のキーが必要
val left = rememberPulseViewModel(key = "left") { CounterViewModel(leftRepository) }
val right = rememberPulseViewModel(key = "right") { CounterViewModel(rightRepository) }

// 画面全体ではなく Navigation 3 の destination にスコープする
NavDisplay(
    backStack = backStack,
    entryDecorators = rememberPulseNavEntryDecorators(),
    entryProvider = entryProvider {
        entry<Route.Counter> {
            val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
            // ...
        }
    },
)
```

## rememberPulseContainer

```kotlin
@Composable
inline fun <reified Container : PulseContainer<*, *>> rememberPulseContainer(
    key: String? = null,
    noinline factory: () -> Container,
): Container
```

コンポジションの再起動をまたいで生き残る Container を生成し、Unicast の購読を維持します。オーナーの `ViewModelStore` が破棄されるときに `PulseContainer.close()` が呼ばれます。

### パラメータ

| パラメータ | 型 | 説明 |
|---|---|---|
| `key` | `String?` | オーナー内で一意なキー。既定値は Container の完全修飾クラス名 |
| `factory` | `() -> Container` | オーナーごとに一度だけ呼ばれ、Container を生成する |

### 例

```kotlin
val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }
```

## rememberPulseNavEntryDecorators

```kotlin
@Composable
fun <T : Any> rememberPulseNavEntryDecorators(): List<NavEntryDecorator<T>>
```

ViewModel をバックスタックエントリにスコープするために `NavDisplay` が必要とする `NavEntryDecorator` のリストです。saveable state holder のデコレータと ViewModel のデコレータの 2 つを含みます。

`NavDisplay` の `entryDecorators` の既定値は saveable state holder のみなので、ViewModel のデコレータだけを渡すと saveable state が黙って失われます。この関数は両方を返します。

```kotlin
NavDisplay(
    backStack = backStack,
    entryDecorators = rememberPulseNavEntryDecorators(),
    entryProvider = entryProvider {
        entry<Route.Counter> {
            // このエントリにスコープされる。ルートが pop されるとキャンセルされる
            val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
            // ...
        }
    },
)
```

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
