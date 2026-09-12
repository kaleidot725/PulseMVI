# Container

`PulseContainer` は 1 つ以上の ViewModel の上に位置し、**Broadcast**、**Unicast の処理**、**View Refresh** の 3 つの調整機能を提供します。

## Container を作る

調整したい ViewModel のリストを渡します。

```kotlin
class AppContainer(
    viewModels: List<PulseViewModel<*, *, *, AppBroadcast, AppUnicast>>,
) : PulseContainer<AppBroadcast, AppUnicast>(viewModels = viewModels)
```

ViewModel と同じ階層で生成します。

```kotlin
val sidebarViewModel = rememberPulseViewModel { SidebarViewModel() }
val contentViewModel = rememberPulseViewModel { ContentViewModel() }
val container = rememberPulseContainer {
    AppContainer(viewModels = listOf(sidebarViewModel, contentViewModel))
}
```

## Broadcast

登録済みの**すべての** ViewModel へ、型付きメッセージを同時に送ります。

```kotlin
container.broadcast(AppBroadcast.UserLoggedOut)
```

リスト内のすべての ViewModel が `onReceive(AppBroadcast.UserLoggedOut)` を受け取り、それぞれ独立して反応できます。

### Broadcast を使う場面

- 複数の ViewModel にまたがる状態の同期（テーマ変更、ロケール変更など）
- 全体的な出来事の通知（セッション切れ、ネットワーク再接続など）
- 複数の ViewModel が必要とするデータの伝播（更新されたユーザープロフィールなど）

## View Refresh

`PulseHost` 配下の Compose ビューツリー全体を強制的に再構築します。

```kotlin
container.refresh()
```

::: tip 何がリセットされるか
- **Compose の状態**（Composable 内の `remember { }` など）は**リセットされる**
- **ViewModel の状態**（`PulseViewModel.state` の値）は**保持される**
:::

### Refresh を使う場面

- レイアウト全体に影響するテーマやロケールの変更を適用する
- 壊れた Compose の状態から復帰する
- 状態変化に反応しない Composable を強制的に作り直す

## PulseHost の中で使う

`PulseHost` は Container の内部キーを読み、コンテンツを `CompositionLocalProvider` で包みます。`PulseHost` 内の `PulseContent` は `refresh()` に自動的に反応します。

```kotlin
PulseHost(container = appContainer) { onRefresh, onBroadcast ->
    // onRefresh() は container.refresh() を呼ぶ
    // onBroadcast(b) は container.broadcast(b) を呼ぶ

    Button(onClick = { onBroadcast(AppBroadcast.Sync) }) {
        Text("Sync All")
    }
    Button(onClick = { onRefresh() }) {
        Text("Refresh View")
    }
}
```
