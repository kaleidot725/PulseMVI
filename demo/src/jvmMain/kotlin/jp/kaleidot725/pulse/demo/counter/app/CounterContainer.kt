package jp.kaleidot725.pulse.demo.counter.app

import jp.kaleidot725.pulse.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.pulse.mvi.PulseContainer
import jp.kaleidot725.pulse.mvi.PulseStore

class CounterContainer(
    stores: List<PulseStore<*, *, *, CounterAppBroadcast>>,
) : PulseContainer<CounterAppBroadcast>(stores = stores)
