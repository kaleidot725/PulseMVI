# Konsist

`pulsemvi-konsist` is an optional artifact. It states the conventions PulseMVI expects of the code around it as [Konsist](https://docs.konsist.lemonappdev.com/) assertions, so your own test suite checks them.

The compiler already rejects what the types can express. A ViewModel must implement `onAction`, and its message types must be the ones its Container knows. What is left are habits the types cannot express, each with a consequence that is easy to miss in review.

## Setup

Add the artifact to the test classpath. Konsist itself comes with it, so there is nothing else to declare.

```kotlin
// build.gradle.kts
dependencies {
    testImplementation("com.github.kaleidot725.PulseMVI:pulsemvi-konsist:<version>")
}
```

Then write one test. `scopeFromProduction()` reads the project's main source sets, leaving test code out.

```kotlin
class PulseMviConventionTest {
    @Test
    fun `follows the PulseMVI conventions`() {
        Konsist.scopeFromProduction().assertPulseMviConventions()
    }
}
```

## What it checks about the declarations

`assertPulseMviConventions()` runs seven checks. Four of them are about the declarations. A failure names the declaration and says how to fix it.

| Check | Rule | Why |
|---|---|---|
| States are data types | a `PulseState` is a `data class` or `data object` | `StateFlow` drops an emission equal to the last one, and Compose skips a composable whose arguments are equal. Without `equals` the screen recomposes on every update |
| Messages are sealed | an Action, Event, Broadcast or Unicast interface is `sealed`; a class or object implementing one directly is `data` | each one is consumed by a `when`. Sealing is what turns a new case into a compile error instead of a branch nobody wrote |
| Names follow the base class | a `PulseViewModel` is named `…ViewModel`, a `PulseContainer` is named `…Container` | both are `ViewModel` subclasses to the compiler, so the name is what tells a reader which one this is |
| Data lives in the state | a ViewModel and a Container declare no `var` property | a `var` beside the state changes without an emission, so nothing recomposes |

## What it checks about the views

The other three are about how the views are split. A **screen** is what a route shows: it creates the ViewModels and the Container and lays the parts out. A **section** is the part one ViewModel answers for. A **component** is a leaf that takes data and callbacks.

The package decides which is which, so function names stay free. A screen sits at the root of its feature package, a section under `section`, a component under `component`. Being a component wins over the section around it, because a section's own components normally live inside it.

```
count/
├── PulseCountHost.kt          # screen
├── PulseCountContainer.kt
├── component/                 # components of the screen
│   └── PulseCountHeader.kt
├── section/
│   └── area/
│       ├── PulseAreaContent.kt    # section
│       ├── PulseAreaViewModel.kt
│       ├── component/             # components of the section
│       │   └── PulseAreaCell.kt
│       └── state/
└── state/
```

| Check | Rule | Why |
|---|---|---|
| Sections bind one ViewModel | a file with a composable under `section` calls `PulseContent` | the section is the only place the ViewModel and the UI meet. A file that binds nothing is a component in the wrong package |
| Components are stateless | a composable under `component` takes no `…ViewModel` or `…Container` parameter, and calls neither `PulseContent` nor `PulseHost` | data and callbacks are what make a component reusable between sections and previewable on its own |
| Screens compose sections | a composable outside both packages does not call `PulseContent` | binding a ViewModel in the screen instead of a section is how a screen grows into the file that knows everything |

## Choosing the checks

Each check is public on its own. A project that disagrees with one can run the others.

```kotlin
@Test
fun `follows the conventions we agreed on`() {
    val scope = Konsist.scopeFromProduction()
    scope.assertStatesAreDataTypes()
    scope.assertMessagesAreSealedOrDataTypes()
    scope.assertViewModelsHoldTheirDataInTheirState()
}
```

## Writing your own rules

The queries behind the checks are public too, so a project can add rules of its own on top of them. `pulseViewModels()`, `pulseContainers()`, `pulseStateClasses()`, `pulseStateObjects()`, `pulseMessageInterfaces()`, `pulseMessageClasses()` and `pulseMessageObjects()` each return the declarations that extend or implement the matching type, and `pulseScreens()`, `pulseSections()` and `pulseComponents()` return the files that hold composables of each role.

```kotlin
@Test
fun `every ViewModel lives in a feature package`() {
    Konsist
        .scopeFromProduction()
        .pulseViewModels()
        .assertTrue { it.resideInPackage("..feature..") }
}
```

## What it cannot do

Konsist has no plugin mechanism, unlike ktlint's rule sets or detekt's plugins. The artifact cannot install itself, which is why your project writes the test that calls it.

Konsist also reads source code rather than compiled classes. A ViewModel that reaches `PulseViewModel` through a base class from a library is not recognised, because that base class is not in the sources being scanned.

An empty result passes. A module with no PulseMVI types yet does not fail, so the test can be added before the code it checks.

## Next steps

- [Konsist API](/api/konsist) — the signature of every assertion and query
- [Architecture](/guide/architecture) — the conventions these checks come from
- [ViewModel](/guide/viewmodel) — state, actions and the lifecycle hooks
