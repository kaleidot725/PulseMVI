package jp.kaleidot725.pulse.demo.counter.detail.screen.content.state

import jp.kaleidot725.pulse.mvi.PulseAction

sealed interface CounterDetailAction : PulseAction {
    data object Reload : CounterDetailAction
}
