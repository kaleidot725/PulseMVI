package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

public abstract class PulseContainer<Broadcast : PulseBroadcast, Unicast : PulseUnicast>(
    private val stores: List<PulseStore<*, *, *, Broadcast, Unicast>>,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + Dispatchers.IO)

    private val containerKey: MutableStateFlow<String> = MutableStateFlow(UUID.randomUUID().toString())
    internal val key: StateFlow<String> = containerKey.asStateFlow()

    init {
        stores.forEach { store ->
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                store.unicast.collect { unicast -> onReceived(unicast) }
            }
        }
    }

    public fun refresh() {
        containerKey.value = UUID.randomUUID().toString()
    }

    public fun broadcast(broadcast: Broadcast) {
        stores.forEach { it.onReceive(broadcast) }
    }

    public open fun onReceived(unicast: Unicast) {}
}
