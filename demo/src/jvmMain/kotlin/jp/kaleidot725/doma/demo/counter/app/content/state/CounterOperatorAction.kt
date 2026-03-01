package jp.kaleidot725.doma.demo.counter.app.content.state

import jp.kaleidot725.doma.mvi.DomaAction

sealed interface CounterOperatorAction : DomaAction {
    data object Increment : CounterOperatorAction

    data object Decrement : CounterOperatorAction

    data object Reset : CounterOperatorAction
}
