package jp.kaleidot725.pulse.mvi.navigation3

import jp.kaleidot725.pulse.mvi.PulseViewModel
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The two plain functions behind `rememberPulseViewModel` and `rememberPulseContainer`, tested without a composition.
 */
class PulseNavigationInternalsTest {
    /**
     * The key identifies the instance inside its owner's `ViewModelStore` and `viewModel()` will not take null for it. A
     * top-level class has a qualified name, a class declared in a function has only a simple name, and an anonymous object
     * has neither — hence the third step.
     */
    @Test
    fun `defaultPulseKey falls back from the qualified name to the simple name to the given name`() {
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
     * Calling `rememberPulseViewModel` outside a `NavDisplay` or a window is an easy mistake, and the message has to name
     * the cause rather than read as a null pointer.
     */
    @Test
    fun `requirePulseViewModelStoreOwner says what is missing when no owner is in scope`() {
        val error = assertFailsWith<IllegalStateException> { requirePulseViewModelStoreOwner(null) }
        assertTrue(error.message!!.startsWith("No ViewModelStoreOwner in scope"))

        val owner = TestOwner()
        assertSame(owner, requirePulseViewModelStoreOwner(owner))
    }
}
