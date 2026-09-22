package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.ContinuationInterceptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Lifecycle and event delivery of [PulseViewModel]: when [PulseViewModel.onSetup] runs, what [PulseViewModel.cancel]
 * and [PulseViewModel.close] leave behind, and what happens to events nobody is collecting yet.
 */
class PulseViewModelTest {
    /**
     * Work is launched on the dispatcher the ViewModel was given.
     */
    @Test
    fun usesConfiguredCoroutineDispatcher() {
        val viewModel = TestViewModel(coroutineDispatcher = Dispatchers.Unconfined)

        assertSame(Dispatchers.Unconfined, viewModel.coroutineScope.coroutineContext[ContinuationInterceptor])
    }

    /**
     * [PulseViewModel.onSetup] does not run until something observes the instance.
     */
    @Test
    fun setupIsNotRunUntilTheOwnerStartsIt() {
        val viewModel = TestViewModel()

        assertEquals(0, viewModel.setupCount)

        viewModel.onSetup()

        assertEquals(1, viewModel.setupCount)
        assertFalse(requireNotNull(viewModel.setupJob).isCancelled)
    }

    /**
     * [PulseViewModel.cancel] stops the work [PulseViewModel.onSetup] started.
     */
    @Test
    fun cancelStopsWorkStartedInSetup() {
        val viewModel = TestViewModel()

        viewModel.onSetup()
        val setupJob = requireNotNull(viewModel.setupJob)
        viewModel.cancel()

        assertTrue(setupJob.isCancelled)
    }

    /**
     * [PulseViewModel.close] leaves no scope to launch into, so a later emission cannot resurrect the ViewModel.
     */
    @Test
    fun closeLeavesNoScopeToLaunchInto() {
        val viewModel = TestViewModel()

        viewModel.onSetup()
        val setupJob = requireNotNull(viewModel.setupJob)
        viewModel.close()

        assertTrue(setupJob.isCancelled)
        assertFalse(viewModel.coroutineScope.isActive)
    }

    /**
     * Events emitted before anything collects them are buffered and keep their order.
     */
    @Test
    fun eventsEmittedWithNoCollectorKeepTheirOrder() =
        runTest {
            val viewModel = TestViewModel()

            viewModel.event(TestEvent.Message("first"))
            viewModel.event(TestEvent.Message("second"))

            assertEquals(
                listOf(TestEvent.Message("first"), TestEvent.Message("second")),
                viewModel.event.take(2).toList(),
            )
        }

    /**
     * Once the event buffer is full it drops the oldest event instead of suspending the emitter.
     */
    @Test
    fun eventBufferDropsTheOldestOnceItIsFull() =
        runTest {
            val viewModel = TestViewModel()

            repeat(70) { viewModel.event(TestEvent.Message("event $it")) }

            assertEquals(TestEvent.Message("event 6"), viewModel.event.first())
        }

    /**
     * After [PulseViewModel.cancel], the next observer sets the instance up again.
     */
    @Test
    fun setupOnceRunsAgainAfterCancel() {
        val viewModel = TestViewModel()

        viewModel.setupOnce()
        viewModel.setupOnce()
        assertEquals(1, viewModel.setupCount)

        viewModel.cancel()
        viewModel.setupOnce()

        assertEquals(2, viewModel.setupCount)
    }

    /**
     * [PulseViewModel.onSetup] and [PulseViewModel.onReceive] do nothing unless a subclass overrides them.
     */
    @Test
    fun hooksDoNothingUnlessOverridden() {
        val viewModel =
            object : PulseViewModel<TestState, TestAction, TestEvent, TestBroadcast, TestUnicast>(TestState()) {
                override fun onAction(uiAction: TestAction) = Unit
            }

        viewModel.setupOnce()
        viewModel.onReceive(TestBroadcast)

        assertEquals(TestState(), viewModel.currentState)
    }

    /**
     * State survives a [PulseViewModel.cancel] and the setup that follows it.
     */
    @Test
    fun stateIsPreservedAcrossSetups() {
        val viewModel = TestViewModel()

        viewModel.onSetup()
        viewModel.update { copy(value = 42) }
        viewModel.cancel()
        viewModel.onSetup()

        assertEquals(TestState(value = 42), viewModel.currentState)
        assertEquals(2, viewModel.setupCount)
    }
}

private data class TestState(
    val value: Int = 0,
) : PulseState

private data object TestAction : PulseAction

private sealed interface TestEvent : PulseEvent {
    data class Message(
        val text: String,
    ) : TestEvent
}

private data object TestBroadcast : PulseBroadcast

private data object TestUnicast : PulseUnicast

private class TestViewModel(
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
