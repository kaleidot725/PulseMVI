package jp.kaleidot725.pulse.demo.count.content.area

import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaAction
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaEvent
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaPosition
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaState
import jp.kaleidot725.pulse.demo.count.state.PulseCountBroadcast
import jp.kaleidot725.pulse.demo.count.state.PulseCountUnicast
import jp.kaleidot725.pulse.mvi.PulseViewModel

class PulseAreaViewModel(
    position: PulseAreaPosition,
) : PulseViewModel<PulseAreaState, PulseAreaAction, PulseAreaEvent, PulseCountBroadcast, PulseCountUnicast>(
        initialUiState = PulseAreaState(position = position),
    ) {
    override fun onSetup() {
        update { copy(setupCount = setupCount + 1) }
    }

    override fun onAction(uiAction: PulseAreaAction) {
        when (uiAction) {
            PulseAreaAction.Pulse -> sendPulse()
        }
    }

    override fun onReceive(broadcast: PulseCountBroadcast) {
        when (broadcast) {
            is PulseCountBroadcast.Pulse -> receivePulse(broadcast.origin)
            PulseCountBroadcast.Reset -> reset()
        }
    }

    private fun sendPulse() {
        val position = currentState.position
        count()
        unicast(PulseCountUnicast.Pulsed(position))
    }

    private fun receivePulse(origin: PulseAreaPosition) {
        val position = currentState.position
        if (origin == position) return
        if (origin !in position.neighbors) return

        count()
    }

    private fun reset() {
        update { copy(count = 0) }
    }

    private fun count() {
        val before = currentState.count
        update { copy(count = count + 1) }
        event(PulseAreaEvent.Pulsed)

        if (before < CHARGED_AT && currentState.count >= CHARGED_AT) {
            event(PulseAreaEvent.Charged(currentState.position, currentState.count))
        }
    }
}

private const val CHARGED_AT = 12
