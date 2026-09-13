# Navigation 3

`pulsemvi-navigation3` は任意のアーティファクトです。追加されるのは 3 つの Composable だけです。スコープ内の `ViewModelStoreOwner` の下に `PulseViewModel` や `PulseContainer` を生成する 2 つと、そのオーナーをバックスタックエントリにするために `NavDisplay` が必要とするデコレータを返す 1 つです。

コアのアーティファクトはこれに依存しないので、`pulsemvi` 単体では Compose ランタイム、`androidx.lifecycle`、コルーチンにしか依存しません。

## 1. 依存を追加する

コアと `pulsemvi-navigation3` を追加します。Navigation 3 と lifecycle のアーティファクトも一緒に入るため、`navigation3-ui`、`lifecycle-viewmodel-compose`、`lifecycle-viewmodel-navigation3` を自分で宣言する必要はありません。

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.github.kaleidot725:pulsemvi:<version>")
    implementation("com.github.kaleidot725:pulsemvi-navigation3:<version>")
}
```

## 2. ルートを定義する

ルートは任意の `NavKey` です。バックスタックは `SnapshotStateList` で保持します。

```kotlin
sealed interface Route : NavKey {
    data object Counter : Route

    data class CounterDetails(val count: Int) : Route
}

@Composable
fun App() {
    val backStack = remember { mutableStateListOf<Route>(Route.Counter) }
    val popLast: () -> Unit = {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    // ...
}
```

## 3. `NavDisplay` にデコレータを渡す

ViewModel をバックスタックにスコープするのはこのステップです。

::: warning
`NavDisplay` の `entryDecorators` の既定値は saveable state holder のみです。ViewModel のデコレータだけを渡すと saveable state が失われるため、[`rememberPulseNavEntryDecorators`](/ja/api/navigation3#rememberpulsenaventrydecorators) は両方を返します。リストを自分で組み立てるのではなく、こちらを使ってください。
:::

```kotlin
NavDisplay(
    backStack = backStack,
    onBack = popLast,
    entryDecorators = rememberPulseNavEntryDecorators(),
    entryProvider =
        entryProvider {
            entry<Route.Counter> {
                CounterScreen(onShowDetails = { backStack.add(Route.CounterDetails(it)) })
            }
            entry<Route.CounterDetails> { route ->
                CounterDetailScreen(count = route.count, onBack = popLast)
            }
        },
)
```

## 4. destination の中で ViewModel を生成する

各 destination が必要なものを自分で作ります。`NavDisplay` より上では何も作りません。それがライフタイムをルートに結びつけるための要点です。

```kotlin
@Composable
fun CounterScreen(onShowDetails: (Int) -> Unit) {
    val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
    val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

    PulseHost(container = container) { onRefresh, onBroadcast ->
        PulseContent(viewModel = viewModel) { state, onAction ->
            // Compose UI
        }
    }
}
```

## 得られる挙動

| 出来事 | 何が起きるか |
|---|---|
| ルートが push される | ViewModel が生成され、`PulseContent` が `onSetup()` を一度実行する |
| 別の destination で覆われる | ViewModel は保持される。状態も実行中のコルーチンも無傷 |
| 戻ってくる | 同じインスタンスが見つかる。`onSetup()` は繰り返されない |
| ルートが pop される | エントリの `ViewModelStore` が破棄され、`onCleared()` がスコープをキャンセルして Container を close する |
| オーナーが生きたままコンポジションが再起動する | ViewModel は再利用され、状態は維持される |

## 同じ型の ViewModel を 2 つ置く

`rememberPulseViewModel` の key の既定値は ViewModel の完全修飾クラス名で、key はグローバルではなくオーナー単位で一意です。同じオーナーの下に同じ型のインスタンスを 2 つ置くと衝突します。明示的な key を与えてください。デモは 4 つのエリアがすべて同じクラスなので、4 つすべてでこれを行っています。

```kotlin
val left = rememberPulseViewModel(key = "left") { CounterViewModel(leftRepository) }
val right = rememberPulseViewModel(key = "right") { CounterViewModel(rightRepository) }
```

## Navigation 3 を使わない場合

このアーティファクトを使う必要はありません。`PulseViewModel` は `androidx.lifecycle.ViewModel` を継承しているので、`viewModel()` や `koinViewModel()` でも同じように生成でき、`PulseContent` はどう作られたかに関係なく `onSetup()` を実行します。変わるのは後始末の方です。`close()` は `onCleared()` から呼ばれ、それを呼ぶのは `ViewModelStore` だけです。[ライフサイクルを自前で扱う](/ja/guide/viewmodel#ライフサイクルを自前で扱う) を参照してください。

## 次のステップ

- [Navigation 3 API](/ja/api/navigation3) — 完全なシグネチャとオーナー解決のルール
- [ViewModel](/ja/guide/viewmodel) — ライフサイクルフックと状態更新
- [Container](/ja/guide/container) — Broadcast と Refresh
