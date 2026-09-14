package jp.kaleidot725.pulse.mvi

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.CompositionTracer
import androidx.compose.runtime.InternalComposeTracingApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PulseContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersStateAndDispatchesActions() {
        val viewModel = ContentViewModel()

        composeRule.setContent {
            PulseContent(viewModel = viewModel) { state, onAction ->
                Button(onClick = { onAction(ContentAction.Increment) }) {
                    Text("count ${state.count}")
                }
            }
        }

        composeRule.onNodeWithText("count 0").performClick()

        composeRule.onNodeWithText("count 1").assertTextEquals("count 1")
    }

    @Test
    fun runsSetupOnceAndDeliversEventsThroughTheLatestHandler() {
        val viewModel = ContentViewModel()
        val received = mutableListOf<String>()
        var tag by mutableStateOf("first")

        composeRule.setContent {
            PulseContent(
                viewModel = viewModel,
                onEvent = { event -> received += "$tag:${(event as ContentEvent.Ping).text}" },
            ) { _, _ -> Text(tag) }
        }
        composeRule.waitForIdle()

        viewModel.event(ContentEvent.Ping("a"))
        composeRule.waitForIdle()
        tag = "second"
        composeRule.waitForIdle()
        viewModel.event(ContentEvent.Ping("b"))
        composeRule.waitForIdle()

        assertEquals(1, viewModel.setupCount)
        assertEquals(listOf("first:a", "second:b"), received)
    }

    @Test
    fun defaultsObserveWithoutRenderingOrHandling() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))

        val other = ContentViewModel()
        val received = mutableListOf<ContentEvent>()

        composeRule.setContent {
            PulseHost(container = container)
            PulseContent(viewModel = viewModel)
            PulseContent(viewModel = other, onEvent = { received += it })
        }
        composeRule.waitForIdle()
        viewModel.event(ContentEvent.Ping("ignored"))
        other.event(ContentEvent.Ping("kept"))
        composeRule.waitForIdle()

        assertEquals(1, viewModel.setupCount)
        assertEquals(listOf<ContentEvent>(ContentEvent.Ping("kept")), received)
    }

    @Test
    fun hostHandsOutRefreshAndBroadcast() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))
        var generation = 0

        composeRule.setContent {
            PulseHost(container = container) { onRefresh, onBroadcast ->
                Column {
                    Button(onClick = { onBroadcast(ContentBroadcast.Reset) }, modifier = Modifier.testTag("reset")) { Text("reset") }
                    Button(onClick = onRefresh, modifier = Modifier.testTag("refresh")) { Text("refresh") }
                    PulseContent(viewModel = viewModel) { state, onAction ->
                        val id = remember { generation++ }
                        Button(onClick = { onAction(ContentAction.Increment) }, modifier = Modifier.testTag("count")) {
                            Text("count ${state.count} gen $id")
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithTag("count").performClick()
        composeRule.onNodeWithTag("count").assertTextEquals("count 1 gen 0")

        composeRule.onNodeWithTag("reset").performClick()
        composeRule.onNodeWithTag("count").assertTextEquals("count 0 gen 0")

        composeRule.onNodeWithTag("refresh").performClick()
        composeRule.onNodeWithTag("count").assertTextEquals("count 0 gen 1")
    }

    @Test
    fun skipsRecompositionWhenNothingChanged() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))
        var tick by mutableStateOf(0)

        composeRule.setContent {
            Text("tick $tick")
            PulseHost(container = container) { _, _ -> Text("host") }
            PulseContent(viewModel = viewModel) { _, _ -> Text("content") }
        }
        composeRule.waitForIdle()
        tick = 1
        composeRule.waitForIdle()

        // Parent recomposes in the same frame as the children's own state changes.
        tick = 2
        container.refresh()
        viewModel.onAction(ContentAction.Increment)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("tick 2").assertTextEquals("tick 2")
        assertEquals(1, viewModel.setupCount)
    }

    @Test
    fun acceptsParametersWhoseChangednessTheCallerAlreadyKnows() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))
        val hostContent: @Composable (() -> Unit, (ContentBroadcast) -> Unit) -> Unit = { _, _ -> Text("host") }
        val body: @Composable (ContentState, (ContentAction) -> Unit) -> Unit = { _, _ -> Text("content") }
        val onEvent: (ContentEvent) -> Unit = {}
        var tick by mutableStateOf(0)

        composeRule.setContent {
            Text("tick $tick")
            Host(container = container, content = hostContent)
            Content(viewModel = viewModel, onEvent = onEvent, content = body)
        }
        composeRule.waitForIdle()
        tick = 1
        composeRule.waitForIdle()

        composeRule.onNodeWithText("host").assertTextEquals("host")
        composeRule.onNodeWithText("content").assertTextEquals("content")
    }

    @Test
    fun reusesLambdasPassedInFromOutsideTheComposition() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))
        val hostContent: @Composable (() -> Unit, (ContentBroadcast) -> Unit) -> Unit = { _, _ -> Text("host") }
        val body: @Composable (ContentState, (ContentAction) -> Unit) -> Unit = { _, _ -> Text("content") }
        val onEvent: (ContentEvent) -> Unit = {}
        var tick by mutableStateOf(0)

        composeRule.setContent {
            Text("tick $tick")
            PulseHost(container = container, content = hostContent)
            PulseContent(viewModel = viewModel, onEvent = onEvent, content = body)
        }
        composeRule.waitForIdle()
        tick = 1
        composeRule.waitForIdle()

        composeRule.onNodeWithText("host").assertTextEquals("host")
        assertEquals(1, viewModel.setupCount)
    }

    @Test
    fun swapsContentHandedOverAsAValue() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))
        val hosts =
            listOf<@Composable (() -> Unit, (ContentBroadcast) -> Unit) -> Unit>(
                { _, _ -> Text("host a") },
                { _, _ -> Text("host b") },
            )
        val bodies =
            listOf<@Composable (ContentState, (ContentAction) -> Unit) -> Unit>(
                { _, _ -> Text("content a") },
                { _, _ -> Text("content b") },
            )
        var index by mutableStateOf(0)
        var tick by mutableStateOf(0)

        composeRule.setContent {
            Text("tick $tick")
            PulseHost(container = container, content = hosts[index])
            PulseContent(viewModel = viewModel, content = bodies[index])
        }
        composeRule.onNodeWithText("host a").assertTextEquals("host a")
        tick = 1
        composeRule.waitForIdle()
        index = 1

        composeRule.onNodeWithText("host b").assertTextEquals("host b")
        composeRule.onNodeWithText("content b").assertTextEquals("content b")
        assertEquals(1, viewModel.setupCount)
    }

    @OptIn(InternalComposeTracingApi::class)
    @Test
    fun reportsToTheComposeTracerWhenOneIsInstalled() {
        val viewModel = ContentViewModel()
        val container = ContentContainer(listOf(viewModel))
        val events = mutableListOf<String>()
        Composer.setTracer(
            object : CompositionTracer {
                override fun isTraceInProgress(): Boolean = true

                override fun traceEventStart(
                    key: Int,
                    dirty1: Int,
                    dirty2: Int,
                    info: String,
                ) {
                    events += info
                }

                override fun traceEventEnd() = Unit
            },
        )

        try {
            composeRule.setContent {
                PulseHost(container = container) { _, _ ->
                    PulseContent(viewModel = viewModel) { _, _ -> Text("traced") }
                }
            }
            composeRule.waitForIdle()
        } finally {
            Composer.setTracer(null)
        }

        assertTrue(events.any { it.startsWith("jp.kaleidot725.pulse.mvi.PulseHost") })
        assertTrue(events.any { it.startsWith("jp.kaleidot725.pulse.mvi.PulseContent") })
    }
}

