package jp.kaleidot725.doma.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

public abstract class DomaBase<UiState : DomaState, UiAction : DomaAction, Event : DomaEvent>(
    private val initialUiState: UiState,
) {
    public var coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + Dispatchers.IO)
        private set

    private val uiState: MutableStateFlow<UiState> = MutableStateFlow(initialUiState)

    public var state: StateFlow<UiState> =
        uiState
            .onSubscription {
                onSetup()
            }.stateIn(
                coroutineScope,
                SharingStarted.WhileSubscribed(),
                initialUiState,
            )
        private set

    public val currentState: UiState get() = state.value

    private val _event: Channel<Event> by lazy { Channel() }

    public var event: Flow<Event> = _event.receiveAsFlow()
        private set

    public abstract fun onSetup()

    public abstract fun onRefresh()

    public abstract fun onAction(uiAction: UiAction)

    public fun onReset() {
        coroutineScope.cancel()
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + Dispatchers.IO)
        uiState.update { initialUiState }
        state =
            uiState
                .onSubscription {
                    onSetup()
                }.stateIn(
                    coroutineScope,
                    SharingStarted.WhileSubscribed(),
                    initialUiState,
                )
        event = _event.receiveAsFlow()
    }

    public fun update(block: UiState.() -> UiState) {
        uiState.update { block(it) }
    }

    public fun event(effect: Event) {
        coroutineScope.launch { _event.send(effect) }
    }
}
