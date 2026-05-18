package jp.kaleidot725.pulse.demo.counter.app

import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppUnicast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseStore
import jp.kaleidot725.pulse.mvi.PulseUnicast

class CounterContainer(
    stores: List<PulseStore<*, *, *, CounterAppBroadcast>>,
) : PulseContainer<CounterAppBroadcast>(stores = stores) {
    override fun onReceived(unicast: PulseUnicast) {
        when (unicast) {
            CounterAppUnicast.ResetRequested -> {
                broadcast(CounterAppBroadcast.ResetNotified)
            }
        }
    }
}
