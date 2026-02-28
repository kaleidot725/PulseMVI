package jp.kaleidot725.doma.demo.counter.display

import jp.kaleidot725.doma.mvi.DomaEvent

sealed interface CounterDisplayEvent : DomaEvent {
    data class ShowMessage(
        val message: String,
    ) : CounterDisplayEvent
}
