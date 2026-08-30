package jp.kaleidot725.pulse.demo.counter.screen.content.state

import jp.kaleidot725.pulse.mvi.PulseEvent

sealed interface CounterOperatorEvent : PulseEvent {
    data class ShowMessage(
        val message: String,
    ) : CounterOperatorEvent
}
