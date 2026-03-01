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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import jp.kaleidot725.doma.demo.counter.app.CounterStoreContainer
import jp.kaleidot725.doma.demo.counter.app.content.CounterDisplayAction
import jp.kaleidot725.doma.demo.counter.app.content.CounterDisplayEvent
import jp.kaleidot725.doma.demo.counter.app.content.CounterDisplayStore
import jp.kaleidot725.doma.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.doma.demo.counter.repository.CounterRepository
import jp.kaleidot725.doma.mvi.DomaContainer
import jp.kaleidot725.doma.mvi.DomaContent
import kotlinx.coroutines.launch
import java.util.Date

fun main() =
    application {
        val repository = remember { CounterRepository() }
        val store = remember { CounterDisplayStore(repository) }
        val storeContainer = remember { CounterStoreContainer(stores = listOf(store)) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "DomaKt Demo - Counter",
        ) {
            val scope = rememberCoroutineScope()
            MaterialTheme {
                DomaContainer(storeContainer = storeContainer) { key, onRefresh, onBroadcast ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { onRefresh() }) {
                            Text("Refresh View")
                        }

                        Button(onClick = { onBroadcast(CounterAppBroadcast.Refresh) }) {
                            Text("Refresh Stores")
                        }

                        val snackbarHostState = remember { SnackbarHostState() }
                        DomaContent(
                            containerKey = key,
                            store = store,
                            onEvent = {
                                when (it) {
                                    is CounterDisplayEvent.ShowMessage -> {
                                        scope.launch { snackbarHostState.showSnackbar(it.message) }
                                    }
                                }
                            },
                        ) { state, onAction ->
                            val time by remember { mutableStateOf(Date().time) }

                            Text("TIME: $time")

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(onClick = { onAction(CounterDisplayAction.Decrement) }) {
                                    Text("-")
                                }
                                Button(onClick = { onAction(CounterDisplayAction.Increment) }) {
                                    Text("+")
                                }
                                Button(onClick = { onAction(CounterDisplayAction.Reset) }) {
                                    Text("Reset")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

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
