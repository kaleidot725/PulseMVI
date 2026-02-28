package jp.kaleidot725.doma.demo

import jp.kaleidot725.doma.mvi.DomaEvent

sealed interface CounterEvent : DomaEvent {
    data class ShowMessage(val message: String) : CounterEvent
}
