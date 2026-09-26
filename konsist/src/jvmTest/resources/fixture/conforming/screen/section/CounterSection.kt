package fixture.conforming.screen.section

import androidx.compose.runtime.Composable
import fixture.conforming.CounterState
import fixture.conforming.CounterViewModel
import fixture.conforming.screen.section.component.CounterLabel
import jp.kaleidot725.pulse.mvi.PulseContent

@Composable
fun CounterSection(viewModel: CounterViewModel) {
    PulseContent(viewModel = viewModel) { state, _ ->
        CounterSection(state = state)
    }
}

@Composable
fun CounterSection(state: CounterState) {
    CounterLabel(count = state.count)
}
