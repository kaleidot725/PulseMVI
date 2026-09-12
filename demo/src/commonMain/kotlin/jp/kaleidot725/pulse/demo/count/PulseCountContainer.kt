package jp.kaleidot725.pulse.demo.count

import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaPosition
import jp.kaleidot725.pulse.demo.count.state.PulseCountBroadcaset
import jp.kaleidot725.pulse.demo.count.state.PulseCountUnicast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseViewModel

class PulseCountContainer(
    viewModels: List<PulseViewModel<*, *, *, PulseCountBroadcaset, PulseCountUnicast>>,
) : PulseContainer<PulseCountBroadcaset, PulseCountUnicast>(viewModels = viewModels) {
    override fun onReceived(unicast: PulseCountUnicast) {
        when (unicast) {
            is PulseCountUnicast.Pulsed -> relayPulse(unicast.origin)
        }
    }

    private fun relayPulse(origin: PulseAreaPosition) {
        broadcast(PulseCountBroadcaset.Pulse(origin))
    }
}
