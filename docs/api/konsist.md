# Konsist

Everything on this page lives in the optional `pulsemvi-konsist` artifact. The package is `jp.kaleidot725.pulse.mvi.konsist`, and every declaration is an extension of Konsist's `KoScope`.

For what the checks mean and why they exist, see the [Konsist guide](/guide/konsist).

## assertPulseMviConventions

```kotlin
fun KoScope.assertPulseMviConventions()
```

Runs every check below, in order. The first failing check throws, so a run reports one problem at a time.

## assertStatesAreDataTypes

```kotlin
fun KoScope.assertStatesAreDataTypes()
```

Requires every class and object implementing `PulseState` to carry the `data` modifier.

### Failure message

| Declaration | Message |
|---|---|
| class | Add `data` so equal states compare equal and Compose can skip the recomposition |
| object | Add `data` so the object prints and compares as a state rather than as an identity |

## assertMessagesAreSealedOrDataTypes

```kotlin
fun KoScope.assertMessagesAreSealedOrDataTypes()
```

Requires every interface implementing `PulseAction`, `PulseEvent`, `PulseBroadcast` or `PulseUnicast` to carry the `sealed` modifier, and every class or object implementing one of them directly to carry `data`.

## assertViewModelsAndContainersAreNamedAfterTheirBase

```kotlin
fun KoScope.assertViewModelsAndContainersAreNamedAfterTheirBase()
```

Requires a `PulseViewModel` subclass to have a name ending in `ViewModel`, and a `PulseContainer` subclass to have a name ending in `Container`.

## assertViewModelsHoldTheirDataInTheirState

```kotlin
fun KoScope.assertViewModelsHoldTheirDataInTheirState()
```

Requires every `PulseViewModel` and `PulseContainer` subclass to declare no `var` property.

## assertSectionsBindOneViewModel

```kotlin
fun KoScope.assertSectionsBindOneViewModel()
```

Requires every file with a composable under a `section` package to call `PulseContent`. The check is per file, so the stateless overload and the `@Preview` beside a section are fine.

## assertComponentsAreStateless

```kotlin
fun KoScope.assertComponentsAreStateless()
```

Requires every composable under a `component` package to take no parameter whose type name ends in `ViewModel` or `Container`, and the file to call neither `PulseContent` nor `PulseHost`.

## assertScreensComposeSections

```kotlin
fun KoScope.assertScreensComposeSections()
```

Requires every file with a composable outside both a `section` and a `component` package — a screen — not to call `PulseContent`.

## Queries

The first seven queries return the declarations that extend or implement the matching type directly; the last three return files, since a role belongs to a file's package rather than to one function. They are the building blocks of the assertions above, and of rules a project writes itself.

| Query | Returns |
|---|---|
| `pulseViewModels()` | `List<KoClassDeclaration>` — classes extending `PulseViewModel` |
| `pulseContainers()` | `List<KoClassDeclaration>` — classes extending `PulseContainer` |
| `pulseStateClasses()` | `List<KoClassDeclaration>` — classes implementing `PulseState` |
| `pulseStateObjects()` | `List<KoObjectDeclaration>` — objects implementing `PulseState` |
| `pulseMessageInterfaces()` | `List<KoInterfaceDeclaration>` — interfaces implementing one of the four message markers |
| `pulseMessageClasses()` | `List<KoClassDeclaration>` — classes implementing one of the four message markers |
| `pulseMessageObjects()` | `List<KoObjectDeclaration>` — objects implementing one of the four message markers |
| `pulseScreens()` | `List<KoFileDeclaration>` — files with a composable outside a `section` and a `component` package |
| `pulseSections()` | `List<KoFileDeclaration>` — files with a composable under a `section` package, components inside it excluded |
| `pulseComponents()` | `List<KoFileDeclaration>` — files with a composable under a `component` package |

## Scopes

The assertions work on any `KoScope`, so the choice of scope decides what is checked.

| Scope | What it reads |
|---|---|
| `Konsist.scopeFromProduction()` | every main source set of the project, test code left out |
| `Konsist.scopeFromProject()` | every source set, tests included |
| `Konsist.scopeFromProject(moduleName = "app")` | one module |
| `Konsist.scopeFromPackage("com.example.counter..")` | one package and everything under it |

## Next steps

- [Konsist guide](/guide/konsist) — setup and the reasoning behind each check
- [Marker Interfaces](/api/interfaces) — the types the checks are about
