package jp.kaleidot725.pulse.demo.counter.detail.screen.content

import jp.kaleidot725.pulse.demo.counter.detail.screen.content.state.CounterDetailAction
import jp.kaleidot725.pulse.demo.counter.detail.screen.content.state.CounterDetailEvent
import jp.kaleidot725.pulse.demo.counter.detail.screen.content.state.CounterDetailState
import jp.kaleidot725.pulse.demo.counter.screen.state.CounterBroadcast
import jp.kaleidot725.pulse.demo.counter.screen.state.CounterUnicast
import jp.kaleidot725.pulse.mvi.PulseStore

class CounterDetailStore(
    private val initialCount: Int,
) : PulseStore<CounterDetailState, CounterDetailAction, CounterDetailEvent, CounterBroadcast, CounterUnicast>(
        initialUiState = CounterDetailState(),
    ) {
    override fun onSetup() {
        update { copy(count = initialCount, setupCount = setupCount + 1) }
    }

    override fun onAction(uiAction: CounterDetailAction) {
        when (uiAction) {
            CounterDetailAction.Reload -> update { copy(reloadCount = reloadCount + 1) }
        }
    }
}
