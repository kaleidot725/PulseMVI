package jp.kaleidot725.doma.demo.counter.app

import jp.kaleidot725.doma.demo.counter.app.state.CounterAppAction
import jp.kaleidot725.doma.demo.counter.app.state.CounterAppEvent
import jp.kaleidot725.doma.demo.counter.app.state.CounterAppState
import jp.kaleidot725.doma.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.doma.mvi.DomaBroadcast
import jp.kaleidot725.doma.mvi.DomaStoreContainer
import jp.kaleidot725.doma.mvi.DomaStore
import kotlinx.coroutines.launch

class CounterStoreContainer(
    stores: List<DomaStore<*, *, *, CounterAppBroadcast>>,
) : DomaStoreContainer<CounterAppBroadcast>(stores = stores)
