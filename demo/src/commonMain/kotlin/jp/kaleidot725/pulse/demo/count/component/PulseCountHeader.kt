package jp.kaleidot725.pulse.demo.count.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun PulseCountHeader(
    depth: Int,
    onNewArea: () -> Unit,
    onBack: (() -> Unit)?,
    onReset: () -> Unit,
    onRefresh: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Area $depth",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text =
                "Tap a quadrant to pulse it. It counts the tap, and so do the two it shares an " +
                    "edge with. The diagonal is out of reach.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNewArea) {
                Text("New Area")
            }
            if (onBack != null) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
            }
            TextButton(onClick = onReset) {
                Text("Reset")
            }
            TextButton(onClick = onRefresh) {
                Text("Refresh view")
            }
        }
    }
}
