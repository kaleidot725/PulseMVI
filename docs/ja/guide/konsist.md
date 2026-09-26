# Konsist

`pulsemvi-konsist` は任意のアーティファクトです。PulseMVI が周りのコードに期待する規約を [Konsist](https://docs.konsist.lemonappdev.com/) のアサートとして公開しているので、自分のテストで検査できます。

型で表せることはコンパイラが弾きます。ViewModel は `onAction` を実装しなければならず、メッセージの型は Container が知っているものに限られます。残るのは型で表せない書き方の習慣で、どれもレビューで見落としやすいものです。

## セットアップ

テストの依存に追加します。Konsist 自体も一緒に入るので、ほかに宣言するものはありません。

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("com.github.kaleidot725.PulseMVI:pulsemvi-konsist:<version>")
}
```

あとはテストを 1 本書きます。`scopeFromProduction()` はプロダクションのソースセットだけを読み、テストコードを除きます。

```kotlin
class PulseMviConventionTest {
    @Test
    fun `follows the PulseMVI conventions`() {
        Konsist.scopeFromProduction().assertPulseMviConventions()
    }
}
```

## 検査する内容

`assertPulseMviConventions()` は 4 つの検査を実行します。失敗したときは、該当する宣言の名前と直し方を出します。

| 検査 | ルール | 理由 |
|---|---|---|
| State は data 型 | `PulseState` は `data class` または `data object` | `StateFlow` は直前と等しい値を流さず、Compose は引数が等しい Composable をスキップする。`equals` がないと更新ごとに画面全体が再コンポジションされる |
| メッセージは sealed | Action・Event・Broadcast・Unicast の interface は `sealed`、直接実装する class・object は `data` | どれも `when` で処理される。sealed にしてあれば、ケースの追加がコンパイルエラーになる |
| 名前は基底クラスに従う | `PulseViewModel` は `…ViewModel`、`PulseContainer` は `…Container` | コンパイラにとってはどちらも `ViewModel` のサブクラスなので、どちらなのかを示すのは名前だけ |
| データは State に置く | ViewModel と Container は `var` プロパティを持たない | State の外の `var` は変更しても emission が起きず、何も再コンポジションされない |

## 検査を選ぶ

それぞれの検査は単体でも公開しています。合わない検査があるプロジェクトは、残りだけを実行できます。

```kotlin
@Test
fun `follows the conventions we agreed on`() {
    val scope = Konsist.scopeFromProduction()
    scope.assertStatesAreDataTypes()
    scope.assertMessagesAreSealedOrDataTypes()
    scope.assertViewModelsHoldTheirDataInTheirState()
}
```

## 自分のルールを書く

検査の土台になっているクエリも公開しているので、その上に独自のルールを足せます。`pulseViewModels()`、`pulseContainers()`、`pulseStateClasses()`、`pulseStateObjects()`、`pulseMessageInterfaces()`、`pulseMessageClasses()`、`pulseMessageObjects()` が、それぞれ対応する型を継承・実装している宣言を返します。

```kotlin
@Test
fun `every ViewModel lives in a feature package`() {
    Konsist
        .scopeFromProduction()
        .pulseViewModels()
        .assertTrue { it.resideInPackage("..feature..") }
}
```

## できないこと

Konsist には ktlint のルールセットや detekt のプラグインのような読み込みの仕組みがありません。アーティファクト自身が勝手に走ることはできないので、呼び出すテストはプロジェクト側で書くことになります。

Konsist が読むのはコンパイル済みのクラスではなくソースコードです。ライブラリ側の基底クラスを経由して `PulseViewModel` に到達する ViewModel は、その基底クラスが走査対象のソースにないため認識できません。

対象が 0 件のときは成功します。PulseMVI の型がまだないモジュールでも落ちないので、検査するコードより先にテストを置けます。

## 次のステップ

- [Konsist API](/ja/api/konsist) — アサートとクエリの完全なシグネチャ
- [アーキテクチャ](/ja/guide/architecture) — これらの検査が由来する規約
- [ViewModel](/ja/guide/viewmodel) — State、Action、ライフサイクルフック
