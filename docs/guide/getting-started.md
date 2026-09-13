# Getting Started

This guide walks you through building a simple counter app with PulseMVI.

It uses `rememberPulseViewModel` to own the ViewModel, which comes from the `pulsemvi-navigation3` artifact.
Add it alongside the core one, or see [ViewModel](/guide/viewmodel) for driving the lifecycle yourself with
the core artifact alone.

```kotlin
dependencies {
    implementation("com.github.kaleidot725.PulseMVI:pulsemvi:<version>")
    implementation("com.github.kaleidot725.PulseMVI:pulsemvi-navigation3:<version>")
}
```

## 1. Define State, Action, Event, Broadcast, and Unicast

Start by defining the five types that describe your feature:

```kotlin
// State: the UI data rendered by your Composable
data class CounterState(val count: Int = 0) : PulseState

// Action: intents dispatched by the user
sealed class CounterAction : PulseAction {
    data object Increment : CounterAction()
    data object Decrement : CounterAction()
    data object Reset : CounterAction()
}

// Event: one-time side effects (navigation, snackbar, etc.)
sealed class CounterEvent : PulseEvent {
    data class ShowMessage(val message: String) : CounterEvent()
}

// Broadcast: messages sent from Container to all ViewModels
sealed class CounterBroadcast : PulseBroadcast {
    data object Refresh : CounterBroadcast()
}

// Unicast: messages sent from ViewModel to Container
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}
```

## 2. Create a ViewModel

`PulseViewModel` manages its own UI state. Override the lifecycle hooks to handle actions and broadcasts:

```kotlin
class CounterViewModel(
    private val repository: CounterRepository,
) : PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    // Called once, by PulseContent, the first time it observes the ViewModel
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count ->
                update { copy(count = count) }
                if (count != 0 && count % 10 == 0) {
                    event(CounterEvent.ShowMessage("$count reached!"))
                }
            }
        }
    }

    // Called when the user dispatches an action
    override fun onAction(uiAction: CounterAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterAction.Increment -> repository.increment()
                CounterAction.Decrement -> repository.decrement()
                CounterAction.Reset -> repository.reset()
            }
        }
    }

    // Called when the Container broadcasts a message
    override fun onReceive(broadcast: CounterBroadcast) {
        when (broadcast) {
            CounterBroadcast.Refresh ->
                event(CounterEvent.ShowMessage("Refreshed!"))
        }
    }
}
```

## 3. Create a Container

`PulseContainer` takes a list of ViewModels. It lets you broadcast to all of them, or refresh the view:

```kotlin
class CounterContainer(
    viewModels: List<PulseViewModel<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(viewModels = viewModels)
```

## 4. Connect to Compose UI

### Entry point

Create the ViewModel and Container once at the top level:

```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Counter") {
        MaterialTheme {
            val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
            val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

            CounterScreen(container = container, viewModel = viewModel)
        }
    }
}
```

### Screen composable

Wrap your layout with `PulseHost` to enable refresh and broadcast:

```kotlin
@Composable
fun CounterScreen(container: CounterContainer, viewModel: CounterViewModel) {
    PulseHost(container = container) { onRefresh, onBroadcast ->
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(modifier = Modifier.align(Alignment.TopEnd)) {
                Button(onClick = { onRefresh() }) {
                    Text("Refresh View")
                }
                Button(onClick = { onBroadcast(CounterBroadcast.Refresh) }) {
                    Text("Send Broadcast")
                }
            }
            CounterContent(
                viewModel = viewModel,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
```

### Content composable

Use `PulseContent` to observe a ViewModel and handle events:

```kotlin
@Composable
fun CounterContent(viewModel: CounterViewModel, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier) {
        PulseContent(
            viewModel = viewModel,
            onEvent = { event ->
                when (event) {
                    is CounterEvent.ShowMessage ->
                        scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
            },
        ) { state, onAction ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${state.count}", fontSize = 72.sp)
                Row {
                    Button(onClick = { onAction(CounterAction.Decrement) }) { Text("−") }
                    Button(onClick = { onAction(CounterAction.Increment) }) { Text("+") }
                }
                OutlinedButton(onClick = { onAction(CounterAction.Reset) }) { Text("Reset") }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
```

## 5. Scope the ViewModel to a Navigation 3 destination

Step 4 created the ViewModel at the top level. There it lives as long as the window. With
Navigation 3, create it inside a destination instead. It then lives exactly as long as that route is
on the back stack. Two things make that happen. First, `NavDisplay` gets
`rememberPulseNavEntryDecorators()` as its `entryDecorators`, which gives every back stack entry its
own `ViewModelStoreOwner`. Second, the ViewModel and Container are created inside the destination,
not above `NavDisplay`. How this works is covered in [Navigation 3](/guide/navigation3).

```kotlin
sealed interface Route : NavKey {
    data object Counter : Route
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Counter") {
        MaterialTheme {
            val backStack = remember { mutableStateListOf<Route>(Route.Counter) }

            NavDisplay(
                backStack = backStack,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
                entryDecorators = rememberPulseNavEntryDecorators(),
                entryProvider =
                    entryProvider {
                        entry<Route.Counter> {
                            val viewModel = rememberPulseViewModel { CounterViewModel(CounterRepository()) }
                            val container = rememberPulseContainer { CounterContainer(viewModels = listOf(viewModel)) }

                            CounterScreen(container = container, viewModel = viewModel)
                        }
                    },
            )
        }
    }
}
```

## Running the Demo

The repository includes a pulse grid demo. Four areas share one Container. A tap on one spreads to
the two it shares an edge with. Clone the repo and run:

```bash
./gradlew :demo:run
```

## Next Steps

- [Architecture](/guide/architecture) — understand the data flow in depth
- [ViewModel](/guide/viewmodel) — advanced ViewModel patterns
- [Container](/guide/container) — coordinating multiple ViewModels
- [Navigation 3](/guide/navigation3) — how back stack scoped lifetimes work
