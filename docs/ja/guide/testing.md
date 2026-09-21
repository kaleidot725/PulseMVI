# テストで保証していること

PulseMVI にはテストがあります。このページは、そのテストが何を保証しているかを一覧にしたものです。ドキュメントに書かれた振る舞いが壊れたとき、レポートの数値が動くのではなく、名前のついたテストが落ちて分かるようにしてあります。

## テストの置き場所

2 つのアーティファクトを 3 つのソースセットでテストしています。コアのロジックは `commonTest` に置き、コンポジションが必要なものは JVM の Compose UI テストとして実行します。

| ソースセット | 対象 |
|---|---|
| `library/src/commonTest` | `PulseViewModel` と `PulseContainer` |
| `library/src/jvmTest` | `PulseHost` と `PulseContent` の Composable |
| `navigation3/src/jvmTest` | `rememberPulseViewModel`、`rememberPulseContainer`、`rememberPulseNavEntryDecorators` |

## PulseViewModel

ここで保証しているのはライフサイクルとイベント配送です。`onSetup()` がいつ走るか、`cancel()` と `close()` の後に何が残るか、まだ誰も collect していないイベントがどうなるかを確認しています。

| 保証している振る舞い | テスト |
|---|---|
| 与えられたディスパッチャで処理を起動する | `usesConfiguredCoroutineDispatcher` |
| 観測されるまで `onSetup()` は走らない | `setupIsNotRunUntilTheOwnerStartsIt` |
| `cancel()` は `onSetup()` で始めた処理を止める | `cancelStopsWorkStartedInSetup` |
| `cancel()` の後、次に観測したところで再びセットアップされる | `setupOnceRunsAgainAfterCancel` |
| `cancel()` とその後のセットアップをまたいで State は保たれる | `stateIsPreservedAcrossSetups` |
| `close()` の後は起動できるスコープが残らない | `closeLeavesNoScopeToLaunchInto` |
| collect されていない状態で発行したイベントも順序を保つ | `eventsEmittedWithNoCollectorKeepTheirOrder` |
| バッファが埋まると最も古いイベントを捨てる | `eventBufferDropsTheOldestOnceItIsFull` |
| `onSetup()` と `onReceive()` はオーバーライドしなければ何もしない | `hooksDoNothingUnlessOverridden` |

## PulseContainer

Container の役割は ViewModel 間のメッセージの受け渡しと、close されたらそれをやめることです。

| 保証している振る舞い | テスト |
|---|---|
| Broadcast は登録されたすべての ViewModel に届く | `broadcastReachesEveryViewModel` |
| `refresh()` は `PulseContent` が content を作り直すキーを変える | `refreshChangesContainerKey` |
| `close()` は Unicast の collect を止める | `closeStopsUnicastCollection` |
| `onReceived()` はオーバーライドしなければ何もしない | `receivedHookDoesNothingUnlessOverridden` |

## PulseHost と PulseContent

描画のほかに、再コンポジションの振る舞いも確認しています。呼び出しをスキップするかどうか、引数が変化したかをどう伝えるかは Compose コンパイラが決めるため、その経路をそれぞれ通しています。何も変わらない再コンポジション、引数の変化を呼び出し側が把握している場合、コンポジションの外に持ち上げたラムダ、そして呼び出し側が判断できない値として渡したラムダの 4 つです。

| 保証している振る舞い | テスト |
|---|---|
| `PulseContent` は State を描画し、Action を ViewModel へ戻す | `rendersStateAndDispatchesActions` |
| `onSetup()` は一度だけ走り、イベントは最新のコンポジションのハンドラに届く | `runsSetupOnceAndDeliversEventsThroughTheLatestHandler` |
| `content` と `onEvent` を省くと、描画もハンドリングもせず観測だけする | `defaultsObserveWithoutRenderingOrHandling` |
| `PulseHost` は content に Container の `refresh` と `broadcast` を渡す | `hostHandsOutRefreshAndBroadcast` |
| 何も変わらない再コンポジションはスキップされ、`onSetup()` は繰り返されない | `skipsRecompositionWhenNothingChanged` |
| 引数の変化を呼び出し側が把握している場合も受け付ける | `acceptsParametersWhoseChangednessTheCallerAlreadyKnows` |
| コンポジションの外から渡したラムダは、新しいものとして扱われず再利用される | `reusesLambdasPassedInFromOutsideTheComposition` |
| 値として渡した content は、ViewModel を再セットアップせずに差し替えられる | `swapsContentHandedOverAsAValue` |
| どちらの Composable もコンポジショントレーサに自身を報告する | `reportsToTheComposeTracerWhenOneIsInstalled` |

## Navigation 3

ここで確認しているのはインスタンスの解決です。どのオーナーが保持するか、どのキーで識別するか、オーナーがスコープにないときどうなるかを見ています。

| 保証している振る舞い | テスト |
|---|---|
| 同じオーナーなら再コンポジションをまたいで同じインスタンスを返す | `viewModelAndContainerSurviveRecompositionUnderTheSameOwner` |
| 明示的なキーで、同じ型の 2 つのインスタンスを区別できる | `explicitKeysTellTwoInstancesOfOneTypeApart` |
| 既定のキーは完全修飾名、単純名、与えられた名前の順にフォールバックする | `defaultKeyFallsBackFromQualifiedNameToSimpleNameToTheGivenName` |
| オーナーがスコープにない呼び出しは、何が足りないかを示して失敗する | `failsPlainlyWithoutAnOwner` |
| `rememberPulseNavEntryDecorators()` は saveable state holder と ViewModel store のデコレータを返す | `navEntryDecoratorsAreTheSaveableStateHolderAndTheViewModelStore` |

## カバレッジ

カバレッジは [Kover](https://github.com/Kotlin/kotlinx-kover) で両方のアーティファクトを対象に計測しており、行または分岐が 95% を下回ると `koverVerify` が失敗します。すべてのプルリクエストで実行され、現在はどちらも 100% です。

```bash
./gradlew test            # 全モジュール
./gradlew koverVerify     # 95% のしきい値
./gradlew koverHtmlReport # build/reports/kover/html/index.html
```

## 次のステップ

- [アーキテクチャ](/ja/guide/architecture) — テストが対象にしている要素の関係
- [ViewModel](/ja/guide/viewmodel) — ここで保証しているライフサイクルフック
- [Container](/ja/guide/container) — broadcast、refresh、close
