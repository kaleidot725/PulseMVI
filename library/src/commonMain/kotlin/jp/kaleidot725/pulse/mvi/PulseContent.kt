package jp.kaleidot725.pulse.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState

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
    viewModel: PulseViewModel<State, Action, Event, Broadcast, Unicast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable ((State, ((Action) -> Unit)) -> Unit) = { _, _ -> },
) {
    val containerKey = LocalPulseContainerKey.current
    val state by viewModel.state.collectAsState()
    val onAction = viewModel::onAction
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(viewModel) {
        viewModel.setupOnce()
        viewModel.event.collect { currentOnEvent(it) }
    }

    key(containerKey) {
        content(state, onAction)
    }
}
