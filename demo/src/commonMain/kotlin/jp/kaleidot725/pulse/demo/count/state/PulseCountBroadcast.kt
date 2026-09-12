package jp.kaleidot725.pulse.demo.count.state

import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaPosition
import jp.kaleidot725.pulse.mvi.PulseBroadcast

sealed interface PulseCountBroadcast : PulseBroadcast {
    data class Pulse(
        val origin: PulseAreaPosition,
    ) : PulseCountBroadcast

    data object Reset : PulseCountBroadcast
}
