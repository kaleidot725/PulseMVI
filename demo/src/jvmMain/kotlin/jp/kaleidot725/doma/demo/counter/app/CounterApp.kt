package jp.kaleidot725.doma.demo.counter.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.kaleidot725.doma.demo.counter.app.content.CounterOperatorContent
import jp.kaleidot725.doma.demo.counter.app.content.CounterOperatorStore
import jp.kaleidot725.doma.demo.counter.app.state.CounterAppBroadcast
import jp.kaleidot725.doma.mvi.DomaContainer

@Composable
fun CounterApp(
    storeContainer: CounterStoreContainer,
    store: CounterOperatorStore,
) {
    DomaContainer(storeContainer = storeContainer) { onRefresh, onBroadcast ->
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = { onRefresh() }) {
                    Text("Refresh View")
                }
                OutlinedButton(onClick = { onBroadcast(CounterAppBroadcast.Refresh) }) {
                    Text("Send Broadcast")
                }
            }

            Box(modifier = Modifier.align(Alignment.Center)) {
                CounterOperatorContent(store = store)
            }
        }
    }
}
