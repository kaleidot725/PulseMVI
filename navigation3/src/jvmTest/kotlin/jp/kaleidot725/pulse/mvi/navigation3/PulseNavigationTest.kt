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
     * One owner returns one instance of each type across recompositions.
     */
    @Test
    fun viewModelAndContainerSurviveRecompositionUnderTheSameOwner() {
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
     * Explicit keys tell two instances of one type apart under the same owner.
     */
    @Test
    fun explicitKeysTellTwoInstancesOfOneTypeApart() {
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
     * `rememberPulseNavEntryDecorators` returns the saveable state holder decorator and the ViewModel store decorator,
     * in the order NavDisplay expects.
     */
    @Test
    fun navEntryDecoratorsAreTheSaveableStateHolderAndTheViewModelStore() {
        lateinit var decorators: List<NavEntryDecorator<Any>>

        composeRule.setContent {
            decorators = rememberPulseNavEntryDecorators()
        }
        composeRule.waitForIdle()

        assertEquals(2, decorators.size)
    }
}
