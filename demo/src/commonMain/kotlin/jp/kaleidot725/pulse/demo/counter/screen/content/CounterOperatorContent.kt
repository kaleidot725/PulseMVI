package jp.kaleidot725.pulse.demo.counter.screen.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kaleidot725.pulse.demo.counter.screen.content.state.CounterOperatorAction
import jp.kaleidot725.pulse.demo.counter.screen.content.state.CounterOperatorEvent
import jp.kaleidot725.pulse.mvi.PulseContent
import kotlinx.coroutines.launch

@Composable
fun CounterOperatorContent(
    store: CounterOperatorStore,
    snackbarHostState: SnackbarHostState,
    onShowCounterDetails: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    PulseContent(
        store = store,
        onEvent = { event ->
            when (event) {
                is CounterOperatorEvent.ShowMessage -> {
                    scope.launch { snackbarHostState.showSnackbar(event.message) }
                }
            }
        },
    ) { state, onAction ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = state.count.toString(),
                modifier = Modifier.testTag("counter-value"),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "onSetup() calls: ${state.setupCount}",
                modifier = Modifier.testTag("setup-count"),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onAction(CounterOperatorAction.Decrement) }) {
                    Text("-")
                }
                Button(onClick = { onAction(CounterOperatorAction.Increment) }) {
                    Text("+")
                }
                OutlinedButton(onClick = { onAction(CounterOperatorAction.Reset) }) {
                    Text("Reset")
                }
            }
            Button(
                onClick = { onShowCounterDetails(state.count) },
                modifier = Modifier.fillMaxWidth().testTag("open-counter-details"),
            ) {
                Text("Show details")
            }
        }
    }
}
