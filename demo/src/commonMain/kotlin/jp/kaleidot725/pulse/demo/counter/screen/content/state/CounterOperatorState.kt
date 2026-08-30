package jp.kaleidot725.pulse.demo.counter.screen.content.state

import jp.kaleidot725.pulse.mvi.PulseState

data class CounterOperatorState(
    val count: Int = 0,
    val setupCount: Int = 0,
) : PulseState
