package jp.kaleidot725.doma.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
public fun <State : DomaState, Action : DomaAction, Event : DomaEvent, Telegram : DomaTelegram> DomaPlatformContent(
    platforms: DomaPlatform<State, Action, Event, Telegram>,
    onEvent: (Event) -> Unit = {},
    content: @Composable ((State, ((Action) -> Unit)) -> Unit) = { _, _ -> },
) {
    val state by platforms.state.collectAsState()
    val onAction = platforms::onAction
    LaunchedEffect(Unit) {
        platforms.onSetup()
    }
    LaunchedEffect(Unit) {
        platforms.event.collect { onEvent(it) }
    }
    DisposableEffect(Unit) {
        onDispose { platforms.onReset() }
    }
    content(state, onAction)
}

@Composable
public fun <State : DomaState, Telegram : DomaTelegram, Event : DomaEvent> DomaStoreContent(
    store: DomaStore<State, Telegram, Event>,
    onEvent: (Event) -> Unit = {},
    content: @Composable (State) -> Unit = { _ -> },
) {
    val state by store.state.collectAsState()
    LaunchedEffect(store) { store.event.collect { onEvent(it) } }
    content(state)
}
