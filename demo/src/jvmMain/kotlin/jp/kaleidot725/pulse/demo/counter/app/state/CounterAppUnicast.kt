package jp.kaleidot725.pulse.demo.counter.app.state

import jp.kaleidot725.pulse.mvi.PulseUnicast

sealed interface CounterAppUnicast : PulseUnicast {
    data object ResetRequested : CounterAppUnicast
}
