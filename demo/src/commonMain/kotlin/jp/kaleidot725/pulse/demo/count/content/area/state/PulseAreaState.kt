package jp.kaleidot725.pulse.demo.count.content.area.state

import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaPosition
import jp.kaleidot725.pulse.mvi.PulseState

data class PulseAreaState(
    val position: PulseAreaPosition = PulseAreaPosition.TopLeft,
    val count: Int = 0,
    val lastOrigin: PulseAreaPosition? = null,
    val setupCount: Int = 0,
    val pulseId: Long = 0,
) : PulseState
