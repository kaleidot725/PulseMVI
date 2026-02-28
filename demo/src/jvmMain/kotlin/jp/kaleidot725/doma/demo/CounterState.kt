package jp.kaleidot725.doma.demo

import jp.kaleidot725.doma.mvi.DomaState

data class CounterState(
    val count: Int = 0,
) : DomaState
