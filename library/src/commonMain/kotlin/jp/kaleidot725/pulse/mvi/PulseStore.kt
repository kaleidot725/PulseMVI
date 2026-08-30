package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

public abstract class PulseStore<
    UiState : PulseState,
    UiAction : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>(
    private val initialUiState: UiState,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    public var coroutineScope: CoroutineScope = createCoroutineScope(coroutineDispatcher)
        private set

    private val uiState: MutableStateFlow<UiState> = MutableStateFlow(initialUiState)

    public val state: StateFlow<UiState> = uiState.asStateFlow()

    public val currentState: UiState get() = state.value

    private val _event: Channel<Event> =
        Channel(capacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    public val event: Flow<Event> = _event.receiveAsFlow()

    private val _unicast: MutableSharedFlow<Unicast> = MutableSharedFlow(extraBufferCapacity = 64)

    public val unicast: SharedFlow<Unicast> = _unicast.asSharedFlow()

    public open fun onSetup() {}

    public abstract fun onAction(uiAction: UiAction)

    public open fun onReceive(broadcast: Broadcast) {}

    /**
     * Cancels the work started in [onSetup] and prepares the Store to be set up again.
     *
     * The scope is replaced with a fresh one, so a later [onSetup] runs normally and the state is
     * kept. Use this when the Store may become active again; use [close] when it will not.
     */
    public fun cancel() {
        coroutineScope.cancel()
        coroutineScope = createCoroutineScope(coroutineDispatcher)
    }

    /**
     * Cancels the work started in [onSetup] for good.
     *
     * Unlike [cancel] this does not replace the scope, so nothing launched afterwards can outlive
     * the Store. Anything still holding a reference gets a cancelled scope rather than a live one.
     */
    public fun close() {
        coroutineScope.cancel()
    }

    public fun update(block: UiState.() -> UiState) {
        uiState.update { block(it) }
    }

    /**
     * Emits a one-time event to [event].
     *
     * Buffered, so this never suspends and never needs a coroutine: events keep their emission
     * order, and emitting while nothing collects - the screen is off the composition, say - queues
     * them instead of parking a coroutine per event. The buffer holds 64; older events are dropped
     * once it is full.
     */
    public fun event(effect: Event) {
        _event.trySend(effect)
    }

    public fun unicast(unicast: Unicast) {
        _unicast.tryEmit(unicast)
    }

    private companion object {
        private fun createCoroutineScope(coroutineDispatcher: CoroutineDispatcher): CoroutineScope =
            CoroutineScope(SupervisorJob() + coroutineDispatcher)
    }
}
