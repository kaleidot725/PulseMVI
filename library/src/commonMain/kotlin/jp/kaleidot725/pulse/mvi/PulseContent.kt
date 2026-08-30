package jp.kaleidot725.pulse.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key

internal val LocalPulseContainerKey = compositionLocalOf { 0L }

/**
 * Scopes a [PulseContainer] to this subtree.
 *
 * Emits no UI of its own: it publishes the Container key that [PulseContent] re-creates its content
 * on, and hands [content] the Container's `refresh` and `broadcast`. Every destination that owns a
 * Container gets its own [PulseHost], so an app can contain several of them.
 */
@Composable
public fun <Broadcast : PulseBroadcast, Unicast : PulseUnicast> PulseHost(
    container: PulseContainer<Broadcast, Unicast>,
    content: @Composable ((onRefresh: () -> Unit, onBroadcast: (Broadcast) -> Unit) -> Unit) = { _, _ -> },
) {
    val containerKey by container.key.collectAsState()

    CompositionLocalProvider(LocalPulseContainerKey provides containerKey) {
        content(container::refresh, container::broadcast)
    }
}

@Composable
public fun <
    State : PulseState,
    Action : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
> PulseContent(
    store: PulseStore<State, Action, Event, Broadcast, Unicast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable ((State, ((Action) -> Unit)) -> Unit) = { _, _ -> },
) {
    val containerKey = LocalPulseContainerKey.current
    val state by store.state.collectAsState()
    val onAction = store::onAction
    LaunchedEffect(store) { store.event.collect { onEvent(it) } }

    key(containerKey) {
        content(state, onAction)
    }
}
