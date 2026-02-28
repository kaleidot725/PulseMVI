package jp.kaleidot725.doma.demo

import jp.kaleidot725.doma.mvi.DomaBase

class CounterViewModel : DomaBase<CounterState, CounterAction, CounterEvent>(
    initialUiState = CounterState(),
) {
    override fun onSetup() {
        // no-op
    }

    override fun onRefresh() {
        // no-op
    }

    override fun onAction(uiAction: CounterAction) {
        when (uiAction) {
            CounterAction.Increment -> {
                update { copy(count = count + 1) }
                if (currentState.count % 10 == 0) {
                    sideEffect(CounterEvent.ShowMessage("${currentState.count} reached!"))
                }
            }
            CounterAction.Decrement -> {
                update { copy(count = count - 1) }
                if (currentState.count % 10 == 0) {
                    sideEffect(CounterEvent.ShowMessage("${currentState.count} reached!"))
                }
            }
            CounterAction.Reset -> {
                update { copy(count = 0) }
                sideEffect(CounterEvent.ShowMessage("Counter reset!"))
            }
        }
    }
}
