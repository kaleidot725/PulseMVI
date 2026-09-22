package jp.kaleidot725.pulse.mvi

import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * What [PulseHost] and [PulseContent] hand to their content in a real composition: the ViewModel's state and
 * `onAction`, the Container's `refresh` and `broadcast`, and one-time setup with event delivery.
 */
class PulseContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * [PulseContent] hands out the current state and an `onAction` that reaches the ViewModel, so a dispatched action
     * comes back as new state.
     */
    @Test
    fun handsOutStateAndAction() {
        val viewModel = ContentViewModel()
        var state = ContentState(count = -1)
        lateinit var onAction: (ContentAction) -> Unit

        composeRule.setContent {
            PulseContent(viewModel = viewModel) { uiState, dispatch ->
                state = uiState
                onAction = dispatch
            }
        }
        assertEquals(ContentState(count = 0), state)

        onAction(ContentAction.Increment)
        composeRule.waitForIdle()

        assertEquals(ContentState(count = 1), state)
    }

    /**
     * [PulseContent] runs setup once per instance, and events reach `onEvent` before and after a recomposition.
     */
    @Test
    fun runsSetupOnceAndDeliversEvents() {
        val viewModel = ContentViewModel()
        val received = mutableListOf<ContentEvent>()

        composeRule.setContent {
            PulseContent(viewModel = viewModel, onEvent = { received += it }) { _, _ -> }
        }

        viewModel.event(ContentEvent.Ping("before"))
        viewModel.onAction(ContentAction.Increment)
        composeRule.waitForIdle()
        viewModel.event(ContentEvent.Ping("after"))
        composeRule.waitForIdle()

        assertEquals(1, viewModel.setupCount)
        assertEquals(listOf<ContentEvent>(ContentEvent.Ping("before"), ContentEvent.Ping("after")), received)
    }

    /**
     * Leaving out `content` and `onEvent` observes the ViewModel without rendering or handling anything.
     */
    @Test
    fun defaultsObserveOnly() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))

        composeRule.setContent {
            PulseHost(container = container)
            PulseContent(viewModel = viewModel)
        }
        viewModel.event(ContentEvent.Ping("dropped"))
        composeRule.waitForIdle()

        assertEquals(1, viewModel.setupCount)
    }

    /**
     * [PulseHost] hands out the Container's `broadcast`, which reaches the ViewModel, and its `refresh`, which
     * re-creates the content below it.
     */
    @Test
    fun handsOutRefreshAndBroadcast() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))
        var state = ContentState(count = -1)
        var created = 0
        var generation = -1
        lateinit var onRefresh: () -> Unit
        lateinit var onBroadcast: (ContentBroadcast) -> Unit

        composeRule.setContent {
            PulseHost(container = container) { refresh, broadcast ->
                onRefresh = refresh
                onBroadcast = broadcast
                PulseContent(viewModel = viewModel) { uiState, _ ->
                    generation = remember { created++ }
                    state = uiState
                }
            }
        }

        viewModel.onAction(ContentAction.Increment)
        composeRule.waitForIdle()
        assertEquals(ContentState(count = 1), state)

        onBroadcast(ContentBroadcast.Reset)
        composeRule.waitForIdle()
        assertEquals(ContentState(count = 0), state)
        assertEquals(0, generation)

        onRefresh()
        composeRule.waitForIdle()
        assertEquals(1, generation)
    }
}
