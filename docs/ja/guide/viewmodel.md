# ViewModel

`PulseViewModel` は PulseMVI の中核となる構成要素です。アプリの 1 画面、または 1 セクションの UI 状態を管理します。

## ViewModel を作る

5 つの型パラメータを指定して `PulseViewModel` を継承します。

```kotlin
class MyViewModel : PulseViewModel<MyState, MyAction, MyEvent, MyBroadcast, MyUnicast>(
    initialUiState = MyState(),
) {
    override fun onSetup() { /* ここでコルーチンを開始する */ }
    override fun onAction(uiAction: MyAction) { /* ユーザーの意図を処理する */ }
    override fun onReceive(broadcast: MyBroadcast) { /* Broadcast に反応する */ }
}
```

## ライフサイクルフック

### `onSetup()`

`PulseContent` が ViewModel を最初に観測したときに一度だけ呼ばれます。リポジトリの Flow のような長時間動くコルーチンの開始に使います。

::: tip
寿命はコンポジションではなく、ViewModel を保持する `ViewModelStoreOwner` に従います。コンポジションが再起動しても、別の destination に覆われても、サブツリーが Refresh されても、状態は保持され `onSetup()` は繰り返されません。オーナーが破棄されたときにスコープがキャンセルされます。

ViewModel を 1 つの destination に紐づける方法は [Navigation 3](/ja/guide/navigation3) を参照してください。
:::

```kotlin
override fun onSetup() {
    coroutineScope.launch {
        repository.data.collect { data ->
            update { copy(items = data) }
        }
    }
}
```

### `onAction(uiAction)`

UI が Action を発行するたびに呼ばれます。ブロックしないよう、非同期処理はコルーチンを起動して行います。

```kotlin
override fun onAction(uiAction: MyAction) {
    coroutineScope.launch {
        when (uiAction) {
            MyAction.Load -> loadData()
            is MyAction.Select -> selectItem(uiAction.id)
        }
    }
}
```

### `onReceive(broadcast)`

親の Container が Broadcast を届けたときに呼ばれます。状態の更新や Event の発行ができます。

```kotlin
override fun onReceive(broadcast: MyBroadcast) {
    when (broadcast) {
        MyBroadcast.Refresh -> update { copy(isRefreshing = true) }
        is MyBroadcast.UserChanged -> update { copy(userId = broadcast.id) }
    }
}
```

## 状態を更新する

`update { }` で次の不変な状態を生成します。ラムダは現在の状態を `this` として受け取ります。

```kotlin
update { copy(count = count + 1, isLoading = false) }
```

## Event を発行する

`event()` で一度きりの副作用を UI に送ります。

```kotlin
event(MyEvent.NavigateTo(Screen.Detail))
event(MyEvent.ShowError("Something went wrong"))
```

## 現在の状態を参照する

`currentState` で最新の状態スナップショットを同期的に読めます。

```kotlin
override fun onAction(uiAction: MyAction) {
    if (currentState.isLoading) return  // ガード
    coroutineScope.launch { /* ... */ }
}
```

## ライフサイクルを自前で扱う

`PulseContent` は、ViewModel がどう作られたかに関係なく `onSetup()` を実行します。変わるのは後始末です。`close()` を呼ぶのは `onCleared()` で、それを呼ぶのは `ViewModelStore` だけです。

ViewModel を素の `remember` で持つと誰も破棄しないので、自分でキャンセルしてください。Container も同様です。

::: warning
この場合、ViewModel はこのコンポジションと同じ長さだけ生きます。離れて戻ってくると新しいインスタンスが作られ、状態は失われます。それが問題になるなら `pulsemvi-navigation3` を追加してください。
:::

```kotlin
val viewModel = remember { CounterViewModel(repository) }
DisposableEffect(viewModel) {
    onDispose { viewModel.cancel() }
}

PulseContent(viewModel = viewModel) { state, onAction ->
    // Compose UI
}
```

```kotlin
val container = remember { CounterContainer(viewModels = listOf(viewModel)) }
DisposableEffect(container) {
    onDispose { container.close() }
}
```
