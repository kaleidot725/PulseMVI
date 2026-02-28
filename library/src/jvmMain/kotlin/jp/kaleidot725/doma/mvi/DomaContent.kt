package jp.kaleidot725.doma.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
public fun <State : DomaState, Action : DomaAction, Event : DomaEvent> DomaRootContent(
    base: DomaBase<State, Action, Event>,
    onEvent: (Event) -> Unit = {},
    content: @Composable ((State, ((Action) -> Unit)) -> Unit) = { _, _ -> },
) {
    val state by base.state.collectAsState()
    val onAction = base::onAction
    LaunchedEffect(Unit) {
        base.onSetup()
    }
    LaunchedEffect(Unit) {
        base.event.collect { onEvent(it) }
    }
    DisposableEffect(Unit) {
        onDispose { base.onReset() }
    }
    content(state, onAction)
}

@Composable
public fun <State : DomaState, Action : DomaAction, Event : DomaEvent> DomaChildContent(
    base: DomaBase<State, Action, Event>,
    onEvent: (Event) -> Unit = {},
    content: @Composable (State, (Action) -> Unit) -> Unit = { _, _ -> },
) {
    val state by base.state.collectAsState()
    val onAction = base::onAction
    LaunchedEffect(base) { base.event.collect { onEvent(it) } }
    content(state, onAction)
}

@Composable
public fun <State : DomaState, Action : DomaAction, Event : DomaEvent> DomaDialogContent(
    base: DomaBase<State, Action, Event>,
    onEvent: (Event) -> Unit = {},
    content: @Composable (State, (Action) -> Unit) -> Unit = { _, _ -> },
) {
    DomaRootContent(base, onEvent, content)
}
