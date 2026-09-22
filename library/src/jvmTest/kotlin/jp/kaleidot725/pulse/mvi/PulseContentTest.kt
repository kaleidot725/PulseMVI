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
     * This is the whole contract of the composable: the content never touches the ViewModel itself, and a dispatched action
     * comes back as the next state.
     */
    @Test
    fun `PulseContent hands the content the state and an onAction that reaches the ViewModel`() {
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
     * Setup belongs to the instance, not to the composition, and `onEvent` is re-read on every recomposition, so events
     * still arrive after the content changes.
     */
    @Test
    fun `PulseContent sets the ViewModel up once and keeps delivering its events`() {
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
     * Both composables are useful with no content at all: the setup still runs, which is how a ViewModel can be started
     * from a place that draws nothing.
     */
    @Test
    fun `PulseHost and PulseContent observe without rendering when content is left out`() {
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
     * A broadcast reaches the ViewModels through the Container, and a refresh rebuilds the content below the host without
     * setting the ViewModels up again.
     */
    @Test
    fun `PulseHost hands the content the refresh and broadcast of its Container`() {
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
