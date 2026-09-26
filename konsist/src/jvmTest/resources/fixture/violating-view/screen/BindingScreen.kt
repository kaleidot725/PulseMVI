package fixture.violating.screen

import androidx.compose.runtime.Composable
import fixture.violating.CounterViewModel
import jp.kaleidot725.pulse.mvi.PulseContent

@Composable
fun BindingScreen(viewModel: CounterViewModel) {
    PulseContent(viewModel = viewModel) { _, _ -> }
}
