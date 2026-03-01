package jp.kaleidot725.doma.demo.counter.app.content

import jp.kaleidot725.doma.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaStore
import kotlinx.coroutines.launch

class CounterDisplayStore(
    private val repository: CounterRepository,
) : DomaStore<CounterDisplayState, CounterDisplayAction, CounterDisplayEvent, CounterAppBroadcast>(
    initialUiState = CounterDisplayState(),
) {
    override fun onSetup() {
        coroutineScope.launch {
            repository.count.collect { count ->
                update { copy(count = count) }
                if (count == 10) event(CounterDisplayEvent.ShowMessage("10 Count"))
            }
        }
    }

    override fun onAction(uiAction: CounterDisplayAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterDisplayAction.Increment -> {
                    repository.increment()
                }

                CounterDisplayAction.Decrement -> {
                    repository.decrement()
                }

                CounterDisplayAction.Reset -> {
                    repository.reset()
                }
            }
        }
    }

    override fun onReceive(telegram: CounterAppBroadcast) {
        coroutineScope.launch {
            when (telegram) {
                is CounterAppBroadcast.Refresh -> {
                    event(CounterDisplayEvent.ShowMessage("Restart"))
                }
            }
        }
    }
}
