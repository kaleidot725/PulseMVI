# API 概要

PulseMVI の API は小さく、焦点が絞られています。コアのアーティファクトは `jp.kaleidot725.pulse.mvi` にあります。任意のアーティファクト `pulsemvi-navigation3` は `jp.kaleidot725.pulse.mvi.navigation3` に 3 つの Composable を追加します（[拡張](#拡張) を参照）。

## クラス

| クラス | 説明 |
|---|---|
| [`PulseViewModel`](/ja/api/pulse-viewmodel) | UI 状態を管理する抽象基底クラス |
| [`PulseContainer`](/ja/api/pulse-container) | 複数の ViewModel を調整する |

## Composable

| Composable | 説明 |
|---|---|
| [`PulseHost`](/ja/api/composables#pulsehost) | Container をサブツリーにスコープし、Refresh と Broadcast のコールバックを提供する |
| [`PulseContent`](/ja/api/composables#pulsecontent) | ViewModel を観測し、状態と Action のディスパッチャを提供する |

## マーカーインターフェース

| インターフェース | 説明 |
|---|---|
| [`PulseState`](/ja/api/interfaces#pulsestate) | クラスを ViewModel の UI 状態として印付ける |
| [`PulseAction`](/ja/api/interfaces#pulseaction) | クラスをユーザーの Action として印付ける |
| [`PulseEvent`](/ja/api/interfaces#pulseevent) | クラスを一度きりの副作用として印付ける |
| [`PulseBroadcast`](/ja/api/interfaces#pulsebroadcast) | クラスを Container の Broadcast メッセージとして印付ける |
| [`PulseUnicast`](/ja/api/interfaces#pulseunicast) | クラスを子から親への Unicast メッセージとして印付ける |

## 拡張

### Navigation 3

| Composable | 説明 |
|---|---|
| [`rememberPulseViewModel`](/ja/api/navigation3#rememberpulseviewmodel) | `ViewModelStoreOwner` にスコープされた ViewModel |
| [`rememberPulseContainer`](/ja/api/navigation3#rememberpulsecontainer) | `ViewModelStoreOwner` にスコープされた Container |
| [`rememberPulseNavEntryDecorators`](/ja/api/navigation3#rememberpulsenaventrydecorators) | ViewModel をバックスタックエントリにスコープする `NavDisplay` のデコレータ |
