package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

class PulseContainerTest {
    @Test
    fun broadcastReachesEveryStore() {
        val firstStore = BroadcastStore()
        val secondStore = BroadcastStore()
        val container = TestContainer(listOf(firstStore, secondStore))

        container.broadcast(ContainerBroadcast.Refresh)

        assertEquals(1, firstStore.receivedCount)
        assertEquals(1, secondStore.receivedCount)
    }

    @Test
    fun refreshChangesContainerKey() {
        val container = TestContainer(emptyList())

        container.refresh()
        container.refresh()

        assertEquals(2L, container.key.value)
    }

    @Test
    fun closeStopsUnicastCollection() {
        val store = BroadcastStore()
        val container = TestContainer(listOf(store), coroutineDispatcher = Dispatchers.Unconfined)

        store.unicast(ContainerUnicast)
        assertEquals(1, container.receivedCount)

        container.close()
        store.unicast(ContainerUnicast)

        assertEquals(1, container.receivedCount)
    }
}

private data object ContainerState : PulseState

private data object ContainerAction : PulseAction

private data object ContainerEvent : PulseEvent

private sealed interface ContainerBroadcast : PulseBroadcast {
    data object Refresh : ContainerBroadcast
}

private data object ContainerUnicast : PulseUnicast

private class BroadcastStore :
    PulseStore<ContainerState, ContainerAction, ContainerEvent, ContainerBroadcast, ContainerUnicast>(ContainerState) {
    var receivedCount: Int = 0

    override fun onAction(uiAction: ContainerAction) = Unit

    override fun onReceive(broadcast: ContainerBroadcast) {
        receivedCount += 1
    }
}

private class TestContainer(
    stores: List<PulseStore<*, *, *, ContainerBroadcast, ContainerUnicast>>,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PulseContainer<ContainerBroadcast, ContainerUnicast>(stores, coroutineDispatcher) {
    var receivedCount: Int = 0

    override fun onReceived(unicast: ContainerUnicast) {
        receivedCount += 1
    }
}