@Composable
private fun Host(
    container: ContentContainer,
    content: @Composable (() -> Unit, (ContentBroadcast) -> Unit) -> Unit,
) {
    PulseHost(container = container, content = content)
}

@Composable
private fun Content(
    viewModel: ContentViewModel,
    onEvent: (ContentEvent) -> Unit,
    content: @Composable (ContentState, (ContentAction) -> Unit) -> Unit,
) {
    PulseContent(viewModel = viewModel, onEvent = onEvent, content = content)
}

private data class ContentState(
    val count: Int = 0,
) : PulseState

private sealed interface ContentAction : PulseAction {
    data object Increment : ContentAction
}

private sealed interface ContentEvent : PulseEvent {
    data class Ping(
        val text: String,
    ) : ContentEvent
}

private sealed interface ContentBroadcast : PulseBroadcast {
    data object Reset : ContentBroadcast
}

private sealed interface ContentUnicast : PulseUnicast

private class ContentViewModel :
    PulseViewModel<ContentState, ContentAction, ContentEvent, ContentBroadcast, ContentUnicast>(ContentState()) {
    var setupCount = 0

    override fun onSetup() {
        setupCount++
    }

    override fun onAction(uiAction: ContentAction) {
        when (uiAction) {
            ContentAction.Increment -> update { copy(count = count + 1) }
        }
    }

    override fun onReceive(broadcast: ContentBroadcast) {
        when (broadcast) {
            ContentBroadcast.Reset -> update { copy(count = 0) }
        }
    }
}

private class ContentContainer(
    viewModels: List<PulseViewModel<*, *, *, ContentBroadcast, ContentUnicast>>,
) : PulseContainer<ContentBroadcast, ContentUnicast>(viewModels)
