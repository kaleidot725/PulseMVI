# Broadcast

Broadcast は、`PulseContainer` から登録済みのすべての `PulseViewModel` インスタンスへ、型付きメッセージを同時に届けるための PulseMVI の仕組みです。

## Broadcast を定義する

sealed class で `PulseBroadcast` を実装します。

```kotlin
sealed class AppBroadcast : PulseBroadcast {
    data object Sync : AppBroadcast()
    data object UserLoggedOut : AppBroadcast()
    data class ThemeChanged(val isDark: Boolean) : AppBroadcast()
}
```

## Broadcast を送る

Container の `broadcast()` を呼びます。

```kotlin
container.broadcast(AppBroadcast.ThemeChanged(isDark = true))
```

## Broadcast を受け取る

反応が必要な各 ViewModel で `onReceive()` をオーバーライドします。

```kotlin
override fun onReceive(broadcast: AppBroadcast) {
    when (broadcast) {
        AppBroadcast.Sync -> syncData()
        AppBroadcast.UserLoggedOut -> update { AppState() }
        is AppBroadcast.ThemeChanged -> update { copy(isDark = broadcast.isDark) }
    }
}
```

## Broadcast と Event の違い

| | Broadcast | Event |
|---|---|---|
| 方向 | Container → すべての ViewModel | ViewModel → UI |
| 多重度 | 1 対多 | 1 対 1 |
| 目的 | ViewModel 横断の調整 | 一度きりの UI 副作用 |
| 型パラメータ | `PulseBroadcast` | `PulseEvent` |

## 例: 複数の ViewModel を同期する

```kotlin
// 同じ Broadcast と Unicast の型を共有する 2 つの ViewModel
class HeaderViewModel : PulseViewModel<HeaderState, HeaderAction, HeaderEvent, AppBroadcast, AppUnicast>(...) {
    override fun onReceive(broadcast: AppBroadcast) {
        if (broadcast is AppBroadcast.UserLoggedOut) update { HeaderState() }
    }
}

class SidebarViewModel : PulseViewModel<SidebarState, SidebarAction, SidebarEvent, AppBroadcast, AppUnicast>(...) {
    override fun onReceive(broadcast: AppBroadcast) {
        if (broadcast is AppBroadcast.UserLoggedOut) update { SidebarState() }
    }
}

// 1 回の呼び出しで両方の ViewModel がリセットされる
container.broadcast(AppBroadcast.UserLoggedOut)
```
