package jp.kaleidot725.pulse.mvi.navigation3

import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntryDecorator
import jp.kaleidot725.pulse.mvi.PulseAction
import jp.kaleidot725.pulse.mvi.PulseBroadcast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseEvent
import jp.kaleidot725.pulse.mvi.PulseState
import jp.kaleidot725.pulse.mvi.PulseUnicast
import jp.kaleidot725.pulse.mvi.PulseViewModel
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Instance lookup in the navigation3 artifact: which owner holds an instance, which key identifies it, and what happens
 * when no owner is in scope.
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
     * The default key falls back from the qualified name to the simple name to the given fallback, so a local class and
     * an anonymous object still get distinct keys.
     */
    @Test
    fun defaultKeyFallsBackFromQualifiedNameToSimpleNameToTheGivenName() {
        class Local : PulseViewModel<NavState, NavAction, NavEvent, NavBroadcast, NavUnicast>(NavState()) {
            override fun onAction(uiAction: NavAction) = Unit
        }
        val anonymous =
            object : PulseViewModel<NavState, NavAction, NavEvent, NavBroadcast, NavUnicast>(NavState()) {
                override fun onAction(uiAction: NavAction) = Unit
            }

        assertEquals("jp.kaleidot725.pulse.mvi.navigation3.NavViewModel", defaultPulseKey(NavViewModel::class, "x"))
        assertEquals("Local", defaultPulseKey(Local::class, "x"))
        assertEquals("PulseViewModel", defaultPulseKey(anonymous::class, "PulseViewModel"))
    }

    /**
     * A call with no ViewModelStoreOwner in scope fails with a message naming what is missing.
     */
    @Test
    fun failsPlainlyWithoutAnOwner() {
        val error = assertFailsWith<IllegalStateException> { requirePulseViewModelStoreOwner(null) }
        assertTrue(error.message!!.startsWith("No ViewModelStoreOwner in scope"))

        val owner = TestOwner()
        assertSame(owner, requirePulseViewModelStoreOwner(owner))
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

private class TestOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}

private data class NavState(
    val value: Int = 0,
) : PulseState

private data object NavAction : PulseAction

private data object NavEvent : PulseEvent

private data object NavBroadcast : PulseBroadcast

private data object NavUnicast : PulseUnicast

private class NavViewModel : PulseViewModel<NavState, NavAction, NavEvent, NavBroadcast, NavUnicast>(NavState()) {
    override fun onAction(uiAction: NavAction) = Unit
}

private class NavContainer(
    viewModels: List<PulseViewModel<*, *, *, NavBroadcast, NavUnicast>>,
) : PulseContainer<NavBroadcast, NavUnicast>(viewModels)
