package jp.kaleidot725.pulse.mvi

internal data class ContentState(
    val count: Int = 0,
) : PulseState

internal sealed interface ContentAction : PulseAction {
    data object Increment : ContentAction
}

internal sealed interface ContentEvent : PulseEvent {
    data class Ping(
        val text: String,
    ) : ContentEvent
}

internal sealed interface ContentBroadcast : PulseBroadcast {
    data object Reset : ContentBroadcast
}

internal sealed interface ContentUnicast : PulseUnicast

internal class ContentViewModel :
    PulseViewModel<ContentState, ContentAction, ContentEvent, ContentBroadcast, ContentUnicast>(ContentState()) {
    var setupCount = 0

    override fun onSetup() {
        setupCount++
    }

    override fun onAction(uiAction: ContentAction) {
        when (uiAction) {
            ContentAction.Increment -> update { copy(count = count + 1) }
        }
    }

    override fun onReceive(broadcast: ContentBroadcast) {
        when (broadcast) {
            ContentBroadcast.Reset -> update { copy(count = 0) }
        }
    }
}

internal class ContentContainer(
    viewModels: List<PulseViewModel<*, *, *, ContentBroadcast, ContentUnicast>>,
) : PulseContainer<ContentBroadcast, ContentUnicast>(viewModels)
