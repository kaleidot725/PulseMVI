---
layout: home

hero:
  name: PulseMVI
  text: MVI for Compose Desktop
  tagline: A lightweight, coroutine-based MVI library for Compose Desktop.
  actions:
    - theme: brand
      text: Get Started
      link: /guide/getting-started
    - theme: alt
      text: View on GitHub
      link: https://github.com/kaleidot725/PulseMVI

features:
  - icon: 🏗️
    title: MVI Architecture
    details: Clear separation of State, Action, Event, Broadcast, and Unicast. Keeps your code organized and testable.
  - icon: 🔄
    title: ViewModel & Container
    details: PulseViewModel manages state autonomously. PulseContainer coordinates multiple ViewModels across your app.
  - icon: 📡
    title: Broadcast
    details: Type-safe messages delivered from Container to all registered ViewModels simultaneously.
  - icon: ⬆️
    title: Unicast
    details: Type-safe messages emitted from ViewModels to their parent Container for coordination.
  - icon: 🖥️
    title: View Refresh
    details: Forces the entire view tree to reconstruct on demand while preserving ViewModel state.
  - icon: ⚡
    title: Coroutine-Based
    details: Built on Kotlin Coroutines and StateFlow. Efficient, non-blocking reactive state management.
  - icon: 🎨
    title: Compose Integration
    details: Ready-to-use PulseHost and PulseContent composables with automatic lifecycle management.
---
