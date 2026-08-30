package jp.kaleidot725.pulse.demo

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.junit.Rule
import org.junit.Test

/**
 * Reproduces what an Android configuration change does to the composition: the whole tree is thrown
 * away and rebuilt while the host [ViewModelStoreOwner] survives. The Store is expected to survive
 * with it, so its state is kept and `onSetup()` is not repeated.
 */
@OptIn(ExperimentalTestApi::class)
class DemoConfigurationChangeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun storeSurvivesCompositionRecreationWhileOwnerIsRetained() {
        val owner = RetainedViewModelStoreOwner()
        val generation = mutableStateOf(0)

        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                key(generation.value) { DemoApp() }
            }
        }

        composeRule.onNodeWithText("+").performClick()
        composeRule.onNodeWithText("+").performClick()
        waitForTaggedText("counter-value", "2")
        composeRule.onNodeWithTag("setup-count").assertTextEquals("onSetup() calls: 1")

        composeRule.runOnUiThread { generation.value += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("counter-value").assertTextEquals("2")
        composeRule.onNodeWithTag("setup-count").assertTextEquals("onSetup() calls: 1")
    }

    @Test
    fun storeIsRecreatedOnceTheOwnerIsCleared() {
        val owner = RetainedViewModelStoreOwner()
        val generation = mutableStateOf(0)

        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                key(generation.value) { DemoApp() }
            }
        }

        composeRule.onNodeWithText("+").performClick()
        waitForTaggedText("counter-value", "1")

        composeRule.runOnUiThread {
            generation.value += 1
            owner.viewModelStore.clear()
        }
        composeRule.waitForIdle()

        waitForTaggedText("counter-value", "0")
        composeRule.onNodeWithTag("setup-count").assertTextEquals("onSetup() calls: 1")
    }

    private fun waitForTaggedText(
        tag: String,
        text: String,
    ) {
        composeRule.waitUntilExactlyOneExists(
            matcher = hasTestTag(tag) and hasText(text),
            timeoutMillis = 5_000,
        )
    }
}

private class RetainedViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}
