package jp.kaleidot725.pulse.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key

internal val LocalPulseContainerKey = compositionLocalOf { "" }

@Composable
public fun <Broadcast : PulseBroadcast> PulseApp(
    container: PulseContainer<Broadcast>,
    content: @Composable ((onRefresh: () -> Unit, onBroadcast: (Broadcast) -> Unit) -> Unit) = { _, _ -> },
) {
    val containerKey by container.key.collectAsState()

    CompositionLocalProvider(LocalPulseContainerKey provides containerKey) {
        content(container::refresh, container::broadcast)
    }
}

@Composable
public fun <State : PulseState, Action : PulseAction, Event : PulseEvent, Broadcast : PulseBroadcast> PulseContent(
    store: PulseStore<State, Action, Event, Broadcast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable ((State, ((Action) -> Unit)) -> Unit) = { _, _ -> },
) {
    val containerKey = LocalPulseContainerKey.current
    val state by store.state.collectAsState()
    val onAction = store::onAction
    LaunchedEffect(store) { store.event.collect { onEvent(it) } }
    DisposableEffect(store) {
        onDispose { store.cancel() }
    }

    key(containerKey) {
        content(state, onAction)
    }
}
