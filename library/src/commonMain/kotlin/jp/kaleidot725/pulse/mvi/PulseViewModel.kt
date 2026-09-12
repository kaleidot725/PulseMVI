package jp.kaleidot725.pulse.mvi

import androidx.lifecycle.ViewModel
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

/**
 * Owns the state of one screen or component: holds [UiState], handles [UiAction], emits [Event] and
 * [Unicast], and receives [Broadcast] from its [PulseContainer].
 *
 * A [ViewModel], so anything that creates ViewModels can create it — `viewModel()`,
 * `koinViewModel()`, or `rememberPulseViewModel` from `pulsemvi-navigation3` — and its lifetime is
 * the owning `ViewModelStore`'s. [onSetup] runs once, the first time a [PulseContent] observes it;
 * [onCleared] cancels the work it started.
 */
public abstract class PulseViewModel<
    UiState : PulseState,
    UiAction : PulseAction,
    Event : PulseEvent,
    Broadcast : PulseBroadcast,
    Unicast : PulseUnicast,
>(
    private val initialUiState: UiState,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
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

    private var isSetUp: Boolean = false

    /**
     * Runs [onSetup] the first time this instance is observed, and never again.
     *
     * Called by [PulseContent]. Re-entering the composition does not repeat it, because the
     * instance outlives the composition.
     */
    internal fun setupOnce() {
        if (isSetUp) return
        isSetUp = true
        onSetup()
    }

    public open fun onSetup() {}

    public abstract fun onAction(uiAction: UiAction)

    public open fun onReceive(broadcast: Broadcast) {}

    /**
     * Cancels the work started in [onSetup] and prepares the ViewModel to be set up again.
     *
     * The scope is replaced with a fresh one and [setupOnce] is re-armed, so the next
     * [PulseContent] to observe this instance runs [onSetup] again with the state kept. Use this
     * when the ViewModel may become active again; use [close] when it will not.
     */
    public fun cancel() {
        coroutineScope.cancel()
        coroutineScope = createCoroutineScope(coroutineDispatcher)
        isSetUp = false
    }

    /**
     * Cancels the work started in [onSetup] for good.
     *
     * Unlike [cancel] this does not replace the scope, so nothing launched afterwards can outlive
     * the ViewModel. Anything still holding a reference gets a cancelled scope rather than a live one.
     */
    public fun close() {
        coroutineScope.cancel()
    }

    override fun onCleared() {
        close()
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
