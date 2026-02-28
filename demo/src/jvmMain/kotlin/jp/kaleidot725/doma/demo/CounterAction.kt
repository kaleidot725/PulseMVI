package jp.kaleidot725.doma.demo

import jp.kaleidot725.doma.mvi.DomaAction

sealed interface CounterAction : DomaAction {
    data object Increment : CounterAction
    data object Decrement : CounterAction
    data object Reset : CounterAction
}
