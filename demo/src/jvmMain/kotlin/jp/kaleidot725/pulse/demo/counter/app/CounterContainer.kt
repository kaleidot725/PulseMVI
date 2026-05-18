package jp.kaleidot725.pulse.demo.counter.app

import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppUnicast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseStore

class CounterContainer(
    stores: List<PulseStore<*, *, *, CounterAppBroadcast, CounterAppUnicast>>,
) : PulseContainer<CounterAppBroadcast, CounterAppUnicast>(stores = stores) {
    override fun onReceived(unicast: CounterAppUnicast) {
        when (unicast) {
            CounterAppUnicast.ResetRequested -> {
                broadcast(CounterAppBroadcast.ResetNotified)
            }
        }
    }
}
