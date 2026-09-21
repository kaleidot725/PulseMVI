# Tested Behavior

PulseMVI ships its own test suite. This page lists what each test guarantees, so a change that breaks a documented behavior shows up as a named failing test rather than as a number moving in a report.

## Where the tests live

Three source sets cover the two artifacts. The core logic is tested on `commonTest`, and anything that needs a composition runs as a Compose UI test on the JVM.

| Source set | What it covers |
|---|---|
| `library/src/commonTest` | `PulseViewModel` and `PulseContainer` |
| `library/src/jvmTest` | The `PulseHost` and `PulseContent` composables |
| `navigation3/src/jvmTest` | `rememberPulseViewModel`, `rememberPulseContainer` and `rememberPulseNavEntryDecorators` |

## PulseViewModel

The guarantees here are about lifecycle and event delivery: when `onSetup()` runs, what `cancel()` and `close()` leave behind, and what happens to events nobody is collecting yet.

| Guarantee | Test |
|---|---|
| Work is launched on the dispatcher the ViewModel was given | `usesConfiguredCoroutineDispatcher` |
| `onSetup()` does not run until something observes the instance | `setupIsNotRunUntilTheOwnerStartsIt` |
| `cancel()` stops the work `onSetup()` started | `cancelStopsWorkStartedInSetup` |
| After `cancel()`, the next observer sets the instance up again | `setupOnceRunsAgainAfterCancel` |
| State survives a `cancel()` and the setup that follows it | `stateIsPreservedAcrossSetups` |
| `close()` leaves no scope to launch into | `closeLeavesNoScopeToLaunchInto` |
| Events emitted with no collector keep their order | `eventsEmittedWithNoCollectorKeepTheirOrder` |
| The event buffer drops the oldest event once it is full | `eventBufferDropsTheOldestOnceItIsFull` |
| `onSetup()` and `onReceive()` do nothing unless overridden | `hooksDoNothingUnlessOverridden` |

## PulseContainer

The Container's job is to pass messages between ViewModels and to stop doing so when it is closed.

| Guarantee | Test |
|---|---|
| A broadcast reaches every registered ViewModel | `broadcastReachesEveryViewModel` |
| `refresh()` changes the key that `PulseContent` re-creates its content on | `refreshChangesContainerKey` |
| `close()` stops the Container from collecting unicasts | `closeStopsUnicastCollection` |
| `onReceived()` does nothing unless overridden | `receivedHookDoesNothingUnlessOverridden` |

## PulseHost and PulseContent

Besides rendering, these tests pin down the recomposition behavior. The Compose compiler decides when to skip a call and how a parameter's changedness is reported, and the suite drives each of those paths: an unchanged recomposition, a caller that already knows whether a parameter changed, a lambda hoisted out of the composition, and a lambda handed over as a value that the caller cannot judge.

| Guarantee | Test |
|---|---|
| `PulseContent` renders the state and sends actions back to the ViewModel | `rendersStateAndDispatchesActions` |
| `onSetup()` runs once, and events reach the handler from the latest composition | `runsSetupOnceAndDeliversEventsThroughTheLatestHandler` |
| Leaving out `content` and `onEvent` observes the instance without rendering or handling | `defaultsObserveWithoutRenderingOrHandling` |
| `PulseHost` hands its content the Container's `refresh` and `broadcast` | `hostHandsOutRefreshAndBroadcast` |
| A recomposition with nothing changed is skipped, and `onSetup()` is not repeated | `skipsRecompositionWhenNothingChanged` |
| A caller that already knows a parameter's changedness is accepted | `acceptsParametersWhoseChangednessTheCallerAlreadyKnows` |
| Lambdas hoisted out of the composition are reused, not treated as new | `reusesLambdasPassedInFromOutsideTheComposition` |
| Content handed over as a value can be swapped without setting the ViewModel up again | `swapsContentHandedOverAsAValue` |
| Both composables report themselves to a composition tracer | `reportsToTheComposeTracerWhenOneIsInstalled` |

## Navigation 3

These tests cover instance lookup: which owner holds an instance, which key identifies it, and what happens when no owner is in scope.

| Guarantee | Test |
|---|---|
| One owner returns one instance across recompositions | `viewModelAndContainerSurviveRecompositionUnderTheSameOwner` |
| Explicit keys tell two instances of one type apart | `explicitKeysTellTwoInstancesOfOneTypeApart` |
| The default key falls back from the qualified name to the simple name to the given name | `defaultKeyFallsBackFromQualifiedNameToSimpleNameToTheGivenName` |
| Calling with no owner in scope fails with a message that names what is missing | `failsPlainlyWithoutAnOwner` |
| `rememberPulseNavEntryDecorators()` returns the saveable state holder and the ViewModel store decorators | `navEntryDecoratorsAreTheSaveableStateHolderAndTheViewModelStore` |

## Coverage

Coverage is measured with [Kover](https://github.com/Kotlin/kotlinx-kover) across both artifacts, and `koverVerify` fails when lines or branches fall below 95%. Every pull request runs it, and both metrics currently sit at 100%.

```bash
./gradlew test            # every module
./gradlew koverVerify     # the 95% gate
./gradlew koverHtmlReport # build/reports/kover/html/index.html
```

## Next steps

- [Architecture](/guide/architecture) — how the pieces the tests cover fit together
- [ViewModel](/guide/viewmodel) — the lifecycle hooks these guarantees are about
- [Container](/guide/container) — broadcast, refresh and close
