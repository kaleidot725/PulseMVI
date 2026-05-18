---
layout: home

hero:
  name: PulseMVI
  text: MVI for Compose Desktop
  tagline: A lightweight, coroutine-based MVI library designed for multi-Composable Compose Desktop layouts.
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
    details: Clear separation of State, Action, Event, Broadcast, and Unicast keeps your code organized and testable.
  - icon: 🔄
    title: Store & Container
    details: PulseStore manages state autonomously. PulseContainer coordinates multiple Stores across your app.
  - icon: 📡
    title: Broadcast
    details: Type-safe messages delivered from Container to all registered Stores simultaneously.
  - icon: ⬆️
    title: Unicast
    details: Type-safe messages emitted from Stores to their parent Container for coordination.
  - icon: 🖥️
    title: View Refresh
    details: Forces the entire view tree to reconstruct on demand while preserving Store state.
  - icon: ⚡
    title: Coroutine-Based
    details: Built on Kotlin Coroutines and StateFlow for efficient, non-blocking reactive state management.
  - icon: 🎨
    title: Compose Integration
    details: Ready-to-use PulseApp and PulseContent composables with automatic lifecycle management.
---
