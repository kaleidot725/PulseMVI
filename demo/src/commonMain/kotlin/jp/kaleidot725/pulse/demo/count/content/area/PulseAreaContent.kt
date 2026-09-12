package jp.kaleidot725.pulse.demo.count.content.area

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import jp.kaleidot725.pulse.demo.count.content.area.component.PulseAreaCell
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaAction
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaEvent
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaPosition
import jp.kaleidot725.pulse.demo.count.content.area.state.PulseAreaState
import jp.kaleidot725.pulse.mvi.PulseContent

@Composable
fun PulseAreaContent(
    viewModel: PulseAreaViewModel,
    onCharged: (PulseAreaEvent.Charged) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFlashing by remember { mutableStateOf(false) }

    PulseContent(
        viewModel = viewModel,
        onEvent = { event ->
            when (event) {
                PulseAreaEvent.Pulsed -> isFlashing = true
                is PulseAreaEvent.Charged -> onCharged(event)
            }
        },
    ) { state, onAction ->
        PulseAreaContent(
            state = state,
            isFlashing = isFlashing,
            onFlashFinished = { isFlashing = false },
            onAction = onAction,
            modifier = modifier,
        )
    }
}

@Composable
fun PulseAreaContent(
    state: PulseAreaState,
    isFlashing: Boolean,
    onFlashFinished: () -> Unit,
    onAction: (PulseAreaAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    PulseAreaCell(
        state = state,
        isFlashing = isFlashing,
        onFlashFinished = onFlashFinished,
        onPulse = { onAction(PulseAreaAction.Pulse) },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PulseAreaContentPreview() {
    PulseAreaContent(
        state = PulseAreaState(position = PulseAreaPosition.TopLeft, count = 7),
        isFlashing = false,
        onFlashFinished = {},
        onAction = {},
    )
}
