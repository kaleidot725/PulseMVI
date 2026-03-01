package jp.kaleidot725.doma.demo.counter.app.content.state

import jp.kaleidot725.doma.mvi.DomaEvent

sealed interface CounterOperatorEvent : DomaEvent {
    data class ShowMessage(
        val message: String,
    ) : CounterOperatorEvent
}
