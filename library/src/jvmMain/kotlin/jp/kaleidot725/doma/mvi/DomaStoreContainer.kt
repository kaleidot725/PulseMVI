package jp.kaleidot725.doma.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

public abstract class DomaStoreContainer<Broadcast : DomaBroadcast>(
    private val stores: List<DomaStore<*, *, *, Broadcast>>,
) {
    public var coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + Dispatchers.IO)
        private set

    private val _key: MutableStateFlow<String> = MutableStateFlow(UUID.randomUUID().toString())
    public val key: StateFlow<String> = _key.asStateFlow()

    public fun refresh() {
        _key.value = UUID.randomUUID().toString()
    }

    public fun broadcast(broadcast: Broadcast) {
        stores.forEach { it.onReceive(broadcast) }
    }
}
