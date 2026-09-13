# アーキテクチャ

PulseMVI は MVI（Model-View-Intent）パターンに従います。そこに **Broadcast**、**Unicast**、**View Refresh** の 3 つの調整プリミティブを加えています。

## データフロー

```mermaid
flowchart TB
    subgraph UI["Compose UI"]
        A["ユーザー操作"]
    end
    subgraph VM["PulseViewModel"]
        OA["onAction()"]
        U["update { }"]
        S["StateFlow&lt;State&gt;"]
        EV["event(effect)"]
    end
    subgraph OUT["Compose UI"]
        R["PulseContent が再描画"]
        E["副作用を処理"]
    end
    A -- "onAction(action)" --> OA
    OA --> U --> S --> R
    OA --> EV -- "onEvent" --> E
```

## Broadcast のフロー

複数の ViewModel が同じ出来事に反応する必要があるときは `PulseContainer.broadcast()` を使います。

```mermaid
flowchart LR
    C["Container.broadcast(Sync)"]
    C --> A["ViewModelA.onReceive(Sync)"] --> AU["update { }"] --> AR["UI が再描画"]
    C --> B["ViewModelB.onReceive(Sync)"] --> BU["update { }"] --> BR["UI が再描画"]
```

## Unicast のフロー

子の ViewModel が親の Container に知らせる必要があるときは `PulseViewModel.unicast()` を使います。

```mermaid
flowchart LR
    A["ViewModelA.unicast(SaveRequested)"] --> C["Container.onReceived(SaveRequested)"]
    C --> B["broadcast(...)"]
    C --> R["refresh()"]
```

## View Refresh のフロー

`Container.refresh()` は Compose のビューツリーを強制的に再構築します。ViewModel の状態は**保持**されます。Composable だけが作り直されます。

```mermaid
flowchart TB
    R["Container.refresh()"] --> H["PulseHost が新しいキーを検知"]
    H --> P["PulseContent のサブツリーが key() 経由で再生成"]
    P --> V["ViewModel は無傷<br/>onSetup() は繰り返されない"]
```

## 各コンポーネントの責務

| コンポーネント | 責務 |
|---|---|
| `PulseState` | UI データの不変スナップショット |
| `PulseAction` | ユーザーの意図 — ユーザーが何をしたいか |
| `PulseEvent` | 一度きりの副作用 — ナビゲーション、ダイアログ、スナックバー |
| `PulseBroadcast` | Container から ViewModel 群への横断的な通知 |
| `PulseUnicast` | ViewModel から親への通知 |
| `PulseViewModel` | 状態を所有し、Action と Broadcast を処理し、Unicast を発行できる |
| `PulseContainer` | ViewModel 群を調整し、Broadcast・Unicast 処理・Refresh を提供する |
| `PulseHost` | Container のキーを伝播する Compose ラッパー |
| `PulseContent` | ViewModel を観測する Compose ラッパー |

## ライフサイクル

::: tip
`onSetup()` は、`PulseContent` が ViewModel を最初に観測したときに一度だけ実行されます。ViewModel は `ViewModelStoreOwner` が生きている間ずっと有効です。コンポジションの再起動でも `refresh()` でも、セットアップは繰り返されません。

どのオーナーかによって、ViewModel のライフタイムが決まります。ホストのオーナー配下で生成すれば、画面全体の間生き続けます。Navigation 3 の destination の中で生成すれば、そのバックスタックエントリにスコープされます。このとき `NavDisplay` のデコレータには `rememberPulseNavEntryDecorators()` を渡します。別の destination で覆われても、ViewModel は保持されます。ルートが pop されるとキャンセルされます。デモはすべての destination をこの方法で構築しています。
:::

```mermaid
flowchart TB
    subgraph create["生成"]
        direction TB
        A["rememberPulseViewModel が ViewModel を生成"]
        A --> B["オーナーの ViewModelStore に保持"]
        B --> C["PulseContent が観測"]
        C --> D["onSetup() が一度だけ実行"]
        D --> E["coroutineScope が有効"]
    end
```

```mermaid
flowchart TB
    subgraph clear["破棄"]
        direction TB
        F["ViewModelStoreOwner が破棄される"]
        F --> G["ViewModel.onCleared()"]
        G --> H["coroutineScope がキャンセル"]
    end
```
