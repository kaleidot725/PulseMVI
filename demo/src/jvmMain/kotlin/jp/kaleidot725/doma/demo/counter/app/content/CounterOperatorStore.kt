package jp.kaleidot725.doma.demo.counter.app.content

import jp.kaleidot725.doma.demo.counter.app.content.state.CounterOperatorAction
import jp.kaleidot725.doma.demo.counter.app.content.state.CounterOperatorEvent
import jp.kaleidot725.doma.demo.counter.app.content.state.CounterOperatorState
import jp.kaleidot725.doma.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaStore
import kotlinx.coroutines.launch

class CounterOperatorStore(
    private val repository: CounterRepository,
) : DomaStore<CounterOperatorState, CounterOperatorAction, CounterOperatorEvent, CounterAppBroadcast>(
        initialUiState = CounterOperatorState(),
    ) {
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count ->
                update { copy(count = count) }
                if (count == 10) event(CounterOperatorEvent.ShowMessage("10 Count"))
            }
        }
    }

    override fun onAction(uiAction: CounterOperatorAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterOperatorAction.Increment -> {
                    repository.increment()
                }

                CounterOperatorAction.Decrement -> {
                    repository.decrement()
                }

                CounterOperatorAction.Reset -> {
                    repository.reset()
                }
            }
        }
    }

    override fun onReceive(broadcast: CounterAppBroadcast) {
        coroutineScope.launch {
            when (broadcast) {
                is CounterAppBroadcast.Refresh -> {
                    event(CounterOperatorEvent.ShowMessage("Restart"))
                }
            }
        }
    }
}
