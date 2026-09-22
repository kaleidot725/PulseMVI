package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.isActive
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
     * The dispatcher reaches `coroutineScope`, which is what [PulseViewModel.onSetup] launches into, so a test can pin the
     * ViewModel to its own dispatcher.
     */
    @Test
    fun `launches its work on the dispatcher it was constructed with`() {
        val viewModel = TestViewModel(coroutineDispatcher = Dispatchers.Unconfined)

        assertSame(Dispatchers.Unconfined, viewModel.coroutineScope.coroutineContext[ContinuationInterceptor])
    }

    /**
     * Construction stays cheap: whoever owns the instance decides when the work starts, which for a composition is
     * [PulseContent] calling [PulseViewModel.setupOnce].
     */
    @Test
    fun `does not run onSetup until it is set up`() {
        val viewModel = TestViewModel()

        assertEquals(0, viewModel.setupCount)

        viewModel.onSetup()

        assertEquals(1, viewModel.setupCount)
        assertFalse(requireNotNull(viewModel.setupJob).isCancelled)
    }

    /**
     * [PulseViewModel.cancel] is for a ViewModel that is kept but no longer observed, so the job launched in
     * [PulseViewModel.onSetup] must end with it.
     */
    @Test
    fun `cancels the coroutines onSetup started when cancel is called`() {
        val viewModel = TestViewModel()

        viewModel.onSetup()
        val setupJob = requireNotNull(viewModel.setupJob)
        viewModel.cancel()

        assertTrue(setupJob.isCancelled)
    }

    /**
     * [PulseViewModel.close] is the end of the instance, called from `onCleared`. Anything launched afterwards must not
     * run, so the scope is no longer active.
     */
    @Test
    fun `leaves a dead scope behind after close`() {
        val viewModel = TestViewModel()

        viewModel.onSetup()
        val setupJob = requireNotNull(viewModel.setupJob)
        viewModel.close()

        assertTrue(setupJob.isCancelled)
        assertFalse(viewModel.coroutineScope.isActive)
    }

    /**
     * Events are one-time messages to the UI, and the UI is not collecting yet while it composes for the first time. The
     * buffer keeps them until it does, in the order they were sent.
     */
    @Test
    fun `buffers events emitted before a collector arrives, in order`() =
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
     * Emitting an event never suspends the caller, so a full buffer has to lose something: it is the oldest event, not the
     * newest.
     */
    @Test
    fun `drops the oldest event once the buffer is full`() =
        runTest {
            val viewModel = TestViewModel()

            repeat(70) { viewModel.event(TestEvent.Message("event $it")) }

            assertEquals(TestEvent.Message("event 6"), viewModel.event.first())
        }

    /**
     * [PulseViewModel.setupOnce] is once per setup, not once per instance — the pair of calls is what lets an observer come
     * back to a ViewModel it had released.
     */
    @Test
    fun `runs onSetup again on the first setup after cancel`() {
        val viewModel = TestViewModel()

        viewModel.setupOnce()
        viewModel.setupOnce()
        assertEquals(1, viewModel.setupCount)

        viewModel.cancel()
        viewModel.setupOnce()

        assertEquals(2, viewModel.setupCount)
    }

    /**
     * Both hooks are optional: a ViewModel that needs neither can leave them alone.
     */
    @Test
    fun `does nothing in onSetup and onReceive until a subclass overrides them`() {
        val viewModel =
            object : PulseViewModel<TestState, TestAction, TestEvent, TestBroadcast, TestUnicast>(TestState()) {
                override fun onAction(uiAction: TestAction) = Unit
            }

        viewModel.setupOnce()
        viewModel.onReceive(TestBroadcast)

        assertEquals(TestState(), viewModel.currentState)
    }

    /**
     * Cancelling is about the running work, not the data. A ViewModel that is observed again shows what it held before.
     */
    @Test
    fun `keeps its state across a cancel and the setup that follows`() {
        val viewModel = TestViewModel()

        viewModel.onSetup()
        viewModel.update { copy(value = 42) }
        viewModel.cancel()
        viewModel.onSetup()

        assertEquals(TestState(value = 42), viewModel.currentState)
        assertEquals(2, viewModel.setupCount)
    }
}
