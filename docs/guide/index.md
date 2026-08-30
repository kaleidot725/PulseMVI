# What is PulseMVI?

PulseMVI is a lightweight MVI (Model-View-Intent) library for **Compose Multiplatform** on Android, iOS, and Desktop. It extends the standard MVI pattern with three coordination features for multi-Composable layouts:

- **Broadcast** — deliver a typed message from a Container to all registered Stores at once
- **Unicast** — send a typed message from a child Store up to its Container
- **View Refresh** — reconstruct the entire Compose view tree on demand without losing Store state

## Why PulseMVI?

Compose apps often contain multiple independent Composable sections, each with its own state. PulseMVI makes it easy to coordinate these sections without tightly coupling them.

```
┌─────────────────────────────────────┐
│           Window                    │
│  ┌──────────┐  ┌──────────────────┐ │
│  │ SideBar  │  │   Main Content   │ │
│  │ (Store A)│  │   (Store B)      │ │
│  └──────────┘  └──────────────────┘ │
│         PulseContainer              │
└─────────────────────────────────────┘
```

`PulseContainer` sits above both Stores. When you call `container.broadcast(MyBroadcast.Sync)`, both Store A and Store B receive the message and can react independently.

## Installation

Add JitPack to your repositories:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Then add the dependency:

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.github.kaleidot725:pulsemvi:<version>")

    // Optional: ViewModel owned Store lifetimes and Navigation 3 back stack scoping
    implementation("com.github.kaleidot725:pulsemvi-navigation3:<version>")
}
```

`pulsemvi` alone leaves the Store lifetime to you — see [Store](/guide/store). Add
`pulsemvi-navigation3` for `rememberPulseStore`, which hands that lifetime to a `ViewModel`.

Replace `<version>` with the latest tag from [GitHub Releases](https://github.com/kaleidot725/PulseMVI/releases).

## Requirements

| Requirement | Version |
|---|---|
| Java | 17+ |
| Kotlin | 2.0+ |
| Compose Multiplatform | 1.6+ |
| Android | API 21+ |

## Next Steps

- [Getting Started](/guide/getting-started) — build your first counter app
- [Architecture](/guide/architecture) — understand how all the pieces fit together
- [Unicast](/guide/unicast) — send child Store messages up to a Container
