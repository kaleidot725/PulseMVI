package jp.kaleidot725.doma.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key

@Composable
public fun <Broadcast : DomaBroadcast> DomaContainer(
    storeContainer: DomaStoreContainer<Broadcast>,
    content: @Composable ((key: String, onRefresh: (() -> Unit), onBroadcast: ((Broadcast) -> Unit)) -> Unit) = { _, _, _ -> },
) {
    val key by storeContainer.key.collectAsState()

    val onBroadcast = storeContainer::broadcast
    val onRefresh = storeContainer::refresh
    content(key, onRefresh, onBroadcast)
}

@Composable
public fun <State : DomaState, Action : DomaAction, Event : DomaEvent, Broadcast : DomaBroadcast> DomaContent(
    containerKey: String,
    store: DomaStore<State, Action, Event, Broadcast>,
    onEvent: (Event) -> Unit = {},
    content: @Composable ((State, ((Action) -> Unit)) -> Unit) = { _, _ -> },
) {
    key(containerKey) {
        val state by store.state.collectAsState()
        val onAction = store::onAction
        LaunchedEffect(store) { store.event.collect { onEvent(it) } }
        content(state, onAction)
    }
}
