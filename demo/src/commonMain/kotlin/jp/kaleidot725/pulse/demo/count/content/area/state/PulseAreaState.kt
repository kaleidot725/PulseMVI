package jp.kaleidot725.pulse.demo.count.content.area.state

import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaPosition
import jp.kaleidot725.pulse.mvi.PulseState

data class PulseAreaState(
    val count: Int = 0,
    val position: PulseAreaPosition = PulseAreaPosition.TopLeft,
    val setupCount: Int = 0,
) : PulseState
