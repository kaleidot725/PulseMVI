package jp.kaleidot725.pulse.mvi.navigation3

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import jp.kaleidot725.pulse.mvi.PulseAction
import jp.kaleidot725.pulse.mvi.PulseBroadcast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseEvent
import jp.kaleidot725.pulse.mvi.PulseState
import jp.kaleidot725.pulse.mvi.PulseUnicast
import jp.kaleidot725.pulse.mvi.PulseViewModel

internal class TestOwner : ViewModelStoreOwner {
    override val viewModelStore = ViewModelStore()
}

internal data class NavState(
    val value: Int = 0,
) : PulseState

internal data object NavAction : PulseAction

internal data object NavEvent : PulseEvent

internal data object NavBroadcast : PulseBroadcast

internal data object NavUnicast : PulseUnicast

internal class NavViewModel : PulseViewModel<NavState, NavAction, NavEvent, NavBroadcast, NavUnicast>(NavState()) {
    override fun onAction(uiAction: NavAction) = Unit
}

internal class NavContainer(
    viewModels: List<PulseViewModel<*, *, *, NavBroadcast, NavUnicast>>,
) : PulseContainer<NavBroadcast, NavUnicast>(viewModels)
