package jp.kaleidot725.pulse.demo.counter.app.content.state

import jp.kaleidot725.pulse.mvi.PulseAction

sealed interface CounterOperatorAction : PulseAction {
    data object Increment : CounterOperatorAction

    data object Decrement : CounterOperatorAction

    data object Reset : CounterOperatorAction
}
