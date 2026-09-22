package jp.kaleidot725.pulse.mvi.navigation3

import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntryDecorator
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * The navigation3 composables under a real composition: which owner holds an instance, which key identifies it, and
 * what `rememberPulseNavEntryDecorators` hands to `NavDisplay`.
 */
class PulseNavigationTest {
    @get:Rule
    val composeRule = createComposeRule()

    /**
     * The instance lives in the owner's `ViewModelStore`, not in the composition, so recomposing finds what is already
     * there.
     */
    @Test
    fun `returns the same ViewModel and Container while the owner stays the same`() {
        val owner = TestOwner()
        var tick by mutableStateOf(0)
        val viewModels = mutableListOf<NavViewModel>()
        val containers = mutableListOf<NavContainer>()

        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                val viewModel = rememberPulseViewModel { NavViewModel() }
                val container = rememberPulseContainer { NavContainer(listOf(viewModel)) }
                viewModels += viewModel
                containers += container
                Text("tick $tick")
            }
        }
        composeRule.waitForIdle()
        tick = 1
        composeRule.waitForIdle()

        assertSame(viewModels.first(), viewModels.last())
        assertSame(containers.first(), containers.last())
    }

    /**
     * Without a key the type name identifies the instance, so two calls share it. A screen that needs several of one
     * ViewModel passes keys, as the demo's four areas do.
     */
    @Test
    fun `separates two instances of one type when each is given its own key`() {
        val owner = TestOwner()
        lateinit var unkeyedA: NavViewModel
        lateinit var unkeyedB: NavViewModel
        lateinit var left: NavViewModel
        lateinit var right: NavViewModel

        composeRule.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                unkeyedA = rememberPulseViewModel { NavViewModel() }
                unkeyedB = rememberPulseViewModel { NavViewModel() }
                left = rememberPulseViewModel(key = "left") { NavViewModel() }
                right = rememberPulseViewModel(key = "right") { NavViewModel() }
                rememberPulseContainer(key = "container") { NavContainer(listOf(left)) }
            }
        }
        composeRule.waitForIdle()

        assertSame(unkeyedA, unkeyedB)
        assertNotSame(left, right)
    }

    /**
     * Passing `entryDecorators` replaces the default saveable state holder decorator, and losing it silently throws away
     * `rememberSaveable` state — so both are returned together, in the order NavDisplay expects.
     */
    @Test
    fun `returns both NavEntry decorators, not only the ViewModel store one`() {
        lateinit var decorators: List<NavEntryDecorator<Any>>

        composeRule.setContent {
            decorators = rememberPulseNavEntryDecorators()
        }
        composeRule.waitForIdle()

        assertEquals(2, decorators.size)
    }
}
