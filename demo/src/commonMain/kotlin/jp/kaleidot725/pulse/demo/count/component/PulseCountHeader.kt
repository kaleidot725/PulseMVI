package jp.kaleidot725.pulse.demo.count.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("grid-title"),
        )
        Text(
            text =
                "Tap a quadrant to pulse it. It counts the tap, and so do the two it shares an " +
                    "edge with. The diagonal is out of reach.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onNewArea, modifier = Modifier.testTag("new-area")) {
                Text("New Area")
            }
            if (onBack != null) {
                TextButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                    Text("Back")
                }
            }
            TextButton(onClick = onReset, modifier = Modifier.testTag("reset")) {
                Text("Reset")
            }
            TextButton(onClick = onRefresh, modifier = Modifier.testTag("refresh")) {
                Text("Refresh view")
            }
        }
    }
}
