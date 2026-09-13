---
layout: home

hero:
  name: PulseMVI
  text: Compose Desktop のための MVI
  tagline: コルーチンベースの軽量 MVI ライブラリ。
  actions:
    - theme: brand
      text: はじめる
      link: /ja/guide/getting-started
    - theme: alt
      text: GitHub で見る
      link: https://github.com/kaleidot725/PulseMVI

features:
  - icon: 🏗️
    title: MVI アーキテクチャ
    details: State・Action・Event・Broadcast・Unicast を明確に分離します。整理された、テストしやすいコードを保てます。
  - icon: 🔄
    title: ViewModel と Container
    details: PulseViewModel が状態を自律的に管理します。PulseContainer がアプリ全体の複数の ViewModel を調整します。
  - icon: 📡
    title: Broadcast
    details: Container から登録済みのすべての ViewModel へ、型安全なメッセージを同時に届けます。
  - icon: ⬆️
    title: Unicast
    details: ViewModel から親の Container へ、調整のための型安全なメッセージを送ります。
  - icon: 🖥️
    title: View Refresh
    details: ViewModel の状態を保ったまま、ビューツリー全体をオンデマンドで再構築します。
  - icon: ⚡
    title: コルーチンベース
    details: Kotlin Coroutines と StateFlow の上に構築されています。効率的でノンブロッキングなリアクティブ状態管理です。
  - icon: 🎨
    title: Compose 連携
    details: ライフサイクルを自動管理する PulseHost / PulseContent をそのまま使えます。
---
