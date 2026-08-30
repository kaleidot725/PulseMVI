package jp.kaleidot725.pulse.demo.counter.detail.screen

import androidx.compose.runtime.Composable
import jp.kaleidot725.pulse.demo.DemoPage
import jp.kaleidot725.pulse.demo.counter.detail.screen.content.CounterDetailContent
import jp.kaleidot725.pulse.demo.counter.detail.screen.content.CounterDetailStore
import jp.kaleidot725.pulse.demo.counter.screen.CounterContainer
import jp.kaleidot725.pulse.mvi.PulseHost
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseContainer
import jp.kaleidot725.pulse.mvi.navigation3.rememberPulseStore

/**
 * The second destination, built the same way as the first one: its own Store, Container and
 * [PulseHost] subtree.
 *
 * Popping this route clears the entry's `ViewModelStoreOwner`, so the Store is cancelled and a
 * later visit starts a fresh one. The counter destination underneath is untouched.
 */
@Composable
fun CounterDetailScreen(
    count: Int,
    onBack: () -> Unit,
) {
    val store = rememberPulseStore { CounterDetailStore(count) }
    val container = rememberPulseContainer { CounterContainer(stores = listOf(store)) }

    PulseHost(container = container) { _, _ ->
        DemoPage(title = "Count details") {
            CounterDetailContent(store = store, onBack = onBack)
        }
    }
}
