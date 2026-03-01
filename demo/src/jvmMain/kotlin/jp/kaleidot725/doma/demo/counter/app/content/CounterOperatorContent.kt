package jp.kaleidot725.doma.demo.counter.app.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kaleidot725.doma.demo.counter.app.content.state.CounterOperatorAction
import jp.kaleidot725.doma.demo.counter.app.content.state.CounterOperatorEvent
import jp.kaleidot725.doma.mvi.DomaContent
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun CounterOperatorContent(store: CounterOperatorStore) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box {
        DomaContent(
            store = store,
            onEvent = {
                when (it) {
                    is CounterOperatorEvent.ShowMessage -> {
                        scope.launch { snackbarHostState.showSnackbar(it.message) }
                    }
                }
            },
        ) { state, onAction ->
            val time by remember { mutableStateOf(Date().time) }

            Card(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 64.dp, vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Text(
                        text = "Counter",
                        style = MaterialTheme.typography.h6,
                        color = MaterialTheme.colors.primary,
                    )

                    Text(
                        text = "Generated at $time",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    )

                    Text(
                        text = "${state.count}",
                        fontSize = 96.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Divider()

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = { onAction(CounterOperatorAction.Decrement) },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text("−", fontSize = 24.sp)
                        }

                        Button(
                            onClick = { onAction(CounterOperatorAction.Increment) },
                            modifier = Modifier.size(56.dp),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text("+", fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(onClick = { onAction(CounterOperatorAction.Reset) }) {
                        Text("Reset")
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
