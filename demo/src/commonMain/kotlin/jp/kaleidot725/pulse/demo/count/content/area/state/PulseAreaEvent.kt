package jp.kaleidot725.pulse.demo.count.content.area.state

import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaPosition
import jp.kaleidot725.pulse.mvi.PulseEvent

sealed interface PulseAreaEvent : PulseEvent {
    data object Pulsed : PulseAreaEvent

    data class Charged(
        val position: PulseAreaPosition,
        val count: Int,
    ) : PulseAreaEvent
}
