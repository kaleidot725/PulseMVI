package jp.kaleidot725.pulse.demo.count.state

import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaPosition
import jp.kaleidot725.pulse.mvi.PulseUnicast

sealed interface PulseCountUnicast : PulseUnicast {
    data class Pulsed(
        val origin: PulseAreaPosition,
    ) : PulseCountUnicast
}
