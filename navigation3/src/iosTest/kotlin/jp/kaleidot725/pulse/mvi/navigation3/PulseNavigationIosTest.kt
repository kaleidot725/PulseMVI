package jp.kaleidot725.pulse.mvi.navigation3

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import jp.kaleidot725.pulse.mvi.PulseAction
import jp.kaleidot725.pulse.mvi.PulseBroadcast
import jp.kaleidot725.pulse.mvi.PulseEvent
import jp.kaleidot725.pulse.mvi.PulseState
import jp.kaleidot725.pulse.mvi.PulseStore
import jp.kaleidot725.pulse.mvi.PulseUnicast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * iOS has no configuration change, so nothing recreates the composition behind the app's back. These
 * tests still pin down the two properties [rememberPulseStore] relies on, because they are what make
 * the same code path behave on iOS: the host provides a [ViewModelStoreOwner], and a Store scoped to
 * it outlives a composition rebuild.
 */
@OptIn(ExperimentalTestApi::class)
class PulseNavigationIosTest {
    @Test
    fun hostProvidesViewModelStoreOwner() =
        runComposeUiTest {
            var owner: ViewModelStoreOwner? = null

            setContent { owner = LocalViewModelStoreOwner.current }
            waitForIdle()

            assertNotNull(owner, "Compose Multiplatform should provide a default ViewModelStoreOwner on iOS")
        }

    @Test
    fun storeSurvivesCompositionRecreationWhileOwnerIsRetained() =
        runComposeUiTest {
            val owner = RetainedViewModelStoreOwner()
            val generation = mutableStateOf(0)
            var store: IosSetupStore? = null

            setContent {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                    key(generation.value) {
                        store = rememberPulseStore { IosSetupStore() }
                    }
                }
            }
            waitForIdle()

            val firstStore = assertNotNull(store)
            firstStore.update { copy(value = 42) }

            generation.value += 1
            waitForIdle()

            assertSame(firstStore, store)
            assertEquals(42, firstStore.currentState.value)
            assertEquals(1, firstStore.setupCount)
        }

    @Test
    fun storeIsRecreatedOnceTheOwnerIsCleared() =
        runComposeUiTest {
            val owner = RetainedViewModelStoreOwner()
            val generation = mutableStateOf(0)
            var store: IosSetupStore? = null

            setContent {
                CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
                    key(generation.value) {
                        store = rememberPulseStore { IosSetupStore() }
                    }
                }
            }
            waitForIdle()

            val firstStore = assertNotNull(store)
            firstStore.update { copy(value = 42) }

            owner.viewModelStore.clear()
            generation.value += 1
            waitForIdle()

            val secondStore = assertNotNull(store)
            assertNotSame(firstStore, secondStore)
            assertEquals(0, secondStore.currentState.value)
        }
}

private data class IosState(
    val value: Int = 0,
) : PulseState

private data object IosAction : PulseAction

private data object IosEvent : PulseEvent

private data object IosBroadcast : PulseBroadcast

private data object IosUnicast : PulseUnicast

private class IosSetupStore : PulseStore<IosState, IosAction, IosEvent, IosBroadcast, IosUnicast>(IosState()) {
    var setupCount: Int = 0

    override fun onSetup() {
        setupCount += 1
    }

    override fun onAction(uiAction: IosAction) = Unit
}

private class RetainedViewModelStoreOwner : ViewModelStoreOwner {
    override val viewModelStore: ViewModelStore = ViewModelStore()
}
