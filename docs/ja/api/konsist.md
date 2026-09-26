# Konsist

このページの内容はすべて任意のアーティファクト `pulsemvi-konsist` にあります。パッケージは `jp.kaleidot725.pulse.mvi.konsist` で、いずれも Konsist の `KoScope` の拡張関数です。

それぞれの検査の意味と理由は [Konsist ガイド](/ja/guide/konsist) にあります。

## assertPulseMviConventions

```kotlin
fun KoScope.assertPulseMviConventions()
```

以下の検査をすべて順に実行します。最初に失敗した検査で例外が投げられるので、1 回の実行で報告されるのは 1 件です。

## assertStatesAreDataTypes

```kotlin
fun KoScope.assertStatesAreDataTypes()
```

`PulseState` を実装するすべての class と object に `data` 修飾子を要求します。

### 失敗時のメッセージ

| 宣言 | メッセージ |
|---|---|
| class | `data` を付けて、等しい State が等しく比較され Compose が再コンポジションをスキップできるようにする |
| object | `data` を付けて、同一性ではなく State として表示・比較されるようにする |

## assertMessagesAreSealedOrDataTypes

```kotlin
fun KoScope.assertMessagesAreSealedOrDataTypes()
```

`PulseAction`、`PulseEvent`、`PulseBroadcast`、`PulseUnicast` を実装する interface に `sealed` を、それらを直接実装する class と object に `data` を要求します。

## assertViewModelsAndContainersAreNamedAfterTheirBase

```kotlin
fun KoScope.assertViewModelsAndContainersAreNamedAfterTheirBase()
```

`PulseViewModel` のサブクラスに `ViewModel` で終わる名前を、`PulseContainer` のサブクラスに `Container` で終わる名前を要求します。

## assertViewModelsHoldTheirDataInTheirState

```kotlin
fun KoScope.assertViewModelsHoldTheirDataInTheirState()
```

`PulseViewModel` と `PulseContainer` のサブクラスが `var` プロパティを宣言していないことを要求します。

## assertSectionsBindOneViewModel

```kotlin
fun KoScope.assertSectionsBindOneViewModel()
```

`section` パッケージ配下の Composable を持つすべてのファイルに `PulseContent` の呼び出しを要求します。判定はファイル単位なので、Section の隣に置かれる stateless なオーバーロードや `@Preview` は問題ありません。

## assertComponentsAreStateless

```kotlin
fun KoScope.assertComponentsAreStateless()
```

`component` パッケージ配下のすべての Composable に、型名が `ViewModel` または `Container` で終わる引数がないこと、そしてそのファイルが `PulseContent` も `PulseHost` も呼ばないことを要求します。

## assertScreensComposeSections

```kotlin
fun KoScope.assertScreensComposeSections()
```

`section` にも `component` にも属さない Composable を持つファイル、つまり Screen に、`PulseContent` を呼ばないことを要求します。

## クエリ

前半の 7 つは対応する型を直接継承・実装している宣言を返します。後半の 3 つは、役割が関数ではなくファイルのパッケージで決まるため、ファイルを返します。いずれも上のアサートの土台であり、プロジェクト独自のルールの土台にもなります。

| クエリ | 戻り値 |
|---|---|
| `pulseViewModels()` | `List<KoClassDeclaration>` — `PulseViewModel` を継承した class |
| `pulseContainers()` | `List<KoClassDeclaration>` — `PulseContainer` を継承した class |
| `pulseStateClasses()` | `List<KoClassDeclaration>` — `PulseState` を実装した class |
| `pulseStateObjects()` | `List<KoObjectDeclaration>` — `PulseState` を実装した object |
| `pulseMessageInterfaces()` | `List<KoInterfaceDeclaration>` — 4 つのメッセージマーカーのいずれかを実装した interface |
| `pulseMessageClasses()` | `List<KoClassDeclaration>` — 同じマーカーを実装した class |
| `pulseMessageObjects()` | `List<KoObjectDeclaration>` — 同じマーカーを実装した object |
| `pulseScreens()` | `List<KoFileDeclaration>` — `section` と `component` のどちらにも属さない Composable を持つファイル |
| `pulseSections()` | `List<KoFileDeclaration>` — `section` 配下の Composable を持つファイル。内側の `component` を除く |
| `pulseComponents()` | `List<KoFileDeclaration>` — `component` 配下の Composable を持つファイル |

## スコープ

アサートは任意の `KoScope` に対して動くので、何を検査するかはスコープの選び方で決まります。

| スコープ | 読む範囲 |
|---|---|
| `Konsist.scopeFromProduction()` | プロジェクトのプロダクションのソースセット。テストコードを除く |
| `Konsist.scopeFromProject()` | すべてのソースセット。テストコードを含む |
| `Konsist.scopeFromProject(moduleName = "app")` | 単一のモジュール |
| `Konsist.scopeFromPackage("com.example.counter..")` | 単一のパッケージとその配下 |

## 次のステップ

- [Konsist ガイド](/ja/guide/konsist) — セットアップと各検査の理由
- [マーカーインターフェース](/ja/api/interfaces) — 検査の対象になっている型
