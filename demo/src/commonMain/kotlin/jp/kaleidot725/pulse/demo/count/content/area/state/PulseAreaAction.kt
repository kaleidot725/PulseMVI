package jp.kaleidot725.pulse.demo.count.content.area.state

import jp.kaleidot725.pulse.mvi.PulseAction

sealed interface PulseAreaAction : PulseAction {
    data object Pulse : PulseAreaAction

    data object FlashFinished : PulseAreaAction
}
