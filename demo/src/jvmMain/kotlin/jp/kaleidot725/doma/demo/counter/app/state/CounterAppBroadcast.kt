package jp.kaleidot725.doma.demo.counter.app.state

import jp.kaleidot725.doma.mvi.DomaBroadcast

sealed class CounterAppBroadcast : DomaBroadcast {
    data object Refresh : CounterAppBroadcast()
}
