package jp.kaleidot725.pulse.demo.counter.detail.screen.content.state

import jp.kaleidot725.pulse.mvi.PulseState

data class CounterDetailState(
    val count: Int = 0,
    val setupCount: Int = 0,
    val reloadCount: Int = 0,
) : PulseState
