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
     * [defaultPulseKey] is what identifies an instance in its owner's `ViewModelStore` when the caller passes no key,
     * and `viewModel()` needs it to be a name, never null. A class declared at the top level has a qualified name, a
     * class declared inside a function only has a simple name, and an anonymous object has neither — so the fallback
     * chain ends at the name the caller supplies.
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
}
