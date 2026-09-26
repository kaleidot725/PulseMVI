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

## What it checks

`assertPulseMviConventions()` runs four checks. A failure names the declaration and says how to fix it.

| Check | Rule | Why |
|---|---|---|
| States are data types | a `PulseState` is a `data class` or `data object` | `StateFlow` drops an emission equal to the last one, and Compose skips a composable whose arguments are equal. Without `equals` the screen recomposes on every update |
| Messages are sealed | an Action, Event, Broadcast or Unicast interface is `sealed`; a class or object implementing one directly is `data` | each one is consumed by a `when`. Sealing is what turns a new case into a compile error instead of a branch nobody wrote |
| Names follow the base class | a `PulseViewModel` is named `…ViewModel`, a `PulseContainer` is named `…Container` | both are `ViewModel` subclasses to the compiler, so the name is what tells a reader which one this is |
| Data lives in the state | a ViewModel and a Container declare no `var` property | a `var` beside the state changes without an emission, so nothing recomposes |

## Choosing the checks

Each check is public on its own. A project that disagrees with one can run the other three.

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

The queries behind the checks are public too, so a project can add rules of its own on top of them. `pulseViewModels()`, `pulseContainers()`, `pulseStateClasses()`, `pulseStateObjects()`, `pulseMessageInterfaces()`, `pulseMessageClasses()` and `pulseMessageObjects()` each return the declarations that extend or implement the matching type.

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
