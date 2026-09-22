package jp.kaleidot725.pulse.mvi

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal data object ContainerState : PulseState

internal data object ContainerAction : PulseAction

internal data object ContainerEvent : PulseEvent

internal sealed interface ContainerBroadcast : PulseBroadcast {
    data object Refresh : ContainerBroadcast
}

internal data object ContainerUnicast : PulseUnicast

internal class BroadcastViewModel :
    PulseViewModel<ContainerState, ContainerAction, ContainerEvent, ContainerBroadcast, ContainerUnicast>(
        ContainerState,
    ) {
    var receivedCount: Int = 0

    override fun onAction(uiAction: ContainerAction) = Unit

    override fun onReceive(broadcast: ContainerBroadcast) {
        receivedCount += 1
    }
}

internal class TestContainer(
    viewModels: List<PulseViewModel<*, *, *, ContainerBroadcast, ContainerUnicast>>,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : PulseContainer<ContainerBroadcast, ContainerUnicast>(viewModels, coroutineDispatcher) {
    var receivedCount: Int = 0

    override fun onReceived(unicast: ContainerUnicast) {
        receivedCount += 1
    }
}
