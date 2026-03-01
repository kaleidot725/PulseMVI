package jp.kaleidot725.pulse.demo.counter.app.content.state

import jp.kaleidot725.pulse.mvi.PulseEvent

sealed interface CounterOperatorEvent : PulseEvent {
    data class ShowMessage(
        val message: String,
    ) : CounterOperatorEvent
}
