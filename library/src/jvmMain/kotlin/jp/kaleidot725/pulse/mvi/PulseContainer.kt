package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

public abstract class PulseContainer<Broadcast : PulseBroadcast>(
    private val stores: List<PulseStore<*, *, *, Broadcast>>,
) {
    private val containerKey: MutableStateFlow<String> = MutableStateFlow(UUID.randomUUID().toString())
    internal val key: StateFlow<String> = containerKey.asStateFlow()

    public fun refresh() {
        containerKey.value = UUID.randomUUID().toString()
    }

    public fun broadcast(broadcast: Broadcast) {
        stores.forEach { it.onReceive(broadcast) }
    }
}
