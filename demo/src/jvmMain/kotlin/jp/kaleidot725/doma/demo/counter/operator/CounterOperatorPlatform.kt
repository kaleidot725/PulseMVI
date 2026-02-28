package jp.kaleidot725.doma.demo.counter.operator

import jp.kaleidot725.doma.demo.counter.CounterTelegram
import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaPlatform
import jp.kaleidot725.doma.mvi.DomaStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CounterOperatorPlatform(
    stores: List<DomaStore<*, CounterTelegram, *>>,
    private val repository: CounterRepository,
) : DomaPlatform<CounterOperatorState, CounterOperatorAction, CounterOperatorEvent, CounterTelegram>(
        stores = stores,
        initialUiState = CounterOperatorState,
    ) {
    override fun onAction(uiAction: CounterOperatorAction) {
        coroutineScope.launch {
            when (uiAction) {
                CounterOperatorAction.Increment -> {
                    repository.increment()
                    broadcast(CounterTelegram.UpdateCounter(repository.count.first()))
                }
                CounterOperatorAction.Decrement -> {
                    repository.decrement()
                    broadcast(CounterTelegram.UpdateCounter(repository.count.first()))
                }
                CounterOperatorAction.Reset -> {
                    repository.reset()
                    broadcast(CounterTelegram.UpdateCounter(repository.count.first()))
                }
            }
        }
    }
}
