package jp.kaleidot725.doma.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import jp.kaleidot725.doma.demo.counter.display.CounterDisplayEvent
import jp.kaleidot725.doma.demo.counter.display.CounterDisplaySubStore
import jp.kaleidot725.doma.demo.counter.operator.CounterOperatorAction
import jp.kaleidot725.doma.demo.counter.operator.CounterOperatorStore
import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaContent
import jp.kaleidot725.doma.mvi.DomaSubContent
import kotlinx.coroutines.launch

fun main() =
    application {
        val repository = remember { CounterRepository() }
        val displayStore = remember { CounterDisplaySubStore(repository) }
        val operatorPlatform = remember { CounterOperatorStore(subStores = listOf(displayStore), repository = repository) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "DomaKt Demo - Counter",
        ) {
            val scope = rememberCoroutineScope()
            MaterialTheme {
                DomaContent(platforms = operatorPlatform) { _, onAction ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = { onAction(CounterOperatorAction.Decrement) }) {
                                Text("-")
                            }
                            Button(onClick = { onAction(CounterOperatorAction.Increment) }) {
                                Text("+")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = { onAction(CounterOperatorAction.Reset) }) {
                            Text("Reset")
                        }

                        val snackbarHostState = remember { SnackbarHostState() }
                        DomaSubContent(
                            subStore = displayStore,
                            onEvent = {
                                when (it) {
                                    is CounterDisplayEvent.ShowMessage -> {
                                        scope.launch { snackbarHostState.showSnackbar(it.message) }
                                    }
                                }
                            },
                        ) { state, onAction ->
                            Text(
                                text = "${state.count}",
                                fontSize = 72.sp,
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            SnackbarHost(hostState = snackbarHostState)
                        }
                    }
                }
            }
        }
    }
