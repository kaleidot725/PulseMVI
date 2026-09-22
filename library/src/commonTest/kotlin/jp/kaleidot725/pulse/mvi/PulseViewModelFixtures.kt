package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

internal data class TestState(
    val value: Int = 0,
) : PulseState

internal data object TestAction : PulseAction

internal sealed interface TestEvent : PulseEvent {
    data class Message(
        val text: String,
    ) : TestEvent
}

internal data object TestBroadcast : PulseBroadcast

internal data object TestUnicast : PulseUnicast

internal class TestViewModel(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PulseViewModel<TestState, TestAction, TestEvent, TestBroadcast, TestUnicast>(
        initialUiState = TestState(),
        coroutineDispatcher = coroutineDispatcher,
    ) {
    var setupCount: Int = 0
    var setupJob: Job? = null

    override fun onSetup() {
        setupCount += 1
        setupJob = coroutineScope.launch { awaitCancellation() }
    }

    override fun onAction(uiAction: TestAction) = Unit
}
