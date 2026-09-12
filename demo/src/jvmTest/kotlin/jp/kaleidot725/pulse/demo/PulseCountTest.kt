package jp.kaleidot725.pulse.demo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class PulseCountTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pulseReachesTheEdgeNeighborsButNotTheDiagonal() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()

        assertCount("TopLeft", 1)
        assertCount("TopRight", 1)
        assertCount("BottomLeft", 1)
        assertCount("BottomRight", 0)
    }

    @Test
    fun everyAreaCanBeTheOrigin() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-BottomRight").performClick()

        assertCount("BottomRight", 1)
        assertCount("TopRight", 1)
        assertCount("BottomLeft", 1)
        assertCount("TopLeft", 0)
    }

    @Test
    fun pulsesAccumulate() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 1)
        composeRule.onNodeWithTag("area-TopRight").performClick()

        assertCount("TopRight", 2)
        assertCount("TopLeft", 2)
        assertCount("BottomRight", 1)
        assertCount("BottomLeft", 1)
    }

    @Test
    fun resetClearsEveryAreaAtOnce() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 1)

        composeRule.onNodeWithTag("reset").performClick()

        assertCount("TopLeft", 0)
        assertCount("TopRight", 0)
        assertCount("BottomLeft", 0)
        assertCount("BottomRight", 0)
    }

    @Test
    fun refreshRebuildsTheCellsWithoutLosingTheirCounts() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 1)

        composeRule.onNodeWithTag("refresh").performClick()

        assertCount("TopLeft", 1)
        assertCaption("TopLeft", "setup 1")
    }

    @Test
    fun newAreaStartsFreshAndGoingBackKeepsTheGridUnderneath() {
        composeRule.setContent { DemoApp() }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 1)

        composeRule.onNodeWithTag("new-area").performClick()
        assertTitle("Area 2")
        assertCount("TopLeft", 0)

        composeRule.onNodeWithTag("back").performClick()
        assertTitle("Area 1")
        assertCount("TopLeft", 1)
    }

    @Test
    fun countsSurviveACompositionRestartWhileTheOwnerIsRetained() {
        val owner = RetainedViewModelStoreOwner()
        val generation = mutableStateOf(0)

        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                key(generation.value) { DemoApp() }
            }
        }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 1)

        composeRule.runOnUiThread { generation.value += 1 }
        composeRule.waitForIdle()

        assertCount("TopLeft", 1)
        assertCaption("TopLeft", "setup 1")
    }

    @Test
    fun clearingTheOwnerStartsTheGridOver() {
        val owner = RetainedViewModelStoreOwner()
        val generation = mutableStateOf(0)

        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                key(generation.value) { DemoApp() }
            }
        }

        composeRule.onNodeWithTag("area-TopLeft").performClick()
        assertCount("TopLeft", 1)

        composeRule.runOnUiThread {
            generation.value += 1
            owner.viewModelStore.clear()
        }
        composeRule.waitForIdle()

        assertCount("TopLeft", 0)
        assertCaption("TopLeft", "setup 1")
    }

    private fun assertCount(
        position: String,
        count: Int,
    ) = awaitSingleNode(hasTestTag("count-$position") and hasText(count.toString()))

    private fun assertCaption(
        position: String,
        caption: String,
    ) = awaitSingleNode(hasTestTag("caption-$position") and hasText(caption, substring = true))

    private fun assertTitle(title: String) = awaitSingleNode(hasTestTag("grid-title") and hasText(title))

    private fun awaitSingleNode(matcher: SemanticsMatcher) =
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().size == 1
        }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}

private class RetainedViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}
