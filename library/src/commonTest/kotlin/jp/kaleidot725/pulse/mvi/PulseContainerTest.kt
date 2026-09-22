package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Message passing in [PulseContainer]: a broadcast reaching every registered ViewModel, the key that [PulseContent] re-
 * creates its content on, and what [PulseContainer.close] stops.
 */
class PulseContainerTest {
    /**
     * This is the fan-out the Container exists for: one message, `onReceive` on all of them, not just the first.
     */
    @Test
    fun `delivers a broadcast to every registered ViewModel`() {
        val firstViewModel = BroadcastViewModel()
        val secondViewModel = BroadcastViewModel()
        val container = TestContainer(listOf(firstViewModel, secondViewModel))

        container.broadcast(ContainerBroadcast.Refresh)

        assertEquals(1, firstViewModel.receivedCount)
        assertEquals(1, secondViewModel.receivedCount)
    }

    /**
     * The key is what [PulseContent] rebuilds its content on, so a refresh discards the composition below every [PulseHost]
     * holding this Container.
     */
    @Test
    fun `changes its key when refresh is called`() {
        val container = TestContainer(emptyList())

        container.refresh()
        container.refresh()

        assertEquals(2L, container.key.value)
    }

    /**
     * A Container that only broadcasts does not have to answer unicasts.
     */
    @Test
    fun `does nothing in onReceived until a subclass overrides it`() {
        val viewModel = BroadcastViewModel()
        val container =
            object : PulseContainer<ContainerBroadcast, ContainerUnicast>(listOf(viewModel), Dispatchers.Unconfined) {}

        viewModel.unicast(ContainerUnicast)

        assertEquals(0L, container.key.value)
    }

    /**
     * [PulseContainer.close] cancels the scope that collects each ViewModel's `unicast` flow, so a message sent afterwards
     * reaches nobody.
     */
    @Test
    fun `stops collecting unicasts after close`() {
        val viewModel = BroadcastViewModel()
        val container = TestContainer(listOf(viewModel), coroutineDispatcher = Dispatchers.Unconfined)

        viewModel.unicast(ContainerUnicast)
        assertEquals(1, container.receivedCount)

        container.close()
        viewModel.unicast(ContainerUnicast)

        assertEquals(1, container.receivedCount)
    }
}
