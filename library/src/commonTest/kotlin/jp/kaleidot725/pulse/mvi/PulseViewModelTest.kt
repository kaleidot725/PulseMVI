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

class PulseViewModelTest {
    @Test
    fun usesConfiguredCoroutineDispatcher() {
        val viewModel = TestViewModel(coroutineDispatcher = Dispatchers.Unconfined)

        assertSame(Dispatchers.Unconfined, viewModel.coroutineScope.coroutineContext[ContinuationInterceptor])
    }

    @Test
    fun setupIsNotRunUntilTheOwnerStartsIt() {
        val viewModel = TestViewModel()

        assertEquals(0, viewModel.setupCount)

        viewModel.onSetup()

        assertEquals(1, viewModel.setupCount)
        assertFalse(requireNotNull(viewModel.setupJob).isCancelled)
    }

    @Test
    fun cancelStopsWorkStartedInSetup() {
        val viewModel = TestViewModel()

        viewModel.onSetup()
        val setupJob = requireNotNull(viewModel.setupJob)
        viewModel.cancel()

        assertTrue(setupJob.isCancelled)
    }

    @Test
    fun closeLeavesNoScopeToLaunchInto() {
        val viewModel = TestViewModel()

        viewModel.onSetup()
        val setupJob = requireNotNull(viewModel.setupJob)
        viewModel.close()

        assertTrue(setupJob.isCancelled)
        assertFalse(viewModel.coroutineScope.isActive)
    }

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

    @Test
    fun eventBufferDropsTheOldestOnceItIsFull() =
        runTest {
            val viewModel = TestViewModel()

            repeat(70) { viewModel.event(TestEvent.Message("event $it")) }

            assertEquals(TestEvent.Message("event 6"), viewModel.event.first())
        }

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
