package jp.kaleidot725.doma.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import jp.kaleidot725.doma.mvi.DomaRootContent
import kotlinx.coroutines.launch

fun main() = application {
    val viewModel = remember { CounterViewModel() }

    Window(
        onCloseRequest = ::exitApplication,
        title = "DomaKt Demo - Counter",
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        MaterialTheme {
            DomaRootContent(
                base = viewModel,
                onEvent = { effect ->
                    when (effect) {
                        is CounterEvent.ShowMessage -> {
                            scope.launch { snackbarHostState.showSnackbar(effect.message) }
                        }
                    }
                },
            ) { state, onAction ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "${state.count}",
                            fontSize = 72.sp,
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = { onAction(CounterAction.Decrement) }) {
                                Text("-")
                            }
                            Button(onClick = { onAction(CounterAction.Increment) }) {
                                Text("+")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { onAction(CounterAction.Reset) }) {
                            Text("Reset")
                        }
                    }

                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}
