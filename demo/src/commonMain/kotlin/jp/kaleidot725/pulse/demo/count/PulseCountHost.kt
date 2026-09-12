package jp.kaleidot725.pulse.demo.count

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaContent
import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaPosition
import jp.kaleidot725.pulse.demo.count.content.area.PulseAreaViewModel
import jp.kaleidot725.pulse.demo.count.state.PulseCountBroadcast
import jp.kaleidot725.pulse.mvi.PulseHost
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseContainer
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseViewModel
import kotlinx.coroutines.launch

@Composable
fun PulseCountHost(
    depth: Int,
    onNewArea: () -> Unit,
    onBack: (() -> Unit)?,
) {
    val areas =
        PulseAreaPosition.entries.map { position ->
            rememberPulseViewModel(key = position.name) { PulseAreaViewModel(position) }
        }
    val container = rememberPulseContainer { PulseCountContainer(viewModels = areas) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    PulseHost(container = container) { onRefresh, onBroadcast ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PulseCountHeader(
                    depth = depth,
                    onNewArea = onNewArea,
                    onBack = onBack,
                    onReset = { onBroadcast(PulseCountBroadcast.Reset) },
                    onRefresh = onRefresh,
                )

                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    areas.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            row.forEach { area ->
                                PulseAreaContent(
                                    viewModel = area,
                                    onCharged = { charged ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "${charged.position.label} charged at ${charged.count}",
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                )
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun PulseCountHeader(
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
