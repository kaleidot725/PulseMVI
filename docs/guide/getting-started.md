# Getting Started

This guide walks you through building a simple counter app with PulseMVI.

It uses `rememberPulseStore` to own the Store, which comes from the `pulsemvi-navigation3` artifact.
Add it alongside the core one, or see [Store](/guide/store) for driving the lifecycle yourself with
the core artifact alone.

```kotlin
dependencies {
    implementation("com.github.kaleidot725:pulsemvi:<version>")
    implementation("com.github.kaleidot725:pulsemvi-navigation3:<version>")
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

// Broadcast: messages sent from Container to all Stores
sealed class CounterBroadcast : PulseBroadcast {
    data object Refresh : CounterBroadcast()
}

// Unicast: messages sent from Store to Container
sealed interface CounterUnicast : PulseUnicast {
    data object ResetRequested : CounterUnicast
}
```

## 2. Create a Store

`PulseStore` manages its own UI state. Override the lifecycle hooks to handle actions and broadcasts:

```kotlin
class CounterStore(
    private val repository: CounterRepository,
) : PulseStore<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(
    initialUiState = CounterState(),
) {
    // Called once, by rememberPulseStore, when the Store is created
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

`PulseContainer` takes a list of Stores and lets you broadcast to all of them or refresh the view:

```kotlin
class CounterContainer(
    stores: List<PulseStore<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(stores = stores)
```

## 4. Connect to Compose UI

### Entry point

Create the Store and Container once at the top level:

```kotlin
fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Counter") {
        MaterialTheme {
            val store = rememberPulseStore { CounterStore(CounterRepository()) }
            val container = rememberPulseContainer { CounterContainer(stores = listOf(store)) }

            CounterScreen(container = container, store = store)
        }
    }
}
```

### Screen composable

Wrap your layout with `PulseHost` to enable refresh and broadcast:

```kotlin
@Composable
fun CounterScreen(container: CounterContainer, store: CounterStore) {
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
                store = store,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
```

### Content composable

Use `PulseContent` to observe a Store and handle events:

```kotlin
@Composable
fun CounterContent(store: CounterStore, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = modifier) {
        PulseContent(
            store = store,
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

## Running the Demo

The repository includes a complete counter demo. Clone the repo and run:

```bash
./gradlew :demo:run
```

## Next Steps

- [Architecture](/guide/architecture) — understand the data flow in depth
- [Store](/guide/store) — advanced Store patterns
- [Container](/guide/container) — coordinating multiple Stores
