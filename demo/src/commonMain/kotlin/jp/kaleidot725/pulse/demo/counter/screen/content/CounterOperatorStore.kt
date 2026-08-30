package jp.kaleidot725.pulse.demo.counter.screen.content

import jp.kaleidot725.pulse.demo.counter.repository.CounterRepository
import jp.kaleidot725.pulse.demo.counter.screen.content.state.CounterOperatorAction
import jp.kaleidot725.pulse.demo.counter.screen.content.state.CounterOperatorEvent
import jp.kaleidot725.pulse.demo.counter.screen.content.state.CounterOperatorState
import jp.kaleidot725.pulse.demo.counter.screen.state.CounterBroadcast
import jp.kaleidot725.pulse.demo.counter.screen.state.CounterUnicast
import jp.kaleidot725.pulse.mvi.PulseStore
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

class CounterOperatorStore(
    private val repository: CounterRepository,
) : PulseStore<CounterOperatorState, CounterOperatorAction, CounterOperatorEvent, CounterBroadcast, CounterUnicast>(
        initialUiState = CounterOperatorState(),
    ) {
    override fun onSetup() {
        update { copy(setupCount = setupCount + 1) }
        coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
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
}
