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
import jp.kaleidot725.doma.demo.counter.display.CounterDisplayStore
import jp.kaleidot725.doma.demo.counter.operator.CounterOperatorAction
import jp.kaleidot725.doma.demo.counter.operator.CounterOperatorPlatform
import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaPlatformContent
import jp.kaleidot725.doma.mvi.DomaStoreContent
import kotlinx.coroutines.launch

fun main() =
    application {
        val repository = remember { CounterRepository() }
        val displayStore = remember { CounterDisplayStore(repository) }
        val operatorPlatform = remember { CounterOperatorPlatform(stores = listOf(displayStore), repository = repository) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "DomaKt Demo - Counter",
        ) {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            MaterialTheme {
                DomaPlatformContent(platforms = operatorPlatform) { _, onAction ->
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

                        DomaStoreContent(
                            store = displayStore,
                            onEvent = {
                                when (it) {
                                    is CounterDisplayEvent.ShowMessage -> {
                                        scope.launch { snackbarHostState.showSnackbar(it.message) }
                                    }
                                }
                            },
                        ) { state ->
                            Text(
                                text = "${state.count}",
                                fontSize = 72.sp,
                            )

                            Spacer(modifier = Modifier.height(32.dp))
                        }

                        SnackbarHost(hostState = snackbarHostState)
                    }
                }
            }
        }
    }
