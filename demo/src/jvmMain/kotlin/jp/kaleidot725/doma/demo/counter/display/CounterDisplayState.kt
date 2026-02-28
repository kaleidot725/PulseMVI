package jp.kaleidot725.doma.demo.counter.display

import jp.kaleidot725.doma.mvi.DomaState

data class CounterDisplayState(
    val count: Int = 0,
) : DomaState
