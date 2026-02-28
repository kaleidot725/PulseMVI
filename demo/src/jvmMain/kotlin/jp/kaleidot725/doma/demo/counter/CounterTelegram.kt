package jp.kaleidot725.doma.demo.counter

import jp.kaleidot725.doma.mvi.DomaTelegram

sealed class CounterTelegram : DomaTelegram {
    data class UpdateCounter(val value: Int): CounterTelegram()
}
