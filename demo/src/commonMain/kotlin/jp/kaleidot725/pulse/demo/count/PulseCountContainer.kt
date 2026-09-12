package jp.kaleidot725.pulse.demo.count

import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaPosition
import jp.kaleidot725.pulse.demo.count.state.PulseCountBroadcast
import jp.kaleidot725.pulse.demo.count.state.PulseCountUnicast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseViewModel

class PulseCountContainer(
    viewModels: List<PulseViewModel<*, *, *, PulseCountBroadcast, PulseCountUnicast>>,
) : PulseContainer<PulseCountBroadcast, PulseCountUnicast>(viewModels = viewModels) {
    override fun onReceived(unicast: PulseCountUnicast) {
        when (unicast) {
            is PulseCountUnicast.Pulsed -> relayPulse(unicast.origin)
        }
    }

    private fun relayPulse(origin: PulseAreaPosition) {
        broadcast(PulseCountBroadcast.Pulse(origin))
    }
}
