package jp.kaleidot725.doma.demo.counter.app.content.state

import jp.kaleidot725.doma.mvi.DomaState

data class CounterOperatorState(
    val count: Int = 0,
) : DomaState
