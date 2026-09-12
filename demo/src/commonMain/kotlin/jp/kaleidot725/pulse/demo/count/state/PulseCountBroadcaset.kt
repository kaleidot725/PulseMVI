package jp.kaleidot725.pulse.demo.count.state

import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaPosition
import jp.kaleidot725.pulse.mvi.PulseBroadcast

sealed interface PulseCountBroadcaset : PulseBroadcast {
    data class Pulse(
        val origin: PulseAreaPosition,
    ) : PulseCountBroadcaset

    data object Reset : PulseCountBroadcaset
}
