package jp.kaleidot725.pulse.demo.counter.detail.screen.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kaleidot725.pulse.demo.counter.detail.screen.content.state.CounterDetailAction
import jp.kaleidot725.pulse.mvi.PulseContent

@Composable
fun CounterDetailContent(
    store: CounterDetailStore,
    onBack: () -> Unit,
) {
    PulseContent(store = store) { state, onAction ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Counter value",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = state.count.toString(),
                modifier = Modifier.testTag("counter-detail-value"),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "onSetup() calls: ${state.setupCount}",
                modifier = Modifier.testTag("detail-setup-count"),
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = { onAction(CounterDetailAction.Reload) }) {
                Text("Reload (${state.reloadCount})")
            }
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().testTag("back-to-counter"),
            ) {
                Text("Back")
            }
        }
    }
}
