# PulseContainer

```kotlin
abstract class PulseContainer<Broadcast : PulseBroadcast, Unicast : PulseUnicast>(
    viewModels: List<PulseViewModel<*, *, *, Broadcast, Unicast>>,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
)
```

複数の `PulseViewModel` インスタンスを調整します。Broadcast の配信、子からの Unicast の処理、View Refresh を提供します。

内部の Unicast 収集には、コンストラクタで別のディスパッチャが渡されない限り `Dispatchers.Default` を使います。

## メソッド

### `broadcast(broadcast)`

```kotlin
fun broadcast(broadcast: Broadcast)
```

生成時に登録されたすべての `PulseViewModel` の `onReceive()` を呼び、`broadcast` を届けます。

```kotlin
container.broadcast(AppBroadcast.UserLoggedOut)
```

---

### `onReceived(unicast)`

```kotlin
open fun onReceived(unicast: Unicast)
```

登録済みの `PulseViewModel` が Unicast を発行したときに呼ばれます。`PulseContainer` は各 ViewModel の `unicast` Flow を内部で収集し、値をこのフックに転送します。

```kotlin
override fun onReceived(unicast: AppUnicast) {
    when (unicast) {
        AppUnicast.SaveRequested -> broadcast(AppBroadcast.SaveStarted)
    }
}
```

---

### `refresh()`

```kotlin
fun refresh()
```

Container の内部キーを進め、`PulseHost` 内のすべての `PulseContent` ブロックを作り直させます。ViewModel の状態は保持され、Compose の状態だけが破棄されます。

```kotlin
container.refresh()
```

---

### `close()`

```kotlin
fun close()
```

Container のスコープをキャンセルし、ViewModel からの Unicast の収集を止めます。Container が完全に不要になったときに呼んでください。`rememberPulseContainer` を使っていれば、所有する `ViewModelStore` が破棄されるときに自動で呼ばれます。

```kotlin
container.close()
```

## 例

```kotlin
class AppContainer(
    viewModels: List<PulseViewModel<*, *, *, AppBroadcast, AppUnicast>>,
) : PulseContainer<AppBroadcast, AppUnicast>(viewModels = viewModels) {
    override fun onReceived(unicast: AppUnicast) {
        when (unicast) {
            AppUnicast.SaveRequested -> broadcast(AppBroadcast.SaveStarted)
        }
    }
}

// 使い方
val container = rememberPulseContainer {
    AppContainer(viewModels = listOf(sidebarViewModel, contentViewModel))
}

// すべての ViewModel へ送る
container.broadcast(AppBroadcast.Sync)

// ビューツリーを再構築する
container.refresh()
```
