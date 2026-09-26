package fixture.conforming

import jp.kaleidot725.pulse.mvi.PulseAction
import jp.kaleidot725.pulse.mvi.PulseBroadcast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseEvent
import jp.kaleidot725.pulse.mvi.PulseState
import jp.kaleidot725.pulse.mvi.PulseUnicast
import jp.kaleidot725.pulse.mvi.PulseViewModel

data class CounterState(
    val count: Int = 0,
) : PulseState

sealed interface CounterAction : PulseAction {
    data object Increment : CounterAction
}

sealed interface CounterEvent : PulseEvent

sealed interface CounterBroadcast : PulseBroadcast

sealed interface CounterUnicast : PulseUnicast

class CounterViewModel :
    PulseViewModel<CounterState, CounterAction, CounterEvent, CounterBroadcast, CounterUnicast>(CounterState()) {
    override fun onAction(uiAction: CounterAction) = Unit
}

class CounterContainer(
    viewModels: List<PulseViewModel<*, *, *, CounterBroadcast, CounterUnicast>>,
) : PulseContainer<CounterBroadcast, CounterUnicast>(viewModels)

data object EmptyState : PulseState

data class Ping(
    val text: String,
) : PulseEvent

data object Reset : PulseBroadcast
