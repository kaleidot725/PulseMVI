package jp.kaleidot725.pulse.demo.counter.app.content.state

import jp.kaleidot725.pulse.mvi.PulseState

data class CounterOperatorState(
    val count: Int = 0,
) : PulseState
