package jp.kaleidot725.pulse.demo.counter.app.state

import jp.kaleidot725.pulse.mvi.PulseBroadcast

sealed class CounterAppBroadcast : PulseBroadcast {
    data object Refresh : CounterAppBroadcast()
}
