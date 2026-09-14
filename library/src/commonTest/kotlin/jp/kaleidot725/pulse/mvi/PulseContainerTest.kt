package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

class PulseContainerTest {
    @Test
    fun broadcastReachesEveryViewModel() {
        val firstViewModel = BroadcastViewModel()
        val secondViewModel = BroadcastViewModel()
        val container = TestContainer(listOf(firstViewModel, secondViewModel))

        container.broadcast(ContainerBroadcast.Refresh)

        assertEquals(1, firstViewModel.receivedCount)
        assertEquals(1, secondViewModel.receivedCount)
    }

    @Test
    fun refreshChangesContainerKey() {
        val container = TestContainer(emptyList())

        container.refresh()
        container.refresh()

        assertEquals(2L, container.key.value)
    }

    @Test
    fun receivedHookDoesNothingUnlessOverridden() {
        val viewModel = BroadcastViewModel()
        val container =
            object : PulseContainer<ContainerBroadcast, ContainerUnicast>(listOf(viewModel), Dispatchers.Unconfined) {}

        viewModel.unicast(ContainerUnicast)

        assertEquals(0L, container.key.value)
    }

    @Test
    fun closeStopsUnicastCollection() {
        val viewModel = BroadcastViewModel()
        val container = TestContainer(listOf(viewModel), coroutineDispatcher = Dispatchers.Unconfined)

        viewModel.unicast(ContainerUnicast)
        assertEquals(1, container.receivedCount)

        container.close()
        viewModel.unicast(ContainerUnicast)

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

private class BroadcastViewModel :
    PulseViewModel<ContainerState, ContainerAction, ContainerEvent, ContainerBroadcast, ContainerUnicast>(ContainerState) {
    var receivedCount: Int = 0

    override fun onAction(uiAction: ContainerAction) = Unit

    override fun onReceive(broadcast: ContainerBroadcast) {
        receivedCount += 1
    }
}

private class TestContainer(
    viewModels: List<PulseViewModel<*, *, *, ContainerBroadcast, ContainerUnicast>>,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PulseContainer<ContainerBroadcast, ContainerUnicast>(viewModels, coroutineDispatcher) {
    var receivedCount: Int = 0

    override fun onReceived(unicast: ContainerUnicast) {
        receivedCount += 1
    }
}
