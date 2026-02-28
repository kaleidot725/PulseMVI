package jp.kaleidot725.doma.demo.counter.display

import jp.kaleidot725.doma.demo.counter.CounterTelegram
import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaSubStore
import kotlinx.coroutines.launch

class CounterDisplaySubStore(
    private val repository: CounterRepository,
) : DomaSubStore<CounterDisplayState, CounterTelegram, CounterDisplayEvent>(
    initialUiState = CounterDisplayState(),
) {
    override fun onReceive(telegram: CounterTelegram) {
        coroutineScope.launch {
            when (telegram) {
                is CounterTelegram.Update -> {
                    update { copy(count = telegram.value) }
                    if (telegram.value == 10) event(CounterDisplayEvent.ShowMessage("10 Count"))
                }
            }
        }
    }
}
